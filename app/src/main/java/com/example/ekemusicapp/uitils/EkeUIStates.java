package com.example.ekemusicapp.uitils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ekemusicapp.model.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.Manifest.permission_group.STORAGE;

/**
 * Created by Ekemini on 9/27/2017.
 */

public class EkeUIStates {
    private final String UISTORAGE = "com.example.ekemusicapp.UISTORAGE";
    private SharedPreferences preferences;
    private Context context;

    public EkeUIStates(Context context) {
        this.context = context;
    }

    public void storePlaylistPics(String imageUri){
        preferences = context.getSharedPreferences(UISTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("imageFilePath", imageUri);
        editor.apply();
    }

    public String loadPlaylistPics(){
        preferences = context.getSharedPreferences(UISTORAGE, Context.MODE_PRIVATE);
        return preferences.getString("imageFilePath", null);
    }

    public void storeLayoutBackgroundA(int uriA){
        preferences = context.getSharedPreferences(UISTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("backImageA", uriA);
        editor.apply();
    }

    public int loadLayoutBackgroundA(){
        preferences = context.getSharedPreferences(UISTORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("backImageA", 0);
    }

        public void storeSongSearcFts(ArrayList<Song> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public ArrayList<Song> loadSongSearchFts() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        Type type = new TypeToken<ArrayList<Song>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeShareFile(String fileUriPos){
        preferences = context.getSharedPreferences(UISTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("shareFilePath", fileUriPos);
        editor.apply();
    }

    public String loadShareFile(){
        preferences = context.getSharedPreferences(UISTORAGE, Context.MODE_PRIVATE);
        return preferences.getString("shareFilePath", " ");
    }

    public void storeSongPosition(int pos) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioPos", pos);
        editor.apply();
    }

    public int loadSongPosition() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioPos", -1);//return -1 if no data found
    }

    public void storeSearchPosition(int mark) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("searchPos", mark);
        editor.apply();
    }

    public int loadSearchPosition() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("searchPos", -1);//return -1 if no data found
    }

    public void storeSearchTitle(String title) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("searchTitle", title);
        editor.apply();
    }

    public String loadSearchTitle() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("searchTitle", " ");//return -1 if no data found
    }

    public void storeSearchTitlePos(String titlepos) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("searchTitlePos", titlepos);
        editor.apply();
    }

    public String loadSearchTitlePos() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("searchTitlePos", " ");//return -1 if no data found
    }

    public void storeButtonColor(int col) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("buttonColor", col);
        editor.apply();
    }

    public int loadButtonColor() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("buttonColor", 0);//return -1 if no data found
    }

    public void storeComplete(int col) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("comp", col);
        editor.apply();
    }

    public int loadComplete() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("comp", -1);//return -1 if no data found
    }

    public void storeSearchComplete(int col) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("com", col);
        editor.apply();
    }

    public int loadSearchComplete() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("com", -1);//return -1 if no data found
    }

}
