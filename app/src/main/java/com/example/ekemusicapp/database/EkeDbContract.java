package com.example.ekemusicapp.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ekemini on 9/3/2017.
 */

public final class EkeDbContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private EkeDbContract() {}

    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */
    public static final String CONTENT_AUTHORITY = "com.example.ekemini.musicplayer.EkeContentProvider";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for Sunshine.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MUSIC = "MusicLibraryTable";
    public static final String PATH_VIRTUAL_MUSIC = "MusicSearchTable";
    public static final String PATH_MUSIC_SUGGEST = "SuggestionsTable";


    /* Inner class that defines the pets table contents */
    public static final class EkeMusicInfoEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the music library table from the content provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MUSIC);

        public static final Uri CONTENT_FTS_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VIRTUAL_MUSIC);

        public static final Uri CONTENT_SUGGEST_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MUSIC_SUGGEST);

        /**
         * The MIME type of the  for a list of songs.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSIC;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSIC;

        /**
         * The MIME type of song search
         */
        public static final String CONTENT_SEARCH_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIRTUAL_MUSIC;

        public static final String CONTENT_SEARCH_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VIRTUAL_MUSIC;

        /**
         * The MIME type of song search suggestion storage
         */
        public static final String CONTENT_SUGGESST_LIST =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSIC_SUGGEST;

        public static final String CONTENT_SUGGEST_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MUSIC_SUGGEST;

        public static final String MUSIC_LIBRARY_TABLE = "MusicLibraryTable";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_SONG_TITLE = "title";
        public static final String COLUMN_SONG_SIZE = "size";
        public static final String COLUMN_SONG_DURATION = "duration";
        public static final String COLUMN_SONG_ARTIST = "artist";
        public static final String COLUMN_SONG_FILE_PATH = "file_path";

        /**
         * Database virtual table columns for searching songs in the music librayr
         */
        public static final String MUSIC_VIRTUAL_TABLE = "MusicSearchTable";
        public static final String _id = BaseColumns._ID;
        public static final String SUGGEST_COLUMN_TEXT_1 = "searchTitle";
        public static final String COLUMN_SONG_FTS_SIZE = "searchSize";
        public static final String COLUMN_SONG_FTS_DURATION = "searchDur";
        public static final String COLUMN_SONG_FTS_ARTIST = "searchArtist";
        public static final String COLUMN_SONG_FTS_FILEPATH = "searchPath";

        public static final String SUGGESTIONS_TABLE = "SuggestionsTable";
        public static final String _Id = BaseColumns._ID;
        public static final String COLUMN_SUG_TITLE = "suggestTitle";
        public static final String COLUMN_SUG_SIZE = "suggestSize";
        public static final String COLUMN_SUG_DURATION = "suggestDuration";
        public static final String COLUMN_SUG_ARTIST = "suggestArtist";
        public static final String COLUMN_SUG_FILE_PATH = "suggestPath";

        /**
         * A projection of all columns
         * in the music table.
         */
        public static final String[] PROJECTION_ALL =
                {_ID,
                        COLUMN_SONG_TITLE,
                        COLUMN_SONG_SIZE,
                        COLUMN_SONG_DURATION,
                        COLUMN_SONG_ARTIST,
                        COLUMN_SONG_FILE_PATH};

        /**
         * The default sort order for
         * queries containing NAME fields.
         */
        public static final String SORT_ORDER_DEFAULT =
                COLUMN_SONG_TITLE + " ASC";


        public static final String[] PROJECTION_FTS = {_id,
                SUGGEST_COLUMN_TEXT_1,
                COLUMN_SONG_FTS_SIZE,
                COLUMN_SONG_FTS_DURATION,
                COLUMN_SONG_FTS_ARTIST,
                COLUMN_SONG_FTS_FILEPATH
        };

        /**
         * The default sort order for
         * queries containing NAME fields.
         */
        public static final String SORT_ORDER_FTS =
                SUGGEST_COLUMN_TEXT_1 + " ASC";


        public static final String[] PROJECTION_SUG =
                {_Id,
                        COLUMN_SUG_TITLE,
                        COLUMN_SUG_SIZE,
                        COLUMN_SUG_DURATION,
                        COLUMN_SUG_ARTIST,
                        COLUMN_SUG_FILE_PATH};

        /**
         * The default sort order for
         * queries containing NAME fields.
         */
        public static final String SORT_ORDER_SUG =
                COLUMN_SUG_TITLE + " ASC";
    }


}
