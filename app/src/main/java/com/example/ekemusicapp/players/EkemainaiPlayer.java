package com.example.ekemusicapp.players;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ekemusicapp.R;
import com.example.ekemusicapp.activities.EkeDialogActivity;
import com.example.ekemusicapp.activities.EkeSearchableActivity;
import com.example.ekemusicapp.activities.EkeSkinActivity;
import com.example.ekemusicapp.adapters.EkeCursorAdapter;
import com.example.ekemusicapp.database.EkeDbContract;
import com.example.ekemusicapp.model.Song;
import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.uitils.EkemainaiUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.os.Build.VERSION_CODES.M;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


public class EkemainaiPlayer extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>{

    // Music Library Recycler Design
    public static final int READ_EXTERNAL_STORAGE_PERMISSION_RESULT = 10;
    public ListView listView;
    public ArrayList<Song> songList;
    public ArrayList<Song> songLists;
    public TextView text;
    public TextView tolSongs;
    public Cursor musicCursor;
    EkeCursorAdapter adp;
    private static final int URL_LOADER = 0;
    public ImageView imageView_a;
    public SearchView searchView;
    public IntentFilter intentFilterShareSong;
    public IntentFilter intentFilterSearchMark;
    public EkeUIStates ekeUIStates;

    String[] projection = new String[]{

            EkeDbContract.EkeMusicInfoEntry._ID,
            EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_TITLE,
            EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_SIZE,
            EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_DURATION,
            EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_ARTIST,
            EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FILE_PATH};

    // The path to the root of this app's internal storage
    private File mPrivateRootDir;
    // The path to the "audio" subdirectory
    private File mAudioDir;
    // Array of files in the audio subdirectory
    File[] mAudioFiles;
    // Array of filenames corresponding to mAudioFiles
    String[] mAudioFilenames;
    // File uri
    Uri fileUri;
    // Intent for song sharing
    Intent mResultIntent;

    private Intent mRequestFileIntent;
    private ParcelFileDescriptor mInputPFD;


