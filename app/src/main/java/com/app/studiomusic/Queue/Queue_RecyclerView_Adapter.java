package com.app.studiomusic.Queue;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studiomusic.Audio_Controller.MusicForegroundService;
import com.app.studiomusic.Common.EqualizerView;
import com.app.studiomusic.MusicData.QueueTrack;
import com.bumptech.glide.Glide;
import com.app.studiomusic.R;

public class Queue_RecyclerView_Adapter extends RecyclerView.Adapter<Queue_RecyclerView_Adapter.QueueViewHolder> {

    private Context context = null;
    private QueueTouch queueTouch = null;

    public Queue_RecyclerView_Adapter(Context context, QueueTouch queueTouch) {
        this.queueTouch = queueTouch;
        this.context = context;
    };

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.queue_activity_list, parent, false);
        QueueViewHolder viewHolder = new QueueViewHolder(view);
        return viewHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {

        QueueTrack track = MusicForegroundService.NowPlayingData.getInstance(context).getQueueTrackList().get(position);

        holder.title.setText(track.getTitle());
        holder.artist.setText(track.getArtist());
        holder.drag.setImageResource(R.drawable.ic_drag_handle);

        if (track.hasLyrics()) holder.lyrics_tag.setVisibility(View.VISIBLE);
        else holder.lyrics_tag.setVisibility(View.GONE);

        if (track.isNowPlaying()) {
            holder.mainView.setBackgroundColor(Color.parseColor("#202020"));
            Glide.with(context)
                    .asBitmap()
                    .load(track.getThumbnail())
                    .into(holder.album_art_1);
            holder.album_art_1.setVisibility(View.VISIBLE);
            holder.equalizerView.setVisibility(View.VISIBLE);
            holder.equalizer_holder.setVisibility(View.VISIBLE);
        }
        else {
            holder.mainView.setBackgroundResource(R.drawable.basic_ripple);
            Glide.with(context)
                .asBitmap()
                .load(track.getThumbnail())
                .into(holder.album_art_1);
            holder.album_art_1.setVisibility(View.VISIBLE);
            holder.equalizerView.setVisibility(View.GONE);
            holder.equalizer_holder.setVisibility(View.GONE);
        }

        if (MusicForegroundService.NowPlayingData.getInstance(context).getMediaIsPlaying()) {
            holder.equalizerView.animateBars();
        } else {
            holder.equalizerView.stopBars();
        }

    };

    @Override
    public int getItemCount() {
        return MusicForegroundService.NowPlayingData.getInstance(context).getQueueTrackList().size();
    };

    public class QueueViewHolder extends RecyclerView.ViewHolder {

        private ImageView album_art_1 = null;
//        private ImageView album_art_2 = null;
        private EqualizerView equalizerView = null;
        private ConstraintLayout equalizer_holder = null;
        private TextView title = null;
        private TextView artist = null;
        private ConstraintLayout mainView = null;
        private ImageView drag = null;
        private LinearLayout lyrics_tag = null;

        public QueueViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = (ConstraintLayout) itemView;
            album_art_1 = itemView.findViewById(R.id.queue_each_albumart_1);
//            album_art_2 = itemView.findViewById(R.id.queue_each_albumart_2);
            equalizerView = itemView.findViewById(R.id.queue_equalizerview);
            equalizer_holder = itemView.findViewById(R.id.equalizer_holder);
            title = itemView.findViewById(R.id.queue_each_title);
            artist = itemView.findViewById(R.id.queue_each_artist);
            drag = itemView.findViewById(R.id.queue_each_drag);
            lyrics_tag = itemView.findViewById(R.id.queue_lyrics_tag);

            drag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    queueTouch.dragTouch(QueueViewHolder.this);
                    return true;
                };
            });
            itemView.setOnClickListener(v -> {
                queueTouch.click(getAdapterPosition());
            });
            itemView.setOnLongClickListener(v -> {
                return queueTouch.longClick(getAdapterPosition());
            });
        };

    };

};
