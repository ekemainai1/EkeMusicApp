package com.example.ekemusicapp.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.example.ekemusicapp.players.EkemainaiMediaPlayer;
import com.example.ekemusicapp.R;
import com.example.ekemusicapp.model.Song;
import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.uitils.EkemainaiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class EkemainaiPlayerService extends Service implements OnCompletionListener, OnPreparedListener,
        OnErrorListener, OnSeekCompleteListener, OnInfoListener, OnBufferingUpdateListener,
        OnAudioFocusChangeListener {

    public MediaPlayer mediaPlayer;

    //List of available Audio files
    public ArrayList<Song> audioList;
    public int audioIndex = -1;
    public Song activeAudio; //an object of the currently playing audio
    public String songTitle;
    public String songCur;
    public String songTol;
    private Handler handler = new Handler();
    public Timer seekBarTimer = new Timer();
    public TimerTask timerTask;

    //Used to pause/resume MediaPlayer
    public int resumePosition;
    public int CurrentPosition;
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    private static final String CHANNEL_ID = "media_playback_channel";

    public static final String ACTION_PLAY = "com.example.ekemusicapp.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.ekemusicapp.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.ekemusicapp.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.ekemusicapp.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.ekemusicapp.ACTION_STOP";
    public static final String Broadcast_SONG_TITLE = "com.example.ekemusicapp.SONGTITLE";
    public static final String Broadcast_SONG_TIME = "com.example.ekemusicapp.SONGTIME";
    public static final String Broadcast_NOTE_PLAY = "com.example.ekemusicapp.NOTE_PLAY";
    public static final String Broadcast_NOTE_PAUSE = "com.example.ekemusicapp.NOTE_PAUSE";
    public static final String Broadcast_NOTE_NEXT = "com.example.ekemusicapp.NOTE_NEXT";
    public static final String Broadcast_NOTE_PREVIOUS = "com.example.ekemusicapp.NOTE_PREVIOUS";

    //MediaSession
    private MediaSessionManager emediaSessionManager;
    public MediaSessionCompat emediaSession;
    private MediaControllerCompat.TransportControls etransportControls;


    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call, and Resume on hangup.
        callStateListener();

        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();

        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();

        register_broadcastReceiverPlay();

    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            int d = audioList.size();
            Log.e(String.valueOf(audioIndex), "benIndex");
            Log.e(String.valueOf(d), "benSong");

            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(activeAudio.getSongPath());
            songTitle = activeAudio.getSongTitle();
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
            mediaPlayer.prepareAsync();

    }

    public void playSong() {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
    }

    private void stopSong() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();

        }
    }

    private void resumeSong() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            mediaPlayer.seekTo(resumePosition);

        }
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null){
                    initMediaPlayer();
                }
                else if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);

    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            //Load data from SharedPreferences
            EkemainaiUtils storage = new EkemainaiUtils(getApplicationContext());
            audioList = storage.loadSong();
            audioIndex = storage.loadSongIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
                songTitle = activeAudio.getSongTitle();

                // Intent for sending song title broadcast
                Intent songTitleIntent = new Intent();
                songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
                songTitleIntent.putExtra("Title",songTitle);
                sendBroadcast(songTitleIntent);

            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        //Handle Intent action from MediaSession.TransportControls
        if (emediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);

        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        // Intent for sending song duration and tracking
        startTimer();

        new EkeUIStates(getApplicationContext()).storeComplete(audioIndex);
        new EkeUIStates(getApplicationContext()).storeSearchComplete(audioIndex);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopSong();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
        unregisterReceiver(broadcastRecieverPlay);

        //clear cached playlist
        new EkemainaiUtils(getApplicationContext()).clearCachedSongPlaylist();
        stopTimerTask();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        EkemainaiUtils storage = new EkemainaiUtils(getApplicationContext());
        String stateShuffOn = storage.loadShuffleStateOn();
        String stateShuffOff = storage.loadShuffleStateOff();
        String stateRepOn = storage.loadRepeatStateOn();
        String stateRepOff = storage.loadRepeatStateOff();


        if(stateShuffOn.equals("stateOn") && stateShuffOff.equals("shuff")){
            songShuffle();
            Toast.makeText(getApplicationContext(), stateShuffOn , Toast.LENGTH_LONG).show();
        }else if(stateRepOn.equals("repOn") && stateRepOff.equals("rep")){
            songRepeat();
            Toast.makeText(getApplicationContext(), stateRepOn, Toast.LENGTH_LONG).show();
        }else if((stateShuffOn.equals("shuff") || stateRepOn.equals("rep"))){
            if (audioIndex < (audioList.size() - 1)) {
                int b = 0;
                audioIndex = audioIndex + b;
                activeAudio = audioList.get(audioIndex);
                songTitle = activeAudio.getSongTitle();
                normalPlayback();
                Toast.makeText(getApplicationContext(), "Once", Toast.LENGTH_LONG).show();
            } else {
                audioIndex = 0;
                activeAudio = audioList.get(audioIndex);
                songTitle = activeAudio.getSongTitle();
                normalPlayback();
                Toast.makeText(getApplicationContext(), "Twice", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playSong();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public class LocalBinder extends Binder {
        public EkemainaiPlayerService getService() {
            return EkemainaiPlayerService.this;
        }
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseSong();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseSong();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeSong();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index from SharedPreferences
            audioIndex = new EkemainaiUtils(getApplicationContext()).loadSongIndex();
            Toast.makeText(getApplicationContext(), "Broadcast Launched", Toast.LENGTH_SHORT).show();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                //index is in a valid range
                activeAudio = audioList.get(audioIndex);
                //Update stored index
                Intent songTitleIntent = new Intent();
                songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
                songTitleIntent.putExtra("Title",songTitle);
                sendBroadcast(songTitleIntent);

            } else {
                stopSelf();
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopSong();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(EkemainaiMediaPlayer.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private BroadcastReceiver broadcastRecieverPlay  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(mediaPlayer.isPlaying()){
                songPauseNote();
            }else{
                songPlayingNote();
            }
        }
    };

    private void register_broadcastReceiverPlay(){
        IntentFilter intentFilter = new IntentFilter(EkemainaiMediaPlayer.Broadcast_PLAY_NOTE_BUTTON);
        registerReceiver(broadcastRecieverPlay, intentFilter);
    }
    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ekemainai_logo_200px); //replace with your own image

        Intent resultIntent = new Intent(this, EkemainaiMediaPlayer.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setShowWhen(true)
                .setContentIntent(resultPendingIntent)
                // Set the Notification style
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(emediaSession.getSessionToken())
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this, PlaybackStateCompat.ACTION_STOP)))
                /** Show our playback controls in the compact notification view.
                 .setShowActionsInCompactView(0, 1, 2))
                 // Set the large and small icons
                 .setLargeIcon(largeIcon)*/
                .setSmallIcon(android.R.drawable.stat_sys_headset)

                // Set Notification content information
                .setContentText(activeAudio.getSongArtist())
                .setContentInfo(activeAudio.getSongTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

            startForeground(NOTIFICATION_ID, notificationBuilder.build());
            //((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());

    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    @RequiresApi(VERSION_CODES.O)
    private void createChannel() {
        NotificationManager
                mNotificationManager =
                (NotificationManager) getApplicationContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = CHANNEL_ID;
        // The user-visible name of the channel.
        CharSequence name = "Media playback";
        // The user-visible description of the channel.
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        assert mNotificationManager != null;
        mNotificationManager.createNotificationChannel(mChannel);
    }

    private void initMediaSession() throws RemoteException {
        if (emediaSessionManager != null) return; //mediaSessionManager exists

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            emediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        // Create a new MediaSession
        emediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        etransportControls = emediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        emediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        emediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS |
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        emediaSession.setCallback(new Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeSong();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseSong();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                stopSong();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                stopSong();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.ekemainai_logo_200px); //replace with medias albumArt
        // Update the current metadata
        emediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getSongArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getSongTitle())
                .build());
    }

    private void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            //if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
            songTitle = activeAudio.getSongTitle();

        } else {
            //get next in playlist
            activeAudio = audioList.get(++audioIndex);
            songTitle = activeAudio.getSongTitle();

            // Intent for sending song title broadcast
            Intent songTitleIntent = new Intent();
            songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
            songTitleIntent.putExtra("Title",songTitle);
            sendBroadcast(songTitleIntent);
        }

        //Update stored index
        new EkemainaiUtils(getApplicationContext()).storeSongIndex(audioIndex);

        stopSong();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {

        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
            songTitle = activeAudio.getSongTitle();

            // Intent for sending song title broadcast
            Intent songTitleIntent = new Intent();
            songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
            songTitleIntent.putExtra("Title",songTitle);
            sendBroadcast(songTitleIntent);

        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
            songTitle = activeAudio.getSongTitle();

        }

        //Update stored index
        new EkemainaiUtils(getApplicationContext()).storeSongIndex(audioIndex);

        stopSong();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void songPauseNote(){

        buildNotification(PlaybackStatus.PLAYING);
    }

    private void songPlayingNote(){

        buildNotification(PlaybackStatus.PAUSED);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, EkemainaiPlayerService.class);

        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            etransportControls.play();
            Intent broadcastIntentNotePlay = new Intent(Broadcast_NOTE_PLAY);
            sendBroadcast(broadcastIntentNotePlay);
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            etransportControls.pause();
            Intent broadcastIntentNotePause = new Intent(Broadcast_NOTE_PAUSE);
            sendBroadcast(broadcastIntentNotePause);
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            etransportControls.skipToNext();
            Intent broadcastIntentNext = new Intent(Broadcast_NOTE_NEXT);
            sendBroadcast(broadcastIntentNext);
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            etransportControls.skipToPrevious();
            Intent broadcastIntentPrevious = new Intent(Broadcast_NOTE_PREVIOUS);
            sendBroadcast(broadcastIntentPrevious);
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            etransportControls.stop();
        }
    }

    public void songNext() {
        skipToNext();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);

        songTitle = activeAudio.getSongTitle();
        new EkeUIStates(getApplicationContext()).storeComplete(audioIndex);
        new EkeUIStates(getApplicationContext()).storeSearchComplete(audioIndex);

        Intent songTitleIntent = new Intent();
        songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
        songTitleIntent.putExtra("Title",songTitle);
        sendBroadcast(songTitleIntent);


    }

    public void songPrevious() {
        skipToPrevious();
        updateMetaData();
        buildNotification(PlaybackStatus.PLAYING);

        songTitle = activeAudio.getSongTitle();
        new EkeUIStates(getApplicationContext()).storeComplete(audioIndex);
        new EkeUIStates(getApplicationContext()).storeSearchComplete(audioIndex);

        Intent songTitleIntent = new Intent();
        songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
        songTitleIntent.putExtra("Title",songTitle);
        sendBroadcast(songTitleIntent);

        // Intent for sending song duration and tracking
        //handler.postDelayed(UpdateTimer, 100);
        //seekBarTimer.scheduleAtFixedRate(UpdateTimer, 100, 100);

    }

    public void songRepeat(){
        //set index to the last of audioList
        EkemainaiUtils storage = new EkemainaiUtils(getApplicationContext());
        audioIndex = storage.loadSongIndex();
        audioIndex = audioIndex + 0;
        activeAudio = audioList.get(audioIndex);
        updateMetaData();
        //Update stored index
        new EkemainaiUtils(getApplicationContext()).storeSongIndex(audioIndex);
        new EkeUIStates(getApplicationContext()).storeComplete(audioIndex);
        new EkeUIStates(getApplicationContext()).storeSearchComplete(audioIndex);

        songTitle = activeAudio.getSongTitle();

        buildNotification(PlaybackStatus.PLAYING);
        Intent songTitleIntent = new Intent();
        songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
        songTitleIntent.putExtra("Title",songTitle);
        sendBroadcast(songTitleIntent);

        stopSong();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();

        //Intent for sending song duration and tracking
        // handler.postDelayed(UpdateTimer, 100);
        //  seekBarTimer.scheduleAtFixedRate(UpdateTimer, 100, 100);

    }

    public void songShuffle(){
        // shuffle is on - play a random song
        Random rand = new Random();
        audioIndex = rand.nextInt((audioList.size() - 1) + 1);
        activeAudio = audioList.get(audioIndex);

        songTitle = activeAudio.getSongTitle();
        //Update stored index
        new EkemainaiUtils(getApplicationContext()).storeSongIndex(audioIndex);
        new EkeUIStates(getApplicationContext()).storeComplete(audioIndex);
        new EkeUIStates(getApplicationContext()).storeSearchComplete(audioIndex);
        buildNotification(PlaybackStatus.PLAYING);


        Intent songTitleIntent = new Intent();
        songTitleIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TITLE);
        songTitleIntent.putExtra("Title",songTitle);
        sendBroadcast(songTitleIntent);

        stopSong();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();

    }

    private void normalPlayback(){
        songNext();
        //Invoked when playback of a media source has completed.
        stopSong();
        //stop the service
        stopSelf();
    }

   public void initializeTimerTask() {
        timerTask = new TimerTask() {

            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        if(mediaPlayer.isPlaying() && mediaPlayer != null) {
                            CurrentPosition = mediaPlayer.getCurrentPosition();
                            int totalPosition = mediaPlayer.getDuration();
                            songTol = String.valueOf(totalPosition);
                            songCur = String.valueOf(CurrentPosition);

                            Intent songTimerIntent = new Intent();
                            songTimerIntent.setAction(EkemainaiPlayerService.Broadcast_SONG_TIME);
                            songTimerIntent.putExtra("songCurr", songCur);
                            songTimerIntent.putExtra("songToll", songTol);
                            sendBroadcast(songTimerIntent);
                        }

                    }

                });

            }

        };

   }

   public void startTimer() {
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 5000ms the TimerTask will run every 100ms
        seekBarTimer.scheduleAtFixedRate(timerTask, 100, 100);
   }

   public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (seekBarTimer != null) {
            seekBarTimer.cancel();
            seekBarTimer = null;
        }
   }


}


