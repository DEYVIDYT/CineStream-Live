package com.cinestream.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {
    
    private Context context;
    private List<Episode> episodes;
    private OnEpisodeClickListener listener;

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode);
    }

    public EpisodeAdapter(Context context, OnEpisodeClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.episodes = new ArrayList<>();
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes != null ? episodes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode episode = episodes.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return episodes.size();
    }

    class EpisodeViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnailImageView;
        private TextView episodeNumberTextView;
        private TextView titleTextView;
        private TextView durationTextView;
        private TextView plotTextView;
        private TextView releaseDateTextView;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            episodeNumberTextView = itemView.findViewById(R.id.episodeNumberTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            plotTextView = itemView.findViewById(R.id.plotTextView);
            releaseDateTextView = itemView.findViewById(R.id.releaseDateTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEpisodeClick(episodes.get(position));
                }
            });
        }

        public void bind(Episode episode) {
            // Episode number with enhanced formatting
            String episodeNumber = episode.getFormattedEpisodeNumber();
            if (!episodeNumber.isEmpty()) {
                episodeNumberTextView.setText(episodeNumber);
                episodeNumberTextView.setVisibility(View.VISIBLE);
            } else if (episode.getEpisode_num() != null && !episode.getEpisode_num().isEmpty()) {
                episodeNumberTextView.setText("E" + episode.getEpisode_num());
                episodeNumberTextView.setVisibility(View.VISIBLE);
            } else {
                episodeNumberTextView.setVisibility(View.GONE);
            }

            // Title with fallback to episode number
            if (episode.getTitle() != null && !episode.getTitle().isEmpty()) {
                titleTextView.setText(episode.getTitle());
                titleTextView.setVisibility(View.VISIBLE);
            } else if (episode.getEpisode_num() != null) {
                titleTextView.setText("Episódio " + episode.getEpisode_num());
            } else {
                titleTextView.setText("Episódio");
            }

            // Duration
            if (episode.getDuration() != null && !episode.getDuration().isEmpty()) {
                durationTextView.setText(episode.getDuration());
                durationTextView.setVisibility(View.VISIBLE);
            } else {
                durationTextView.setVisibility(View.GONE);
            }

            // Plot
            if (episode.getPlot() != null && !episode.getPlot().isEmpty()) {
                plotTextView.setText(episode.getPlot());
                plotTextView.setVisibility(View.VISIBLE);
            } else {
                plotTextView.setVisibility(View.GONE);
            }

            // Release date
            if (episode.getRelease_date() != null && !episode.getRelease_date().isEmpty()) {
                releaseDateTextView.setText(episode.getRelease_date());
                releaseDateTextView.setVisibility(View.VISIBLE);
            } else {
                releaseDateTextView.setVisibility(View.GONE);
            }

            // Thumbnail
            String thumbnailUrl = episode.getMovie_image();
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                Glide.with(context)
                        .load(thumbnailUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                        .placeholder(R.drawable.ic_tv)
                        .error(R.drawable.ic_tv)
                        .into(thumbnailImageView);
            } else {
                thumbnailImageView.setImageResource(R.drawable.ic_tv);
            }
        }
    }
}