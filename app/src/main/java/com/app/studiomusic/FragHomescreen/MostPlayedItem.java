package com.app.studiomusic.FragHomescreen;

import com.app.studiomusic.MusicData.Album;

public class MostPlayedItem {

    private Album album = null;
    private boolean isPaused;
    private boolean isNowPlaying;

    public MostPlayedItem(Album a) {
        this.album = a;
        this.isPaused = true;
        this.isNowPlaying = false;
    };

    public void setPaused(boolean b) {
        this.isPaused = b;
    };

    public boolean getIsPaused() {
        return this.isPaused;
    };

    public void setNowPlaying(boolean b) {
        this.isNowPlaying = b;
    };

    public boolean getIsNowPlaying() {
        return this.isNowPlaying;
    };

    public Album getAlbum() {
        return this.album;
    };

};
