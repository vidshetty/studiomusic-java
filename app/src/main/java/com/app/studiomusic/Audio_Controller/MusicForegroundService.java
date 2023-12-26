package com.app.studiomusic.Audio_Controller;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.app.studiomusic.API_Controller.APIService;
import com.app.studiomusic.Main.MainActivity;
import com.app.studiomusic.MusicData.QueueTrack;
import com.app.studiomusic.MusicData.Track;
import com.app.studiomusic.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MusicForegroundService extends Service {

    public class MusicForegroundServiceBinder extends Binder {
        public MusicForegroundService getService() {
            return MusicForegroundService.this;
        }
    };

    private static final String tag = "foreground_service_log";
    private static List<QueueItem> queueItems = null;
    private static MediaSessionCompat mediaSession = null;
    private static PlaybackStateCompat.Builder playbackStateBuilder = null;

    private static final int previous_button = R.drawable.ic_previousbutton_notification;
    private static final int next_button = R.drawable.ic_nextbutton_notification;
    private static final int pause_button = R.drawable.ic_pausebutton_notification;
    private static final int play_button = R.drawable.ic_playbutton_notification;
    private static final int rewind10_button = R.drawable.ic_rewind10button_notification;
    private static final int forward10_button = R.drawable.ic_forward10button_notification;

    private MusicForegroundServiceBinder binder = new MusicForegroundServiceBinder();

    private MediaPlayer mp = null;
    private boolean manually_paused = false;

    private NowPlayingData nowplayingdata_instance = null;
    private Track currentTrack = null;
    private NotificationManager notificationManager = null;
    private NotificationCompat.Builder notificationBuilder = null;
    private MediaMetadataCompat.Builder metadataBuilder = null;
    private AudioManager audioManager = null;
    private AudioAttributes audioAttributes = null;
    private AudioFocusRequest audioFocusRequest = null;
    private MediaSessionCompat.Callback mediaSessionCallback = null;
    private Timer timer = null;

    private Intent intent = null;
    private PendingIntent notificationClick = null;

    private Intent play_intent = null;
    private PendingIntent play_pending_intent = null;

    private Intent pause_intent = null;
    private PendingIntent pause_pending_intent = null;

    private Intent previous_intent = null;
    private PendingIntent previous_pending_intent = null;

    private Intent next_intent = null;
    private PendingIntent next_pending_intent = null;

    private Intent rewind_intent = null;
    private PendingIntent rewind_pending_intent = null;

    private Intent forward_intent = null;
    private PendingIntent forward_pending_intent = null;


    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int audioFocusChanged) {
            switch (audioFocusChanged) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    play();
                    break;
                default:
                    break;
            }
        };
    };

    public class ActionBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTIONS.PLAY)) play();
            else if (action.equals(ACTIONS.PAUSE)) pause();
            else if (action.equals(ACTIONS.NEXT)) playNextTrack();
            else if (action.equals(ACTIONS.PREVIOUS)) playPreviousTrack();
            else if (action.equals(ACTIONS.REWIND)) rewind();
            else if (action.equals(ACTIONS.FORWARD)) forward();
        };
    };
    private ActionBroadcastReceiver actionReceiver = null;

    private static class ACTIONS {
        private static final String PLAY = "play";
        private static final String PAUSE = "pause";
        private static final String PREVIOUS = "previous";
        private static final String NEXT = "next";
        private static final String REWIND = "rewind";
        private static final String FORWARD = "forward";
    };

    public static class NowPlayingData {

        private static NowPlayingData instance = null;

        public enum REPEAT_TYPE {
            NO_REPEAT, REPEAT_CURRENT, REPEAT_ALL
        };

        private Context context = null;
        private int GLOBAL_INDEX = -1;

        private QueueTrack nowplaying_track = null;
        private List<QueueTrack> queue = null;
        private NowPlayingData.REPEAT_TYPE repeat_type = NowPlayingData.REPEAT_TYPE.REPEAT_ALL;

        private int buffered_percent = 0;
        private int active_lyric_index = -1;
        private boolean media_prepared = false;
        private boolean media_is_playing = false;

        public void destroyInstance() {
            context = null;
            GLOBAL_INDEX = -1;
            nowplaying_track = null;
            queue = null;
            buffered_percent = 0;
            active_lyric_index = -1;
            media_prepared = false;
            media_is_playing = false;
        };

        private void updateMediaQueueItems() {
//            mediaSession.setQueue(null);
            mediaSession.setQueue(queueItems);
            int now_playing_media_id = getNowPlayingMediaId();
            playbackStateBuilder.setActiveQueueItemId(now_playing_media_id);
            mediaSession.setPlaybackState(playbackStateBuilder.build());
        };

        private NowPlayingData(Context context) {
            this.context = context;
            this.queue = new ArrayList<>();
            queueItems = new ArrayList<>();
            this.nowplaying_track = null;
        };

        public static synchronized NowPlayingData getInstance(@NonNull Context context) {
            if (instance == null) instance = new NowPlayingData(context);
            return instance;
        };

        public Track getTrack() {
            if (nowplaying_track == null) return null;
            return this.nowplaying_track.getTrack();
        };

        private void reset() {
            buffered_percent = 0;
            active_lyric_index = -1;
            media_prepared = false;
            media_is_playing = false;
        };

        private void resetGlobalIndex() {
            GLOBAL_INDEX = -1;
        };

        private void trackChangeUpdate() {
            Intent intent = new Intent(MusicApplication.TRACK_CHANGE);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            changeNowPlaying();
            updateMediaQueueItems();
        };

        private void trackRemoveBroadcast(int position) {
            Intent intent = new Intent(MusicApplication.TRACK_REMOVE);
            intent.putExtra("position", position);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        };

        public void removeQueueTrack(int position) {
            if (queue == null || queue.size() == 0) return;
            if (queueItems == null || queueItems.size() == 0) return;
            if (queue.size() == 1) {
                queue.remove(position);
                queueItems.remove(position);
                nowplaying_track = null;
                reset();
                return;
            }
            trackRemoveBroadcast(position);
            queue.remove(position);
            queueItems.remove(position);
            updateMediaQueueItems();
        };

        private QueueTrack createQueueTrack(Track track) {
            GLOBAL_INDEX++;
            return new QueueTrack(GLOBAL_INDEX, track);
        };

        private void createAndAddQueueItem(int index, QueueTrack track) {

            MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getAlbum())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, track.getArtist());

            MediaDescriptionCompat mediaDesc = metadataBuilder.build().getDescription();
            if (index == -1) queueItems.add(new MediaSessionCompat.QueueItem(mediaDesc, track.getTrackIndex()));
            else queueItems.add(index, new MediaSessionCompat.QueueItem(mediaDesc, track.getTrackIndex()));
