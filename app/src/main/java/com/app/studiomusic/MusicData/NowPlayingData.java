package com.app.studiomusic.MusicData;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.app.studiomusic.Audio_Controller.MusicApplication;
import com.app.studiomusic.Audio_Controller.MusicService;

import java.util.LinkedList;
import java.util.List;

public class NowPlayingData {

    private static final String tag = "nowplaying_data";

    public enum REPEAT_TYPE {
        NO_REPEAT, REPEAT_CURRENT, REPEAT_ALL
    };

    private static NowPlayingData instance = null;
    private Context context = null;

    private int buffered_percent = 0;
    private boolean media_prepared = false;
    private boolean media_is_playing = false;
    private REPEAT_TYPE repeatType = REPEAT_TYPE.REPEAT_ALL;

    private QueueTrack uncommitted_track = null;
    private QueueTrack committed_track = null;

    private List<QueueTrack> queue = null;

    private NowPlayingData(Context context) {
        this.queue = new LinkedList<>();
        this.context = context;
    };

    public void destroyInstance() {
        context = null;
        instance = null;
        buffered_percent = 0;
        media_is_playing = false;
        media_prepared = false;
        uncommitted_track = null;
        committed_track = null;
        queue = null;
    };

    public static synchronized NowPlayingData getInstance(@NonNull Context context) {
        if (instance == null) instance = new NowPlayingData(context);
        return instance;
    };

    public NowPlayingData setTrack(@NonNull Track track) {
        this.uncommitted_track = new QueueTrack(-1, track);
        return this;
    };

    private void reset() {
        buffered_percent = 0;
        media_prepared = false;
        media_is_playing = false;
    };

    private void trackChange() {
        reset();
        Intent intent = new Intent(MusicApplication.TRACK_CHANGE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    };

    public void commit() {
        this.committed_track = this.uncommitted_track;
        this.uncommitted_track = null;
        if (queue.size() > 0) queue = new LinkedList<>();
        queue.add(committed_track);
        setTrackIndexes();
        trackChange();
    };

    public void playNext(@NonNull Track track) {
        if (queue.size() == 0) {
            setTrack(track).commit();
            MusicService.start(context);
            return;
        }
        int current_track_index = getIndex();
        queue.add(current_track_index+1, new QueueTrack(-1, track));
        setTrackIndexes();
    };

    public void playNextMultipleTracks(@NonNull List<Track> tracks) {
        for (int i=tracks.size()-1; i>=0; i--) {
            playNext(tracks.get(i));
        }
    };

    public void addToQueue(@NonNull Track track) {
        if (queue.size() == 0) {
            setTrack(track).commit();
            MusicService.start(context);
            return;
        }
        queue.add(queue.size(), new QueueTrack(-1, track));
        setTrackIndexes();
    };

    public void addToQueueMultipleTracks(@NonNull List<Track> tracks) {
        for (int i=0; i<tracks.size(); i++) {
            addToQueue(tracks.get(i));
        }
    };

    public void changeTrackNextInQueue() {
        if (queue.size() < 2) {
            reset();
            return;
        }
        int index = getIndex();
        int new_index = -1;
        if (index == queue.size() - 1) new_index = 0;
        else new_index = index + 1;
        setTrack(queue.get(new_index).getTrack());
        this.committed_track = this.uncommitted_track;
        this.uncommitted_track = null;
        committed_track.setTrackIndex(new_index);
        setTrackIndexes();
        trackChange();
    };

    public void changeTrackPreviousInQueue() {
        if (queue.size() < 2) {
            reset();
            return;
        }
        int index = getIndex();
        int new_index = -1;
        if (index == 0) new_index = queue.size() - 1;
        else new_index = index - 1;
        setTrack(queue.get(new_index).getTrack());
        this.committed_track = this.uncommitted_track;
        this.uncommitted_track = null;
        committed_track.setTrackIndex(new_index);
        setTrackIndexes();
        trackChange();
    };

    public Track getTrack() {
        if (this.committed_track == null) return null;
        return this.committed_track.getTrack();
    };

    public List<Track> getQueue() {
        List<Track> list = new LinkedList<>();
        for (int i=0; i<queue.size(); i++) {
            list.add(queue.get(i).getTrack());
        }
        return list;
    };

    public int getIndex() {
        if (queue == null) return -1;
        for (int i=0; i<queue.size(); i++) {
            if (queue.get(i).getTrackIndex() == committed_track.getTrackIndex()) {
                return i;
            }
        }
        return -1;
    };

    public int getBufferedPercent() {
        return this.buffered_percent;
    };

    public void setBufferedPercent(int percent) {
        this.buffered_percent = percent;
    };

    public boolean getMediaPrepared() {
        return this.media_prepared;
    };

    public void setMediaPrepared(boolean media_prepared) {
        this.media_prepared = media_prepared;
    };

    public boolean getMediaIsPlaying() { return this.media_is_playing; };

    public void setMediaIsPlaying(boolean value) { this.media_is_playing = value; };

    public REPEAT_TYPE getRepeatType() {
        return repeatType;
    };

    public void setRepeatType(REPEAT_TYPE repeatType) {
        this.repeatType = repeatType;
    };

    private void setTrackIndexes() {
        if (queue == null) return;
        for (int i=0; i<queue.size(); i++) {
            queue.get(i).setTrackIndex(i);
        }
//        display();
    };

    private void display() {
        Log.d(tag, "commited track index " + committed_track.getTrackIndex());
        for (int i=0; i<queue.size(); i++) {
            Log.d(tag, "queue index " + i + " " + queue.get(i).getTitle() + " track index " + queue.get(i).getTrackIndex());
        }
        Log.d(tag, "--------------------------------");
    };

};
