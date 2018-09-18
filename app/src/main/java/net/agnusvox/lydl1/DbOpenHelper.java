package net.agnusvox.lydl1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ken on 2/9/2017.
 * Reference: http://blog.changyy.org/2012/05/android-sqlite-sqlitedatabase.html
 * https://stackoverflow.com/questions/17529766/view-contents-of-database-file-in-android-studio
 */

public class DbOpenHelper extends SQLiteOpenHelper {

    final private static int _DB_VERSION = 12;  //Increase this number if you want tables recreated.
    final private static String _DB_DATABASE_NAME = "LYDL.db";

    public DbOpenHelper(Context context) {
        super(context,_DB_DATABASE_NAME,null,_DB_VERSION);
    }
    public DbOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        /* TmpPrograms and TmpAudios are table popluated when user triggers import action.
            PrfPrograms stores also the preference users added. It got updated from TmpPrograms.
         */
        db.execSQL(
                "CREATE TABLE TmpPrograms (" +
                        " _id INTEGER PRIMARY KEY, " +
                        " _pid INTEGER NOT NULL, " +
                        " _title TEXT NOT NULL, " +
                        " _picture TEXT NOT NULL, " +
                        " _created DATETIME NOT NULL " +
                        ")"
        );
        db.execSQL(
                "CREATE TABLE PrfPrograms (" +
                        " _id INTEGER PRIMARY KEY, " +
                        " _pid INTEGER NOT NULL, " +
                        " _title TEXT NOT NULL, " +
                        " _picture TEXT NOT NULL, " +
                        " _created DATETIME NOT NULL, " +
                        " _liked BOOLEAN " +
                        ")"
        );
        //Index on _pid handy when linking Audios to Programs.
        //Reference: https://stackoverflow.com/questions/27373344/sqlite-database-gives-warning-automatic-index-on-table-namecolumn-after-upgr
        db.execSQL(
                "CREATE INDEX pid_index ON PrfPrograms(_pid)");
        db.execSQL(
                "CREATE TABLE TmpAudios (" +
                        " _id INTEGER PRIMARY KEY, " +
                        " _aid INTEGER NOT NULL, " +
                        " _title TEXT NOT NULL, " +
                        " _notes TEXT NOT NULL, " +
                        " _url TEXT NOT NULL, " +
                        " _pid INTEGER NOT NULL, " +
                        " _created DATETIME NOT NULL, " +
                        " _requested DATETIME," +
                        " _dlid INTEGER, " +
                        " _downloaded DATETIME" +
                        ")"
        );
        db.execSQL(
                "CREATE TABLE PrfAudios (" +
                        " _id INTEGER PRIMARY KEY, " +
                        " _aid INTEGER NOT NULL, " +
                        " _title TEXT NOT NULL, " +
                        " _notes TEXT NOT NULL, " +
                        " _url TEXT NOT NULL, " +
                        " _pid INTEGER NOT NULL, " +
                        " _created DATETIME NOT NULL " +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS TmpPrograms");
        db.execSQL("DROP TABLE IF EXISTS PrfPrograms");
        db.execSQL("DROP TABLE IF EXISTS TmpAudios");
        db.execSQL("DROP TABLE IF EXISTS PrfAudios");
        onCreate(db);
    }
}