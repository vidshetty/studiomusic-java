package com.example.studiomusic.MusicData;

import java.util.List;

public class Album {

    private String _albumId = null;
    private String album = null;
    private String albumArtist = null;
    private String type = null;
    private String year = null;
    private String color = null;
    private String thumbnail = null;
    private String releaseDate = null;
    private List<Track> tracks = null;

    public String getAlbumId() { return _albumId; }

    public String getAlbum() { return album; }

    public String getAlbumArtist() { return albumArtist; }

    public String getType() { return type; }

    public String getYear() { return year; }

    public String getColor() { return color; }

    public String getThumbnail() { return thumbnail; }

    public String getReleaseDate() { return releaseDate; }

    public List<Track> getTracks() { return tracks; }

    private Album(Builder builder) {
        this._albumId = builder._albumId;
        this.album = builder.album;
        this.albumArtist = builder.albumArtist;
        this.type = builder.type;
        this.year = builder.year;
        this.color = builder.color;
        this.thumbnail = builder.thumbnail;
        this.releaseDate = builder.releaseDate;
        this.tracks = builder.tracks;
    }

    public static class Builder {

        private String _albumId = null;
        private String album = null;
        private String albumArtist = null;
        private String type = null;
        private String year = null;
        private String color = null;
        private String thumbnail = null;
        private String releaseDate = null;
        private List<Track> tracks = null;

        public Builder setAlbumId(String id) {
            this._albumId = id;
            return this;
        }

        public Builder setAlbum(String id) {
            this.album = id;
            return this;
        }

        public Builder setAlbumArtist(String id) {
            this.albumArtist = id;
            return this;
        }

        public Builder setType(String id) {
            this.type = id;
            return this;
        }

        public Builder setYear(String id) {
            this.year = id;
            return this;
        }

        public Builder setColor(String id) {
            this.color = id;
            return this;
        }

        public Builder setThumbnail(String id) {
            this.thumbnail = id;
            return this;
        }

        public Builder setReleaseDate(String id) {
            this.releaseDate = id;
            return this;
        }

        public Builder setTracks(List<Track> id) {
            this.tracks = id;
            return this;
        }

        public Album build() {
            return new Album(this);
        }

    }

}
