package com.app.studiomusic.MusicData;

import com.app.studiomusic.Common.Common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Album {

    private Builder builder = null;

    public String getAlbumId() { return builder._albumId; }

    public String getAlbum() { return builder.album; }

    public String getAlbumArtist() { return builder.albumArtist; }

    public String getType() { return builder.type; }

    public String getYear() { return builder.year; }

    public String getColor() { return builder.color; }

    public String getThumbnail() { return builder.thumbnail; }

    public String getReleaseDate() { return builder.releaseDate; }

    public List<Track> getTracks() { return builder.tracks; }

    private Album(Builder builder) {
        this.builder = builder;
    };

    public JSONObject getJSONObject() {

        Map<String,Object> map = new HashMap<>();

        map.put("_albumId", builder._albumId);
        map.put("Album", builder.album);
        map.put("AlbumArtist", builder.albumArtist);
        map.put("Type", builder.type);
        map.put("Year", builder.year);
        map.put("Color", builder.color);
        map.put("Light_Color", builder.light_color);
        map.put("Dark_Color", builder.dark_color);
        map.put("Thumbnail", builder.thumbnail);
        map.put("releaseDate", builder.releaseDate);

        JSONArray array = new JSONArray();
        for (int i=0; i<builder.tracks.size(); i++) {
            array.put(builder.tracks.get(i).getJSONObject());
        }
        map.put("Tracks", array);

        return new JSONObject(map);

    };

    public static class Builder {

        private String _albumId = null;
        private String album = null;
        private String albumArtist = null;
        private String type = null;
        private String year = null;
        private String color = null;
        private String light_color = null;
        private String dark_color = null;
        private String thumbnail = null;
        private String releaseDate = null;
        private List<Track> tracks = null;

        public Builder setAlbumId(String id) {
            this._albumId = id;
            return this;
        };

        public Builder setAlbum(String id) {
            this.album = id;
            return this;
        };

        public Builder setAlbumArtist(String id) {
            this.albumArtist = Common.convertStringWithAnd(id);
            return this;
        };

        public Builder setType(String id) {
            this.type = id;
            return this;
        };

        public Builder setYear(String id) {
            this.year = id;
            return this;
        };

        public Builder setColor(String id) {
            this.color = id;
            return this;
        };

        public Builder setLightColor(String id) {
            if (id == null || id.equals("null")) this.light_color = null;
            else this.light_color = id;
            return this;
        };

        public Builder setDarkColor(String id) {
            if (id == null || id.equals("null")) this.dark_color = null;
            else this.dark_color = id;
            return this;
        };

        public Builder setThumbnail(String id) {
            this.thumbnail = id;
            return this;
        };

        public Builder setReleaseDate(String id) {
            this.releaseDate = id;
            return this;
        };

        public Builder setTracks(List<Track> id) {
            this.tracks = id;
            return this;
        };

        public Album build() {
            return new Album(this);
        }

    }

}
