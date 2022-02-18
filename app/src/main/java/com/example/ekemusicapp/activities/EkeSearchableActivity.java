package com.example.ekemusicapp.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ekemusicapp.EkeSearchEmpty;
import com.example.ekemusicapp.dialogs.EkeShowDialogActivity;
import com.example.ekemusicapp.players.EkemainaiMediaPlayer;
import com.example.ekemusicapp.R;
import com.example.ekemusicapp.adapters.EkeSearchCursorAdapter;
import com.example.ekemusicapp.database.EkeDbContract;
import com.example.ekemusicapp.model.Song;
import com.example.ekemusicapp.services.EkeLoadSugsIntentService;
import com.example.ekemusicapp.uitils.EkeUIStates;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class EkeSearchableActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public ListView searchlist;
    public ImageView imageView_c;
    public Cursor eCursor;
    public ArrayList<Song> songList;
    public ArrayList<Song> songListFts;
    MySearchCursorAdapter mySearchCursorAdapter;
    public static final String Broadcast_SEARCH_MARK = "com.example.ekemusicapp.SearchMark";
    EkeUIStates ekeUIStates;
    EkeSearchCursorAdapter ekeSearchCursorAdapter;
    public SearchView searchViewMain;
    private static final int URI_LOADER = 0;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_eke_searchable);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar_search = (Toolbar) findViewById(R.id.toolbar_search);

        setSupportActionBar(toolbar_search);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            // Start database loading service from the foreground
            Intent intentSugsLoading = new Intent(this, EkeLoadSugsIntentService.class);
            startForegroundService(intentSugsLoading);
        } else {
            // Start database loading service from the foreground
            Intent intentSugsLoading = new Intent(this, EkeLoadSugsIntentService.class);
            startService(intentSugsLoading);
        }
        
        songList = new ArrayList<>();
        songListFts = new ArrayList<>();
        eCursor = getContentResolver().query(EkeDbContract.EkeMusicInfoEntry.CONTENT_SUGGEST_URI, null, null, null, null);

        mySearchCursorAdapter = new MySearchCursorAdapter(getApplicationContext(), eCursor);

        // create database object databaseObject = new DbBackend(SearchableActivity.this);
        searchlist = (ListView) findViewById(R.id.list_search);
        searchlist.setAdapter(mySearchCursorAdapter);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(URI_LOADER, null, this);

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        searchlist.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ekeUIStates = new EkeUIStates(getApplicationContext());
                songListFts = ekeUIStates.loadSongSearchFts();
                int s = ekeUIStates.loadSearchPosition();

                ekeUIStates.storeSearchPosition(position);

                mySearchCursorAdapter = (MySearchCursorAdapter) parent.getAdapter();
                String til = mySearchCursorAdapter.getCursor().getString(1);

                ekeUIStates.storeSearchTitle(til);

                if (s == -1) {
                    mySearchCursorAdapter.setSelectedSearch(position, true);
                    mySearchCursorAdapter.notifyDataSetChanged();
                } else if (s != position) {
                    mySearchCursorAdapter.setSelectedSearch(s, false);
                    mySearchCursorAdapter.setSelectedSearch(position, true);
                    mySearchCursorAdapter.notifyDataSetChanged();
                }else {
                    mySearchCursorAdapter.setSelectedSearch(position, true);
                    mySearchCursorAdapter.notifyDataSetChanged();
                }

                Toast.makeText(getApplicationContext(), " "+position, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EkeSearchableActivity.this, EkemainaiMediaPlayer.class);
                Bundle songPosition = new Bundle();
                songPosition.putInt("songIndex", position);
                songPosition.putParcelableArrayList("songs", songListFts);
                intent.putExtras(songPosition);
                startActivity(intent);
                overridePendingTransition(R.anim.main_fadein, R.anim.main_fadeout);
            }
        });

        searchViewMain = (SearchView) findViewById(R.id.search_word);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) EkeSearchableActivity.this.getSystemService(Context.SEARCH_SERVICE);
        searchViewMain.setSearchableInfo(searchManager.getSearchableInfo(EkeSearchableActivity.this.getComponentName()));
        searchViewMain.setSubmitButtonEnabled(true);
        searchViewMain.setQueryHint("Search Songs");
        searchViewMain.setIconifiedByDefault(false);
        searchViewMain.onActionViewExpanded();

        onSearchRequested();

        searchViewMain.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleIntent(getIntent());
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (newText.length() > 0) {
                    handleIntent(getIntent());

                }
                return false;
            }
        });

        searchViewMain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSearchRequested();

            }
        });
        onSearchRequested();
        handleIntent(getIntent());
        imageView_c = (ImageView) findViewById(R.id.player_c_image);

    }

    public ArrayList<Song> searchSongsMain(String searchWord) {

        ArrayList<Song> mItems = new ArrayList<>();
        String query = EkeDbContract.EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1 + " LIKE ?";
        String []selectionArgs = new String[]{ "%" + searchWord + "%" };
        Cursor c = getContentResolver().query(EkeDbContract.EkeMusicInfoEntry.CONTENT_FTS_URI,
                EkeDbContract.EkeMusicInfoEntry.PROJECTION_FTS, query, selectionArgs, EkeDbContract.EkeMusicInfoEntry.SORT_ORDER_FTS);

        //process Cursor and display results
        if (c != null && c.moveToFirst()) {

            int searchIdIndex = c.getColumnIndex(EkeDbContract.EkeMusicInfoEntry._id);
            int searchTitIndex = c.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.SUGGEST_COLUMN_TEXT_1);
            int searchSizIndex = c.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_SIZE);
            int searchDurIndex = c.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_DURATION);
            int searchArtIndex = c.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_ARTIST);
            int searchPathIndex = c.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FTS_FILEPATH);

            do {
                long searchId = c.getLong(searchIdIndex);
                String searchTit = c.getString(searchTitIndex);
                String searchSiz = c.getString(searchSizIndex);
                String searchDur = c.getString(searchDurIndex);
                String searchArt = c.getString(searchArtIndex);
                String searchPath = c.getString(searchPathIndex);

                    ContentValues values = new ContentValues();
                    values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_TITLE, searchTit);
                    values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_SIZE, searchSiz);
                    values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_DURATION, searchDur);
                    values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_ARTIST, searchArt);
                    values.put(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_FILE_PATH, searchPath);

                    Uri newUriSugs = getContentResolver().insert(EkeDbContract.EkeMusicInfoEntry.CONTENT_SUGGEST_URI, values);

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

                mItems.add(new Song(searchId, searchTit, searchSiz, searchDur, searchArt, searchPath));


            } while (c.moveToNext());

        }
        return mItems;
    }

    private void doMySearchMain(String query) {
        ArrayList<EkeSearchEmpty> strings = new ArrayList<>();
        String string = getResources().getString(R.string.empty_search);
        strings.add(new EkeSearchEmpty(string));
        ArrayList<Song> songs = searchSongsMain(query);

        new EkeUIStates(getApplicationContext()).storeSongSearcFts(songs);
        int s = songs.size();
        Log.e("searchSug", String.valueOf(s));

        //eCursor = getContentResolver().query(EkeMusicInfoEntry.CONTENT_SUGGEST_URI, null, null, null, null);
        /*if(s == 0){
            ekeSearchCursorAdapter = new EkeSearchCursorAdapter(getApplicationContext(), strings);
            searchlist.setAdapter(ekeSearchCursorAdapter);
        }

        /**else {
            mySearchCursorAdapter = new MySearchCursorAdapter(getApplicationContext(), eCursor);
            Toast.makeText(getApplicationContext(), "search: " + s, Toast.LENGTH_LONG).show();
            searchlist.setAdapter(mySearchCursorAdapter);
        }*/

    }

      @Override
    protected void onNewIntent(Intent intent) {

          super.onNewIntent(intent);
          setIntent(intent);
          handleIntent(intent);

      }

    private void handleIntent(Intent intent){

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(getApplicationContext(), " See: "+query, Toast.LENGTH_LONG).show();
            getContentResolver().delete(EkeDbContract.EkeMusicInfoEntry.CONTENT_SUGGEST_URI, null, null);
            doMySearchMain(query);

        }else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
        Uri detailUri = intent.getData();
        String id = detailUri.getLastPathSegment();
        Intent detailsIntent = new Intent(getApplicationContext(), EkeSearchableActivity.class);
        detailsIntent.putExtra("ID", id);
        startActivity(detailsIntent);
        finish();
        }

    }

    @Override
    protected void onStart() {
          super.onStart();

          EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
          int backImageA = ekeUIStates.loadLayoutBackgroundA();

          if (backImageA == 0) {
              imageView_c.setImageResource(R.drawable.player_ground);

          } else if (backImageA == 1) {
              imageView_c.setImageResource(R.drawable.player_ground_a);   //

          } else if (backImageA == 2) {
              imageView_c.setImageResource(R.drawable.player_ground_b);   // optional

          } else if (backImageA == 3) {
              imageView_c.setImageResource(R.drawable.player_ground_c);   // optional

          } else if (backImageA == 4) {
              imageView_c.setImageResource(R.drawable.player_ground_aa);   // optional

          } else if (backImageA == 5) {
              imageView_c.setImageResource(R.drawable.player_ground_d);   // optional

          } else if (backImageA == 6) {
              imageView_c.setImageResource(R.drawable.player_ground_e);   // optional

          } else if (backImageA == 7) {
              imageView_c.setImageResource(R.drawable.background_a);  // optional

          } else if (backImageA == 8) {
              imageView_c.setImageResource(R.drawable.background_b);

          }


        /**int d = ekeUIStates.loadSearchPosition();
        int j = ekeUIStates.loadComplete();
        Toast.makeText(getApplicationContext(), "Just"+j, Toast.LENGTH_LONG).show();

        if (d == -1) {
            Log.e("mark", String.valueOf(d));
        }else if(d == j){
            mySearchCursorAdapter.setSelectedSearch(d, true);
            mySearchCursorAdapter.notifyDataSetChanged();
        } else {
            mySearchCursorAdapter.setSelectedSearch(j, true);
            mySearchCursorAdapter.setSelectedSearch(d, false);
            mySearchCursorAdapter.notifyDataSetChanged();
            ekeUIStates.storeSearchPosition(j);
        }

        Uri uriCheck = EkeMusicInfoEntry.CONTENT_SUGGEST_URI;
        Cursor cc = getContentResolver().query(uriCheck, null, null, null, null);
        if(cc != null) {
            getContentResolver().delete(uriCheck, null, null);
        }*/



    }

    @Override
    protected void onResume() {
        super.onResume();

        EkeUIStates ekeUIStates1 = new EkeUIStates(getApplicationContext());
        int backImageA = ekeUIStates1.loadLayoutBackgroundA();


        if(backImageA == 0) {
            imageView_c.setImageResource(R.drawable.player_ground);

        }else if(backImageA == 1){
            imageView_c.setImageResource(R.drawable.player_ground_a);   //

        }else if(backImageA == 2){
            imageView_c.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageView_c.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageView_c.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageView_c.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageView_c.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageView_c.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageView_c.setImageResource(R.drawable.background_b);

        }

    }

    private void sendSerachMarkBroadcast(){
        EkeUIStates ekeStates = new EkeUIStates(getApplicationContext());
        int h = ekeStates.loadSearchPosition();
        int hh = ekeStates.loadSongPosition();
        int [] hhh;
        hhh = new int[]{h, hh};
        Intent intent = new Intent(EkeSearchableActivity.Broadcast_SEARCH_MARK);
        intent.putExtra("search", hhh);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        sendSerachMarkBroadcast();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Starts the query
        Uri mMusicUri;
        mMusicUri = EkeDbContract.EkeMusicInfoEntry.CONTENT_SUGGEST_URI;
        CursorLoader cursorLoaderEx;

        switch (id) {
            case URI_LOADER:
                cursorLoaderEx = new CursorLoader(
                        getApplicationContext(),
                        mMusicUri,
                        EkeDbContract.EkeMusicInfoEntry.PROJECTION_SUG,
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mySearchCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            mySearchCursorAdapter.swapCursor(null);
    }

    public class MySearchCursorAdapter extends CursorAdapter {

        public CircleImageView albumArt;
        public TextView songSearchTitle;
        public TextView songSerachSize;
        public TextView songSearchDur;
        public TextView songSearchArt;
        public ImageView songSelect;
        public String songName;

        // Initialize the array
        private SparseBooleanArray selectionArraySearch = new SparseBooleanArray();

        public MySearchCursorAdapter(Context context, Cursor c) {
            super(context, c, 0 /* flags */);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //Inflate a list item view using the layout specified in song.xml
            return LayoutInflater.from(context).inflate(R.layout.list_item_search, parent, false);
        }

        @Override
        public void bindView(View view, final Context context, final Cursor cursor) {

            // Find individual views that we want to modify in the list item layout
            albumArt = view.findViewById(R.id.thumbnailsearch);
            songSearchTitle = view.findViewById(R.id.song_titlesearch);
            songSerachSize = view.findViewById(R.id.song_sizesearch);
            songSearchDur = view.findViewById(R.id.song_durationsearch);
            songSearchArt = view.findViewById(R.id.song_artistsearch);
            songSelect = view.findViewById(R.id.spinnersearch);

            // Find the columns of songs attributes that we're interested in
            int titleColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_TITLE);
            int sizeColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_SIZE);
            int durColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_DURATION);
            int artistColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_ARTIST);
            int songFileIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SUG_FILE_PATH);

            // Read the songs attributes from the Cursor for the current song
            songName = cursor.getString(titleColumnIndex);
            String songSized = cursor.getString(sizeColumnIndex);
            String songTimed = cursor.getString(durColumnIndex);
            String songOwner = cursor.getString(artistColumnIndex);
            String songFilePath = cursor.getString(songFileIndex);

            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(songFilePath);

            byte[] datamat = mediaMetadataRetriever.getEmbeddedPicture();
            if (datamat != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(datamat, 0, datamat.length);
                albumArt.setImageBitmap(bitmap);
            } else {
                Picasso.with(context)
                        .load(songFilePath)
                        .placeholder(R.drawable.listiconface_35px)   // optional
                        .into(albumArt);
            }

            songSearchTitle.setText(songName);
            songSerachSize.setText(songSized);
            songSearchDur.setText(songTimed);
            songSearchArt.setText(songOwner);

            int position = cursor.getPosition();

            EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
            int buttonCol = ekeUIStates.loadButtonColor();

            boolean isSelected = selectionArraySearch.get(position);
            final int pos = position;

            if(buttonCol == 0) {

                if (isSelected) {
                    songSearchTitle.setTextColor(Color.BLUE);
                    songSerachSize.setTextColor(Color.BLUE);
                    songSearchDur.setTextColor(Color.BLUE);
                    songSearchArt.setTextColor(Color.BLUE);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 1) {
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.RED);
                    songSerachSize.setTextColor(Color.RED);
                    songSearchDur.setTextColor(Color.RED);
                    songSearchArt.setTextColor(Color.RED);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 2) {
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.YELLOW);
                    songSerachSize.setTextColor(Color.YELLOW);
                    songSearchDur.setTextColor(Color.YELLOW);
                    songSearchArt.setTextColor(Color.YELLOW);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 3){
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.BLACK);
                    songSerachSize.setTextColor(Color.BLACK);
                    songSearchDur.setTextColor(Color.BLACK);
                    songSearchArt.setTextColor(Color.BLACK);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 4){
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 5){
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.GREEN);
                    songSerachSize.setTextColor(Color.GREEN);
                    songSearchDur.setTextColor(Color.GREEN);
                    songSearchArt.setTextColor(Color.GREEN);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 6){
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.parseColor("#f908f1"));
                    songSerachSize.setTextColor(Color.parseColor("#f908f1"));
                    songSearchDur.setTextColor(Color.parseColor("#f908f1"));
                    songSearchArt.setTextColor(Color.parseColor("#f908f1"));
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 7){
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.parseColor("#08f7ef"));
                    songSerachSize.setTextColor(Color.parseColor("#08f7ef"));
                    songSearchDur.setTextColor(Color.parseColor("#08f7ef"));
                    songSearchArt.setTextColor(Color.parseColor("#08f7ef"));
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }else if(buttonCol == 8){
                if (isSelected) {
                    songSearchTitle.setTextColor(Color.parseColor("#7709ae"));
                    songSerachSize.setTextColor(Color.parseColor("#7709ae"));
                    songSearchDur.setTextColor(Color.parseColor("#7709ae"));
                    songSearchArt.setTextColor(Color.parseColor("#7709ae"));
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songSearchTitle.setTextColor(Color.WHITE);
                    songSerachSize.setTextColor(Color.WHITE);
                    songSearchDur.setTextColor(Color.WHITE);
                    songSearchArt.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
            }
            songSelect.setTag(cursor.getString(5));
            songSelect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "Adapter Position:" +pos, Toast.LENGTH_LONG).show();
                    String sPos = (String) view.getTag();
                    new EkeUIStates(getApplicationContext()).storeSearchTitlePos(sPos);
                    new EkeUIStates(getApplicationContext()).storeShareFile(sPos);
                    showDialog();
                }
            });

        }

        private void showDialog() {
            int mStackLevel = 0;
            mStackLevel++;

            // DialogFragment.show() will take care of adding the fragment
            // in a transaction.  We also want to remove any currently showing
            // dialog, so make our own transaction and take care of that here.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = EkeShowDialogActivity.newInstance(mStackLevel);
            newFragment.show(ft, "Dialog");
        }

        // Method to mark items in selection
        public void setSelectedSearch(int position, boolean isSelected) {
            selectionArraySearch.put(position, isSelected);
        }

    }
}
