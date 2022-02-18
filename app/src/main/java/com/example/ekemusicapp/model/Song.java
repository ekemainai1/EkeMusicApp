package com.example.ekemusicapp.model;

//Created by Ekemini on 5/24/2017.

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable{
    private long songID;
    private String songTitle;
    private String songSized;
    private String songDur;
    private String songArtist;
    private String songPath;



    public Song(Parcel in) {
        songID = in.readLong();
        songTitle = in.readString();
        songSized = in.readString();
        songDur = in.readString();
        songArtist = in.readString();
        songPath = in.readString();

    }


    public Song(long songID, String songTitle, String songSized, String songDur, String songArtist,
                String songPath) {

        this.songID = songID;
        this.songTitle = songTitle;
        this.songSized = songSized;
        this.songDur = songDur;
        this.songArtist = songArtist;
        this.songPath = songPath;

    }

    public Song() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(songID);
        dest.writeString(songTitle);
        dest.writeString(songSized);
        dest.writeString(songDur);
        dest.writeString(songArtist);
        dest.writeString(songPath);

    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];

        }
    };

    public long getSongID() {
        return songID;
    }

    public void setSongID(long songID) {
        this.songID = songID;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongSized() {
        return songSized;
    }

    public void setSongSized(String songSized) {
        this.songSized = songSized;
    }

    public String getSongDur() {
        return songDur;
    }

    public void setSongDur(String songDur) {
        this.songDur = songDur;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }




}
