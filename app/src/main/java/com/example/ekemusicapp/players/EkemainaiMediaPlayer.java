package com.example.ekemusicapp.players;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ekemusicapp.R;
import com.example.ekemusicapp.model.Song;
import com.example.ekemusicapp.services.EkemainaiPlayerService;
import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.uitils.EkemainaiUtils;
import com.example.ekemusicapp.uitils.PlayerUtilities;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;
import static java.lang.Long.parseLong;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

public class EkemainaiMediaPlayer extends AppCompatActivity implements OnClickListener,
        SeekBar.OnSeekBarChangeListener {

    // Define UI for Ekemainai Player
    public ImageButton bplay, bprev, bnext;
    public ImageButton brepeat;
    public ImageButton bshuffle;
    TextView songTitle, songDurationCurrent, songDurationTotal;
    public SeekBar seekbar;
    ArrayList<Song> songList;
    TranslateAnimation songTitleSlide;
    public EkemainaiPlayerService eService;
    boolean serviceBound = false;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.ekemini.musicplayer.PlayNewAudio";
    public static final String Broadcast_PLAY_NOTE_BUTTON = "com.example.ekemini.musicplayer.PLAY_NOTE_BUTTON";
    public boolean isShuffle = false;
    private boolean isRepeat = false;
    private Handler myHandler = new Handler();
    PlayerUtilities utils = new PlayerUtilities();
    int totalDuration;
    int currentDuration;
    public String songDisplay;
    private IntentFilter intentFilter;
    private IntentFilter intentDuration;
    private IntentFilter intentFilterNotePlay;
    private IntentFilter intentFilterNotePause;
    private IntentFilter intentFilterNoteNext;
    private IntentFilter intentFilterNotePrevious;
    public int tolDuration;
    public int curDuration;
    public String songDuration;
    public String songCurrent;
    private CircleImageView circleImageView;
    public final int RESULT_LOAD_IMG = 0;
    public FloatingActionButton floatingActionButton;
    public RelativeLayout relativeLayout_b;
    public ImageView imageView_b;


    @TargetApi(VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_ekemainai_mediaplayer);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar_player = (Toolbar) findViewById(R.id.toolbar_player);

        setSupportActionBar(toolbar_player);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Get handles of MediaPlayer Controls
        bplay = (ImageButton) findViewById(R.id.btnPlay);
        bprev = (ImageButton) findViewById(R.id.btnPrevious);
        bnext = (ImageButton) findViewById(R.id.btnNext);
        brepeat = (ImageButton) findViewById(R.id.btnRepeat);
        bshuffle = (ImageButton) findViewById(R.id.btnShuffle);
        seekbar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitle = (TextView) findViewById(R.id.songTitle);
        songDurationCurrent = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songDurationTotal = (TextView) findViewById(R.id.songTotalDurationLabel);

        bplay.setOnClickListener(this);
        bprev.setOnClickListener(this);
        bnext.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        brepeat.setOnClickListener(this);
        bshuffle.setOnClickListener(this);

        Drawable drawable = createDrawable(getApplicationContext());
        drawable.setColorFilter(new PorterDuffColorFilter(Color.YELLOW, Mode.SRC_IN));
        seekbar.setProgressDrawable(drawable);

        //Get songs position
        Bundle songPosition = getIntent().getExtras();
        int songIndex = songPosition.getInt("songIndex");
        songList = new ArrayList<>();
        songList = songPosition.getParcelableArrayList("songs");

        assert songList != null;
        int s = songList.size();
        Log.d("songSize", String.valueOf(s));

        Toast.makeText(getApplicationContext(), "" + songIndex, Toast.LENGTH_SHORT).show();

        playAudioFile(songIndex);

        // Initialize and add action for song title broadcast intent filter
        intentFilter = new IntentFilter();
        intentFilter.addAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);

        // Initialize and add action for song duration intent filter
        intentDuration = new IntentFilter();
        intentDuration.addAction(EkemainaiPlayerService.Broadcast_SONG_TIME);

        // Initialize and add action for song duration intent filter
        intentFilterNotePlay = new IntentFilter();
        intentFilterNotePlay.addAction(EkemainaiPlayerService.Broadcast_NOTE_PLAY);

        // Initialize and add action for song duration intent filter
        intentFilterNotePause = new IntentFilter();
        intentFilterNotePause.addAction(EkemainaiPlayerService.Broadcast_NOTE_PAUSE);

        // Initialize and add action for song duration intent filter
        intentFilterNoteNext = new IntentFilter();
        intentFilterNoteNext.addAction(EkemainaiPlayerService.Broadcast_NOTE_NEXT);

        // Initialize and add action for song duration intent filter
        intentFilterNotePrevious = new IntentFilter();
        intentFilterNotePrevious.addAction(EkemainaiPlayerService.Broadcast_NOTE_PREVIOUS);

        // Load image from gallery
        circleImageView = (CircleImageView) findViewById(R.id.thumbnailiface);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        relativeLayout_b = (RelativeLayout) findViewById(R.id.player_b);
        imageView_b = (ImageView) findViewById(R.id.player_b_image);

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                String imageFilePath = imageUri.toString();
                new EkeUIStates(getApplicationContext()).storePlaylistPics(imageFilePath);

                Picasso.with(getApplicationContext())
                        .load(imageUri)
                        .placeholder(R.drawable.btn_playlist)   // optional
                        .resize(300, 300)
                        .centerCrop()
                        .transform(new CropSquareTransformation())
                        .into(circleImageView);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(EkemainaiMediaPlayer.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(EkemainaiMediaPlayer.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.player_share:
                //ekemainaiShare();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            EkemainaiPlayerService.LocalBinder binder = (EkemainaiPlayerService.LocalBinder) service;
            eService = binder.getService();
            serviceBound = true;

            //Toast.makeText(EkemainaiMediaPlayer.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void playAudioFile(int position) {

        //Check is service is active
        if (!serviceBound) {

            //Store parcellable audioList to SharedPreference
            EkemainaiUtils storage = new EkemainaiUtils(getApplicationContext());
            storage.storeSong(songList);
            storage.storeSongIndex(position);


            Intent playerIntent = new Intent(this, EkemainaiPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            bplay.setImageResource(R.drawable.ic_pause_button);
            // set Progress bar values
            seekbar.setProgress(0);
            seekbar.setMax(100);

            // Updating progress bar
            updateProgressBar();


        } else {

            //Store the new audioIndex to SharedPreferences
            EkemainaiUtils storage = new EkemainaiUtils(getApplicationContext());
            storage.storeSongIndex(position);

            //Service is active, Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
            bplay.setImageResource(R.drawable.ic_pause_button);
            // set Progress bar values
            seekbar.setProgress(0);
            seekbar.setMax(100);

            // Updating progress bar
            updateProgressBar();

        }

    }

    private void notificationButton() {
        Intent broadcastIntentPlay = new Intent(Broadcast_PLAY_NOTE_BUTTON);
        sendBroadcast(broadcastIntentPlay);

    }

    // Register song title broadcast receiver
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(EkemainaiPlayerService.Broadcast_SONG_TITLE)) {
                songDisplay = intent.getStringExtra("Title");
                songTitle.setText(songDisplay);
                // Animate Song Title
                myHandler.postDelayed(AnimateSongTile, 100);


            }
        }
    };

    // Register song duration broadcast receiver
    private BroadcastReceiver durationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(EkemainaiPlayerService.Broadcast_SONG_TIME)) {

                songDuration = intent.getStringExtra("songToll");
                songCurrent = intent.getStringExtra("songCurr");
                long tol = parseLong(songDuration);
                long cur = parseLong(songCurrent);

                // Displaying Total Duration time
                songDurationTotal.setText(utils.milliSecondsToTimer(tol));
                // Displaying time completed playing
                songDurationCurrent.setText(utils.milliSecondsToTimer(cur));

                // Updating progress bar
                int progress = (utils.getProgressPercentage(cur, tol));
                //Log.d("Progress", ""+progress);
                seekbar.setProgress(progress);

            }

        }

    };

    private BroadcastReceiver broadcastReceiverPlayNote = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bplay.setImageResource(R.drawable.ic_pause_button);
        }
    };

    private BroadcastReceiver broadcastReceiverPauseNote = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bplay.setImageResource(R.drawable.ic_play_button);
        }
    };

    private BroadcastReceiver broadcastReceiverNextNote = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!eService.mediaPlayer.isPlaying()) {
                bplay.setImageResource(R.drawable.ic_pause_button);

            }
        }
    };

    private BroadcastReceiver broadcastReceiverPreviousNote = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!eService.mediaPlayer.isPlaying()) {
                bplay.setImageResource(R.drawable.ic_pause_button);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);

            eService.stopTimerTask();
            //service is active
            eService.stopSelf();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register receiver in on start of the bound activity
        registerReceiver(receiver, intentFilter);

        // Register song time broadcast receiver on start of bound activity
        registerReceiver(durationReceiver, intentDuration);

        // Register song time broadcast receiver on start of bound activity
        registerReceiver(broadcastReceiverPlayNote, intentFilterNotePlay);

        // Register song time broadcast receiver on start of bound activity
        registerReceiver(broadcastReceiverPauseNote, intentFilterNotePause);

        // Register song time broadcast receiver on start of bound activity
        registerReceiver(broadcastReceiverNextNote, intentFilterNoteNext);

        // Register song time broadcast receiver on start of bound activity
        registerReceiver(broadcastReceiverPreviousNote, intentFilterNotePrevious);

        EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
        int backImageA = ekeUIStates.loadLayoutBackgroundA();
        int buttonCol = ekeUIStates.loadButtonColor();

        if(backImageA == 0) {
            imageView_b.setImageResource(R.drawable.player_ground);     // optional

        }else if(backImageA == 1){
            imageView_b.setImageResource(R.drawable.player_ground_a);   // optional

        }else if(backImageA == 2){
            imageView_b.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageView_b.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageView_b.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageView_b.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageView_b.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageView_b.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageView_b.setImageResource(R.drawable.background_b);     // optional

        }

        /**
         * All player button color settings
         */
        if(buttonCol == 0) {
            bplay.setColorFilter(Color.BLUE);
            bnext.setColorFilter(Color.BLUE);
            bprev.setColorFilter(Color.BLUE);
            brepeat.setColorFilter(Color.BLUE);
            bshuffle.setColorFilter(Color.BLUE);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.BLUE, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.BLUE);
            seekbar.setThumb(d);

        }else if(buttonCol == 1){
            bplay.setColorFilter(Color.RED);
            bnext.setColorFilter(Color.RED);
            bprev.setColorFilter(Color.RED);
            brepeat.setColorFilter(Color.RED);
            bshuffle.setColorFilter(Color.RED);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.RED, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.RED);
            seekbar.setThumb(d);

        }else if(buttonCol == 2){
            bplay.setColorFilter(Color.YELLOW);
            bnext.setColorFilter(Color.YELLOW);
            bprev.setColorFilter(Color.YELLOW);
            brepeat.setColorFilter(Color.YELLOW);
            bshuffle.setColorFilter(Color.YELLOW);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.YELLOW, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.YELLOW);
            seekbar.setThumb(d);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister song title broadcast receiver
        unregisterReceiver(receiver);

        // Unregister song time broadcast receiver
        unregisterReceiver(durationReceiver);

        unregisterReceiver(broadcastReceiverPlayNote);

        unregisterReceiver(broadcastReceiverPauseNote);

        unregisterReceiver(broadcastReceiverNextNote);

        unregisterReceiver(broadcastReceiverPreviousNote);
    }

    @Override
    public void onClick(View v) {
        EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
        int buttonCol = ekeUIStates.loadButtonColor();
        int id = v.getId();
        switch (id) {
            case R.id.btnPlay:
                if (eService.mediaPlayer.isPlaying()) {
                    eService.mediaPlayer.pause();
                    bplay.setImageResource(R.drawable.ic_play_button);


                } else {
                    int curpos = eService.mediaPlayer.getCurrentPosition();
                    Toast.makeText(getApplicationContext(), " " + curpos, Toast.LENGTH_SHORT).show();
                    eService.mediaPlayer.start();
                    eService.mediaPlayer.seekTo(curpos);
                    bplay.setImageResource(R.drawable.ic_pause_button);


                }

                notificationButton();

                break;

            case R.id.btnNext:
                if (eService.mediaPlayer.isPlaying()) {
                    eService.songNext();
                    bplay.setImageResource(R.drawable.ic_pause_button);

                } else if (!eService.mediaPlayer.isPlaying()) {
                    eService.songNext();
                    bplay.setImageResource(R.drawable.ic_pause_button);

                }
                break;

            case R.id.btnPrevious:
                if (eService.mediaPlayer.isPlaying()) {
                    eService.songPrevious();
                    bplay.setImageResource(R.drawable.ic_pause_button);

                } else if (!eService.mediaPlayer.isPlaying()) {
                    eService.songPrevious();
                    bplay.setImageResource(R.drawable.ic_pause_button);

                }
                break;

            case R.id.btnRepeat:
                if (isRepeat) {
                    if(buttonCol == 0) {
                        brepeat.setColorFilter(Color.BLUE);
                    }else if(buttonCol == 1){
                        brepeat.setColorFilter(Color.RED);
                    }else if(buttonCol == 2){
                        brepeat.setColorFilter(Color.YELLOW);
                    }else if(buttonCol == 3) {
                        brepeat.setColorFilter(Color.BLACK);
                    } else if(buttonCol == 4) {
                        brepeat.setColorFilter(Color.WHITE);
                    }else if(buttonCol == 5){
                        brepeat.setColorFilter(Color.GREEN);
                    }else if(buttonCol == 6){
                        brepeat.setColorFilter(Color.parseColor("#f908f1"));
                    }else if(buttonCol == 7){
                        brepeat.setColorFilter(Color.parseColor("#08f7ef"));
                    }else if(buttonCol == 8){
                        brepeat.setColorFilter(Color.parseColor("#7709ae"));
                    }
                        isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    brepeat.setImageResource(R.drawable.ic_rep_button);
                    new EkemainaiUtils(getApplicationContext()).storeRepeatStateOff("repOff");

                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    brepeat.setImageResource(R.drawable.ic_rep_pressed);
                    brepeat.setColorFilter(Color.WHITE);
                    bshuffle.setImageResource(R.drawable.ic_shuff_button);
                    new EkemainaiUtils(getApplicationContext()).storeRepeatStateOn("repOn");
                }

                break;

            case R.id.btnShuffle:
                if (isShuffle) {
                    isShuffle = false;
                    if(buttonCol == 0) {
                        bshuffle.setColorFilter(Color.BLUE);
                    }else if(buttonCol == 1){
                        bshuffle.setColorFilter(Color.RED);
                    }else if(buttonCol == 2){
                        bshuffle.setColorFilter(Color.YELLOW);
                    }else if(buttonCol == 3) {
                        bshuffle.setColorFilter(Color.BLACK);
                    } else if(buttonCol == 4) {
                        bshuffle.setColorFilter(Color.WHITE);
                    }else if(buttonCol == 5){
                        bshuffle.setColorFilter(Color.GREEN);
                    }else if(buttonCol == 6){
                        bshuffle.setColorFilter(Color.parseColor("#f908f1"));
                    }else if(buttonCol == 7){
                        bshuffle.setColorFilter(Color.parseColor("#08f7ef"));
                    }else if(buttonCol == 8){
                        bshuffle.setColorFilter(Color.parseColor("#7709ae"));
                    }
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    bshuffle.setImageResource(R.drawable.ic_shuff_button);
                    new EkemainaiUtils(getApplicationContext()).storeShuffleStateOff("stateOff");
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    bshuffle.setImageResource(R.drawable.ic_shuff_pressed);
                    bshuffle.setColorFilter(Color.WHITE);
                    brepeat.setImageResource(R.drawable.ic_rep_button);
                    new EkemainaiUtils(getApplicationContext()).storeShuffleStateOn("stateOn");
                }
                break;
        }
    }

    public Runnable AnimateSongTile = new Runnable() {
        @Override
        public void run() {
            // Animate song title during playing
            songTitleSlide = new TranslateAnimation(-10, -songTitle.getWidth(), 0, 0);
            songTitleSlide.setDuration(7000);
            songTitleSlide.setRepeatCount(-1);
            songTitleSlide.setRepeatMode(Animation.RESTART);
            songTitleSlide.setInterpolator(new LinearInterpolator());
            songTitleSlide.setFillAfter(true);
            songTitle.startAnimation(songTitleSlide);
        }
    };

    public Runnable UpdateSongTime = new Runnable() {
        public void run() {

            // Displaying Total Duration time
            //songDurationTotal.setText(utils.milliSecondsToTimer(tol));
            // Displaying time completed playing
            //songDurationCurrent.setText(utils.milliSecondsToTimer(cur));

            // Updating progress bar
            //int progress = (utils.getProgressPercentage(cur, tol));

            //Log.d("Progress", ""+progress);
            //seekbar.setProgress(progress);

            // Running this thread after 100 milliseconds
            myHandler.postDelayed(this, 100);
        }

    };


    public void updateProgressBar() {
        // myHandler.postDelayed(UpdateSongTime, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            eService.mediaPlayer.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Remove message Handler from updating progress bar
        //myHandler.removeCallbacks(UpdateSongTime);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        myHandler.removeCallbacks(UpdateSongTime);
        int total = Integer.parseInt(songDuration);

        currentDuration = utils.progressToTimer(seekBar.getProgress(), total);

        // forward or backward to certain seconds
        eService.mediaPlayer.seekTo(currentDuration);

        // update timer progress again
        updateProgressBar();
    }

    public class CropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
    }

    public class EkeCropSquareTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 8;
            int y = (source.getHeight() - size) / 8;
            Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }
            return result;
        }

        @Override
        public String key() {
            return "square()";
        }
    }

    public static Drawable createDrawable(Context context) {

        ShapeDrawable shape = new ShapeDrawable();
        shape.getPaint().setStyle(Style.FILL_AND_STROKE);
        shape.setPadding(0, 11, 0, 12);
        shape.getPaint().setColor(
                context.getResources().getColor(android.R.color.transparent));

        shape.getPaint().setStyle(Style.STROKE);
        shape.getPaint().setStrokeWidth(0.5f);
        shape.getPaint().setColor(
                context.getResources().getColor(android.R.color.holo_green_dark));

        ShapeDrawable shapeD = new ShapeDrawable();
        shapeD.getPaint().setStyle(Style.FILL_AND_STROKE);
        shapeD.setPadding(0, 11, 0, 12);

        shapeD.getPaint().setColor(
                context.getResources().getColor(android.R.color.holo_red_dark));

        ClipDrawable clipDrawable = new ClipDrawable(shapeD, Gravity.START,
                ClipDrawable.HORIZONTAL);

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[] {
                clipDrawable, shape });
                layerDrawable.setLayerInset(0, 0, 11, 0, 12);

        return layerDrawable;

    }

    @Override
    protected void onStart() {
        super.onStart();

        EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
        String imageFilePath = ekeUIStates.loadPlaylistPics();

        Picasso.with(getApplicationContext())
                .load(imageFilePath)
                .placeholder(R.drawable.btn_playlist)   // optional
                .resize(300, 300)
                .centerCrop()
                .transform(new EkeCropSquareTransformation())
                .into(circleImageView);

        ekeUIStates = new EkeUIStates(getApplicationContext());
        int backImageA = ekeUIStates.loadLayoutBackgroundA();
        int buttonCol = ekeUIStates.loadButtonColor();


        if(backImageA == 0) {
            imageView_b.setImageResource(R.drawable.player_ground); // optional

        }else if(backImageA == 1){
            imageView_b.setImageResource(R.drawable.player_ground_a);   // optional

        }else if(backImageA == 2){
            imageView_b.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageView_b.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageView_b.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageView_b.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageView_b.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageView_b.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageView_b.setImageResource(R.drawable.background_b);  // optional

        }

        /**
         * All player button color settings
         */
        if(buttonCol == 0) {
            bplay.setColorFilter(Color.BLUE);
            bnext.setColorFilter(Color.BLUE);
            bprev.setColorFilter(Color.BLUE);
            brepeat.setColorFilter(Color.BLUE);
            bshuffle.setColorFilter(Color.BLUE);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.BLUE, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.BLUE);
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.BLUE, Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 1){
            bplay.setColorFilter(Color.RED);
            bnext.setColorFilter(Color.RED);
            bprev.setColorFilter(Color.RED);
            brepeat.setColorFilter(Color.RED);
            bshuffle.setColorFilter(Color.RED);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.RED, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.RED);
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.RED, Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 2){
            bplay.setColorFilter(Color.YELLOW);
            bnext.setColorFilter(Color.YELLOW);
            bprev.setColorFilter(Color.YELLOW);
            brepeat.setColorFilter(Color.YELLOW);
            bshuffle.setColorFilter(Color.YELLOW);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.YELLOW, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.YELLOW);
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.YELLOW, Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 3){
            bplay.setColorFilter(Color.BLACK);
            bnext.setColorFilter(Color.BLACK);
            bprev.setColorFilter(Color.BLACK);
            brepeat.setColorFilter(Color.BLACK);
            bshuffle.setColorFilter(Color.BLACK);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.BLACK, Mode.SRC_IN));
            floatingActionButton.setColorFilter(new PorterDuffColorFilter(Color.WHITE, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.BLACK);
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.BLACK, Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

            Drawable e = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_seekbar_bg_12dp);
            e = DrawableCompat.wrap(e);
            DrawableCompat.setTint(e, Color.WHITE);
            seekbar.setBackgroundDrawable(e);

        }else if(buttonCol == 4){
            bplay.setColorFilter(Color.WHITE);
            bnext.setColorFilter(Color.WHITE);
            bprev.setColorFilter(Color.WHITE);
            brepeat.setColorFilter(Color.WHITE);
            bshuffle.setColorFilter(Color.WHITE);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.WHITE, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.WHITE);
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.WHITE, Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 5){
            bplay.setColorFilter(Color.GREEN);
            bnext.setColorFilter(Color.GREEN);
            bprev.setColorFilter(Color.GREEN);
            brepeat.setColorFilter(Color.GREEN);
            bshuffle.setColorFilter(Color.GREEN);
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GREEN, Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.GREEN);
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.GREEN, Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 6){
            bplay.setColorFilter(Color.parseColor("#f908f1"));
            bnext.setColorFilter(Color.parseColor("#f908f1"));
            bprev.setColorFilter(Color.parseColor("#f908f1"));
            brepeat.setColorFilter(Color.parseColor("#f908f1"));
            bshuffle.setColorFilter(Color.parseColor("#f908f1"));
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#f908f1"), Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.parseColor("#f908f1"));
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#f908f1"), Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 7){
            bplay.setColorFilter(Color.parseColor("#08f7ef"));
            bnext.setColorFilter(Color.parseColor("#08f7ef"));
            bprev.setColorFilter(Color.parseColor("#08f7ef"));
            brepeat.setColorFilter(Color.parseColor("#08f7ef"));
            bshuffle.setColorFilter(Color.parseColor("#08f7ef"));
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#08f7ef"), Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.parseColor("#08f7ef"));
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#08f7ef"), Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }else if(buttonCol == 8){
            bplay.setColorFilter(Color.parseColor("#7709ae"));
            bnext.setColorFilter(Color.parseColor("#7709ae"));
            bprev.setColorFilter(Color.parseColor("#7709ae"));
            brepeat.setColorFilter(Color.parseColor("#7709ae"));
            bshuffle.setColorFilter(Color.parseColor("#7709ae"));
            floatingActionButton.getBackground().setColorFilter(new PorterDuffColorFilter(Color.parseColor("#7709ae"), Mode.SRC_IN));

            Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fiber_manual_record_amber_400_18dp);
            d = DrawableCompat.wrap(d);
            DrawableCompat.setTint(d, Color.parseColor("#7709ae"));
            seekbar.setThumb(d);

            Drawable drawable = createDrawable(getApplicationContext());
            drawable.setColorFilter(new PorterDuffColorFilter(Color.parseColor("#7709ae"), Mode.SRC_IN));
            seekbar.setProgressDrawable(drawable);

        }
    }
}
