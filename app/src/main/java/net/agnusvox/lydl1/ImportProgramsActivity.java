package net.agnusvox.lydl1;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
//import java.util.HashMap;
import java.util.List;

public class ImportProgramsActivity extends AppCompatActivity {

    /*2017-08-30
        Made reference: https://www.tutorialspoint.com/android/android_json_parser.htm
     */
    /*@Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_programs);
    }*/

    private String TAG = ImportProgramsActivity.class.getSimpleName();
    private ListView lv;
    List<PrfProgram> prfProgramList = new ArrayList<PrfProgram>();
    private MyAdapter adapter;

    //ArrayList<HashMap<String, String>> entryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_programs);

        //entryList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        new GetEntries().execute();
    }

    private class GetEntries extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ImportProgramsActivity.this,"资料下载中",Toast.LENGTH_LONG).show();

            DbOpenHelper mHelper = new DbOpenHelper(ImportProgramsActivity.this);
            SQLiteDatabase mDB = null;
            mDB = mHelper.getWritableDatabase();
            mDB.execSQL("DELETE FROM TmpPrograms");
            mDB.close();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            //String url = "https://api.androidhive.info/contacts/";
            //2017-08-31 Changed JSON contacts to programs at TXLY2
            String url = "http://txly2.net/index.php?option=com_vdata&task=get_feeds&type=prg0920v2";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONArray programs = new JSONArray(jsonStr);

                    DbOpenHelper mHelper = new DbOpenHelper(ImportProgramsActivity.this);
                    //this in a nested class refers to the nested class instance,
                    // not to the outer class instance (MainActivity).
                    // Qualify it with e.g. ImportProgramsActivity.this to refer to the outer class instance
                    SQLiteDatabase mDB = null;
                    mDB = mHelper.getWritableDatabase();

                    // looping through All Programs
                    for (int i = 0; i < programs.length(); i++) {
                        //Putting onto the list
                        JSONObject c = programs.getJSONObject(i);
                        String pid = c.getString("id");
                        String title = c.getString("title");
                        String picture = c.getString("picture");

                        // tmp hash map for single contact
                        //HashMap<String, String> program = new HashMap<>();

                        // adding each child node to HashMap key => value
                        //program.put("pid", pid);
                        //program.put("title", title);
                        //program.put("picture", picture);

                        // adding contact to contact list
                        //entryList.add(program);

                        /* 2017-09-02
                            Putting to the table
                         */
                        mDB = mHelper.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("_pid", pid);
                        values.put("_title", title);
                        values.put("_picture", picture);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date();
                        values.put("_created", dateFormat.format(date));
                        mDB.insertOrThrow("TmpPrograms",null,values);
                    }

                    //Insert new records into prfprograms from tmpprograms
                    mDB.execSQL("INSERT INTO PrfPrograms (_pid,_title,_picture,_created,_liked) SELECT _pid,_title,_picture,_created,0 FROM TmpPrograms AS tp WHERE tp._pid NOT IN (SELECT _pid FROM PrfPrograms)");
                    //Todo: Deleting expired programs and handling changed programs.
                    mDB.close();

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            /* 2017-09-06
                Use MyAdapter instead of SimpleAdapter
             */
            super.onPostExecute(result);
            lv=(ListView)findViewById(R.id.list); //R.id.list in layout/activity_import_programs.xml

            DbOpenHelper mHelper = new DbOpenHelper(ImportProgramsActivity.this);
            Cursor cursor = mHelper.getReadableDatabase().rawQuery("SELECT _pid, _title, _picture, _liked FROM PrfPrograms", null);
            cursor.moveToFirst();
            //ArrayList<PrfProgram> prfProgramList = new ArrayList<PrfProgram>();
            while(!cursor.isAfterLast()) {
                prfProgramList.add(new PrfProgram(
                        cursor.getInt(cursor.getColumnIndex("_pid")),
                        cursor.getString(cursor.getColumnIndex("_title")),
                        //2017-09-17 Hide picture field. Todo: remove _picture from SQL.
                        //cursor.getString(cursor.getColumnIndex("_picture")),
                        "",
                        cursor.getInt(cursor.getColumnIndex("_liked")) == 1 ? true : false
                        )
                );
                cursor.moveToNext();
            }
            cursor.close();
            mHelper.close();
            //SQLiteDatabase mDB = null;
            //mDB = mHelper.getReadableDatabase();
            //mDB.execSQL("XXXXX");
            //mDB.close();
            /* Populate list
            prfProgramList.add(new PrfProgram(1, "HBO電影台","Picture"));
            prfProgramList.add(new PrfProgram(2, "綠光戰警","7:00"));
            prfProgramList.add(new PrfProgram(3, "鋼鐵人","9:00"));
            */
            adapter = new MyAdapter(ImportProgramsActivity.this, prfProgramList);
            lv.setAdapter(adapter);
        }
    }

    public void saveToTable() {
        //Start to update database of the new state
        DbOpenHelper mHelper = new DbOpenHelper(ImportProgramsActivity.this);
        SQLiteDatabase mDB = null;
        mDB = mHelper.getWritableDatabase();
        Log.d(TAG, "saveToTable() start for loop" );
        // looping through All Programs
        for (final PrfProgram prfprogram: prfProgramList){
            //Log.e("n", "PID:" + prfprogram.getPid() + " Liked:" + prfprogram.getLikedInt());
            mDB.execSQL("UPDATE PrfPrograms SET _LIKED = " + prfprogram.getLikedInt() + " WHERE _PID = " + prfprogram.getPid());
        }
        mDB.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "ImportProgramsActivity Paused" );

        saveToTable();
    }
}
