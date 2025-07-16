package com.cinestream.live;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder> {
    
    private Context context;
    private List<Season> seasons;
    private OnSeasonClickListener listener;
    private int selectedSeasonPosition = 0;

    public interface OnSeasonClickListener {
        void onSeasonClick(Season season, int position);
    }

    public SeasonAdapter(Context context, OnSeasonClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.seasons = new ArrayList<>();
    }

    public void setSeasons(List<Season> seasons) {
        this.seasons = seasons != null ? seasons : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public void setSelectedSeason(int position) {
        int oldPosition = selectedSeasonPosition;
        selectedSeasonPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedSeasonPosition);
    }

    @NonNull
    @Override
    public SeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_season, parent, false);
        return new SeasonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonViewHolder holder, int position) {
        Season season = seasons.get(position);
        holder.bind(season, position == selectedSeasonPosition);
    }

    @Override
    public int getItemCount() {
        return seasons.size();
    }

    class SeasonViewHolder extends RecyclerView.ViewHolder {
        private TextView seasonNameTextView;
        private TextView episodeCountTextView;
        private View selectionIndicator;

        public SeasonViewHolder(@NonNull View itemView) {
            super(itemView);
            seasonNameTextView = itemView.findViewById(R.id.seasonNameTextView);
            episodeCountTextView = itemView.findViewById(R.id.episodeCountTextView);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedSeason(position);
                    listener.onSeasonClick(seasons.get(position), position);
                }
            });
        }

        public void bind(Season season, boolean isSelected) {
            seasonNameTextView.setText(season.getSeasonName());
            
            int episodeCount = season.getTotalEpisodes();
            episodeCountTextView.setText(episodeCount + (episodeCount == 1 ? " episódio" : " episódios"));
            
            // Update selection state
            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.modern_gradient_primary);
                seasonNameTextView.setTextColor(context.getResources().getColor(R.color.white, null));
                episodeCountTextView.setTextColor(context.getResources().getColor(R.color.white, null));
                selectionIndicator.setVisibility(View.VISIBLE);
            } else {
                itemView.setBackgroundResource(R.drawable.modern_input_field);
                seasonNameTextView.setTextColor(context.getResources().getColor(R.color.text_primary, null));
                episodeCountTextView.setTextColor(context.getResources().getColor(R.color.text_secondary, null));
                selectionIndicator.setVisibility(View.GONE);
            }
        }
    }
}