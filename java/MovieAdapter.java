package com.vplay.live;

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

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    
    private Context context;
    private List<Movie> movies;
    private OnMovieClickListener listener;
    private FavoritesManager favoritesManager;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
        void onFavoriteClick(Object item, boolean isFavorite);
    }

    public MovieAdapter(Context context, OnMovieClickListener listener, FavoritesManager favoritesManager) {
        this.context = context;
        this.listener = listener;
        this.favoritesManager = favoritesManager;
        this.movies = new ArrayList<>();
    }

    public void setMovies(List<Movie> movies) {
        if (movies == null) {
            this.movies = new ArrayList<>();
        } else {
            // Filtrar filmes adultos se o conteúdo adulto estiver desabilitado
            if (!ProfileFragment.isAdultContentEnabled(context)) {
                this.movies = new ArrayList<>();
                for (Movie movie : movies) {
                    if (!ProfileFragment.isAdultCategory(movie.getName()) &&
                        !ProfileFragment.isAdultCategory(movie.getGenre())) {
                        this.movies.add(movie);
                    }
                }
            } else {
                this.movies = new ArrayList<>(movies);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;
        private TextView titleTextView;
        private TextView yearTextView;
        private TextView ratingTextView;
        private ImageView favoriteImageView;
        private View ratingContainer;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImageView = itemView.findViewById(R.id.posterImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            yearTextView = itemView.findViewById(R.id.yearTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            favoriteImageView = itemView.findViewById(R.id.favoriteImageView);
            ratingContainer = itemView.findViewById(R.id.ratingContainer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onMovieClick(movies.get(position));
                }
            });

            favoriteImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Movie movie = movies.get(position);
                    boolean isFavorite = favoritesManager.isFavorite(movie.getStream_id());
                    if (isFavorite) {
                        favoritesManager.removeFavorite(movie.getStream_id());
                    } else {
                        favoritesManager.addFavorite(movie);
                    }
                    listener.onFavoriteClick(movie, isFavorite);
                    updateFavoriteIcon(movie);
                }
            });
        }

        public void bind(Movie movie) {
            titleTextView.setText(movie.getName() != null ? movie.getName() : "Sem título");
            
            if (movie.getYear() != null && !movie.getYear().isEmpty()) {
                yearTextView.setText(movie.getYear());
                yearTextView.setVisibility(View.VISIBLE);
            } else {
                yearTextView.setVisibility(View.GONE);
            }

            // Rating
            double rating = movie.getRatingValue();
            if (rating > 0) {
                ratingTextView.setText(String.format("%.1f", rating));
                ratingContainer.setVisibility(View.VISIBLE);
            } else {
                ratingContainer.setVisibility(View.GONE);
            }

            // Load poster image
            String posterUrl = movie.getStream_icon();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Glide.with(context)
                        .load(posterUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                        .placeholder(R.drawable.ic_tv) // Use existing placeholder
                        .error(R.drawable.ic_tv)
                        .into(posterImageView);
            } else {
                posterImageView.setImageResource(R.drawable.ic_tv);
            }

            updateFavoriteIcon(movie);
        }

        private void updateFavoriteIcon(Movie movie) {
            boolean isFavorite = favoritesManager.isFavorite(movie.getStream_id());
            favoriteImageView.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }
}