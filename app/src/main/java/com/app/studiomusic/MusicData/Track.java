package com.app.studiomusic.MusicData;

import com.app.studiomusic.Common.Common;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Track {

    private Builder builder = null;

    public String getTrackId() { return builder._trackId; };

    public String getTitle() { return builder.title; };

    public String getArtist() { return builder.artist; };

    public String getDuration() { return builder.duration; };

    public String getUrl() { return builder.url; };

    public Boolean hasLyrics() { return builder.lyrics; };

    public Boolean hasSync() { return builder.sync; };

    public String getAlbumId() { return builder._albumId; };

    public String getAlbum() { return builder.album; };

    public String getThumbnail() { return builder.thumbnail; };

    public String getColor() { return builder.color; };

    public String getLightColor() { return builder.light_color; };

    public String getDarkColor() { return builder.dark_color; };

    public String getYear() { return builder.year; };

    public String getReleaseDate() { return builder.releaseDate; };

    public String getType() { return builder.type; };

    private Track(Builder builder) {
        this.builder = builder;
    };

    public JSONObject getJSONObject() {

        Map<String,Object> map = new HashMap<>();

        map.put("_albumId", builder._albumId);
        map.put("_trackId", builder._trackId);
        map.put("Album", builder.album);
        map.put("Color", builder.color);
        map.put("Light_Color", builder.light_color);
        map.put("Dark_Color", builder.dark_color);
        map.put("Thumbnail", builder.thumbnail);
        map.put("Year", builder.year);
        map.put("releaseDate", (String) builder.releaseDate);
        map.put("Title", builder.title);
        map.put("Artist", builder.artist);
        map.put("Duration", builder.duration);
        map.put("url", builder.url);
        map.put("lyrics", (Boolean) builder.lyrics);
        map.put("sync", (Boolean) builder.sync);
        map.put("Type", builder.type);

        return new JSONObject(map);

    };

    public static class Builder {

        private String _albumId = null;
        private String _trackId = null;
        private String album = null;
        private String color = null;
        private String light_color = null;
        private String dark_color = null;
        private String thumbnail = null;
        private String year = null;
        private String releaseDate = null;
        private String title = null;
        private String artist = null;
        private String duration = null;
        private String url = null;
        private Boolean lyrics = null;
        private Boolean sync = null;
        private String type = null;

        public Builder setReleaseDate(String date) {
            this.releaseDate = date;
            return this;
        };

        public Builder setType(String val) {
            this.type = val;
            return this;
        };

        public Builder setYear(String year) {
            this.year = year;
            return this;
        };

        public Builder setThumbnail(String url) {
            this.thumbnail = url;
            return this;
        };

        public Builder setColor(String color) {
            this.color = color;
            return this;
        };

        public Builder setLightColor(String color) {
            if (color == null || color.equals("null")) this.light_color = null;
            else this.light_color = color;
            return this;
        };

        public Builder setDarkColor(String color) {
            if (color == null || color.equals("null")) this.dark_color = null;
            else this.dark_color = color;
            return this;
        };

        public Builder setAlbum(String name) {
            this.album = name;
            return this;
        };

        public Builder setTrackId(String id) {
            this._trackId = id;
            return this;
        };

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        };

        public Builder setArtist(String artist) {
            this.artist = Common.convertStringWithAnd(artist);
            return this;
        };

        public Builder setDuration(String d) {
            this.duration = d;
            return this;
        };

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        };

        public Builder setLyrics(Boolean lyrics) {
            this.lyrics = lyrics;
            return this;
        };

        public Builder setSync(Boolean sync) {
            this.sync = sync;
            return this;
        };

        public Builder setAlbumId(String albumId) {
            this._albumId = albumId;
            return this;
        };

        public Track build() {
            return new Track(this);
        };

    };

}
