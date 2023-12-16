package com.app.studiomusic.MusicData;

public class QueueTrack {

    private Integer index_id = null;
    private boolean now_playing = false;
    private Track track = null;

    public QueueTrack(int index_id, Track track) {
        this.index_id = index_id;
        this.track = track;
    };

    public String getTrackId() { return track.getTrackId(); };

    public String getTitle() { return track.getTitle(); };

    public String getArtist() { return track.getArtist(); };

    public String getDuration() { return track.getDuration(); };

    public String getUrl() { return track.getUrl(); };

    public Boolean hasLyrics() { return track.hasLyrics(); };

    public Boolean hasSync() { return track.hasSync(); };

    public String getAlbumId() { return track.getAlbumId(); };

    public String getAlbum() { return track.getAlbum(); };

    public String getThumbnail() { return track.getThumbnail(); };

    public String getColor() { return track.getColor(); };

    public String getYear() { return track.getYear(); };

    public String getReleaseDate() { return track.getReleaseDate(); };

    public int getTrackIndex() { return (int) this.index_id; };

    public void setTrackIndex(int index) { this.index_id = index; };

    public Track getTrack() { return this.track; };

    public String getType() { return this.getType(); };

    public void setNowPlaying(boolean now_playing) {
        this.now_playing = now_playing;
    };

    public boolean isNowPlaying() {
        return this.now_playing;
    };

};
