package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class SeriesDetailsActivity extends AppCompatActivity implements EpisodeAdapter.OnEpisodeClickListener {

    private Series series;
    private ImageView backdropImageView;
    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView yearTextView;
    private TextView ratingTextView;
    private TextView plotTextView;
    private TextView castTextView;
    private TextView genreTextView;
    private Button playButton;
    private ImageButton backButton;
    private RecyclerView episodesRecyclerView;
    private EpisodeAdapter episodeAdapter;
    private ProgressBar loadingProgressBar;
    private SharedViewModel sharedViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_details);

        Intent intent = getIntent();
        series = intent.getParcelableExtra("series");

        if (series == null) {
            Toast.makeText(this, "Erro: Dados da série não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupSeriesInfo();
        loadEpisodes();
    }

    private void initViews() {
        backdropImageView = findViewById(R.id.backdropImageView);
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        yearTextView = findViewById(R.id.yearTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        plotTextView = findViewById(R.id.plotTextView);
        castTextView = findViewById(R.id.castTextView);
        genreTextView = findViewById(R.id.genreTextView);
        playButton = findViewById(R.id.playButton);
        backButton = findViewById(R.id.backButton);
        episodesRecyclerView = findViewById(R.id.episodesRecyclerView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        backButton.setOnClickListener(v -> finish());

        episodeAdapter = new EpisodeAdapter(this, this);
        episodesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        episodesRecyclerView.setAdapter(episodeAdapter);

        // Initialize SharedViewModel
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    }

    private void setupSeriesInfo() {
        if (series.getName() != null) {
            titleTextView.setText(series.getName());
        }

        if (series.getRelease_date() != null && series.getRelease_date().length() >= 4) {
            yearTextView.setText(series.getRelease_date().substring(0, 4));
            yearTextView.setVisibility(View.VISIBLE);
        } else {
            yearTextView.setVisibility(View.GONE);
        }

        double rating = series.getRatingValue();
        if (rating > 0) {
            ratingTextView.setText(String.format("%.1f", rating));
            ratingTextView.setVisibility(View.VISIBLE);
        } else {
            ratingTextView.setVisibility(View.GONE);
        }

        if (series.getPlot() != null && !series.getPlot().isEmpty()) {
            plotTextView.setText(series.getPlot());
            plotTextView.setVisibility(View.VISIBLE);
        } else {
            plotTextView.setVisibility(View.GONE);
        }

        if (series.getCast() != null && !series.getCast().isEmpty()) {
            castTextView.setText("Elenco: " + series.getCast());
            castTextView.setVisibility(View.VISIBLE);
        } else {
            castTextView.setVisibility(View.GONE);
        }

        if (series.getGenre() != null && !series.getGenre().isEmpty()) {
            genreTextView.setText(series.getGenre());
            genreTextView.setVisibility(View.VISIBLE);
        } else {
            genreTextView.setVisibility(View.GONE);
        }

        // Load images
        if (series.getBackdrop_path() != null && !series.getBackdrop_path().isEmpty()) {
            Glide.with(this)
                    .load(series.getBackdrop_path())
                    .into(backdropImageView);
        }

        if (series.getCover() != null && !series.getCover().isEmpty()) {
            Glide.with(this)
                    .load(series.getCover())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                    .placeholder(R.drawable.ic_tv)
                    .error(R.drawable.ic_tv)
                    .into(posterImageView);
        } else {
            posterImageView.setImageResource(R.drawable.ic_tv);
        }
    }

    private void loadEpisodes() {
        if (series.getSeries_id() == null) {
            Toast.makeText(this, "ID da série não disponível", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);

        sharedViewModel.getXtreamClient().fetchSeriesInfo(series.getSeries_id(), new XtreamClient.EpisodesCallback() {
            @Override
            public void onSuccess(List<Episode> episodes) {
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    episodeAdapter.setEpisodes(episodes);
                    
                    // Setup play button to play first episode if available
                    if (!episodes.isEmpty()) {
                        Episode firstEpisode = episodes.get(0);
                        playButton.setText("REPRODUZIR " + firstEpisode.getFormattedEpisodeNumber());
                        playButton.setOnClickListener(v -> playEpisode(firstEpisode));
                    } else {
                        playButton.setText("Nenhum episódio disponível");
                        playButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(SeriesDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                    playButton.setText("Erro ao carregar episódios");
                    playButton.setEnabled(false);
                });
            }
        });
    }

    @Override
    public void onEpisodeClick(Episode episode) {
        playEpisode(episode);
    }

    private void playEpisode(Episode episode) {
        Credential credential = sharedViewModel.getCredential();
        if (credential == null) {
            Toast.makeText(this, "Credenciais não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        String streamUrl = episode.getStreamUrl(
                credential.getServer(),
                credential.getUsername(),
                credential.getPassword()
        );

        if (streamUrl != null) {
            Intent intent = new Intent(this, EpisodePlayerActivity.class);
            intent.putExtra("episode", episode);
            intent.putExtra("series", series);
            intent.putExtra("stream_url", streamUrl);
            startActivity(intent);
        } else {
            Toast.makeText(this, "URL do episódio não disponível", Toast.LENGTH_SHORT).show();
        }
    }
}