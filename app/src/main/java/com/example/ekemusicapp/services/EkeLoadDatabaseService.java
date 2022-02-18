package com.example.ekemusicapp.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.ekemusicapp.database.EkeDbContract;

/**
 * Created by Ekemini on 9/18/2017.
 */

public class EkeLoadDatabaseService extends IntentService{


    public EkeLoadDatabaseService() {
        super("EkeLoadDatabaseService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Cursor mCursor = checkDatabase();
        Cursor eCursor = checkDatabaseFts();

        int songsCount = mCursor.getCount();
        int songsCountFts = eCursor.getCount();

        if(songsCount == 0 && songsCountFts == 0){
            savedSongs();

        }else {
            Toast.makeText(getApplicationContext(), "Database Already Loaded", Toast.LENGTH_LONG).show();
        }
    }

    private Cursor checkDatabase(){
        Uri uri = EkeDbContract.EkeMusicInfoEntry.CONTENT_URI;
        return getContentResolver().query(uri, null, null, null, null);
    }

    private Cursor checkDatabaseFts(){
        Uri uri = EkeDbContract.EkeMusicInfoEntry.CONTENT_FTS_URI;
        return getContentResolver().query(uri, null, null, null, null);
    }

    private void savedSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);


        if ((musicCursor != null && musicCursor.moveToFirst())) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(Media._ID);
            int titleColumn = musicCursor.getColumnIndex(Media.TITLE);
            int songSize = musicCursor.getColumnIndex(Media.SIZE);
            int songDur = musicCursor.getColumnIndex(Media.DURATION);
            int artistColumn = musicCursor.getColumnIndex(Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex(Media.DATA);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                long songSized = musicCursor.getLong(songSize);
                String size = convertAudioSize(songSized);
                long songTime = musicCursor.getLong(songDur);
                String sTime = convertDuration(songTime);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);


                Log.e(thisTitle, "ben");
                Log.e(size, "bene");
                Log.e(sTime, "bend");
                Log.e(thisArtist, "benc");
                Log.e(thisPath, "path");


                // Create a ContentValues object where column names are the keys,and Toto's pet
                // attributes are the values.
                ContentValues values = new ContentValues();
                values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_TITLE, thisTitle);
                values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_SIZE, size);
                values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_DURATION, sTime);
                values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_ARTIST, thisArtist);
                values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FILE_PATH, thisPath);

                ContentValues contentValues = new ContentValues();
                contentValues.put(EkeDbContract.EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1, thisTitle);
                contentValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE, size);
                contentValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION, sTime);
                contentValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST, thisArtist);
                contentValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_FILEPATH, thisPath);

                // Receive the new content URI that will allow us to access Toto's data in the future.
                Uri newUri = getContentResolver().insert(EkeDbContract.EkeMusicInfoEntry.CONTENT_URI, values);
                Uri newUriFts = getContentResolver().insert(EkeDbContract.EkeMusicInfoEntry.CONTENT_FTS_URI, contentValues);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null && newUriFts == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    //Toast.makeText(this, getString(R.string.editor_insert_song_failed),
                    //Toast.LENGTH_SHORT).show();

                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    //Toast.makeText(this, getString(R.string.editor_insert_song_successful),
                    //Toast.LENGTH_SHORT).show();
                }

            }
            while (musicCursor.moveToNext());
        }
    }

    public String convertDuration(long duration) {
        String out = null;
        long hours;
        try {
            hours = (duration / 3600000);
        } catch (Exception e) {
            e.printStackTrace();
            return out;
        }
        long remaining_minutes = (duration - (hours * 3600000)) / 60000;
        String minutes = String.valueOf(remaining_minutes);
        if (minutes.equals("0")) {
            minutes = "00";
        }
        long remaining_seconds = (duration - (hours * 3600000) - (remaining_minutes * 60000));
        String seconds = String.valueOf(remaining_seconds);
        if (seconds.length() < 2) {
            seconds = "00";
        } else {
            seconds = seconds.substring(0, 2);
        }

        if (hours > 0) {
            out = hours + ":" + minutes + ":" + seconds;
        } else {
            out = minutes + ":" + seconds;
        }

        return out;

    }

    @SuppressLint("DefaultLocale")
    public String convertAudioSize(long size) {
        String val = null;
        float sized = 0;
        if (size == 0) {
            try {
                sized = (size/1000000);
            } catch (Exception e) {
                e.printStackTrace();
                return val;
            }
        }

        float sizedMb = (size + sized) / 1000000;
        String audioSize = String.valueOf(sizedMb);
        if (audioSize.length() >= 1) {
            val = String.format("%.2f", sizedMb);
            val = val + "MB";
        }

        return val;

    }

}
