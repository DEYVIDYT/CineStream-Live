package com.vplay.live;

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

public class SeriesDetailsActivity extends AppCompatActivity implements EpisodeAdapter.OnEpisodeClickListener, SeasonAdapter.OnSeasonClickListener {

    private Series series;
    private Credential credential;
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
    private RecyclerView seasonsRecyclerView;
    private RecyclerView episodesRecyclerView;
    private TextView totalSeasonsTextView;
    private TextView episodesHeaderTextView;
    private SeasonAdapter seasonAdapter;
    private EpisodeAdapter episodeAdapter;
    private ProgressBar loadingProgressBar;
    private SharedViewModel sharedViewModel;
    private List<Season> allSeasons;
    private Season currentSeason;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_details);

        Intent intent = getIntent();
        series = intent.getParcelableExtra("series");
        
        // Obter credenciais da Intent
        String server = intent.getStringExtra("server");
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        
        if (series == null) {
            Toast.makeText(this, "Erro: Dados da série não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        if (server == null || username == null || password == null) {
            Toast.makeText(this, "Erro: Credenciais não encontradas", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Criar credencial
        credential = new Credential(server, username, password);
        
        // Initialize SharedViewModel and configure XtreamClient
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        sharedViewModel.getXtreamClient().setCredentials(credential);

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
        seasonsRecyclerView = findViewById(R.id.seasonsRecyclerView);
        episodesRecyclerView = findViewById(R.id.episodesRecyclerView);
        totalSeasonsTextView = findViewById(R.id.totalSeasonsTextView);
        episodesHeaderTextView = findViewById(R.id.episodesHeaderTextView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        backButton.setOnClickListener(v -> finish());

        // Setup seasons adapter
        seasonAdapter = new SeasonAdapter(this, this);
        seasonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        seasonsRecyclerView.setAdapter(seasonAdapter);
        
        // Setup episodes adapter
        episodeAdapter = new EpisodeAdapter(this, this);
        episodesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        episodesRecyclerView.setAdapter(episodeAdapter);
        
        // Initialize data
        allSeasons = new ArrayList<>();
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

        sharedViewModel.getXtreamClient().fetchSeriesSeasons(series.getSeries_id(), new XtreamClient.SeasonsCallback() {
            @Override
            public void onSuccess(List<Season> seasons) {
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    allSeasons = seasons;
                    
                    if (!seasons.isEmpty()) {
                        setupSeasonsDisplay(seasons);
                        
                        // Select first season by default
                        currentSeason = seasons.get(0);
                        episodeAdapter.setEpisodes(currentSeason.getEpisodes());
                        updateEpisodesHeader(currentSeason);
                        
                        // Setup play button with first episode
                        Episode firstEpisode = currentSeason.getFirstEpisode();
                        if (firstEpisode != null) {
                            playButton.setText("REPRODUZIR " + firstEpisode.getFormattedEpisodeNumber());
                            playButton.setOnClickListener(v -> playEpisode(firstEpisode));
                        } else {
                            playButton.setText("Nenhum episódio disponível");
                            playButton.setEnabled(false);
                        }
                    } else {
                        playButton.setText("Nenhuma temporada disponível");
                        playButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(SeriesDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                    playButton.setText("Erro ao carregar temporadas");
                    playButton.setEnabled(false);
                });
            }
        });
    }
    
    private void setupSeasonsDisplay(List<Season> seasons) {
        if (seasons.size() > 1) {
            // Multiple seasons - show season selector
            seasonAdapter.setSeasons(seasons);
            seasonsRecyclerView.setVisibility(View.VISIBLE);
            totalSeasonsTextView.setText(seasons.size() + (seasons.size() == 1 ? " temporada" : " temporadas"));
            totalSeasonsTextView.setVisibility(View.VISIBLE);
        } else {
            // Single season - hide season selector
            seasonsRecyclerView.setVisibility(View.GONE);
            totalSeasonsTextView.setVisibility(View.GONE);
        }
    }
    
    private void updateEpisodesHeader(Season season) {
        if (allSeasons.size() > 1) {
            episodesHeaderTextView.setText(season.getFormattedSeasonInfo());
        } else {
            episodesHeaderTextView.setText("Episódios (" + season.getTotalEpisodes() + ")");
        }
    }

    @Override
    public void onEpisodeClick(Episode episode) {
        playEpisode(episode);
    }
    
    @Override
    public void onSeasonClick(Season season, int position) {
        currentSeason = season;
        episodeAdapter.setEpisodes(season.getEpisodes());
        updateEpisodesHeader(season);
        
        // Update play button with first episode of selected season
        Episode firstEpisode = season.getFirstEpisode();
        if (firstEpisode != null) {
            playButton.setText("REPRODUZIR " + firstEpisode.getFormattedEpisodeNumber());
            playButton.setOnClickListener(v -> playEpisode(firstEpisode));
        }
    }

    private void playEpisode(Episode episode) {
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
            intent.putExtra("server", credential.getServer());
            intent.putExtra("username", credential.getUsername());
            intent.putExtra("password", credential.getPassword());
            startActivity(intent);
        } else {
            Toast.makeText(this, "URL do episódio não disponível", Toast.LENGTH_SHORT).show();
        }
    }
}