//            updateMediaQueueItems();

        };

        public int getIndex() {
            for (int i=0; i<queue.size(); i++) {
                if (nowplaying_track.getTrackIndex() == queue.get(i).getTrackIndex()) return i;
            }
            return -1;
        };

        public int getNowPlayingMediaId() {
            int index = getIndex();
            return queue.get(index).getTrackIndex();
        };

        public void setNowPlayingTrack(Track track) {
            reset();
            resetGlobalIndex();
            QueueTrack new_track = createQueueTrack(track);
            this.nowplaying_track = new_track;
            queue = new ArrayList<>();
            queue.add(this.nowplaying_track);
            queueItems = new ArrayList<>();
            createAndAddQueueItem(-1, new_track);
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
                MusicService.start(context);
                return;
            }
            int now_playing_index = getIndex();
            QueueTrack new_track = createQueueTrack(track);
            queue.add(now_playing_index + 1, new_track);
            createAndAddQueueItem(now_playing_index + 1, new_track);
            updateMediaQueueItems();
        };

        public void playNextMultipleTracks(List<Track> tracks) {

            if (tracks == null) return;

            boolean queue_empty = queue.isEmpty();

            if (queue_empty) {
                setNowPlayingTrack(tracks.get(0));
                MusicService.start(context);
            }

            for (int i=tracks.size()-1; i>=(queue_empty ? 1 : 0); i--) {
                int now_playing_index = getIndex();
                QueueTrack new_track = createQueueTrack(tracks.get(i));
                queue.add(now_playing_index + 1, new_track);
                createAndAddQueueItem(now_playing_index + 1, new_track);
            }

            updateMediaQueueItems();

        };

        public void addToQueue(Track track) {
            if (track == null) return;
            if (queue.size() == 0) {
                setNowPlayingTrack(track);
                MusicService.start(context);
                return;
            }
            QueueTrack new_track = createQueueTrack(track);
            queue.add(new_track);
            createAndAddQueueItem(-1, new_track);
            updateMediaQueueItems();
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

        public boolean playSpecificTrackInQueueFromMediaId(long media_id) {
            int index = -1;
            for (int i=0; i<queue.size(); i++) {
                int position = queue.get(i).getTrackIndex();
                if (((long) position) == media_id) {
                    index = i;
                    break;
                }
            }
            if (index == -1) return false;
            reset();
            this.nowplaying_track = queue.get(index);
            trackChangeUpdate();
            return true;
        };

        public void changeTrackNextInQueue() {
            if (queue.size() < 2) {
                reset();
                return;
            }
            int now_playing_index = getIndex();
            reset();
            if (now_playing_index == queue.size() - 1) this.nowplaying_track = queue.get(0);
            else this.nowplaying_track = queue.get(now_playing_index+1);
            trackChangeUpdate();
        };

        private void swapQueueBroadcast(int position1, int position2) {
            Intent intent = new Intent(MusicApplication.QUEUE_TRACK_SWAP);
            intent.putExtra("position1", position1);
            intent.putExtra("position2", position2);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        };

        public void swapInQueue(int position1, int position2) {
            if (queue == null) return;
            Collections.swap(queue, position1, position2);
            Collections.swap(queueItems, position1, position2);
            updateMediaQueueItems();
            swapQueueBroadcast(position1, position2);
        };

        public void changeTrackPreviousInQueue() {
            if (queue.size() < 2) {
                reset();
                return;
            }
            int now_playing_index = getIndex();
            reset();
            if (now_playing_index == 0) this.nowplaying_track = queue.get(queue.size()-1);
            else this.nowplaying_track = queue.get(now_playing_index-1);
            trackChangeUpdate();
        };

        private void changeNowPlaying() {
            int now_playing_index = getIndex();
            for (int i=0; i<queue.size(); i++) {
                queue.get(i).setNowPlaying(now_playing_index == i);
            }
        };

        public void shuffleQueue() {

            int now_playing_index = getIndex();

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

            queueItems = new ArrayList<>();
            for (int i=0; i<final_list.size(); i++) {
                createAndAddQueueItem(i, final_list.get(i));
            }
            updateMediaQueueItems();

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

        public void setRepeatType(NowPlayingData.REPEAT_TYPE repeatType) {
            this.repeat_type = repeatType;
        };

        public NowPlayingData.REPEAT_TYPE getRepeatType() { return this.repeat_type; };

        public List<MediaSessionCompat.QueueItem> getQueueItems() {
            return queueItems;
        };

    };

    public static class Timer {

        private Context context = null;
        private Integer timeInSecs = null;
        private Handler interval = null;
        private Runnable interval_runnable = null;
        private Handler timeout = null;
        private Runnable timeout_runnable = null;
        private Boolean done = null;
        private Boolean paused = null;

        public Timer(Context context, int time) {
            this.context = context;
            this.timeInSecs = time;
            this.interval = new Handler();
            this.timeout = new Handler();
            this.done = false;
            this.paused = true;
            setup();
        };

        private void setup() {

            interval_runnable = new Runnable() {
                @Override
                public void run() {
                    timeInSecs--;
                    if (timeInSecs > 0) interval.postDelayed(this, 1000);
                };
            };

            timeout_runnable = new Runnable() {
                @Override
                public void run() {

                    try {

                        JSONObject body = new JSONObject();
                        body.put("albumId", MusicForegroundService.NowPlayingData.getInstance(context).getNowPlayingTrack().getAlbumId());

                        APIService.addToRecentlyPlayed(context, body);

                        done = true;

                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }

                };
            };

        };

        public void start() {
            timeout.postDelayed(timeout_runnable, this.timeInSecs * 1000);
            interval.postDelayed(interval_runnable, 1000);
            this.paused = false;
        };

        public void pause() {
            timeout.removeCallbacks(timeout_runnable);
            interval.removeCallbacks(interval_runnable);
            this.paused = true;
        };

        public boolean isPaused() {
            return this.paused;
        };

        public boolean isDone() {
            return this.done;
        };

    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(tag, "on bind");
        return binder;
    };

    @Override
    public void onCreate() {

        Log.d(tag, "background service create");
        super.onCreate();

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MusicApplication.NOTIFICATION_CHANNEL_ID);

        playbackStateBuilder = new PlaybackStateCompat.Builder();

        playbackStateBuilder.setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f);

        playbackStateBuilder.setActions(
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
            PlaybackStateCompat.ACTION_PAUSE |
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
            PlaybackStateCompat.ACTION_PLAY |
            PlaybackStateCompat.ACTION_STOP |
            PlaybackStateCompat.ACTION_SEEK_TO |
            PlaybackStateCompat.ACTION_PREPARE
        );

        playbackStateBuilder.addCustomAction(
            new PlaybackStateCompat.CustomAction.Builder(ACTIONS.REWIND, "rewind", rewind10_button).build()
        );

        playbackStateBuilder.addCustomAction(
                new PlaybackStateCompat.CustomAction.Builder(ACTIONS.FORWARD, "forward", forward10_button).build()
        );

        mediaSessionCallback = new MediaSessionCompat.Callback() {
            @Override
            public void onSkipToQueueItem(long id) {
                super.onSkipToQueueItem(id);
                playFromMediaId(id);
            };
            @Override
            public void onPlay() {
                super.onPlay();
                play_manual();
            };
            @Override
            public void onPause() {
                super.onPause();
                pause_manual();
            };
            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNextTrack();
            };
            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPreviousTrack();
            };
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                seekTo((int) pos);
            };
            @Override
            public void onCustomAction(String action, Bundle extras) {
                super.onCustomAction(action, extras);
                if (action.equals(ACTIONS.REWIND)) rewind();
                else if (action.equals(ACTIONS.FORWARD)) forward();
            };
        };

        nowplayingdata_instance = NowPlayingData.getInstance(getApplicationContext());
        currentTrack = nowplayingdata_instance.getTrack();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mediaSession = new MediaSessionCompat(getApplicationContext(), "now_playing");
