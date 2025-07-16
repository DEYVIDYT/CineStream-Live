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

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder> {
    
    private Context context;
    private List<Series> series;
    private OnSeriesClickListener listener;
    private FavoritesManager favoritesManager;

    public interface OnSeriesClickListener {
        void onSeriesClick(Series series);
        void onFavoriteClick(Object item, boolean isFavorite);
    }

    public SeriesAdapter(Context context, OnSeriesClickListener listener, FavoritesManager favoritesManager) {
        this.context = context;
        this.listener = listener;
        this.favoritesManager = favoritesManager;
        this.series = new ArrayList<>();
    }

    public void setSeries(List<Series> series) {
        if (series == null) {
            this.series = new ArrayList<>();
        } else {
            // Filtrar séries adultas se o conteúdo adulto estiver desabilitado
            if (!ProfileFragment.isAdultContentEnabled(context)) {
                this.series = new ArrayList<>();
                for (Series seriesItem : series) {
                    if (!ProfileFragment.isAdultCategory(seriesItem.getName()) &&
                        !ProfileFragment.isAdultCategory(seriesItem.getGenre())) {
                        this.series.add(seriesItem);
                    }
                }
            } else {
                this.series = new ArrayList<>(series);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_series, parent, false);
        return new SeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        Series seriesItem = series.get(position);
        holder.bind(seriesItem);
    }

    @Override
    public int getItemCount() {
        return series.size();
    }

    class SeriesViewHolder extends RecyclerView.ViewHolder {
        private ImageView posterImageView;
        private TextView titleTextView;
        private TextView yearTextView;
        private TextView ratingTextView;
        private ImageView favoriteImageView;
        private View ratingContainer;

        public SeriesViewHolder(@NonNull View itemView) {
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
                    listener.onSeriesClick(series.get(position));
                }
            });

            favoriteImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Series seriesItem = series.get(position);
                    boolean isFavorite = favoritesManager.isFavorite(seriesItem.getSeries_id());
                    if (isFavorite) {
                        favoritesManager.removeFavorite(seriesItem.getSeries_id());
                    } else {
                        favoritesManager.addFavorite(seriesItem);
                    }
                    listener.onFavoriteClick(seriesItem, isFavorite);
                    updateFavoriteIcon(seriesItem);
                }
            });
        }

        public void bind(Series seriesItem) {
            titleTextView.setText(seriesItem.getName() != null ? seriesItem.getName() : "Sem título");
            
            if (seriesItem.getRelease_date() != null && !seriesItem.getRelease_date().isEmpty()) {
                // Extract year from release date
                String releaseDate = seriesItem.getRelease_date();
                if (releaseDate.length() >= 4) {
                    yearTextView.setText(releaseDate.substring(0, 4));
                    yearTextView.setVisibility(View.VISIBLE);
                } else {
                    yearTextView.setVisibility(View.GONE);
                }
            } else {
                yearTextView.setVisibility(View.GONE);
            }

            // Rating
            double rating = seriesItem.getRatingValue();
            if (rating > 0) {
                ratingTextView.setText(String.format("%.1f", rating));
                ratingContainer.setVisibility(View.VISIBLE);
            } else {
                ratingContainer.setVisibility(View.GONE);
            }

            // Load poster image
            String posterUrl = seriesItem.getCover();
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

            updateFavoriteIcon(seriesItem);
        }

        private void updateFavoriteIcon(Series seriesItem) {
            boolean isFavorite = favoritesManager.isFavorite(seriesItem.getSeries_id());
            favoriteImageView.setImageResource(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        }
    }
}