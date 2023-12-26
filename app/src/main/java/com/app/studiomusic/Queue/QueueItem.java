package com.app.studiomusic.Queue;

import com.app.studiomusic.MusicData.Track;


public class QueueItem {

    private Track track = null;
    private boolean isPaused;
    private boolean isNowPlaying;

    public QueueItem(Track a) {
        this.track = a;
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

    public Track getTrack() {
        return this.track;
    };

};