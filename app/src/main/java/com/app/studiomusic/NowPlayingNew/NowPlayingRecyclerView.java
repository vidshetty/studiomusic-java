package com.app.studiomusic.NowPlayingNew;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class NowPlayingRecyclerView extends RecyclerView {

    private boolean isScrollEnabled = true;

    public NowPlayingRecyclerView(@NonNull Context context) {
        super(context);
    };

    public NowPlayingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    };

    public NowPlayingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    };

    public void setScrollEnabled(boolean scrollEnabled) {
        isScrollEnabled = scrollEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return isScrollEnabled && super.onTouchEvent(e);
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return isScrollEnabled && super.onInterceptTouchEvent(e);
    };

};
