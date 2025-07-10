package com.example.cinestreamlive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
// import android.widget.ImageView; // Para o logo, se usarmos
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinestreamlive.model.Channel;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private Context context;
    private List<Channel> channelList;
    private OnChannelClickListener onChannelClickListener;

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel);
    }

    public ChannelAdapter(Context context, List<Channel> channelList, OnChannelClickListener onChannelClickListener) {
        this.context = context;
        this.channelList = channelList;
        this.onChannelClickListener = onChannelClickListener;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channelList.get(position);
        holder.channelName.setText(channel.getName());

        // Aqui você poderia carregar o logo do canal usando uma biblioteca como Glide ou Picasso
        // if (channel.getStreamIcon() != null && !channel.getStreamIcon().isEmpty()) {
        //     Glide.with(context).load(channel.getStreamIcon()).into(holder.channelLogo);
        // } else {
        //     holder.channelLogo.setImageResource(R.mipmap.ic_launcher); // Placeholder
        // }

        holder.itemView.setOnClickListener(v -> {
            if (onChannelClickListener != null) {
                onChannelClickListener.onChannelClick(channel);
            }
        });
    }

    @Override
    public int getItemCount() {
        return channelList == null ? 0 : channelList.size();
    }

    public void updateChannels(List<Channel> newChannels) {
        this.channelList.clear();
        if (newChannels != null) {
            this.channelList.addAll(newChannels);
        }
        notifyDataSetChanged(); //  Para simplicidade. Em apps reais, DiffUtil é melhor.
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        TextView channelName;
        // ImageView channelLogo; // Para o logo

        ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            channelName = itemView.findViewById(R.id.channel_name);
            // channelLogo = itemView.findViewById(R.id.channel_logo); // Descomentar se o ImageView for adicionado em item_channel.xml
        }
    }
}
