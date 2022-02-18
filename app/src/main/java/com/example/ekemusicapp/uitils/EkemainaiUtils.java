package com.example.ekemusicapp.uitils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ekemusicapp.model.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class EkemainaiUtils {

        private final String STORAGE = "com.example.ekemini.musicplayer.STORAGE";
        private SharedPreferences preferences;
        private Context context;

        public EkemainaiUtils(Context context) {
            this.context = context;
        }

        public void storeSong(ArrayList<Song> arrayList) {
            preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(arrayList);
            editor.putString("audioArrayList", json);
            editor.apply();
        }

        public ArrayList<Song> loadSong() {
            preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = preferences.getString("audioArrayList", null);
            Type type = new TypeToken<ArrayList<Song>>() {
            }.getType();
            return gson.fromJson(json, type);
        }

        public void storeSongIndex(int index) {
            preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("audioIndex", index);
            editor.apply();
        }

        public int loadSongIndex() {
            preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
            return preferences.getInt("audioIndex", -1);//return -1 if no data found
        }

        // Preference for checking shuffle button state
        public void storeShuffleStateOn(String stateOn){
            preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("shuffleOn", stateOn);
            editor.apply();
        }

    // Preference for checking shuffle button state
    public void storeShuffleStateOff(String stateOff){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("shuffleOff", stateOff);
        editor.apply();
    }

    public String loadShuffleStateOn(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("shuffleOn", "shuff");
    }

    public String loadShuffleStateOff(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("shuffleOff", "shuff");
    }

    // Preference for checking shuffle button state
    public void storeRepeatStateOff(String repOff){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("RepeatOff", repOff);
        editor.apply();
    }

    // Preference for checking shuff button state
    public void storeRepeatStateOn(String repOn){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("RepeatOn", repOn);
        editor.apply();
    }

    public String loadRepeatStateOn(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("RepeatOn", "rep");
    }

    public String loadRepeatStateOff(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("RepeatOff", "rep");
    }



    public void clearCachedSongPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

}

