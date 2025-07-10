package com.example.cinestreamlive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cinestreamlive.model.Channel;
import com.example.cinestreamlive.model.EpgEvent;
// import com.bumptech.glide.Glide;

import java.util.List;

public class EpgChannelAdapter extends RecyclerView.Adapter<EpgChannelAdapter.EpgChannelViewHolder> {

    private Context context;
    private List<Channel> channelList;
    private OnEpgChannelClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnEpgChannelClickListener {
        void onEpgChannelClick(Channel channel);
    }

    public EpgChannelAdapter(Context context, List<Channel> channelList, OnEpgChannelClickListener listener) {
        this.context = context;
        this.channelList = channelList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EpgChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.epg_channel_item, parent, false);
        return new EpgChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpgChannelViewHolder holder, int position) {
        Channel channel = channelList.get(position);

        holder.channelNumber.setText(String.valueOf(channel.getNum()));
        holder.channelName.setText(channel.getName());

        EpgEvent currentEvent = channel.getCurrentEpgEvent();
        if (currentEvent != null) {
            holder.channelProgramName.setText(currentEvent.getTitle());
            holder.channelProgramName.setVisibility(View.VISIBLE);

            long currentTimeSec = System.currentTimeMillis() / 1000;
            long startTimeSec = currentEvent.getStartEpochSeconds();
            long endTimeSec = currentEvent.getEndEpochSeconds();

            if (endTimeSec > startTimeSec) { // Evitar divisão por zero e progresso inválido
                int progress = (int) (((currentTimeSec - startTimeSec) * 100) / (endTimeSec - startTimeSec));
                holder.channelProgramProgress.setProgress(Math.max(0, Math.min(progress, 100))); // Garante 0-100
                holder.channelProgramProgress.setVisibility(View.VISIBLE);
            } else {
                holder.channelProgramProgress.setVisibility(View.GONE);
            }
        } else {
            holder.channelProgramName.setText(R.string.epg_data_unavailable); // Usar string resource
            holder.channelProgramProgress.setVisibility(View.GONE);
        }

        // Carregar logo do canal (ex: com Glide)
        // if (channel.getStreamIcon() != null && !channel.getStreamIcon().isEmpty()) {
        //     // Glide.with(context).load(channel.getStreamIcon()).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_gallery).into(holder.channelLogo);
        // } else {
             holder.channelLogo.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder padrão do Android
        // }

        holder.channelFavoriteIcon.setVisibility(View.GONE);

        holder.itemView.setSelected(selectedPosition == position);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEpgChannelClick(channel);
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
        // Não resetar selectedPosition aqui, pois o foco pode ser gerenciado pela PlayerActivity
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousSelectedPosition = selectedPosition;
        selectedPosition = position;
        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition);
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition);
        }
    }

    public Channel getChannelAt(int position) {
        if (position >= 0 && position < channelList.size()) {
            return channelList.get(position);
        }
        return null;
    }

    static class EpgChannelViewHolder extends RecyclerView.ViewHolder {
        ImageView channelLogo;
        TextView channelNumber;
        TextView channelName;
        TextView channelProgramName;
        ProgressBar channelProgramProgress;
        ImageView channelFavoriteIcon;

        EpgChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            channelLogo = itemView.findViewById(R.id.channel_epg_logo);
            channelNumber = itemView.findViewById(R.id.channel_epg_number);
            channelName = itemView.findViewById(R.id.channel_epg_name);
            channelProgramName = itemView.findViewById(R.id.channel_epg_program_name);
            channelProgramProgress = itemView.findViewById(R.id.channel_epg_program_progress);
            channelFavoriteIcon = itemView.findViewById(R.id.channel_epg_favorite_icon);
        }
    }
}
