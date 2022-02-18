package com.example.ekemusicapp.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.example.ekemusicapp.database.EkeDbContract;

public class EkeLoadSugsIntentService extends IntentService {

    public EkeLoadSugsIntentService() {
        super("EkeLoadSugsIntentService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
            saveSugsToDatabase();
    }

    private void saveSugsToDatabase() {

        getContentResolver().delete(EkeDbContract.EkeMusicInfoEntry.CONTENT_SUGGEST_URI, null, null);

        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = EkeDbContract.EkeMusicInfoEntry.CONTENT_FTS_URI;
        Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);


        if ((musicCursor != null && musicCursor.moveToFirst())) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry._id);
            int titleColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1);
            int songSize = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE);
            int songDur = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION);
            int artistColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST);
            int pathColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_FILEPATH);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String size = musicCursor.getString(songSize);
                String sTime = musicCursor.getString(songDur);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisPath = musicCursor.getString(pathColumn);

                // Create a ContentValues object where column names are the keys,and Toto's pet
                // attributes are the values.
                ContentValues contValues = new ContentValues();
                contValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_TITLE, thisTitle);
                contValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_SIZE, size);
                contValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_DURATION, sTime);
                contValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_ARTIST, thisArtist);
                contValues.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_FILE_PATH, thisPath);

                // Receive the new content URI that will allow us to access Toto's data in the future.

                Uri newUriSugs = getContentResolver().insert(EkeDbContract.EkeMusicInfoEntry.CONTENT_SUGGEST_URI, contValues);


                // Show a toast message depending on whether or not the insertion was successful
                if (newUriSugs == null) {
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


}
