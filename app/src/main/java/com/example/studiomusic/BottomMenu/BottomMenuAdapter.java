package com.example.studiomusic.BottomMenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studiomusic.R;

import java.util.List;

public class BottomMenuAdapter extends RecyclerView.Adapter<BottomMenuAdapter.BottomMenuViewHolder> {

    private Context context = null;
    private List<BottomMenu.MenuItem> list = null;

    public BottomMenuAdapter(Context context, List<BottomMenu.MenuItem> list) {
        this.context = context;
        this.list = list;
    };

    @NonNull
    @Override
    public BottomMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_menu_list, parent, false);
        BottomMenuViewHolder viewHolder = new BottomMenuViewHolder(view);
        return viewHolder;
    };

    @Override
    public void onBindViewHolder(@NonNull BottomMenuViewHolder holder, int position) {
        BottomMenu.MenuItem item = list.get(position);
//        holder.icon.setImageResource(item.getIconId());
        holder.name.setText(item.getMenuName());
        if (item.getOnClickListener() != null) holder.whole_view.setOnClickListener(item.getOnClickListener());
    };

    @Override
    public int getItemCount() {
        return list.size();
    };

    public class BottomMenuViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon = null;
        private TextView name = null;
        private View whole_view = null;

        public BottomMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            whole_view = itemView;
//            icon = itemView.findViewById(R.id.menu_item_icon);
            name = itemView.findViewById(R.id.menu_item_name);
        };

    };

};
