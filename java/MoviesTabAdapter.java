package com.cinestream.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MoviesTabAdapter extends RecyclerView.Adapter<MoviesTabAdapter.TabViewHolder> {
    
    private Context context;
    private List<String> tabs;
    private OnTabClickListener listener;
    private String selectedTab;

    public interface OnTabClickListener {
        void onTabClick(String tab);
    }

    public MoviesTabAdapter(Context context, OnTabClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.tabs = new ArrayList<>();
    }

    public void setTabs(List<String> tabs) {
        this.tabs = tabs != null ? tabs : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tab, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        String tab = tabs.get(position);
        holder.bind(tab);
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    class TabViewHolder extends RecyclerView.ViewHolder {
        private TextView tabTextView;

        public TabViewHolder(@NonNull View itemView) {
            super(itemView);
            tabTextView = itemView.findViewById(R.id.tabTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onTabClick(tabs.get(position));
                }
            });
        }

        public void bind(String tab) {
            tabTextView.setText(tab);
            
            boolean isSelected = tab.equals(selectedTab);
            if (isSelected) {
                tabTextView.setTextColor(ContextCompat.getColor(context, R.color.accent_color));
                tabTextView.setBackgroundResource(R.drawable.rounded_background);
            } else {
                tabTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                tabTextView.setBackground(null);
            }
        }
    }
}