package com.cinestream.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MoviesSeriesFragment extends Fragment implements
        MoviesTabAdapter.OnTabClickListener,
        MovieAdapter.OnMovieClickListener,
        SeriesAdapter.OnSeriesClickListener {

    private SharedViewModel sharedViewModel;
    private RecyclerView tabsRecyclerView;
    private RecyclerView genreRecyclerView;
    private RecyclerView moviesSeriesRecyclerView;
    private MoviesTabAdapter tabsAdapter;
    private GenreAdapter genreAdapter;
    private MovieAdapter movieAdapter;
    private SeriesAdapter seriesAdapter;
    private FavoritesManager favoritesManager;
    private SearchView searchView;
    private LinearLayout searchContainer;
    private ProgressBar loadingProgressBar;
    
    private List<Movie> allMovies;
    private List<Series> allSeries;
    private List<Category> allGenres;
    private String currentTab = "Filmes";
    private String currentGenre = "TODOS";
    private boolean isSearchVisible = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies_series, container, false);

        favoritesManager = new FavoritesManager(requireContext());

        initViews(view);
        setupRecyclerViews();
        setupSearch();
        loadData();

        return view;
    }

    private void initViews(View view) {
        tabsRecyclerView = view.findViewById(R.id.tabsRecyclerView);
        genreRecyclerView = view.findViewById(R.id.genreRecyclerView);
        moviesSeriesRecyclerView = view.findViewById(R.id.moviesSeriesRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        searchContainer = view.findViewById(R.id.searchContainer);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        view.findViewById(R.id.searchIcon).setOnClickListener(v -> toggleSearch());
        view.findViewById(R.id.clearSearchIcon).setOnClickListener(v -> clearSearch());
    }

    private void setupRecyclerViews() {
        // Setup tabs
        tabsAdapter = new MoviesTabAdapter(requireContext(), this);
        tabsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        tabsRecyclerView.setAdapter(tabsAdapter);
        
        List<String> tabs = new ArrayList<>();
        tabs.add("Filmes");
        tabs.add("Séries");
        tabs.add("Animação");
        tabs.add("Minhas listas");
        tabsAdapter.setTabs(tabs);
        tabsAdapter.setSelectedTab("Filmes");

        // Setup genres
        genreAdapter = new GenreAdapter(requireContext(), this::onGenreClick);
        genreRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        genreRecyclerView.setAdapter(genreAdapter);

        // Setup movies/series grid
        movieAdapter = new MovieAdapter(requireContext(), this, favoritesManager);
        seriesAdapter = new SeriesAdapter(requireContext(), this, favoritesManager);
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        moviesSeriesRecyclerView.setLayoutManager(gridLayoutManager);
        moviesSeriesRecyclerView.setAdapter(movieAdapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }
            
            @Override
            public boolean onQueryTextChange(String newText) {
                if (isSearchVisible) {
                    performSearch(newText);
                }
                return true;
            }
        });
        
        searchView.setOnCloseListener(() -> {
            hideSearch();
            return true;
        });
    }

    private void loadData() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        // Load movies
        sharedViewModel.getXtreamClient().fetchVodStreams(new XtreamClient.MoviesCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allMovies = movies;
                        if (currentTab.equals("Filmes")) {
                            movieAdapter.setMovies(movies);
                        }
                        loadingProgressBar.setVisibility(View.GONE);
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.GONE);
                    });
                }
            }
        });

        // Load series
        sharedViewModel.getXtreamClient().fetchSeries(new XtreamClient.SeriesCallback() {
            @Override
            public void onSuccess(List<Series> series) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allSeries = series;
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

        // Load genres (using VOD categories for movies)
        sharedViewModel.getXtreamClient().fetchVodCategories(new XtreamClient.CategoriesCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allGenres = categories;
                        setupGenres(categories);
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Handle error silently for genres
            }
        });
    }

    private void setupGenres(List<Category> categories) {
        List<Category> displayGenres = new ArrayList<>();
        
        // Add default categories
        Category allCategory = new Category("", "TODOS", "");
        displayGenres.add(allCategory);
        
        // Add some popular genres
        displayGenres.add(new Category("", "Ação", ""));
        displayGenres.add(new Category("", "Aventura", ""));
        displayGenres.add(new Category("", "Comédia", ""));
        displayGenres.add(new Category("", "Drama", ""));
        displayGenres.add(new Category("", "Terror", ""));
        
        // Add API categories
        displayGenres.addAll(categories);
        
        genreAdapter.setGenres(displayGenres);
    }

    @Override
    public void onTabClick(String tab) {
        currentTab = tab;
        tabsAdapter.setSelectedTab(tab);
        
        switch (tab) {
            case "Filmes":
                moviesSeriesRecyclerView.setAdapter(movieAdapter);
                if (allMovies != null) {
                    movieAdapter.setMovies(allMovies);
                    filterByGenre(currentGenre);
                }
                break;
            case "Séries":
                moviesSeriesRecyclerView.setAdapter(seriesAdapter);
                if (allSeries != null) {
                    seriesAdapter.setSeries(allSeries);
                    filterByGenre(currentGenre);
                }
                break;
            case "Animação":
                // Filter by animation genre
                filterByGenre("Animação");
                break;
            case "Minhas listas":
                // Show favorites
                showFavorites();
                break;
        }
    }

    private void onGenreClick(Category genre) {
        currentGenre = genre.getCategory_name();
        filterByGenre(currentGenre);
    }

    private void filterByGenre(String genre) {
        if (currentTab.equals("Filmes") && allMovies != null) {
            if (genre.equals("TODOS")) {
                movieAdapter.setMovies(allMovies);
            } else {
                List<Movie> filteredMovies = new ArrayList<>();
                for (Movie movie : allMovies) {
                    if (movie.getGenre() != null && movie.getGenre().toLowerCase().contains(genre.toLowerCase()) ||
                        movie.getCategory_name() != null && movie.getCategory_name().toLowerCase().contains(genre.toLowerCase())) {
                        filteredMovies.add(movie);
                    }
                }
                movieAdapter.setMovies(filteredMovies);
            }
        } else if (currentTab.equals("Séries") && allSeries != null) {
            if (genre.equals("TODOS")) {
                seriesAdapter.setSeries(allSeries);
            } else {
                List<Series> filteredSeries = new ArrayList<>();
                for (Series series : allSeries) {
                    if (series.getGenre() != null && series.getGenre().toLowerCase().contains(genre.toLowerCase()) ||
                        series.getCategory_name() != null && series.getCategory_name().toLowerCase().contains(genre.toLowerCase())) {
                        filteredSeries.add(series);
                    }
                }
                seriesAdapter.setSeries(filteredSeries);
            }
        }
    }

    private void showFavorites() {
        // Implementation for showing favorites
        // This would require extending FavoritesManager to support movies and series
    }

    private void toggleSearch() {
        if (isSearchVisible) {
            hideSearch();
        } else {
            showSearch();
        }
    }
    
    private void showSearch() {
        isSearchVisible = true;
        searchContainer.setVisibility(View.VISIBLE);
        searchView.requestFocus();
    }
    
    private void hideSearch() {
        isSearchVisible = false;
        searchContainer.setVisibility(View.GONE);
        searchView.setQuery("", false);
        searchView.clearFocus();
        
        // Restore previous filter
        filterByGenre(currentGenre);
    }
    
    private void clearSearch() {
        searchView.setQuery("", false);
        filterByGenre(currentGenre);
    }
    
    private void performSearch(String query) {
        if (query.isEmpty()) {
            filterByGenre(currentGenre);
            return;
        }
        
        if (currentTab.equals("Filmes") && allMovies != null) {
            List<Movie> filteredMovies = new ArrayList<>();
            for (Movie movie : allMovies) {
                if (movie.getName() != null && movie.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredMovies.add(movie);
                }
            }
            movieAdapter.setMovies(filteredMovies);
        } else if (currentTab.equals("Séries") && allSeries != null) {
            List<Series> filteredSeries = new ArrayList<>();
            for (Series series : allSeries) {
                if (series.getName() != null && series.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredSeries.add(series);
                }
            }
            seriesAdapter.setSeries(filteredSeries);
        }
    }

    @Override
    public void onMovieClick(Movie movie) {
        Credential credential = sharedViewModel.getCredential();
        if (credential == null) {
            Toast.makeText(requireContext(), "Credenciais não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        // Abrir tela de detalhes do filme com as credenciais
        Intent intent = new Intent(requireContext(), MovieDetailsActivity.class);
        intent.putExtra("movie", movie);
        intent.putExtra("server", credential.getServer());
        intent.putExtra("username", credential.getUsername());
        intent.putExtra("password", credential.getPassword());
        startActivity(intent);
    }

    @Override
    public void onSeriesClick(Series series) {
        // Iniciar SeriesDetailsActivity para mostrar detalhes da série
        Intent intent = new Intent(requireContext(), SeriesDetailsActivity.class);
        intent.putExtra("series", series);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Object item, boolean isFavorite) {
        // Implementation for favorites
        String message = isFavorite ? "Removido dos favoritos" : "Adicionado aos favoritos";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

}