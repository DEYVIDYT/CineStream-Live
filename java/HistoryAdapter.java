package com.cinestream.live;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    
    private List<HistoryManager.HistoryItem> historyItems = new ArrayList<>();
    private OnHistoryItemClickListener listener;
    private FavoritesManager favoritesManager;
    private boolean showEmptyView = false;
    private android.content.Context context;
    
    public interface OnHistoryItemClickListener {
        void onChannelClick(Channel channel);
        void onFavoriteClick(Channel channel, boolean isFavorite);
    }
    
    public HistoryAdapter(android.content.Context context, OnHistoryItemClickListener listener, FavoritesManager favoritesManager) {
        this.context = context;
        this.listener = listener;
        this.favoritesManager = favoritesManager;
    }
    
    public void setHistoryItems(List<HistoryManager.HistoryItem> items) {
        this.historyItems.clear();
        this.historyItems.addAll(items);
        this.showEmptyView = items.isEmpty();
        notifyDataSetChanged();
    }
    
    @Override
    public int getItemCount() {
        return showEmptyView ? 1 : historyItems.size();
    }
    
    @Override
    public int getItemViewType(int position) {
        return showEmptyView ? 1 : 0;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.empty_view, parent, false);
            return new EmptyViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyHolder = (EmptyViewHolder) holder;
            emptyHolder.emptyTitle.setText("Nenhum histórico");
            emptyHolder.emptyMessage.setText("Você ainda não assistiu nenhum canal.\nO histórico aparecerá aqui conforme você assiste.");
            emptyHolder.emptyIcon.setImageResource(R.drawable.ic_history);
            return;
        }
        
        HistoryViewHolder historyHolder = (HistoryViewHolder) holder;
        HistoryManager.HistoryItem item = historyItems.get(position);
        Channel channel = item.getChannel();
        
        historyHolder.channelName.setText(channel.getName() != null ? channel.getName() : "Canal");
        historyHolder.watchTime.setText(item.getFormattedTime());
        
        if (channel.getCategory_name() != null && !channel.getCategory_name().isEmpty()) {
            historyHolder.channelDescription.setVisibility(View.VISIBLE);
            historyHolder.channelDescription.setText(channel.getCategory_name());
        } else {
            historyHolder.channelDescription.setVisibility(View.GONE);
        }
        
        if (channel.getStream_icon() != null && !channel.getStream_icon().isEmpty()) {
            Glide.with(historyHolder.itemView.getContext())
                    .load(channel.getStream_icon())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(8))
                            .placeholder(R.drawable.channel_logo_background)
                            .error(R.drawable.channel_logo_background))
                    .into(historyHolder.channelLogo);
        } else {
            historyHolder.channelLogo.setImageResource(R.drawable.channel_logo_background);
        }
        
        // Update favorite icon
        if (favoritesManager != null) {
            boolean isFavorite = favoritesManager.isFavorite(channel);
            if (isFavorite) {
                historyHolder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                historyHolder.favoriteIcon.setColorFilter(ContextCompat.getColor(historyHolder.itemView.getContext(), R.color.accent_color));
            } else {
                historyHolder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
                historyHolder.favoriteIcon.setColorFilter(ContextCompat.getColor(historyHolder.itemView.getContext(), R.color.text_secondary));
            }
            
            historyHolder.favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(channel, isFavorite);
                }
            });
        }
        
        historyHolder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChannelClick(channel);
            }
        });
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }
    }
    
    static class HistoryViewHolder extends ViewHolder {
        ImageView channelLogo;
        TextView channelName;
        TextView channelDescription;
        TextView watchTime;
        ImageView favoriteIcon;
        ImageView playIcon;
        
        HistoryViewHolder(View itemView) {
            super(itemView);
            channelLogo = itemView.findViewById(R.id.channelLogo);
            channelName = itemView.findViewById(R.id.channelName);
            channelDescription = itemView.findViewById(R.id.channelDescription);
            watchTime = itemView.findViewById(R.id.watchTime);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            playIcon = itemView.findViewById(R.id.playIcon);
        }
    }
    
    static class EmptyViewHolder extends ViewHolder {
        ImageView emptyIcon;
        TextView emptyTitle;
        TextView emptyMessage;
        
        EmptyViewHolder(View itemView) {
            super(itemView);
            emptyIcon = itemView.findViewById(R.id.emptyIcon);
            emptyTitle = itemView.findViewById(R.id.emptyTitle);
            emptyMessage = itemView.findViewById(R.id.emptyMessage);
        }
    }
}