package com.example.studiomusic.Audio_Controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.studiomusic.MusicData.QueueTrack;
import com.example.studiomusic.MusicData.Track;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class NowPlayingData_Test {

    private static NowPlayingData_Test instance = null;

    public enum REPEAT_TYPE {
        NO_REPEAT, REPEAT_CURRENT, REPEAT_ALL
    };

    private Context context = null;
    private int GLOBAL_INDEX = -1;

    private QueueTrack nowplaying_track = null;
    private List<QueueTrack> queue = null;
    private REPEAT_TYPE repeat_type = REPEAT_TYPE.REPEAT_ALL;

    private int buffered_percent = 0;
    private int active_lyric_index = -1;
    private boolean media_prepared = false;
    private boolean media_is_playing = false;

    private NowPlayingData_Test(Context context) {
        this.context = context;
        this.queue = new ArrayList<>();
        this.nowplaying_track = null;
    };

    public static synchronized NowPlayingData_Test getInstance(@NonNull Context context) {
        if (instance == null) instance = new NowPlayingData_Test(context);
        return instance;
    };

    private void reset() {
        buffered_percent = 0;
        active_lyric_index = -1;
        media_prepared = false;
        media_is_playing = false;
        GLOBAL_INDEX = -1;
    };

    private void trackChangeUpdate() {
        Intent intent = new Intent(MusicApplication.TRACK_CHANGE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    };

    private QueueTrack createQueueTrack(Track track) {
        GLOBAL_INDEX++;
        return new QueueTrack(GLOBAL_INDEX, track);
    };

    private int getNowPlayingIndexInQueue() {
        for (int i=0; i<queue.size(); i++) {
            if (nowplaying_track.getTrackIndex() == queue.get(i).getTrackIndex()) return i;
        }
        return -1;
    };

    public void setNowPlayingTrack(Track track) {
        reset();
        this.nowplaying_track = createQueueTrack(track);
        queue = new ArrayList<>();
        queue.add(this.nowplaying_track);
        trackChangeUpdate();
    };

    public QueueTrack getNowPlayingTrack() {
        return this.nowplaying_track;
    };

    public List<QueueTrack> getQueueTrackList() {
        return this.queue;
    };

    public int getActiveLyricIndex() { return this.active_lyric_index; };

    public void setActiveLyricIndex(int index) { this.active_lyric_index = index; };

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

    public void playNext(Track track) {
        if (track == null) return;
        if (queue.size() == 0) {
            setNowPlayingTrack(track);
            return;
        }
        int now_playing_index = getNowPlayingIndexInQueue();
        queue.add(now_playing_index + 1, createQueueTrack(track));
    };

    public void playNextMultipleTracks(List<Track> tracks) {
        if (tracks == null) return;
        for (int i=0; i<tracks.size(); i++) playNext(tracks.get(i));
    };

    public void addToQueue(Track track) {
        if (track == null) return;
        if (queue.size() == 0) {
            setNowPlayingTrack(track);
            return;
        }
        queue.add(createQueueTrack(track));
    };

    public void addToQueueMultipleTracks(List<Track> tracks) {
        if (tracks == null) return;
        for (int i=0; i<tracks.size(); i++) addToQueue(tracks.get(i));
    };

    public void playSpecificTrackInQueue(int position) {
        reset();
        this.nowplaying_track = queue.get(position);
        trackChangeUpdate();
    };

    public void playTrackNextInQueue() {
        if (queue.size() < 2) {
            reset();
            return;
        }
        int now_playing_index = getNowPlayingIndexInQueue();
        reset();
        if (now_playing_index == queue.size() - 1) this.nowplaying_track = queue.get(0);
        else this.nowplaying_track = queue.get(now_playing_index+1);
        trackChangeUpdate();
    };

    public void playTrackPreviousInQueue() {
        if (queue.size() < 2) {
            reset();
            return;
        }
        int now_playing_index = getNowPlayingIndexInQueue();
        reset();
        if (now_playing_index == 0) this.nowplaying_track = queue.get(queue.size()-1);
        else this.nowplaying_track = queue.get(now_playing_index-1);
        trackChangeUpdate();
    };

    public void shuffleQueue() {

        int now_playing_index = getNowPlayingIndexInQueue();

        List<QueueTrack> final_list = new ArrayList<>();
        final_list.add(queue.get(now_playing_index));

        QueueTrack[] arr = new QueueTrack[queue.size()-1];
        int arr_index = -1;
        for (int i=0; i<queue.size(); i++) {
            if (i != now_playing_index) {
                arr_index++;
                arr[arr_index] = queue.get(i);
            }
        }

        randomize(arr);

        for (int i=0; i<arr.length; i++) {
            final_list.add(arr[i]);
        }

        this.queue = final_list;

    };

    private void randomize(QueueTrack[] arr) {

        Random random = new Random();

        for (int i=arr.length-1; i>0; i--) {
            int val = random.nextInt(i);
            QueueTrack temp = arr[i];
            arr[i] = arr[val];
            arr[val] = temp;
        }

    };

    public void setRepeatType(REPEAT_TYPE repeatType) {
        this.repeat_type = repeatType;
    };

    public REPEAT_TYPE getRepeatType() { return this.repeat_type; };

};

//public static class NowPlayingData {
//
//    private static final String tag = "nowplaying_data";
//
//    public enum REPEAT_TYPE {
//        NO_REPEAT, REPEAT_CURRENT, REPEAT_ALL
//    };
//
//    private static NowPlayingData instance = null;
//    private Context context = null;
//
//    private int buffered_percent = 0;
//    private boolean media_prepared = false;
//    private boolean media_is_playing = false;
//    private int active_lyric_index = -1;
//    private NowPlayingData.REPEAT_TYPE repeatType = NowPlayingData.REPEAT_TYPE.REPEAT_ALL;
//
//    private QueueTrack uncommitted_track = null;
//    private QueueTrack committed_track = null;
//
//    private List<QueueTrack> queue = null;
//
//    private NowPlayingData(Context context) {
//        this.queue = new LinkedList<>();
//        this.context = context;
//    };
//
//    public void destroyInstance() {
//        context = null;
//        instance = null;
//        buffered_percent = 0;
//        active_lyric_index = -1;
//        media_is_playing = false;
//        media_prepared = false;
//        uncommitted_track = null;
//        committed_track = null;
//        queue = null;
//    };
//
//    public static synchronized NowPlayingData getInstance(@NonNull Context context) {
//        if (instance == null) instance = new NowPlayingData(context);
//        return instance;
//    };
//
//    public NowPlayingData setTrack(@NonNull Track track) {
//        this.uncommitted_track = new QueueTrack(-1, track);
//        return this;
//    };
//
//    private void reset() {
//        buffered_percent = 0;
//        active_lyric_index = -1;
//        media_prepared = false;
//        media_is_playing = false;
//    };
//
//    private void trackChange() {
//        reset();
//        Intent intent = new Intent("current_track");
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//    };
//
//    public void commit() {
//        this.committed_track = this.uncommitted_track;
//        this.uncommitted_track = null;
//        if (queue.size() > 0) queue = new LinkedList<>();
//        queue.add(committed_track);
//        setTrackIndexes();
//        trackChange();
//    };
//
//    public void playNext(@NonNull Track track) {
//        if (queue.size() == 0) {
//            setTrack(track).commit();
//            MusicServiceNew.start(context);
//            return;
//        }
//        int current_track_index = getIndex();
//        queue.add(current_track_index+1, new QueueTrack(-1, track));
//        setTrackIndexes();
//    };
//
//    public void playNextMultipleTracks(@NonNull List<Track> tracks) {
//        for (int i=tracks.size()-1; i>=0; i--) {
//            playNext(tracks.get(i));
//        }
//    };
//
//    public void addToQueue(@NonNull Track track) {
//        if (queue.size() == 0) {
//            setTrack(track).commit();
//            MusicServiceNew.start(context);
//            return;
//        }
//        queue.add(queue.size(), new QueueTrack(-1, track));
//        setTrackIndexes();
//    };
//
//    public void addToQueueMultipleTracks(@NonNull List<Track> tracks) {
//        for (int i=0; i<tracks.size(); i++) {
//            addToQueue(tracks.get(i));
//        }
//    };
//
//    public void changeTrackNextInQueue() {
//        if (queue.size() < 2) {
//            reset();
//            return;
//        }
//        int index = getIndex();
//        int new_index = -1;
//        if (index == queue.size() - 1) new_index = 0;
//        else new_index = index + 1;
//        setTrack(queue.get(new_index).getTrack());
//        this.committed_track = this.uncommitted_track;
//        this.uncommitted_track = null;
//        committed_track.setTrackIndex(new_index);
//        setTrackIndexes();
//        trackChange();
//    };
//
//    public void changeTrackPreviousInQueue() {
//        if (queue.size() < 2) {
//            reset();
//            return;
//        }
//        int index = getIndex();
//        int new_index = -1;
//        if (index == 0) new_index = queue.size() - 1;
//        else new_index = index - 1;
//        setTrack(queue.get(new_index).getTrack());
//        this.committed_track = this.uncommitted_track;
//        this.uncommitted_track = null;
//        committed_track.setTrackIndex(new_index);
//        setTrackIndexes();
//        trackChange();
//    };
//
//    public void playSpecificTrackInQueue(int new_index) {
//        setTrack(queue.get(new_index).getTrack());
//        this.committed_track = this.uncommitted_track;
//        this.uncommitted_track = null;
//        committed_track.setTrackIndex(new_index);
//        setTrackIndexes();
//        trackChange();
//    };
//
//    public Track getTrack() {
//        if (this.committed_track == null) return null;
//        return this.committed_track.getTrack();
//    };
//
//    public List<Track> getQueue() {
//        List<Track> list = new LinkedList<>();
//        for (int i=0; i<queue.size(); i++) {
//            list.add(queue.get(i).getTrack());
//        }
//        return list;
//    };
//
//    public List<QueueTrack> getQueueTracks() {
//        return queue;
//    };
//
//    public int getIndex() {
//        if (queue == null) return -1;
//        for (int i=0; i<queue.size(); i++) {
//            if (queue.get(i).getTrackIndex() == committed_track.getTrackIndex()) {
//                return i;
//            }
//        }
//        return -1;
//    };
//
//    public void removeQueueTrack(int position) {
//        if (queue == null) return;
//        queue.remove(position);
//        setTrackIndexes();
//    };
//
//    public int getActiveLyricIndex() { return this.active_lyric_index; };
//
//    public void setActiveLyricIndex(int index) { this.active_lyric_index = index; };
//
//    public int getBufferedPercent() {
//        return this.buffered_percent;
//    };
//
//    public void setBufferedPercent(int percent) {
//        this.buffered_percent = percent;
//    };
//
//    public boolean getMediaPrepared() {
//        return this.media_prepared;
//    };
//
//    public void setMediaPrepared(boolean media_prepared) {
//        this.media_prepared = media_prepared;
//    };
//
//    public boolean getMediaIsPlaying() { return this.media_is_playing; };
//
//    public void setMediaIsPlaying(boolean value) { this.media_is_playing = value; };
//
//    public NowPlayingData.REPEAT_TYPE getRepeatType() {
//        return repeatType;
//    };
//
//    public void setRepeatType(NowPlayingData.REPEAT_TYPE repeatType) {
//        this.repeatType = repeatType;
//    };
//
//    private void setTrackIndexes() {
//        if (queue == null) return;
//        int committed_track_index = committed_track.getTrackIndex();
//        for (int i=0; i<queue.size(); i++) {
//            if (committed_track_index == queue.get(i).getTrackIndex()) queue.get(i).setNowPlaying(true);
//            else queue.get(i).setNowPlaying(false);
//            queue.get(i).setTrackIndex(i);
//        }
////            for (int i=0; i<queue.size(); i++) {
////                queue.get(i).setTrackIndex(i);
////                if (committed_track.getTrackIndex() == i) queue.get(i).setNowPlaying(true);
////                else queue.get(i).setNowPlaying(false);
////            }
////        display();
//    };
//
//    private void display() {
//        Log.d(tag, "commited track index " + committed_track.getTrackIndex());
//        for (int i=0; i<queue.size(); i++) {
//            Log.d(tag, "queue index " + i + " " + queue.get(i).getTitle() + " track index " + queue.get(i).getTrackIndex());
//        }
//        Log.d(tag, "--------------------------------");
//    };
//
//    public void shuffleQueue() {
//
//        List<QueueTrack> final_list = new ArrayList<>();
//        final_list.add(queue.get(getIndex()));
//
//        QueueTrack[] arr = new QueueTrack[queue.size()-1];
//        int arr_index = -1;
//        for (int i=0; i<queue.size(); i++) {
//            if (i != getIndex()) {
//                arr_index++;
//                arr[arr_index] = queue.get(i);
//            }
//        }
//
//        randomize(arr);
//
//        for (int i=0; i<arr.length; i++) {
//            final_list.add(arr[i]);
//        }
//
//        this.queue = final_list;
//
//    };
//
//    private void randomize(QueueTrack[] arr) {
//
//        Random random = new Random();
//
//        for (int i=arr.length-1; i>0; i--) {
//            int val = random.nextInt(i);
//            QueueTrack temp = arr[i];
//            arr[i] = arr[val];
//            arr[val] = temp;
//        }
//
//    };
//
//};
