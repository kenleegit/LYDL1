package net.agnusvox.lydl1;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ImportAudiosActivity extends AppCompatActivity {

    /*2017-08-30
        Made reference: https://www.tutorialspoint.com/android/android_json_parser.htm
     */
    /*@Override
      protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_audios);
    }*/

    private String TAG = ImportAudiosActivity.class.getSimpleName();
    private ListView lv;

    ArrayList<HashMap<String, String>> entryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_audios);

        entryList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        new GetEntries().execute();
    }

    private class GetEntries extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(ImportAudiosActivity.this,"资料下载中",Toast.LENGTH_LONG).show();

            DbOpenHelper mHelper = new DbOpenHelper(ImportAudiosActivity.this);
            SQLiteDatabase mDB = null;
            mDB = mHelper.getWritableDatabase();
            mDB.execSQL("DELETE FROM TmpAudios");
            mDB.close();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            //String url = "https://api.androidhive.info/contacts/";
            //2017-08-31 Changed JSON contacts to audios at TXLY2
            String url = "http://txly2.net/index.php?option=com_vdata&task=get_feeds&type=aud0920v2";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    // Getting JSON Array node
                    JSONArray audios = new JSONArray(jsonStr);

                    DbOpenHelper mHelper = new DbOpenHelper(ImportAudiosActivity.this);
                    //this in a nested class refers to the nested class instance,
                    // not to the outer class instance (MainActivity).
                    // Qualify it with e.g. ImportProgramsActivity.this to refer to the outer class instance
                    SQLiteDatabase mDB = null;
                    mDB = mHelper.getWritableDatabase();

                    // looping through All Audios
                    for (int i = 0; i < audios.length(); i++) {
                        JSONObject c = audios.getJSONObject(i);
                        String aid = c.getString("sermon_id");
                        String title = c.getString("sermon_title");
                        //String notes = c.getString("sermon_notes");
                        //String picture = c.getString("picture");
                        String audio_url = c.getString("url");
                        String pid = c.getString("series_id");

                        // tmp hash map for single contact
                        HashMap<String, String> audio = new HashMap<>();

                        // adding each child node to HashMap key => value
                        audio.put("aid", aid);
                        audio.put("title", title);
                        //audio.put("picture", picture);
                        audio.put("audio_url", audio_url);

                        // adding contact to contact list
                        entryList.add(audio);

                                                /* 2017-09-02
                            Putting to the table
                         */
                        mDB = mHelper.getWritableDatabase();
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
                    }
                    mDB.close();

                    /*
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("contacts");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);
                        String id = c.getString("id");
                        String name = c.getString("name");
                        String email = c.getString("email");
                        String address = c.getString("address");
                        String gender = c.getString("gender");

                        // Phone node is JSON Object
                        JSONObject phone = c.getJSONObject("phone");
                        String mobile = phone.getString("mobile");
                        String home = phone.getString("home");
                        String office = phone.getString("office");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("name", name);
                        contact.put("email", email);
                        contact.put("mobile", mobile);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                    */
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
            super.onPostExecute(result);
            ListAdapter adapter = new SimpleAdapter(ImportAudiosActivity.this, entryList,
                    R.layout.list_item, new String[]{ "title","audio_url"},
                    new int[]{R.id.title, R.id.picture});
            lv.setAdapter(adapter);
        }
    }
}
