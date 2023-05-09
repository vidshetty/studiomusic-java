package com.example.studiomusic.MusicData;

public class Track_Test {

    public String title = null;
    public String album = null;
    public String artist = null;
    public String albumArtist = null;
    public String thumbnail = null;
    public String url = null;

    private Track_Test() {
        this.title = "Dil Nu";
        this.album = "Two Hearts Never Break The Same";
        this.artist = "AP Dhillon";
        this.albumArtist = "AP Dhillon";
        this.thumbnail = "https://lh3.googleusercontent.com/vWZmXgoqDLCBZk3nU5JvkM7KtviNkgk2MUbF43uilh9v1QMHvKIU4oQGy1uWB6hWv9tV6mjgM0DXu_gk=w544-h544-l90-rj";
        this.url = "https://studiomusic.app/listen/All Night (Live) - AP Dhillon";
    }

    public static Track_Test getDetails() {
        return new Track_Test();
    }

}
