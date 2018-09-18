package net.agnusvox.lydl1;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import static net.agnusvox.lydl1.StorageUtil.getStorageDirectories;

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //TextView CountField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE );
                List<JobInfo> alljobs = jobScheduler.getAllPendingJobs();
                if ( alljobs.size() == 0 ) {
                    StartBgService();
                    refreshFab();
                } else {
                    StopBgService();
                    refreshFab();
                }

                /* 2017-10-21 No longer this this tester.
                String[] storagePaths = getStorageDirectories(getApplicationContext());

                Snackbar.make(view, "Download paths:\n" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + ":" + storagePaths[0], Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                */
                /*
                //2017-10-08 Get storage paths

                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

                // 2. Chain together various setter methods to set the dialog characteristics
                String[] storagePaths = getStorageDirectories(getApplicationContext());
                builder.setMessage(storagePaths[0])
                        .setTitle(R.string.debug_info_title)
                        .show();

                // 3. Get the AlertDialog from create()
                //AlertDialog dialog = builder.create();
                */
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //2017-09-16 Initialize view
        //CountField = (TextView) findViewById(R.id.liked_programs);

        //2017-09-02 Database operations
        /* 2017-09-04 This test code is no longer necessary.
        // Init
        DbOpenHelper mHelper = new DbOpenHelper(this);
        SQLiteDatabase mDB = null;

        // Insert by raw SQL
        mDB = mHelper.getWritableDatabase();

        mDB.execSQL( "INSERT INTO Programs (_TITLE,_PICTURE,_CREATED) VALUES ('Hello World', 'nopic.png', datetime('now'))");
        mDB.close();

        // Insert by object method
        mDB = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_TITLE","XAUDIO");
        values.put("_URL","http://something.mp3");
        values.put("_CREATED", "2017-09-02 13:36:10");
        mDB.insertOrThrow("Audios",null,values);

        mDB.close();
        */

        // Query by raw SQL
        /*
        mDB = mHelper.getWritableDatabase(); // mDB = mHelper.getReadableDatabase();
        Cursor cursor = mDB.rawQuery("SELECT _ID, _DATA, _DATETIME FROM MyTable", null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Log.e("SQLiteDBTestingActivity","_ID = "+cursor.getInt(0));
            Log.e("SQLiteDBTestingActivity","_DATA = "+cursor.getString(1));
            Log.e("SQLiteDBTestingActivity","_DATETIME = "+cursor.getString(2).substring(0, 16));
            cursor.moveToNext();
        }
        startManagingCursor(cursor);
        cursor.close();
        mDB.close();
        */
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //2017-10-02 Skip options menu. Settings now invoked in Drawer menu.
        //getMenuInflater().inflate(R.menu.drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //2017-08-30
            //Starting a new Intent
            Intent nextScreen = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(nextScreen);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_import_programs) {
            // Handle the camera action
            //2017-08-30
            //Starting a new Intent
            Intent nextScreen = new Intent(getApplicationContext(), ImportProgramsActivity.class);
            startActivity(nextScreen);
            return true;

        } else if (id == R.id.nav_import_audios) {
            Intent nextScreen = new Intent(getApplicationContext(), ImportAudiosActivity.class);
            startActivity(nextScreen);
        } else if (id == R.id.nav_get_audio_task) {
            Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            if(isSDPresent)
            {
                /* 2017-09-29 Stop calling GetOneAudio Activity but call GetAudioTask
                Intent nextScreen = new Intent(getApplicationContext(), GetOneAudio.class);
                Log.e("n", "Started Activity GetOneAudio");
                startActivity(nextScreen);
                */
                GetAudioTask mGetAudioTask = new GetAudioTask(this);
                mGetAudioTask.execute();
            }
            else
            {
                Toast.makeText(this,"SD card not present, sorry.",Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.nav_settings) {
            //2017-10-02 Start Settings here
            Intent nextScreen = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(nextScreen);

        } else if (id == R.id.nav_start_bgservice) {
            StartBgService();
        } else if (id == R.id.nav_stop_bgservice) {
            StopBgService();
            /*2017-10-21 Moved code to function StopBgService */
            /*
            JobScheduler jobScheduler = (JobScheduler)this.getSystemService(JOB_SCHEDULER_SERVICE );
            jobScheduler.cancelAll();
            */
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        refreshFab();
        return true;
    }

    /* 2017-09-17
        Update values on showing this activity
     */
    @Override
    public void onResume () {
        super.onResume();
        DbOpenHelper mHelper = new DbOpenHelper(this);
        SQLiteDatabase mDB = null;
        mDB = mHelper.getReadableDatabase();

        Cursor c = mHelper.getReadableDatabase().rawQuery("SELECT count(_PID) AS count FROM PrfPrograms", null);
        c.moveToFirst();
        int PrfCount = c.getInt(c.getColumnIndex("count"));

        c = mHelper.getReadableDatabase().rawQuery("SELECT count(_PID) AS count FROM PrfPrograms WHERE _LIKED = 1", null);
        c.moveToFirst();
        int LikeCount = c.getInt(c.getColumnIndex("count"));
        c.close();
        mDB.close();

        TextView Avail_Programs_Field = (TextView) findViewById(R.id.avail_programs);
        Avail_Programs_Field.setText(String.format("%s", PrfCount));

        TextView Liked_Programs_Field = (TextView) findViewById(R.id.liked_programs);
        Liked_Programs_Field.setText(String.format("%s", LikeCount));

        //2017-10-08 Refresh storage pages shown on screen
        String[] storagePaths = getStorageDirectories(this);
        TextView Path_First_Storage = (TextView) findViewById(R.id.path_first_storage);
        Path_First_Storage.setText(String.format("%s", storagePaths[0]));
        try {
            TextView Path_Second_Storage = (TextView) findViewById(R.id.path_second_storage);
            Path_Second_Storage.setText(String.format("%s", storagePaths[1]));
        } catch ( ArrayIndexOutOfBoundsException e) {
            Log.e("onResume", "No more storage path found!");
        }

        refreshFab();
    }

    /* 2017-10-21 Refresh information lines */
    public void RefreshInfo(String[] message) {
        TextView MessageLine1 = (TextView) findViewById(R.id.message_line1);
        MessageLine1.setText(String.format("%s", message));
    }

    /* 2017-10-21 Start Background Activity, moved code from drawer here */
    private int StartBgService() {
        //2017-09-30 Start JobSchedule
        //Reference: http://blog.teamtreehouse.com/scheduling-work-jobscheduler
        ComponentName componentName = new ComponentName(this, LYDLBgService.class);
        JobInfo jobInfo = new JobInfo.Builder(72901, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPeriodic(5000) //1000 is 1 second!
                .build();
        JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            //2017-10-17 ToDo Hide this item if there is a job started, and show stop button
                /*MenuItem itemToToggle = menu.findItem(R.id.nav_start_bgservice);
                itemToToggle.setVisible(false);
                itemToToggle = (MenuItem) findViewById(R.id.nav_stop_bgservice);
                itemToToggle.setVisible(true);
                this.invalidateOptionsMenu();*/
            //item.setVisible(false);
            Log.d("DrawerActivity", "Job scheduled!");
        } else {
            Log.d("DrawerActivity", "Job not scheduled");
        }
        return resultCode;
    }

    private void StopBgService(){
        JobScheduler jobScheduler = (JobScheduler)this.getSystemService(JOB_SCHEDULER_SERVICE );
        jobScheduler.cancelAll();
    }

    /* 2017-10-21 Refresh Floating Action Button */
    private void refreshFab() {
        //2017-10-21 Update show / not show start and stop service menu item
        JobScheduler jobScheduler = (JobScheduler) this.getSystemService(JOB_SCHEDULER_SERVICE);
        List<JobInfo> alljobs = jobScheduler.getAllPendingJobs();
        Log.d("onResume", "Jobs running: " + alljobs.size());
        //Toast.makeText(this, "Jobs running: " + alljobs.size(), Toast.LENGTH_LONG).show();

        //fab is a vector asset whose png is generated at compile time. So setting color does not work.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (alljobs.size() == 0) {
            //Reference: https://stackoverflow.com/questions/40426928/how-to-programmatically-set-backgroundtint-of-floatingactionbutton-with-colorsta
            //Colors usable in this probject is defined at res/values/colors.xml
            //fab.setImageResource(R.drawable.ic_sync_black_24dp);  //Just tried this for fun
            fab.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorBgServiceStopped));
            Toast.makeText(this, R.string.BgServiceStopped, Toast.LENGTH_LONG).show();
        } else {
            //fab.setImageResource(R.drawable.ic_menu_manage);  //Just tried this for fun
            fab.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.colorBgServiceStarted));
            Toast.makeText(this, R.string.BgServiceStarted, Toast.LENGTH_LONG).show();
        }
    }
}
