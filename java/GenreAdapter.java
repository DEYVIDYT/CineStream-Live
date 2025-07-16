package com.vplay.live;

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

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {
    
    private Context context;
    private List<Category> genres;
    private OnGenreClickListener listener;
    private int selectedPosition = 0;

    public interface OnGenreClickListener {
        void onGenreClick(Category genre);
    }

    public GenreAdapter(Context context, OnGenreClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.genres = new ArrayList<>();
    }

    public void setGenres(List<Category> genres) {
        if (genres == null) {
            this.genres = new ArrayList<>();
        } else {
            // Filtrar gêneros adultos se o conteúdo adulto estiver desabilitado
            if (!ProfileFragment.isAdultContentEnabled(context)) {
                this.genres = new ArrayList<>();
                for (Category genre : genres) {
                    if (!ProfileFragment.isAdultCategory(genre.getCategory_name())) {
                        this.genres.add(genre);
                    }
                }
            } else {
                this.genres = new ArrayList<>(genres);
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Category genre = genres.get(position);
        holder.bind(genre, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return genres.size();
    }

    class GenreViewHolder extends RecyclerView.ViewHolder {
        private TextView genreTextView;

        public GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            genreTextView = itemView.findViewById(R.id.genreTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    setSelectedPosition(position);
                    listener.onGenreClick(genres.get(position));
                }
            });
        }

        public void bind(Category genre, boolean isSelected) {
            genreTextView.setText(genre.getCategory_name());
            
            if (isSelected) {
                genreTextView.setTextColor(ContextCompat.getColor(context, R.color.accent_color));
                genreTextView.setBackgroundResource(R.drawable.rounded_background);
            } else {
                genreTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                genreTextView.setBackground(null);
            }
        }
    }
}