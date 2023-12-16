package com.app.studiomusic.Common;

import android.content.Context;
import android.view.View;

import com.app.studiomusic.R;
import com.google.android.material.snackbar.Snackbar;

public class SnackBar {

    private static View main_activity = null;

    public static void setMainActivityView(View view) {
        main_activity = view;
    };

    public static View getMainActivityView() {
        return main_activity;
    };

    public static void make(Context context, View view, String text, int duration) {
        Snackbar bar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT)
            .setDuration(duration)
            .setBackgroundTint(context.getColor(R.color.white))
            .setTextColor(context.getColor(R.color.black))
            .setAnchorView(SnackBar.getMainActivityView());
        bar.setAction("Clear", v -> {
            bar.dismiss();
        });
        bar.show();
    };

};
