package com.example.studiomusic.MusicData;

public class Track {

//    private String _albumId = null;
//    private String _trackId = null;
//    private String title = null;
//    private String artist = null;
//    private String duration = null;
//    private String url = null;
//    private Boolean lyrics = null;
//    private Boolean sync = null;

    private Builder builder = null;

    public String getTrackId() { return builder._trackId; }

    public String getTitle() { return builder.title; }

    public String getArtist() { return builder.artist; }

    public String getDuration() { return builder.duration; }

    public String getUrl() { return builder.url; }

    public Boolean hasLyrics() { return builder.lyrics; }

    public Boolean hasSync() { return builder.sync; }

    public String getAlbumId() { return builder._albumId; }

    public String getAlbum() { return builder.album; }

    public String getThumbnail() { return builder.thumbnail; }

    public String getColor() { return builder.color; }

    public String getYear() { return builder.year; }

    public String getReleaseDate() { return builder.releaseDate; }

    private Track(Builder builder) {
        this.builder = builder;
//        this._trackId = builder._trackId;
//        this.title = builder.title;
//        this.artist = builder.artist;
//        this.duration = builder.duration;
//        this.url = builder.url;
//        this.lyrics = builder.lyrics;
//        this.sync = builder.sync;
    }

    public static class Builder {

        private String _albumId = null;
        private String _trackId = null;
        private String album = null;
        private String color = null;
        private String thumbnail = null;
        private String year = null;
        private String releaseDate = null;
        private String title = null;
        private String artist = null;
        private String duration = null;
        private String url = null;
        private Boolean lyrics = null;
        private Boolean sync = null;

        public Builder setReleaseDate(String date) {
            this.releaseDate = date;
            return this;
        }

        public Builder setYear(String year) {
            this.year = year;
            return this;
        }

        public Builder setThumbnail(String url) {
            this.thumbnail = url;
            return this;
        }

        public Builder setColor(String color) {
            this.color = color;
            return this;
        }

        public Builder setAlbum(String name) {
            this.album = name;
            return this;
        }

        public Builder setTrackId(String id) {
            this._trackId = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder setDuration(String d) {
            this.duration = d;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setLyrics(Boolean lyrics) {
            this.lyrics = lyrics;
            return this;
        }

        public Builder setSync(Boolean sync) {
            this.sync = sync;
            return this;
        }

        public Builder setAlbumId(String albumId) {
            this._albumId = albumId;
            return this;
        }

        public Track build() {
            return new Track(this);
        }

    }

}
