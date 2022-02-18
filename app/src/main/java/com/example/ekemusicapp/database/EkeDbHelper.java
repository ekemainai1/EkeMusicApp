package com.example.ekemusicapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ekemini on 8/20/2017.
 */

public class EkeDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = EkeDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME =  "EkeMusicDatabase.db";


    public EkeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
    *Once you have defined how your database looks, you should implement methods that create and
    * maintain the database and tables. Here are some typical statements that create and delete
    * a table:
    */

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // The music library table
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + EkeDbContract.EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE + " (" +
                        EkeDbContract.EkeMusicInfoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_TITLE + " TEXT NOT NULL, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_SIZE + " TEXT NOT NULL, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_DURATION + " TEXT NOT NULL, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_ARTIST + " TEXT NOT NULL, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FILE_PATH + " TEXT NOT NULL );";

        // FTS virtual table for search
        final String SQL_CREATE_FTS =
                "CREATE VIRTUAL TABLE " + EkeDbContract.EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE +
                        " USING fts4 (" +
                        EkeDbContract.EkeMusicInfoEntry._id + ", " +
                        EkeDbContract.EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1 + ", " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE + ", " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION + ", " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST + ", " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_FILEPATH + ")";
        //String trigger_1 = insertSongTrigger();
        //String trigger_2 = deleteSongTrigger();

        // The music library table
        final String SQL_CREATE_SUGS =
                "CREATE TABLE " + EkeDbContract.EkeMusicInfoEntry.SUGGESTIONS_TABLE + " (" +
                        EkeDbContract.EkeMusicInfoEntry._Id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_TITLE + " TEXT, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_SIZE + " TEXT, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_DURATION + " TEXT, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_ARTIST + " TEXT, " +
                        EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_FILE_PATH + " TEXT );";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
        sqLiteDatabase.execSQL(SQL_CREATE_FTS);
        sqLiteDatabase.execSQL(SQL_CREATE_SUGS);
       // sqLiteDatabase.execSQL(trigger_1); // create insert trigger
       // sqLiteDatabase.execSQL(trigger_2);  // create delete trigger
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EkeDbContract.EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EkeDbContract.EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EkeDbContract.EkeMusicInfoEntry.SUGGESTIONS_TABLE);
        //sqLiteDatabase.execSQL("DROP trigger add_songs");       // Drop insert trigger
        //sqLiteDatabase.execSQL("DROP trigger delete_songs");    // Drop delete trigger
        onCreate(sqLiteDatabase);
    }

    /**
     * Create triggers to update virtual table rows once external content table is updated
     */
    /**public String insertSongTrigger(){
         String insertRecord = "CREATE TRIGGER [IF NOT EXISTS] add_songs "
                + " AFTER INSERT "
                + "ON[EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE]"
                + " for each row "
                + " BEGIN "
                + " insert into  EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE values " +
                "(new.EkeMusicInfoEntry._ID , new.EkeMusicInfoEntry.COLUMN_SONG_TITLE );"
                + " END;";
        return insertRecord;
    }*/

    /**
     * Create triggers to delete virtual table content when external table is deleted
     */
    /**public String deleteSongTrigger(){
        String deleteRecord = "CREATE TRIGGER [IF NOT EXISTS] delete_songs " +
                " AFTER DELETE " +
                "ON [EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE]" +
                " for each row " +
                " BEGIN " +
                "  delete from EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE " +
                "where EkeMusicInfoEntry.COL_TITLE = old.EkeMusicInfoEntry.COL_TITLE;  " +
                " END; ";
        return deleteRecord;
    }*/



}
