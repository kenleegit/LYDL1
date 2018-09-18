package net.agnusvox.lydl1;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static net.agnusvox.lydl1.StorageUtil.getStorageDirectories;

/**
 * Created by ken on 29/9/2017.
 * https://developer.android.com/reference/android/os/AsyncTask.html
 * https://github.com/romannurik/muzei/blob/master/main/src/main/java/com/google/android/apps/muzei/sync/DownloadArtworkTask.java
 */

public class GetAudioTask extends AsyncTask<Void, Void, Boolean> {
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    private static final String TAG = "GetAudioTask";
    private final Context mContext;

    public GetAudioTask(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        UpdateAudioTable();
        GetAudios();
        return true;
    }

    private void UpdateAudioTable () {
        DbOpenHelper mHelper = new DbOpenHelper(mContext);
        SQLiteDatabase mDB = null;
        mDB = mHelper.getWritableDatabase();
        //2017-10-22 Algorithm changed. No longer flush the table every time, but insert if new.
        //mDB.execSQL("DELETE FROM TmpAudios");
        //mDB.close();

        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        //String url = "https://api.androidhive.info/contacts/";
        //2017-08-31 Changed JSON contacts to audios at TXLY2
        String url = "http://txly2.net/index.php?option=com_vdata&task=get_feeds&type=aud0920v2";
        String jsonStr = sh.makeServiceCall(url);

        //Log.d(TAG, "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                // Getting JSON Array node
                JSONArray audios = new JSONArray(jsonStr);

                //DbOpenHelper mHelper = new DbOpenHelper(mContext);
                //SQLiteDatabase mDB = null;
                //mDB = mHelper.getWritableDatabase();

                // looping through All Audios
                for (int i = 0; i < audios.length(); i++) {
                    JSONObject c = audios.getJSONObject(i);
                    String aid = c.getString("sermon_id");
                    //2017-10-22 Skip it if it is already in table.
                    if ( AudioIDExists(aid)) {
                        Log.d(TAG, "AudioID " + aid + " already exists in table.");
                        continue;
                    }
                    String title = c.getString("sermon_title");
                    //String notes = c.getString("sermon_notes");
                    //String picture = c.getString("picture");
                    String audio_url = c.getString("url");
                    String pid = c.getString("series_id");

                    /* 2017-09-02
                            Putting to the table
                    */
                    mDB = mHelper.getWritableDatabase();

                    //Prepare values for writing to table.
                    ContentValues values = new ContentValues();
                    values.put("_aid", aid);
                    values.put("_title", title);
                    values.put("_notes", "");
                    values.put("_url", audio_url);
                    values.put("_pid", pid);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date();
                    values.put("_created", dateFormat.format(date));
                    mDB.insertOrThrow("TmpAudios",null,values);
                    Log.d(TAG, "Inserted " + aid + " " + title);
                }
                mDB.close();
            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
            }

        } else {
            Log.e(TAG, "Couldn't get json from server.");
        }
    }

    private void GetAudios () {
        //2017-10-23 Download ID stored in table TmpAudios
        Long dlID;

        //2017-09-24 Copied from GetOneAudio.java
        // get download service and enqueue file
        // Reference: https://stackoverflow.com/questions/4870667/how-can-i-use-getsystemservice-in-a-non-activity-class-locationmanager
        // If it was Activity: DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        DbOpenHelper mHelper = new DbOpenHelper(mContext);
        Log.w(TAG, "In function doInBackground.");
        //ToDo exclude audios published certain days ago. Now try to get them no matter how old, as long as files not exist.
        Cursor cursor = mHelper.getReadableDatabase().rawQuery(
                "SELECT a._id AS AID, a._title AS TITLE, a._url AS URL "
                        + "FROM TmpAudios AS a, PrfPrograms AS p "
                        + "WHERE p._pid = a._pid "
                        + "AND p._liked = 1 AND a._requested IS NULL",
                null);
        Log.d(TAG, "Records selected for download: " + cursor.getCount());
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String url = cursor.getString(cursor.getColumnIndex("URL"));
            String fileName =  url.substring(url.lastIndexOf('/') + 1, url.length());
            String audioID=cursor.getString(cursor.getColumnIndex("AID"));

            //2017-10-08 Setting download directory according to SharedPreferences
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            File realDirectory = null;
            String[] storagePaths = null;
            if ( prefs.getBoolean("pref_use_tf_card", true) ) {
                storagePaths = getStorageDirectories(mContext);
                realDirectory = new File(storagePaths[0]);
            } else {
                realDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }

            //2017-09-19 Enqueue only if file not yet there
            //2017-10-08 Use the realDirectory determined above instead of hard-coding
            //File fileFullPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

            File fileFullPath = new File(realDirectory, fileName);
            if ( ! fileFullPath.exists()) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(doRealUrl(url)));
                request.setDescription(cursor.getString(cursor.getColumnIndex("TITLE")));
                request.setTitle(cursor.getString(cursor.getColumnIndex("TITLE")));
                // in order for this if to run, you must use the android 3.2 to compile your app
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                //2017-09-26 Run-time check permission
                //https://stackoverflow.com/questions/40514335/download-manager-android-permissions-error-write-external-storage
                //https://developer.android.com/training/permissions/requesting.html
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, "You have permission");
                        if ( prefs.getBoolean("pref_use_tf_card", true) ) {
                            request.setDestinationInExternalFilesDir(mContext, storagePaths[0], fileName);
                        } else {
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        }
                        dlID = manager.enqueue(request);
                        MarkAudioRequested(audioID, dlID);
                    } else {
                        Log.e(TAG, "You don't have Write_External_Storage permission");
                    }
                } else {
                    //When run-time permission request was not needed
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                    dlID = manager.enqueue(request);
                    MarkAudioRequested(audioID, dlID);
                }
            } else {
                Log.w(TAG, "File " + fileName + " already exists.");
                /*2017-09-29 doInBackGround cannot update UI.
                Reference: https://stackoverflow.com/questions/16830255/how-to-display-toast-in-asynctask-in-android
                 */
                /*Toast.makeText(mContext,
                        TAG + "File " + fileName + " already exists.",
                        Toast.LENGTH_LONG).show();*/
            }
            cursor.moveToNext();
        }
        cursor.close();
        mHelper.close();
    }

    /* 2017-10-22
        Update the audio record of the time stamp when request is submitted to Download Manager
     */
    private void MarkAudioRequested(String audioID, Long dlid){
        DbOpenHelper mHelper = new DbOpenHelper(mContext);
        SQLiteDatabase mDB = null;
        mDB = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        values.put("_requested", dateFormat.format(date));
        values.put("_dlid", dlid);
        mDB.update("TmpAudios", values, "_id=" + audioID, null);
        mDB.close();
    }

    /* 2017-10-22
     * @return true if record with audioID exists; otherwise false
     */
    private boolean AudioIDExists(String audioID) {
        DbOpenHelper mHelper = new DbOpenHelper(mContext);
        SQLiteDatabase mDB = null;
        mDB = mHelper.getReadableDatabase();
        boolean var;
        //Select _id where _aid = audioID
        Cursor c = mDB.query("TmpAudios", new String[] {"_id"}, "_aid=" + audioID, null, null, null, null);
        if (c.getCount() == 0) {
            var = false;
        } else {
            var = true;
        }
        //Log.d(TAG, "Function AudioIDExists(" + audioID + ") returned " + c.getCount() + " rows.");
        c.close();
        mDB.close();
        return var;
    }

    /**
     * 主要是处理小米手机的
     * @param url 下载地址
     * @return 真实的下载地址
     *     作者：吕檀溪 链接：http://www.jianshu.com/p/9bd5d81d6060
     */
    private String doRealUrl(String url) {
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
