package com.example.ekemusicapp.database;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ekemusicapp.database.EkeDbContract.EkeMusicInfoEntry;

import java.util.HashMap;

/**
 * Created by Ekemini on 8/20/2017.
 */

public class EkeContentProvider extends ContentProvider {

    public static final String LOG_TAG = EkeContentProvider.class.getSimpleName();

    //Database helper object created
    private EkeDbHelper mDbHelper;

    // Define cursor to hold the data
    public Cursor cursor;

    /** URI matcher code for the content URI for the songs table */
    public static final int MUSICS = 100;
    public static final int MUSIC_ID = 101;
    public static final int MUSIC_FTS_ID = 102;
    public static final int MUSIC_FTS = 103;
    public static final int MUSIC_FTS_SEARCH = 104;
    public static final int MUSIC_SEARCH_SUGGEST_ID = 105;
    public static final int MUSIC_SEARCH_SUGGEST = 106;


    /** URI matcher object to match a context URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, EkeDbContract.PATH_MUSIC, MUSICS);
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, EkeDbContract.PATH_MUSIC + "/#", MUSIC_ID);
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, EkeDbContract.PATH_VIRTUAL_MUSIC, MUSIC_FTS);
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/#", MUSIC_FTS_ID);
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", MUSIC_FTS_SEARCH);
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, EkeDbContract.PATH_MUSIC_SUGGEST, MUSIC_SEARCH_SUGGEST);
        sUriMatcher.addURI(EkeDbContract.CONTENT_AUTHORITY, EkeDbContract.PATH_MUSIC_SUGGEST + "/#", MUSIC_SEARCH_SUGGEST_ID);

    }

    private static final HashMap<String, String> projectionMap = new HashMap<String, String>();


    static {

        projectionMap.put(EkeMusicInfoEntry._id, EkeMusicInfoEntry._id +
                " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        projectionMap.put(EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1, EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1 +
                " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        projectionMap.put(EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE, EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE +
                " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        projectionMap.put(EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION, EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION +
                " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        projectionMap.put(EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST, EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST +
                " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);

    }

    @Override
    public boolean onCreate() {
        mDbHelper = new EkeDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projections, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        switch (match){
            case MUSICS:
                cursor = sqLiteDatabase.query(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE,
                        EkeMusicInfoEntry.PROJECTION_ALL, selection, selectionArgs, null,null,
                        EkeMusicInfoEntry.SORT_ORDER_DEFAULT);
                break;

            case MUSIC_ID:
                // Define selection clause for table query results with specified ID
                selection = EkeMusicInfoEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDatabase.query(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE,
                        projections, selection, selectionArgs, null, null, sortOrder);
                break;

            case MUSIC_FTS:
                // Define selection clause for searching song titles
                cursor = sqLiteDatabase.query(EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE,
                        EkeMusicInfoEntry.PROJECTION_FTS, selection, selectionArgs, null,null,
                        EkeMusicInfoEntry.SORT_ORDER_FTS);
                break;

            case MUSIC_FTS_ID:
                // Define selection clause for table query results with specified ID
                selection = EkeMusicInfoEntry._id + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = sqLiteDatabase.query(EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE,
                        projections, selection, selectionArgs, null, null, sortOrder);
                break;

            case MUSIC_FTS_SEARCH:
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE);
                builder.setProjectionMap(projectionMap);
                if (selectionArgs != null && selectionArgs.length > 0 && selectionArgs[0].length() > 0) {
                    // the entered text can be found in selectionArgs[0]
                    // return a cursor with appropriate data
                    selection = EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1 + " LIKE ?";
                    selectionArgs = new String[]{"%" + selectionArgs[0] + "%"};

                    cursor = builder.query(sqLiteDatabase, EkeMusicInfoEntry.PROJECTION_FTS, selection,
                            selectionArgs, null, null, EkeMusicInfoEntry.SORT_ORDER_FTS);
                }
                else {
                    // user hasn’t entered anything
                    // thus return a default cursor
                    Toast.makeText(getContext(), "No Search Word Supplied", Toast.LENGTH_LONG).show();
                }
                break;

            case MUSIC_SEARCH_SUGGEST:
                cursor = sqLiteDatabase.query(EkeMusicInfoEntry.SUGGESTIONS_TABLE,
                        EkeMusicInfoEntry.PROJECTION_SUG, selection, selectionArgs, null,null,
                        EkeMusicInfoEntry.SORT_ORDER_SUG);
                break;

            case MUSIC_SEARCH_SUGGEST_ID:
                if (selectionArgs != null && selectionArgs.length > 0 && selectionArgs[0].length() > 0) {
                    // the entered text can be found in selectionArgs[0]
                    // return a cursor with appropriate data
                    selection = EkeMusicInfoEntry.COLUMN_SUG_TITLE + " =?";
                    selectionArgs = new String[]{"%" + selectionArgs[0] + "%"};
                    cursor = sqLiteDatabase.query(EkeMusicInfoEntry.SUGGESTIONS_TABLE,
                            projections, selection, selectionArgs, null, null, sortOrder);
                }
                else {
                    // user hasn’t entered anything
                    // thus return a default cursor
                    //Toast.makeText(getContext(), "No Search Word Supplied", Toast.LENGTH_LONG).show();
                }
             break;

            default:
               throw new IllegalArgumentException("cannot query unknown uri" + uri);
        }

        // Set Notification URI on the loaded cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MUSICS:
                return EkeMusicInfoEntry.CONTENT_LIST_TYPE;
            case MUSIC_ID:
                return EkeMusicInfoEntry.CONTENT_ITEM_TYPE;
            case MUSIC_FTS:
                return EkeMusicInfoEntry.CONTENT_SEARCH_TYPE;
            case MUSIC_FTS_ID:
                return EkeMusicInfoEntry.CONTENT_SEARCH_ITEM_TYPE;
            case MUSIC_SEARCH_SUGGEST:
                return EkeMusicInfoEntry.CONTENT_SUGGESST_LIST;
            case MUSIC_SEARCH_SUGGEST_ID:
                return EkeMusicInfoEntry.CONTENT_SUGGEST_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MUSICS:
                return insertSongs(uri, contentValues);
            case MUSIC_ID:
                return insertSongs(uri, contentValues);
            case MUSIC_FTS:
                return insertSongsFts(uri, contentValues);
            case MUSIC_FTS_ID:
                return insertSongsFts(uri, contentValues);
            case MUSIC_SEARCH_SUGGEST:
                return insertSongsSugs(uri, contentValues);
            case MUSIC_SEARCH_SUGGEST_ID:
                return insertSongsSugs(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        // Get writeable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MUSICS:
                // Delete all rows that match the selection and selection args
                return sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, s, strings);

            case MUSIC_ID:
                // Delete a single row given by the ID in the URI
                s = EkeMusicInfoEntry._ID + "=?";
                strings = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, s, strings);
            case MUSIC_FTS:
                return sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE, s, strings);
            case MUSIC_FTS_ID:
                // Delete a single row given by the ID in the URI
                s = EkeMusicInfoEntry._id + "=?";
                strings = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE, s, strings);

            case MUSIC_SEARCH_SUGGEST:
                return sqLiteDatabase.delete(EkeMusicInfoEntry.SUGGESTIONS_TABLE, s, strings);
            case MUSIC_SEARCH_SUGGEST_ID:
                // Delete a single row given by the ID in the URI
                s = EkeMusicInfoEntry._Id + "=?";
                strings = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return sqLiteDatabase.delete(EkeMusicInfoEntry.SUGGESTIONS_TABLE, s, strings);
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
                      @Nullable String[] strings) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MUSICS:
                return updateSong(uri, contentValues, s, strings);
            case MUSIC_ID:
                // For the Music _ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                s = EkeMusicInfoEntry._ID + "=?";
                strings = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateSong(uri, contentValues, s, strings);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Insert a song into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertSongs(Uri uri, ContentValues values) {

        long id = -1;

        // Get writeable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        try {

        //Clear out the table.
        //sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, null, null);
        sqLiteDatabase.beginTransaction();

        // Conduct sanity checks on music data into database
        String check_title = values.getAsString(EkeMusicInfoEntry.COLUMN_SONG_TITLE);
        String check_size = values.getAsString(EkeMusicInfoEntry.COLUMN_SONG_SIZE);
        String check_duration = values.getAsString(EkeMusicInfoEntry.COLUMN_SONG_DURATION);
        String check_artist = values.getAsString(EkeMusicInfoEntry.COLUMN_SONG_ARTIST);
        String check_file_path = values.getAsString(EkeMusicInfoEntry.COLUMN_SONG_FILE_PATH);

        if ((check_title.isEmpty() || check_size.isEmpty() || check_duration.isEmpty() ||
                check_artist.isEmpty() || check_file_path.isEmpty())) {
            throw new IllegalArgumentException("One song data is required");
        }
            // Insert the new song with the given values
            id = sqLiteDatabase.insert(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, null, values);

            // If the ID is -1, then the insertion failed. Log an error and return null.
            if (id == -1 ) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;


            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        }

        // Notify all listeners that the data has changed with the URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);

    }

    private Uri insertSongsFts(Uri uri, ContentValues values) {

        long ID = -1;

        // Get writeable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        try {

            //Clear out the table.
            //sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, null, null);
            sqLiteDatabase.beginTransaction();

            // Conduct sanity checks on music data into database
            String check_title = values.getAsString(EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1);

            if ((check_title.isEmpty())) {
                throw new IllegalArgumentException("One song data is required");
            }
            // Insert the new song with the given values
            ID = sqLiteDatabase.insert(EkeMusicInfoEntry.MUSIC_VIRTUAL_TABLE, null, values);


            // If the ID is -1, then the insertion failed. Log an error and return null.
            if (ID == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;


            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        }

        // Notify all listeners that the data has changed with the URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, ID);

    }

    private Uri insertSongsSugs(Uri uri, ContentValues values) {

        long Id = -1;

        // Get writeable database
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        try {

            //Clear out the table.
            //sqLiteDatabase.delete(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, null, null);
            sqLiteDatabase.beginTransaction();

            // Conduct sanity checks on music data into database
            String check_title = values.getAsString(EkeMusicInfoEntry.COLUMN_SUG_TITLE);

            if ((check_title.isEmpty())) {
                throw new IllegalArgumentException("One song data is required");
            }
            // Insert the new song with the given values
            Id = sqLiteDatabase.insert(EkeMusicInfoEntry.SUGGESTIONS_TABLE, null, values);


            // If the ID is -1, then the insertion failed. Log an error and return null.
            if (Id == -1) {
                Log.e(LOG_TAG, "Failed to insert row for " + uri);
                return null;


            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sqLiteDatabase.setTransactionSuccessful();
            sqLiteDatabase.endTransaction();
        }

        // Notify all listeners that the data has changed with the URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, Id);

    }

    private int updateSong(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // check that the name value is not null.
        if (values.containsKey(EkeMusicInfoEntry.COLUMN_SONG_TITLE)) {
            String name = values.getAsString(EkeMusicInfoEntry.COLUMN_SONG_TITLE);
            if (name == null) {
                throw new IllegalArgumentException("Song requires a name");
            }
        }


        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        return sqLiteDatabase.update(EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE, values, selection, selectionArgs);
    }



}
