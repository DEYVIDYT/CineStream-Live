package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class MovieDetailsActivity extends AppCompatActivity {

    private Movie movie;
    private String server;
    private String username;
    private String password;
    private ImageView backdropImageView;
    private ImageView posterImageView;
    private TextView titleTextView;
    private TextView originalTitleTextView;
    private TextView ratingTextView;
    private TextView yearTextView;
    private TextView durationTextView;
    private TextView plotTextView;
    private TextView castTextView;
    private TextView genreTextView;
    private Button playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra("movie");
        server = intent.getStringExtra("server");
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");

        if (movie == null) {
            Toast.makeText(this, "Erro: Dados do filme não encontrados", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (server == null || username == null || password == null) {
            Toast.makeText(this, "Erro: Credenciais não disponíveis", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupMovieInfo();
    }

    private void initViews() {
        backdropImageView = findViewById(R.id.backdropImageView);
        posterImageView = findViewById(R.id.posterImageView);
        titleTextView = findViewById(R.id.titleTextView);
        originalTitleTextView = findViewById(R.id.originalTitleTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        yearTextView = findViewById(R.id.yearTextView);
        durationTextView = findViewById(R.id.durationTextView);
        plotTextView = findViewById(R.id.plotTextView);
        castTextView = findViewById(R.id.castTextView);
        genreTextView = findViewById(R.id.genreTextView);
        playButton = findViewById(R.id.playButton);
    }

    private void setupMovieInfo() {
        // Título principal
        if (movie.getName() != null) {
            titleTextView.setText(movie.getName());
        }

        // Título original (se diferente)
        if (movie.getName() != null) {
            originalTitleTextView.setText(movie.getName());
            originalTitleTextView.setVisibility(View.VISIBLE);
        } else {
            originalTitleTextView.setVisibility(View.GONE);
        }

        // Rating
        double rating = movie.getRatingValue();
        if (rating > 0) {
            ratingTextView.setText(String.format("%.1f", rating));
            ratingTextView.setVisibility(View.VISIBLE);
        } else {
            ratingTextView.setVisibility(View.GONE);
        }

        // Ano
        if (movie.getYear() != null && !movie.getYear().isEmpty()) {
            yearTextView.setText(movie.getYear());
            yearTextView.setVisibility(View.VISIBLE);
        } else {
            yearTextView.setVisibility(View.GONE);
        }

        // Duração
        if (movie.getDuration() != null && !movie.getDuration().isEmpty()) {
            durationTextView.setText(movie.getDuration() + " min");
            durationTextView.setVisibility(View.VISIBLE);
        } else {
            durationTextView.setVisibility(View.GONE);
        }

        // Sinopse
        if (movie.getPlot() != null && !movie.getPlot().isEmpty()) {
            plotTextView.setText(movie.getPlot());
            plotTextView.setVisibility(View.VISIBLE);
        } else {
            plotTextView.setText("Sinopse não disponível");
        }

        // Elenco
        if (movie.getCast() != null && !movie.getCast().isEmpty()) {
            castTextView.setText("Elenco: " + movie.getCast());
            castTextView.setVisibility(View.VISIBLE);
        } else {
            castTextView.setVisibility(View.GONE);
        }

        // Gênero
        if (movie.getGenre() != null && !movie.getGenre().isEmpty()) {
            genreTextView.setText(movie.getGenre());
            genreTextView.setVisibility(View.VISIBLE);
        } else {
            genreTextView.setVisibility(View.GONE);
        }

        // Imagens
        if (movie.getStream_icon() != null && !movie.getStream_icon().isEmpty()) {
            // Poster
            Glide.with(this)
                    .load(movie.getStream_icon())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(16))
                            .placeholder(R.drawable.placeholder_movie)
                            .error(R.drawable.placeholder_movie))
                    .into(posterImageView);

            // Backdrop (usando a mesma imagem por enquanto)
            Glide.with(this)
                    .load(movie.getStream_icon())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_movie)
                            .error(R.drawable.placeholder_movie))
                    .into(backdropImageView);
        }
    }

    private void playMovie() {
        String streamUrl = movie.getStreamUrl(server, username, password);

        if (streamUrl != null) {
            Intent intent = new Intent(this, MoviePlayerActivity.class);
            intent.putExtra("movie", movie);
            intent.putExtra("stream_url", streamUrl);
            startActivity(intent);
        } else {
            Toast.makeText(this, "URL do filme não disponível", Toast.LENGTH_SHORT).show();
        }
    }
}