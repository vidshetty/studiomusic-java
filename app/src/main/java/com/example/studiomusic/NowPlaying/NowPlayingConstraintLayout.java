package com.example.studiomusic.NowPlaying;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.ConstraintLayout;

public class NowPlayingConstraintLayout extends ConstraintLayout {

    private float percentage = 0.5f;

    public NowPlayingConstraintLayout(Context context) {
        super(context);
    };

    public NowPlayingConstraintLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    };

    public NowPlayingConstraintLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int desiredHeight = (int) (MeasureSpec.getSize(heightMeasureSpec) * percentage);
        int finalHeightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, finalHeightMeasureSpec);
    };

    public void setPercentage(float percentage) {
        this.percentage = percentage;
        requestLayout();
    };

};