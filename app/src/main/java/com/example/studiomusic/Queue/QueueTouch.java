package com.example.studiomusic.Queue;

public interface QueueTouch {
    void click(int position);
    boolean longClick(int position);
    void dragTouch(Queue_RecyclerView_Adapter.QueueViewHolder holder);
};
