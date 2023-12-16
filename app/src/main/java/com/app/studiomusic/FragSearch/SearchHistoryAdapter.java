package com.app.studiomusic.FragSearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studiomusic.R;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHolder> {

    private Context context = null;
    private List<String> searches = null;
    private SearchHistoryTouch searchHistoryTouch = null;

    public SearchHistoryAdapter(Context context, List<String> searches, SearchHistoryTouch searchHistoryTouch) {
        this.context = context;
        this.searches = searches;
        this.searchHistoryTouch = searchHistoryTouch;
    };

    @NonNull
    @Override
    public SearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_history_list, parent, false);
        SearchHolder holder = new SearchHolder(view);
        return holder;
    };

    @Override
    public void onBindViewHolder(@NonNull SearchHolder holder, int position) {
        String text = searches.get(position);
        holder.textView.setText(text);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.textView.getLayoutParams();
        if (position == 0) {
            params.topMargin = 50;
            holder.textView.setLayoutParams(params);
        }
        if (position == searches.size()-1) {
            params.bottomMargin = 50;
            holder.textView.setLayoutParams(params);
        }
    };

    @Override
    public int getItemCount() {
        return searches.size();
    };

    public class SearchHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout textview_container = null;
        private TextView textView = null;

        public SearchHolder(@NonNull View itemView) {
            super(itemView);
            textview_container = itemView.findViewById(R.id.search_history_text_container);
            textView = itemView.findViewById(R.id.search_history_text);
            textview_container.setOnClickListener(v -> {
                searchHistoryTouch.click(getAdapterPosition());
            });
            textview_container.setOnLongClickListener(v -> {
                return searchHistoryTouch.longClick(getAdapterPosition());
            });
        };

    };

};
