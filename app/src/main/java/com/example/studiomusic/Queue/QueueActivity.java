package com.example.studiomusic.Queue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.studiomusic.Audio_Controller.MusicApplication;
import com.example.studiomusic.Audio_Controller.MusicForegroundService;
import com.example.studiomusic.Audio_Controller.MusicForegroundService.NowPlayingData;
import com.example.studiomusic.Audio_Controller.MusicServiceNew;
import com.example.studiomusic.BottomMenu.BottomMenu;
import com.example.studiomusic.Common.Common;
import com.example.studiomusic.FragLibrary.LibraryTouch;
import com.example.studiomusic.Main.MainActivity;
import com.example.studiomusic.MusicData.QueueTrack;
import com.example.studiomusic.MusicData.Track;
import com.example.studiomusic.NowPlaying.NowPlaying;
import com.example.studiomusic.R;

import java.util.Arrays;
import java.util.List;

public class QueueActivity extends AppCompatActivity {

    private static final String tag = "queue_log";

    private ImageView queue_down = null;
    private ProgressBar queue_progressbar = null;
    private ImageView queue_previous_icon = null;
    private ImageView queue_next_icon = null;
    private ImageView queue_playbutton = null;
    private ImageView queue_pausebutton = null;
    private RecyclerView queue_recyclerview = null;
    private Queue_RecyclerView_Adapter queue_adapter = null;
    private RecyclerView.SmoothScroller smoothScroller_firstTime = null;
    private RecyclerView.SmoothScroller smoothScroller = null;
    private LinearLayoutManager linearLayoutManager = null;

    private Handler progressHandler = null;
    private Runnable progressRunnable = null;

    private boolean queue_progressbar_max_set = false;
    private boolean initialPlayPauseButtonSet = false;
    private int previous_now_playing_index = -1;

    private ServiceConnection serviceConnection = null;
    private QueueBroadcastReceiver receiver = null;
    private QueuePlayPauseBroadcastReceiver playPauseReceiver = null;
    private MusicForegroundService music_service = null;
    private ItemTouchHelper itemTouchHelper = null;

