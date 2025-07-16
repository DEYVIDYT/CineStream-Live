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

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {
    
    private List<Channel> channels = new ArrayList<>();
    private List<Channel> filteredChannels = new ArrayList<>();
    private OnChannelClickListener listener;
    private FavoritesManager favoritesManager;
    private boolean showEmptyView = false;
    private String emptyMessage = "";
    private List<Category> allCategories = new ArrayList<>();
    private android.content.Context context;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
        void onFavoriteClick(Channel channel, boolean isFavorite);
    }

    public ChannelAdapter(android.content.Context context, OnChannelClickListener listener, FavoritesManager favoritesManager) {
        this.context = context;
        this.listener = listener;
        this.favoritesManager = favoritesManager;
    }

    public void setChannels(List<Channel> channels) {
        this.channels.clear();
        
        // Filtrar canais adultos se o conteúdo adulto estiver desabilitado
        if (!ProfileFragment.isAdultContentEnabled(context)) {
            for (Channel channel : channels) {
                if (!ProfileFragment.isAdultCategory(channel.getName())) {
                    this.channels.add(channel);
                }
            }
        } else {
            this.channels.addAll(channels);
        }
        
        this.filteredChannels.clear();
        this.filteredChannels.addAll(this.channels);
        notifyDataSetChanged();
    }

    public void setCategoriesForFilter(List<Category> categories) {
        if (categories != null) {
            this.allCategories = new ArrayList<>(categories);
        } else {
            this.allCategories.clear();
        }
    }

    public void filterByCategory(String categoryName) {
        filteredChannels.clear();
        showEmptyView = false;

        if (categoryName == null) {
            notifyDataSetChanged();
            return;
        }

        // Verificar se a categoria selecionada é adulta e o filtro está desabilitado
        if (!ProfileFragment.isAdultContentEnabled(context) && 
            ProfileFragment.isAdultCategory(categoryName)) {
            showEmptyView = true;
            emptyMessage = "Conteúdo não disponível - Conteúdo adulto desabilitado";
            notifyDataSetChanged();
            return;
        }

        if (categoryName.equals("TODOS")) {
            filteredChannels.addAll(channels);
        } else if (categoryName.equals("FAVORITOS")) {
            if (favoritesManager != null) {
                List<Channel> favorites = favoritesManager.getFavorites();
                // Filtrar favoritos adultos se necessário
                if (!ProfileFragment.isAdultContentEnabled(context)) {
                    for (Channel channel : favorites) {
                        if (!ProfileFragment.isAdultCategory(channel.getName())) {
                            filteredChannels.add(channel);
                        }
                    }
                } else {
                    filteredChannels.addAll(favorites);
                }
            }
        } else {
            String categoryId = null;
            if (allCategories != null) {
                for (Category cat : allCategories) {
                    if (cat.getCategory_name().equals(categoryName)) {
                        categoryId = cat.getCategory_id();
                        break;
                    }
                }
            }

            if (categoryId != null) {
                for (Channel channel : channels) {
                    if (channel.getCategory_id() != null &&
                        channel.getCategory_id().equals(categoryId)) {
                        filteredChannels.add(channel);
                    }
                }
            }
        }

        if (filteredChannels.isEmpty()) {
            showEmptyView = true;
            if (categoryName.equals("FAVORITOS")) {
                emptyMessage = "Nenhum canal favorito";
            } else {
                emptyMessage = "Nenhum canal encontrado";
            }
        }

        notifyDataSetChanged();
    }
    
    public void filterByHistory(List<Channel> historyChannels) {
        filteredChannels.clear();
        showEmptyView = false;
        
        if (historyChannels.isEmpty()) {
            showEmptyView = true;
            emptyMessage = "Nenhum histórico";
        } else {
            filteredChannels.addAll(historyChannels);
        }
        
        notifyDataSetChanged();
    }
    
    public void filterBySearch(String query) {
        filteredChannels.clear();
        showEmptyView = false;
        
        if (query == null || query.trim().isEmpty()) {
            filteredChannels.addAll(channels);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Channel channel : channels) {
                if (channel.getName() != null && 
                    channel.getName().toLowerCase().contains(lowerQuery)) {
                    filteredChannels.add(channel);
                }
            }
            
            if (filteredChannels.isEmpty()) {
                showEmptyView = true;
                emptyMessage = "Nenhum canal encontrado";
            }
        }
        
        notifyDataSetChanged();
    }
    
    
    @Override
    public int getItemCount() {
        return showEmptyView ? 1 : filteredChannels.size();
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
                    .inflate(R.layout.item_channel, parent, false);
            return new ChannelViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyHolder = (EmptyViewHolder) holder;
            emptyHolder.emptyTitle.setText(emptyMessage);
            if (emptyMessage.equals("Nenhum canal favorito")) {
                emptyHolder.emptyMessage.setText("Você ainda não adicionou canais aos favoritos.\nToque no ❤️ em qualquer canal para adicioná-lo.");
                emptyHolder.emptyIcon.setImageResource(R.drawable.ic_favorite_border);
            } else if (emptyMessage.equals("Nenhum histórico")) {
                emptyHolder.emptyMessage.setText("Você ainda não assistiu nenhum canal.\nO histórico aparecerá aqui conforme você assiste.");
                emptyHolder.emptyIcon.setImageResource(R.drawable.ic_history);
            } else {
                emptyHolder.emptyMessage.setText("Tente uma busca diferente.");
                emptyHolder.emptyIcon.setImageResource(R.drawable.ic_search);
            }
            return;
        }
        
        ChannelViewHolder channelHolder = (ChannelViewHolder) holder;
        Channel channel = filteredChannels.get(position);
        
        channelHolder.channelName.setText(channel.getName() != null ? channel.getName() : "Canal " + (position + 1));
        
        if (channel.getCategory_name() != null && !channel.getCategory_name().isEmpty()) {
            channelHolder.channelDescription.setVisibility(View.VISIBLE);
            channelHolder.channelDescription.setText(channel.getCategory_name());
        } else {
            channelHolder.channelDescription.setVisibility(View.GONE);
        }
        
        if (channel.getStream_icon() != null && !channel.getStream_icon().isEmpty()) {
            Glide.with(channelHolder.itemView.getContext())
                    .load(channel.getStream_icon())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(8))
                            .placeholder(R.drawable.channel_logo_background)
                            .error(R.drawable.channel_logo_background))
                    .into(channelHolder.channelLogo);
        } else {
            channelHolder.channelLogo.setImageResource(R.drawable.channel_logo_background);
        }
        
        // Update favorite icon
        if (favoritesManager != null) {
            boolean isFavorite = favoritesManager.isFavorite(channel);
            if (isFavorite) {
                channelHolder.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                channelHolder.favoriteIcon.setColorFilter(ContextCompat.getColor(channelHolder.itemView.getContext(), R.color.accent_color));
            } else {
                channelHolder.favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
                channelHolder.favoriteIcon.setColorFilter(ContextCompat.getColor(channelHolder.itemView.getContext(), R.color.text_secondary));
            }
            
            channelHolder.favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(channel, isFavorite);
                }
            });
        }
        
        channelHolder.itemView.setOnClickListener(v -> {
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
    
    static class ChannelViewHolder extends ViewHolder {
        ImageView channelLogo;
        TextView channelName;
        TextView channelDescription;
        ImageView favoriteIcon;
        ImageView playIcon;
        
        ChannelViewHolder(View itemView) {
            super(itemView);
            channelLogo = itemView.findViewById(R.id.channelLogo);
            channelName = itemView.findViewById(R.id.channelName);
            channelDescription = itemView.findViewById(R.id.channelDescription);
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