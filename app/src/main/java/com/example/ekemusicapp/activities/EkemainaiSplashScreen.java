package com.example.ekemusicapp.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import static android.os.Build.VERSION_CODES.M;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ekemusicapp.players.EkemainaiPlayer;
import com.example.ekemusicapp.R;
import com.example.ekemusicapp.database.EkeDbContract;
import com.example.ekemusicapp.services.EkeLoadDatabaseService;

public class EkemainaiSplashScreen extends AppCompatActivity {

    // Splash screen timer
    public static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_splash);

        // Ask for runtime permission
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            checkStorageAndPermission();
        }

        Cursor cursor = checkDatabase();
        Cursor bcursor = checkDatabaseFts();
        int dance = cursor.getCount();
        int dancFts = bcursor.getCount();
        Log.e("songs", " "+dance);
        Log.e("songsFts", " "+dancFts);
        Toast.makeText(getApplicationContext(), "songs "+dancFts, Toast.LENGTH_LONG).show();

        if(dance == 0 && dancFts == 0){
            if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                // Start database loading service from the foreground
                Intent intentDatabaseLoading = new Intent(this, EkeLoadDatabaseService.class);
                startForegroundService(intentDatabaseLoading);
            } else {
                // Start database loading service from the foreground
                Intent intentDatabaseLoading = new Intent(this, EkeLoadDatabaseService.class);
                startService(intentDatabaseLoading);
            }
        }

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(EkemainaiSplashScreen.this, EkemainaiPlayer.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }

    private Cursor checkDatabase(){
        Uri uri = EkeDbContract.EkeMusicInfoEntry.CONTENT_URI;
        return getContentResolver().query(uri, null, null, null, null);

    }

    private Cursor checkDatabaseFts(){
        Uri uri = EkeDbContract.EkeMusicInfoEntry.CONTENT_FTS_URI;
        return getContentResolver().query(uri, null, null, null, null);
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
            case EkemainaiPlayer.READ_EXTERNAL_STORAGE_PERMISSION_RESULT: {
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
                            EkemainaiPlayer.READ_EXTERNAL_STORAGE_PERMISSION_RESULT);

                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "No storage Media " +
                    "mounted on device", Toast.LENGTH_LONG).show();
        }

    }

}