    @RequiresApi(api = M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_ekemainai_player);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);



        // Ask for runtime permission
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            checkStorageAndPermission();
        }

        songList = new ArrayList<>();
        songLists = new ArrayList<>();
        songList = loadDbSongs();
        songLists = loadDbSongsFts();
        int s = songList.size();
        int ss = songLists.size();
        Log.e("songMan", " "+ss);

        new EkemainaiUtils(getApplicationContext()).storeSong(songLists);

        tolSongs = (TextView) this.findViewById(R.id.footbar_title);
        tolSongs.setText(R.string.songsnum);
        tolSongs.append(" " + "(" + s + ")");

        adp = new EkeCursorAdapter(this, musicCursor);

        // Setting up the ListView
        listView = (ListView) findViewById(R.id.list_view);

        //listView.setItemsCanFocus(true);
        listView.setAdapter(adp);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(URL_LOADER, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int position, long id) {


                ekeUIStates = new EkeUIStates(getApplicationContext());
                int s = ekeUIStates.loadSongPosition();

                new EkeUIStates(getApplicationContext()).storeSongPosition(position);
                adp = (EkeCursorAdapter) adapterView.getAdapter();
                long mydd = adp.getCursor().getLong(0);
                Toast.makeText(getApplicationContext(), "ID: "+mydd, Toast.LENGTH_SHORT).show();

                if (s == -1) {
                    adp.setSelected(position, true);
                    adp.notifyDataSetChanged();
                } else if (s != position) {
                    adp.setSelected(s, false);
                    adp.setSelected(position, true);
                    adp.notifyDataSetChanged();
                }else {
                    adp.setSelected(position, true);
                    adp.notifyDataSetChanged();
                }

                Intent intent = new Intent(EkemainaiPlayer.this, EkemainaiMediaPlayer.class);
                Bundle songPosition = new Bundle();
                songPosition.putInt("songIndex", position);
                songPosition.putParcelableArrayList("songs", songList);
                intent.putExtras(songPosition);
                startActivity(intent);
                overridePendingTransition(R.anim.main_fadein, R.anim.main_fadeout);

            }
        });

        // Set up an Intent to send back to apps that request a file
        mResultIntent = new Intent("com.example.ekemini.musicplayer.ACTION_RETURN_FILE");
        // Get the files/ subdirectory of internal storage
        mPrivateRootDir = new File(getContentResolver().getType(EkeDbContract.EkeMusicInfoEntry.CONTENT_URI),
                EkeDbContract.EkeMusicInfoEntry.MUSIC_LIBRARY_TABLE);
        // Get the files/images subdirectory;
        mAudioDir = new File(mPrivateRootDir, EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_TITLE);
        // Get the files in the images subdirectory
        mAudioFiles = mAudioDir.listFiles();
        // Set the Activity's result to null to begin with
        setResult(Activity.RESULT_CANCELED, null);
        /*
         * Display the file names in the ListView mFileListView.
         * Back the ListView with the array mImageFilenames, which
         * you can create by iterating through mImageFiles and
         * calling File.getAbsolutePath() for each File
         */

        mRequestFileIntent = new Intent(Intent.ACTION_PICK);
        mRequestFileIntent.setType("audio/mp3");

        // Initialize and add action for song title broadcast intent filter
        intentFilterShareSong = new IntentFilter();
        intentFilterShareSong.addAction(EkeDialogActivity.Broadcast_SHARE_AUDIO);

        // Initialize and add action for search list mark broadcast receiver
        intentFilterSearchMark = new IntentFilter();
        intentFilterSearchMark.addAction(EkeSearchableActivity.Broadcast_SEARCH_MARK);

        imageView_a = (ImageView)findViewById(R.id.player_a_image);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                // Search widget
                Intent intent = new Intent(this, EkeSearchableActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.main_fadein, R.anim.main_fadeout);
            case R.id.action_settings:
                //ekemainaiShare();
                return true;
            case R.id.action_sleep:
                //showHelp();
                return true;
            case R.id.action_scan:
                //newGame();
                return true;
            case R.id.action_share:
                //newGame();
                return true;
            case R.id.action_art:
                Intent intentScreen = new Intent(EkemainaiPlayer.this, EkeSkinActivity.class);
                startActivity(intentScreen);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Starts the query
        Uri mMusicUri;
        mMusicUri = EkeDbContract.EkeMusicInfoEntry.CONTENT_URI;
        CursorLoader cursorLoaderEx;

        switch (id) {
            case URL_LOADER:
                cursorLoaderEx = new CursorLoader(
                        getApplicationContext(),
                        mMusicUri,
                        projection,
                        null,
                        null,
                        null);
                return cursorLoaderEx;

            default:
                // An invalid id was passed in
                return null;
        }

    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        adp.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

        adp.swapCursor(null);
    }


    // Checks if external storage is available to at least read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_RESULT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(),
                            "Access to device storage not granted. Enable storage " +
                                    "permission and retry", Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    @TargetApi(M)
    @RequiresApi(api = M)
    protected void checkStorageAndPermission() {

        if (isExternalStorageReadable()) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(getApplicationContext(), "ShareWare needs access " +
                            "to device storage", Toast.LENGTH_LONG).show();

                } else {

                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_PERMISSION_RESULT);

                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "No storage Media " +
                    "mounted on device", Toast.LENGTH_LONG).show();
        }

    }

    private ArrayList<Song> loadDbSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri musicUri = EkeDbContract.EkeMusicInfoEntry.CONTENT_URI;

        Cursor musicCursor = contentResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry._ID);
            int titleColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_TITLE);
            int songSize = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_SIZE);
            int songDur = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_DURATION);
            int artistColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_ARTIST);
            int pathColumn = musicCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FILE_PATH);


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

                songList.add(new Song(thisId, thisTitle, size, sTime, thisArtist, thisPath));

                //Sort song list in alphabetical order
                Collections.sort(songList, new Comparator<Song>() {
                    public int compare(Song a, Song b) {
                        return a.getSongTitle().compareTo(b.getSongTitle());
                    }
                });

            }
            while (musicCursor.moveToNext());
        }

        return songList;
    }

    private ArrayList<Song> loadDbSongsFts() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = EkeDbContract.EkeMusicInfoEntry.CONTENT_FTS_URI;

        Cursor muCursor = contentResolver.query(uri, EkeDbContract.EkeMusicInfoEntry.PROJECTION_FTS, null, null, null);

        if (muCursor != null && muCursor.moveToFirst()) {
            //get columns
            int idColumn = muCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry._id);
            int titleColumn = muCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1);
            int songSize = muCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE);
            int songDur = muCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION);
            int artistColumn = muCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST);
            int pathColumn = muCursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_FILEPATH);

            //add songs to list
            do {
                long thisId = muCursor.getLong(idColumn);
                String thisTitle = muCursor.getString(titleColumn);
                long songSized = muCursor.getLong(songSize);
                String size = convertAudioSize(songSized);
                long songTime = muCursor.getLong(songDur);
                String sTime = convertDuration(songTime);
                String thisArtist = muCursor.getString(artistColumn);
                String thisPath = muCursor.getString(pathColumn);


                Log.e(thisTitle, "benSearch");
                Log.e(size, "beneSearch");
                Log.e(sTime, "bendSearch");
                Log.e(thisArtist, "bencSearch");
                Log.e(thisPath, "pathSearch");

                songLists.add(new Song(thisId, thisTitle, size, sTime, thisArtist, thisPath));

                //Sort song list in alphabetical order
                Collections.sort(songLists, new Comparator<Song>() {
                    public int compare(Song a, Song b) {
                        return a.getSongTitle().compareTo(b.getSongTitle());
                    }
                });

            }
            while (muCursor.moveToNext());
        }

        return songLists;
    }

    public String convertDuration(long duration) {
        String out = null;
        long hours = 0;
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
                sized = (size / 1000000);
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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.eke)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this activity?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.eke)
                    .setTitle(R.string.quit)
                    .setMessage(R.string.really_quit)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Stop the activity
                            EkemainaiPlayer.this.finish();
                        }

                    })
                    .setNegativeButton(R.string.no, null)
                    .show();

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }

    }

    // Register song share broadcast receiver
    private BroadcastReceiver receiverShareSong = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(EkeDialogActivity.Broadcast_SHARE_AUDIO)) {

                String songPos = intent.getStringExtra("songPos");



                mAudioFilenames = new String[]{ songPos };
                File requestFile = new File(mAudioFilenames[0]);
                /*
                 * Most file-related method calls need to be in
                 * try-catch blocks.
                 */
                // Use the FileProvider to get a content URI
                try {
                    fileUri = FileProvider.getUriForFile(
                              EkemainaiPlayer.this,
                              "com.example.ekemusicapp.fileprovider",
                              requestFile);

                } catch (IllegalArgumentException e) {
                    Log.e("File Selector", "The selected file can't be shared: " + songPos);
                }

                if (fileUri != null) {
                    // Grant temporary read permission to the content URI
                    mResultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    // Put the Uri and MIME type in the result Intent
                    mResultIntent.setDataAndType(fileUri, getContentResolver().getType(fileUri));
                    // Set the result
                    EkemainaiPlayer.this.setResult(Activity.RESULT_OK,
                            mResultIntent);
                } else {
                    mResultIntent.setDataAndType(null, " ");
                    EkemainaiPlayer.this.setResult(RESULT_CANCELED,
                            mResultIntent);
                }

            }

        }

    };

    protected void requestFile() {
        /**
         * When the user requests a file, send an Intent to the
         * server app.
         * files.
         */
        startActivityForResult(mRequestFileIntent, 0);
    }

    /*
     * When the Activity of the app that hosts files sets a result and calls
     * finish(), this method is invoked. The returned Intent contains the
     * content URI of a selected file. The result code indicates if the
     * selection worked or not.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent returnIntent) {
        // If the selection didn't work
        super.onActivityResult(requestCode, resultCode, returnIntent);
        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = returnIntent.getData();
            /*
             * Try to open the file for "read" access using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("EkemainaiPlayer", "File not found.");
                return;
            }
            // Get a regular file descriptor for the file
            if (mInputPFD != null) {
                FileDescriptor fd = mInputPFD.getFileDescriptor();
            }


            /*
             * Get the file's content URI from the incoming Intent, then
             * get the file's MIME type
             */
            Uri returnUriFile = returnIntent.getData();
            String mimeType = getContentResolver().getType(returnUri);

        }
    }

    // Register song search mark broadcast receiver
    private BroadcastReceiver receiverSearchMark = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            EkeUIStates ekeUIStates1 = new EkeUIStates(getApplicationContext());

            if (intent.getAction().equals(EkeSearchableActivity.Broadcast_SEARCH_MARK)) {

                int[] sol = intent.getIntArrayExtra("search");
                int sss = sol[0];
                int s = sol[1];
                ekeUIStates1.storeSongPosition(sss);

                if (s == -1) {
                    adp.setSelected(sss, true);
                    adp.notifyDataSetChanged();
                } else if (s == sss) {
                    adp.setSelected(s, true);
                    adp.notifyDataSetChanged();
                } else {
                    adp.setSelected(s, false);
                    adp.setSelected(sss, true);
                    adp.notifyDataSetChanged();
                }

            }

        }

    };

    @Override
    protected void onStart(){
        super.onStart();

        EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
        int backImageA =  ekeUIStates.loadLayoutBackgroundA();
        //Toast.makeText(getApplicationContext(), "I am "+backImageA, Toast.LENGTH_LONG).show();

        if(backImageA == 0) {
            imageView_a.setImageResource(R.drawable.player_ground);

        }else if(backImageA == 1){
            imageView_a.setImageResource(R.drawable.player_ground_a);   // optional

        }else if(backImageA == 2){
            imageView_a.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageView_a.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageView_a.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageView_a.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageView_a.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageView_a.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageView_a.setImageResource(R.drawable.background_b);

        }

        int dd = ekeUIStates.loadSongPosition();
        int jj = ekeUIStates.loadComplete();
        Toast.makeText(getApplicationContext(), "Just"+jj, Toast.LENGTH_LONG).show();

        if (dd == -1) {
            Log.e("mark", String.valueOf(dd));
        }else if(dd == jj){
            adp.setSelected(dd, true);
            adp.notifyDataSetChanged();
        } else {
            adp.setSelected(jj, true);
            adp.setSelected(dd, false);
            adp.notifyDataSetChanged();
            ekeUIStates.storeSongPosition(jj);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
        int backImageA = ekeUIStates.loadLayoutBackgroundA();
        //Toast.makeText(getApplicationContext(), "I am " + backImageA, Toast.LENGTH_LONG).show();

        if(backImageA == 0) {
            imageView_a.setImageResource(R.drawable.player_ground);     // optional

        }else if(backImageA == 1){
            imageView_a.setImageResource(R.drawable.player_ground_a);   // optional

        }else if(backImageA == 2){
            imageView_a.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageView_a.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageView_a.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageView_a.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageView_a.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageView_a.setImageResource(R.drawable.background_a);      // optional

        } else if (backImageA == 8) {
            imageView_a.setImageResource(R.drawable.background_b);      // optional

        }


        // Register receiver in on start of the bound activity
        registerReceiver(receiverShareSong, intentFilterShareSong);

        //Register receiver in on destroy of the searchable activity
        registerReceiver(receiverSearchMark, intentFilterSearchMark);


    }

    @Override
    protected void onPause(){
        super.onPause();
        // Unregister broadcast receive for song sharing
        unregisterReceiver(receiverShareSong);

        // Unregister broadcast receiver for song list markings
        unregisterReceiver(receiverSearchMark);
    }

}



