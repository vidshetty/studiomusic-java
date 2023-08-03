package com.example.studiomusic.Lyrics;

public class SpotifyLyrics {

    private Integer startTimeMs = null;
    private String word = null;
    private Boolean current_lyric = null;

    public SpotifyLyrics(int startTimeMs, String word) {
        this.startTimeMs = startTimeMs;
        this.word = word;
        this.current_lyric = false;
    };

    public int getStartTime() { return this.startTimeMs; };

    public String getWord() { return this.word; };

    public void setCurrentLyric(boolean val) {
        this.current_lyric = val;
    };

    public boolean getCurrentLyric() {
        return this.current_lyric;
    };

};