//        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        mediaSession.setCallback(mediaSessionCallback);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .build();

        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("open_now_playing", true);
        notificationClick = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);

        play_intent = new Intent(ACTIONS.PLAY);
        play_pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, play_intent, PendingIntent.FLAG_IMMUTABLE);

        pause_intent = new Intent(ACTIONS.PAUSE);
        pause_pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, pause_intent, PendingIntent.FLAG_IMMUTABLE);

        previous_intent = new Intent(ACTIONS.PREVIOUS);
        previous_pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, previous_intent, PendingIntent.FLAG_IMMUTABLE);

        next_intent = new Intent(ACTIONS.NEXT);
        next_pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, next_intent, PendingIntent.FLAG_IMMUTABLE);

        rewind_intent = new Intent(ACTIONS.REWIND);
        rewind_pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, rewind_intent, PendingIntent.FLAG_IMMUTABLE);

        forward_intent = new Intent(ACTIONS.FORWARD);
        forward_pending_intent = PendingIntent.getBroadcast(getApplicationContext(), 0, forward_intent, PendingIntent.FLAG_IMMUTABLE);

        actionReceiver = new ActionBroadcastReceiver();

    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag, "on start command");
        initialize();
        return START_STICKY;
    };

    public void stopPlayback() {
        releaseMediaPlayer();
        mediaSession.setActive(false);
        closeNotification();
        Intent intent = new Intent(MusicApplication.PLAYBACK_STOP);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        NowPlayingData.getInstance(getApplicationContext()).destroyInstance();
        playPauseBroadcast();
        stopForeground(true);
    };

    @Override
    public void onDestroy() {
        Log.d(tag, "service destroy");
        stopService();
        super.onDestroy();
    };

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(tag, "on unbind");
        return super.onUnbind(intent);
    };

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(tag, "on task removed service");
        stopService();
        super.onTaskRemoved(rootIntent);
    };

    private void initialize() {
        currentTrack = NowPlayingData.getInstance(getApplicationContext()).getTrack();
        createNotification();
    };

    private void stopService() {
        Log.d(tag, "stopping service, clearing everything");
        if (mp == null) return;
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.setQueue(null);
            mediaSession = null;
        }
        if (queueItems != null) queueItems = null;
        releaseMediaPlayer();
        closeNotification();
        NowPlayingData.getInstance(getApplicationContext()).destroyInstance();
        stopForeground(true);
        stopSelf();
    };

    private void releaseMediaPlayer() {
        if (mp == null) return;
        mp.setOnPreparedListener(null);
        mp.setOnCompletionListener(null);
        mp.setOnBufferingUpdateListener(null);
        mp.release();
        mp = null;
        getApplicationContext().unregisterReceiver(actionReceiver);
        if (timer != null) {
            timer.pause();
            timer = null;
        }
    };

    private void startMusic() {

        Log.d(tag, "start music");

        releaseMediaPlayer();

        timer = new Timer(getApplicationContext(), 20);

        IntentFilter actionIntentFilter = new IntentFilter();
        actionIntentFilter.addAction(ACTIONS.PLAY);
        actionIntentFilter.addAction(ACTIONS.PAUSE);
        actionIntentFilter.addAction(ACTIONS.NEXT);
        actionIntentFilter.addAction(ACTIONS.PREVIOUS);
        actionIntentFilter.addAction(ACTIONS.FORWARD);
        actionIntentFilter.addAction(ACTIONS.REWIND);

        if (Build.VERSION.SDK_INT >= 33) {
            getApplicationContext().registerReceiver(actionReceiver, actionIntentFilter, RECEIVER_EXPORTED);
        } else {
            getApplicationContext().registerReceiver(actionReceiver, actionIntentFilter);
        }

        mediaSession.setActive(true);

        manually_paused = false;
        mp = new MediaPlayer();
        mp.setAudioAttributes(audioAttributes);

        try {

            mp.setDataSource(currentTrack.getUrl());
            mp.prepareAsync();

//            updateMediaSession(PlaybackStateCompat.STATE_PAUSED);
            updateMediaSession(PlaybackStateCompat.STATE_BUFFERING);

            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    play();
                    nowplayingdata_instance.setMediaPrepared(true);
                    Log.d(tag, "on media prepared");
                }
            });

            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (!nowplayingdata_instance.getMediaPrepared()) return;
                    NowPlayingData.REPEAT_TYPE repeatType = nowplayingdata_instance.getRepeatType();
                    if (repeatType == NowPlayingData.REPEAT_TYPE.NO_REPEAT) {
                        seekTo(0);
                        pause();
                        return;
                    }
                    if (repeatType == NowPlayingData.REPEAT_TYPE.REPEAT_CURRENT) {
                        seekTo(0);
                        play();
                        return;
                    }
                    if (repeatType == NowPlayingData.REPEAT_TYPE.REPEAT_ALL) {
                        NowPlayingData.getInstance(getApplicationContext())
                            .changeTrackNextInQueue();
                        initialize();
                    }
                }
            });

            mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int percentage) {
                    nowplayingdata_instance.setBufferedPercent(percentage);
                    Log.d(tag, "on media buffering " + percentage);
                }
            });

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    };

    private void playFromMediaId(long id) {
        boolean result = NowPlayingData.getInstance(getApplicationContext()).playSpecificTrackInQueueFromMediaId(id);
        if (!result) return;
        playPauseBroadcast();
        playbackStateBuilder.setActiveQueueItemId(id);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
        initialize();
    };

    public void playNextTrack() {
        playPauseBroadcast();
        releaseMediaPlayer();
        NowPlayingData.getInstance(getApplicationContext())
            .changeTrackNextInQueue();
        initialize();
    };

    public void playPreviousTrack() {
        playPauseBroadcast();
        if (mp != null && mp.getCurrentPosition() > 5000) {
            seekTo(0);
            return;
        }
        releaseMediaPlayer();
        NowPlayingData.getInstance(getApplicationContext())
            .changeTrackPreviousInQueue();
        initialize();
    };

    public void playSpecificTrackInQueue(int position) {
        playPauseBroadcast();
        releaseMediaPlayer();
        NowPlayingData.getInstance(getApplicationContext())
            .playSpecificTrackInQueue(position);
        initialize();
    };

    public void rewind() {
        if (mp == null) return;
        if (mp.getCurrentPosition() <= 10000) mp.seekTo(0);
        else mp.seekTo(mp.getCurrentPosition() - 10000);
        play_manual();
    };

    public void forward() {
        if (mp == null) return;
        if (mp.getCurrentPosition() < mp.getDuration() - 10000)
            mp.seekTo(mp.getCurrentPosition() + 10000);
        play_manual();
    };

    private void play() {
        if (mp == null) return;
        if (manually_paused) return;
        int requestResult = audioManager.requestAudioFocus(audioFocusRequest);
        if (
                requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
//                        && !mp.isPlaying()
        ) {
            mp.start();
            nowplayingdata_instance.setMediaIsPlaying(true);
            updateMediaSession(PlaybackStateCompat.STATE_PLAYING);
            updateNotification(true);
            if (!timer.isDone() && timer.isPaused()) timer.start();
            playPauseBroadcast();
        }
    };

    private void pause() {
        if (mp == null) return;
//        if (mp.isPlaying()) {
        mp.pause();
        nowplayingdata_instance.setMediaIsPlaying(false);
        updateMediaSession(PlaybackStateCompat.STATE_PAUSED);
        updateNotification(false);
        if (!timer.isDone() && !timer.isPaused()) timer.pause();
        playPauseBroadcast();
//        }
    };

    public void play_manual() {
        manually_paused = false;
        play();
        updateNotification(true);
    };

    public void pause_manual() {
        pause();
        manually_paused = true;
        updateNotification(false);
    };

    public void stop() {
        if (mp == null) return;
        mp.stop();
        nowplayingdata_instance.setMediaIsPlaying(false);
    };

    public void seekTo(int msec) {
        if (mp == null) return;
        mp.seekTo(msec);
//        updateMediaSession(PlaybackStateCompat.STATE_PLAYING);
    };

    private void playPauseBroadcast() {

        if (mp == null) {
            NowPlayingData.getInstance(getApplicationContext())
                    .setMediaIsPlaying(false);
        }
        else {
            NowPlayingData.getInstance(getApplicationContext())
                    .setMediaIsPlaying(mp.isPlaying());
        }

        Intent intent = new Intent(MusicApplication.PLAY_PAUSE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

    };

    private void createNotification() {

        currentTrack = NowPlayingData.getInstance(getApplicationContext()).getTrack();

        notificationBuilder
            .setSmallIcon(R.drawable.ic_studiomusic_white)
            .setContentIntent(notificationClick)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setOngoing(true)
            .setOnlyAlertOnce(true);

        customNotification(true);

        metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentTrack.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentTrack.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentTrack.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, currentTrack.getArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, convertToMs(currentTrack.getDuration()));

        Glide.with(getApplicationContext())
                .asBitmap()
                .load(currentTrack.getThumbnail())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource);
                        mediaSession.setMetadata(metadataBuilder.build());
                        startForeground(1, notificationBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
                        startMusic();
                    };
                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });

    };

    private int convertToMs(String duration) {
        String[] after_split = duration.split(": ");
        int mins = Integer.parseInt(after_split[0]);
        int secs = Integer.parseInt(after_split[1]);
        int mins_ms = mins * 60 * 1000;
        int secs_ms = secs * 1000;
        return mins_ms + secs_ms;
    };

    private void closeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    };

    public boolean isPlaying() {
        if (mp == null) return false;
        return mp.isPlaying();
    };

    public int getDuration() {
        if (mp == null) return 0;
        return mp.getDuration();
    };

    public int getCurrentPosition() {
        if (mp == null) return 0;
        return mp.getCurrentPosition();
    };

    private void updateMediaSession(int playback_state) {
        if (mp == null) return;
        playbackStateBuilder.setState(playback_state, mp.getCurrentPosition(), 1f);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
    };

    private void updateNotification(boolean isPlaying) {

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), MusicApplication.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_studiomusic_white)
            .setContentIntent(notificationClick)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setOngoing(true)
            .setOnlyAlertOnce(true);

        customNotification(isPlaying);

        notificationManager.notify(1, notificationBuilder.build());

    };

    private void customNotification(boolean isPlaying) {

        boolean isAndroid13orAbove = Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2;

        if (isAndroid13orAbove) {
//        if (false) {

            notificationBuilder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
            );

        }
        else {

            NotificationCompat.Action middle = isPlaying ?
                new NotificationCompat.Action(pause_button, "Pause", pause_pending_intent) :
                new NotificationCompat.Action(play_button, "Play", play_pending_intent);

            notificationBuilder
                .addAction(new NotificationCompat.Action(rewind10_button, "Rewind 10 secs", rewind_pending_intent))
                .addAction(new NotificationCompat.Action(previous_button, "Skip Previous", previous_pending_intent))
                .addAction(middle)
                .addAction(new NotificationCompat.Action(next_button, "Skip Next", next_pending_intent))
                .addAction(new NotificationCompat.Action(forward10_button, "Forward 10 secs", forward_pending_intent));

            notificationBuilder.setStyle(
                new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(1,2,3)
            );

        }

    };

};