    private class QueueBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleQueuePlayer();
        };
    };

    private class QueuePlayPauseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleQueuePlayPause();
        };
    };

    private final QueueTouch queueTouch = new QueueTouch() {
        @Override
        public void click(int position) {
            setTrack(position);
        };
        @Override
        public boolean longClick(int position) {
            openTrackMenu(position);
            return true;
        };
        @Override
        public void dragTouch(Queue_RecyclerView_Adapter.QueueViewHolder holder) {
            if (itemTouchHelper == null) return;
            itemTouchHelper.startDrag(holder);
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_queue);
        overridePendingTransition(R.anim.nowplaying_open, R.anim.previous_stay);

        initialise();
        createView();

    };

    private void initialise() {

        NowPlayingData.getInstance(this);

        IntentFilter intentFilter = new IntentFilter(MusicApplication.TRACK_CHANGE);
        if (receiver == null) receiver = new QueueBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        IntentFilter playPauseFilter = new IntentFilter(MusicApplication.PLAY_PAUSE);
        if (playPauseReceiver == null) playPauseReceiver = new QueuePlayPauseBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(playPauseReceiver, playPauseFilter);

        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    music_service = ((MusicForegroundService.MusicForegroundServiceBinder) iBinder).getService();
                };
                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    music_service = null;
                };
            };
        };

        MusicServiceNew.bind(this, serviceConnection);

        queue_progressbar_max_set = false;
        initialPlayPauseButtonSet = false;
        previous_now_playing_index = NowPlayingData.getInstance(this).getIndex();

        if (queue_progressbar == null) queue_progressbar = findViewById(R.id.queue_progressbar);
        if (queue_playbutton == null) queue_playbutton = findViewById(R.id.queue_playbutton);
        if (queue_pausebutton == null) queue_pausebutton = findViewById(R.id.queue_pausebutton);

        queue_pausebutton.setVisibility(View.VISIBLE);
        queue_playbutton.setVisibility(View.GONE);

        runProgressHandler();

    };

    private void createView() {

        if (itemTouchHelper == null) {
            itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.Callback() {
                    @Override
                    public boolean isLongPressDragEnabled() {
                        return false;
                    };
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        int drag_flags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        int swipe_flags = ItemTouchHelper.END;
                        return makeMovementFlags(drag_flags, swipe_flags);
                    };
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        int from = viewHolder.getAdapterPosition();
                        int to = target.getAdapterPosition();
                        NowPlayingData.getInstance(getApplicationContext()).swapInQueue(from, to);
                        queue_adapter.notifyItemMoved(from, to);
                        return true;
                    };
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
                            removeQueueTrack(viewHolder.getAdapterPosition());
                        }
                    };
                }
            );
        }

        if (smoothScroller_firstTime == null) {
            smoothScroller_firstTime = new LinearSmoothScroller(this) {
                @Override
                protected int calculateTimeForScrolling(int dx) {
                    return super.calculateTimeForScrolling(dx) * 2;
                };
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                };
            };
        }

        if (smoothScroller == null) {
            smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int calculateTimeForScrolling(int dx) {
                    return super.calculateTimeForScrolling(dx) * 10;
                };
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                };
            };
        }

        if (queue_recyclerview == null) queue_recyclerview = findViewById(R.id.queue_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        queue_recyclerview.setLayoutManager(linearLayoutManager);
        queue_adapter = new Queue_RecyclerView_Adapter(this, queueTouch);
        queue_recyclerview.setAdapter(queue_adapter);
        scrollToRecyclerViewFirstTime();
        itemTouchHelper.attachToRecyclerView(queue_recyclerview);

        if (queue_down == null) queue_down = findViewById(R.id.queue_down);
        queue_down.setOnClickListener(v -> {
            closeQueue(-1, null);
        });

        if (queue_playbutton == null) queue_playbutton = findViewById(R.id.queue_playbutton);
        queue_playbutton.setOnClickListener(v -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            if (!music_service.isPlaying()) music_service.play_manual();
            queue_playbutton.setVisibility(View.GONE);
            queue_pausebutton.setVisibility(View.VISIBLE);
        });

        if (queue_pausebutton == null) queue_pausebutton = findViewById(R.id.queue_pausebutton);
        queue_pausebutton.setOnClickListener(v -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
            music_service.pause_manual();
            queue_pausebutton.setVisibility(View.GONE);
            queue_playbutton.setVisibility(View.VISIBLE);
        });

        if (queue_previous_icon == null) queue_previous_icon = findViewById(R.id.queue_previous_icon);
        queue_previous_icon.setOnClickListener(view -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
//            int before = NowPlayingData.getInstance(this).getIndex();
            music_service.playPreviousTrack();
            change_track();
//            scrollToRecyclerView();
//            int after = NowPlayingData.getInstance(this).getIndex();
//            queue_adapter.notifyItemChanged(before);
//            queue_adapter.notifyItemChanged(after);
        });

        if (queue_next_icon == null) queue_next_icon = findViewById(R.id.queue_next_icon);
        queue_next_icon.setOnClickListener(view -> {
            Common.vibrate(this, 100);
            if (music_service == null) return;
//            int before = NowPlayingData.getInstance(this).getIndex();
            music_service.playNextTrack();
            change_track();
//            scrollToRecyclerView();
//            int after = NowPlayingData.getInstance(this).getIndex();
//            queue_adapter.notifyItemChanged(before);
//            queue_adapter.notifyItemChanged(after);
        });

    };

    private void change_track() {
        if (queue_adapter == null) return;
        int now_playing_index = NowPlayingData.getInstance(this).getIndex();
        queue_adapter.notifyItemChanged(previous_now_playing_index);
        queue_adapter.notifyItemChanged(now_playing_index);
        previous_now_playing_index = now_playing_index;
    };

    private void removeQueueTrack(int position) {

        List<QueueTrack> queue = NowPlayingData.getInstance(this).getQueueTrackList();
        Track track = queue.get(position).getTrack();
        Toast toast = Toast.makeText(this, "Removed " + track.getTitle() + " from queue!", Toast.LENGTH_SHORT);

        if (queue == null) return;

        if (queue.size() == 1) {
//            toast.show();
            if (music_service != null) music_service.stopPlayback();
            NowPlayingData.getInstance(this).removeQueueTrack(position);
            closeQueue(2, null);
//            startActivity(new Intent(this, MainActivity.class));
//            finishAffinity();
            return;
        }

        int now_playing_index = NowPlayingData.getInstance(this).getIndex();

        if (position == now_playing_index) {
            if (music_service != null) music_service.playNextTrack();
            NowPlayingData.getInstance(this).removeQueueTrack(position);
            previous_now_playing_index = NowPlayingData.getInstance(this).getIndex();
            if (queue_adapter != null) {
                queue_adapter.notifyItemRemoved(position);
//                queue_adapter.notifyItemChanged(position);
//                List<QueueTrack> new_queue = NowPlayingData.getInstance(this).getQueueTrackList();
//                for (int i=0; i<new_queue.size(); i++) queue_adapter.notifyItemChanged(i);
            }
            return;
        }

        NowPlayingData.getInstance(this).removeQueueTrack(position);
        if (queue_adapter != null) {
            queue_adapter.notifyItemRemoved(position);
        }

    };

    private void scrollToRecyclerViewFirstTime() {
        linearLayoutManager.scrollToPosition(Math.max(NowPlayingData.getInstance(this).getIndex() - 2, 0));
//        if (smoothScroller_firstTime == null || linearLayoutManager == null) return;
//        smoothScroller_firstTime.setTargetPosition(Math.max(NowPlayingData.getInstance(this).getIndex() - 2, 0));
//        linearLayoutManager.startSmoothScroll(smoothScroller_firstTime);
    };

    private void scrollToRecyclerView() {

//        int target_position = NowPlayingData.getInstance(this).getIndex();
//        int target_offset = (int) (Common.convertDpToPixel(this, 60) * target_position);
//        int extra_offset = (int) (Common.convertDpToPixel(this, 60) * 2);
//        queue_recyclerview.scrollBy(0, target_offset - extra_offset);
//        queue_recyclerview.smoothScrollToPosition(target_position - 2);

//        int target_position = NowPlayingData.getInstance(this).getIndex();
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        linearLayoutManager.scrollToPositionWithOffset(target_position, Common.convertDpToPixel(this, 60) * 2);

        if (smoothScroller == null || linearLayoutManager == null) return;
        smoothScroller.setTargetPosition(Math.max(NowPlayingData.getInstance(this).getIndex() - 2, 0));
        linearLayoutManager.startSmoothScroll(smoothScroller);

    };

    private void handleQueuePlayer() {
        if (queue_progressbar == null) return;
        if (queue_playbutton == null) return;
        if (queue_pausebutton == null) return;
        if (queue_adapter == null) return;
        queue_progressbar.setMax(0);
        queue_progressbar.setProgress(0);
        queue_progressbar_max_set = false;
        initialPlayPauseButtonSet = false;
        queue_playbutton.setVisibility(View.GONE);
        queue_pausebutton.setVisibility(View.VISIBLE);
        change_track();
    };

    private void handleQueuePlayPause() {
        List<QueueTrack> queue = NowPlayingData.getInstance(this).getQueueTrackList();
        if (queue == null || queue_adapter == null) return;
        int now_playing_index = NowPlayingData.getInstance(this).getIndex();
        queue_adapter.notifyItemChanged(now_playing_index);
    };

    private void runProgressHandler() {

        progressHandler = new Handler();

        progressRunnable = new Runnable() {
            @Override
            public void run() {

                NowPlayingData currentTrack = NowPlayingData.getInstance(getApplicationContext());

                // initial play/pause button set
                if (!initialPlayPauseButtonSet && currentTrack.getMediaPrepared()) {
                    queue_playbutton.setVisibility(View.GONE);
                    queue_pausebutton.setVisibility(View.VISIBLE);
                    initialPlayPauseButtonSet = true;
                }

                // set duration to seekbar
                if (
                        queue_progressbar != null && music_service != null &&
                        !queue_progressbar_max_set && currentTrack.getMediaPrepared()
                ) {
                    queue_progressbar.setMax(music_service.getDuration());
                    queue_progressbar_max_set = true;
                }

                // set current position to seekbar
                if (
                        queue_progressbar != null &&
                        music_service != null
                ) {
                    queue_progressbar.setProgress(music_service.getCurrentPosition());
                }

                // check which button to show
                if (
                        music_service != null &&
                        music_service.isPlaying() &&
                        queue_pausebutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    queue_playbutton.setVisibility(View.GONE);
                    queue_pausebutton.setVisibility(View.VISIBLE);
                }

                // check which button to show
                if (
                        music_service != null &&
                        !music_service.isPlaying() &&
                        queue_playbutton.getVisibility() == View.GONE &&
                        initialPlayPauseButtonSet
                ) {
                    queue_pausebutton.setVisibility(View.GONE);
                    queue_playbutton.setVisibility(View.VISIBLE);
                }

                progressHandler.postDelayed(this, 100);

            };
        };

        progressHandler.postDelayed(progressRunnable, 100);

    };

    private void cleanUp() {
        if (progressHandler != null && progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressHandler = null;
            progressRunnable = null;
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playPauseReceiver);
    };

    @Override
    public void onBackPressed() {
        closeQueue(-1, null);
        super.onBackPressed();
    };

    private void setTrack(int position) {
        if (music_service == null) return;
        int now_playing_index = NowPlayingData.getInstance(this).getIndex();
        if (now_playing_index == position) return;
        new Handler().postDelayed(() -> {
            music_service.playSpecificTrackInQueue(position);
            change_track();
//            queue_adapter.notifyItemChanged(now_playing_index);
//            queue_adapter.notifyItemChanged(position);
        }, 100);
    };

    private void openTrackMenu(int position) {

        Track track = NowPlayingData.getInstance(this)
                .getQueueTrackList().get(position).getTrack();

        View.OnClickListener share = view -> {
//            Common.vibrate(this, 100);
            Intent shareIntent = Common.shareTrack(track);
            if (shareIntent == null) return;
            startActivity(shareIntent);
        };

        View.OnClickListener goToHome = view -> {
            Intent intent = new Intent();
            intent.putExtra("album_id", track.getAlbumId());
            closeQueue(1, intent);
        };

        List<BottomMenu.MenuItem> menu_list = Arrays.asList(
                new BottomMenu.MenuItem(null, "Go To Album", goToHome),
                new BottomMenu.MenuItem(null, "Share Track", share)
        );

        BottomMenu bottomMenu_new = new BottomMenu.Builder()
            .setContext(this)
            .setDataList(menu_list)
            .setName(track.getTitle())
            .setArtist(track.getArtist())
            .setThumbnail(track.getThumbnail())
            .build();

        bottomMenu_new.show();

    };

    private void closeQueue(int res_code, Intent intent) {
        cleanUp();
        setResult(res_code, intent);
        finish();
        overridePendingTransition(R.anim.previous_stay, R.anim.nowplaying_close);
    };

    @Override
    protected void onDestroy() {
        cleanUp();
        super.onDestroy();
    };
};