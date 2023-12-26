package com.app.studiomusic.AppUpdates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studiomusic.BottomMenu.BottomMenu;
import com.app.studiomusic.BottomMenu.BottomMenuAdapter;
import com.app.studiomusic.BottomMenu.BottomMenuTouch;
import com.app.studiomusic.R;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.List;
import java.util.Objects;

class UpdateBottomMenu {

    static class Builder {

        private Context context = null;
        private String file_path = null;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        };

        public Builder setFilePath(String path) {
            this.file_path = path;
            return this;
        };

        public UpdateBottomMenu build() {
            return new UpdateBottomMenu(this);
        };

    };

    private BottomSheetDialog dialog = null;
    private Context context = null;
    private String file_path = null;
    private ConstraintLayout install_button = null;

    private final View.OnClickListener onClickListener = (v) -> {

        if (dialog == null) return;

        if (context == null) {
            dialog.cancel();
            return;
        }
        if (file_path == null) {
            dialog.cancel();
            return;
        }

        try {

            File file = new File(file_path);
            if (!file.exists()) return;

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);

        }
        catch(Exception e) {
            e.printStackTrace();
            return;
        }

    };

    private UpdateBottomMenu(Builder builder) {

        this.context = builder.context;
        this.dialog = new BottomSheetDialog(context);
        this.file_path = builder.file_path;

        dialog.setContentView(R.layout.update_bottommenu);
        Objects.requireNonNull(dialog.getWindow()).setNavigationBarColor(context.getColor(R.color.light_black));

        install_button = dialog.findViewById(R.id.updatesheet_install_button);
        if (install_button != null) {
            install_button.setOnClickListener(onClickListener);
        }

    };

    public void show() {
        dialog.cancel();
        dialog.show();
    };

};
