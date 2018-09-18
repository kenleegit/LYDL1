package net.agnusvox.lydl1;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GetOneAudio extends AppCompatActivity {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_one_audio);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DbOpenHelper mHelper = new DbOpenHelper(GetOneAudio.this);
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(
                "SELECT a._TITLE AS TITLE, a._URL AS URL FROM TmpAudios AS a, PrfPrograms AS p WHERE p._PID = a._PID AND p._LIKED = 1",
                null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String url = cursor.getString(cursor.getColumnIndex("URL"));

            //2017-09-19 Enqueue only if file not yet there
            String fileName =  url.substring(url.lastIndexOf('/') + 1, url.length());

            //Toast.makeText(getApplicationContext(), "URL=" + doRealUrl(url) + "END", Toast.LENGTH_LONG).show();

            File fileFullPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            if ( ! fileFullPath.exists()) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(doRealUrl(url)));
                request.setDescription(cursor.getString(cursor.getColumnIndex("TITLE")));
                request.setTitle(cursor.getString(cursor.getColumnIndex("TITLE")));
                // in order for this if to run, you must use the android 3.2 to compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }

                //2017-09-26 Run-time check permission
                //https://stackoverflow.com/questions/40514335/download-manager-android-permissions-error-write-external-storage
                //https://developer.android.com/training/permissions/requesting.html
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v("Permission","You have permission");
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        manager.enqueue(request);
                    } else {
                        Log.v("Permission","You have asked for permission");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "File " + fileName + " already exists.",
                        Toast.LENGTH_LONG).show();
            }
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();

        /* This code downloads just 1 file. Keep only for reference
        String url = "http://txly2.net/images/program_banners/mw_prog_banner.png";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Just ONE Audio Description");
        request.setTitle("旷野吗哪-Title");
// in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }

        //File folder = new File(Environment.DIRECTORY_DOWNLOADS);
        //folder.mkdirs();
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "mw_prog_banner.png");

// get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        */
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //you have the permission now.
            Toast.makeText(getApplicationContext(),
                    "I have permission now. Please try downloading again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 主要是处理小米手机的
     * @param url 下载地址
     * @return 真实的下载地址
     *     作者：吕檀溪 链接：http://www.jianshu.com/p/9bd5d81d6060
     */
    public String doRealUrl(String url) {
        try {
            //获取build Properties 判断是不是小米手机
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            if (properties.getProperty(KEY_MIUI_VERSION_CODE, null) != null || properties.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || properties.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null) {
                url += " ";
            }
            Log.d("doRealUrl", "MIUI Properties " + properties.getProperty(KEY_MIUI_VERSION_CODE) + ":" + properties.getProperty(KEY_MIUI_VERSION_NAME)
                    + properties.getProperty(KEY_MIUI_INTERNAL_STORAGE) );
            Log.d("doRealUrl", "URL=" + url + "END");

            return url;
        } catch (final IOException e) {
            return url;
        }
    }

}
