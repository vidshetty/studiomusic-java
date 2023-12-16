package com.app.studiomusic.FragHomescreen;

public interface QuickPicksTouch {
    void click(int position);
    boolean longClick(int position);
    void menuClick(int position, boolean should_vibrate);
};
