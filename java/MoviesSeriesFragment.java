package com.vplay.live;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "MoviesSeriesFragment";
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
    private String currentCategoryId = "";
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
        observeViewModel();
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
        
        // Load movies with current category filter
        loadMoviesByCategory(currentCategoryId);
        
        // Load series with current category filter
        loadSeriesByCategory(currentCategoryId);

        // Load genres based on current tab
        loadGenresForCurrentTab();
    }
    
    private void loadMoviesByCategory(String categoryId) {
        sharedViewModel.getXtreamClient().fetchVodStreamsByCategory(categoryId, new XtreamClient.MoviesCallback() {
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
    }
    
    private void loadSeriesByCategory(String categoryId) {
        sharedViewModel.getXtreamClient().fetchSeriesByCategory(categoryId, new XtreamClient.SeriesCallback() {
            @Override
            public void onSuccess(List<Series> series) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        allSeries = series;
                        if (currentTab.equals("Séries")) {
                            seriesAdapter.setSeries(series);
                        }
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
    }

    private void loadGenresForCurrentTab() {
        if (currentTab.equals("Séries")) {
            // Load series categories
            sharedViewModel.getXtreamClient().fetchSeriesCategories(new XtreamClient.CategoriesCallback() {
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
                    // Load default genres on error
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> setupGenres(new ArrayList<>()));
                    }
                }
            });
        } else {
            // Load VOD categories for movies
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
                    // Load default genres on error
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> setupGenres(new ArrayList<>()));
                    }
                }
            });
        }
    }
    
    private void setupGenres(List<Category> categories) {
        List<Category> displayGenres = new ArrayList<>();
        
        Log.d(TAG, "Setting up genres for tab: " + currentTab + ", API categories count: " + (categories != null ? categories.size() : 0));
        
        // Add default "TODOS" category (empty category_id fetches all content)
        Category allCategory = new Category("", "TODOS", "");
        displayGenres.add(allCategory);
        
        // Add API categories directly (they have proper category_id values)
        if (categories != null && !categories.isEmpty()) {
            for (Category category : categories) {
                Log.d(TAG, "Adding category: " + category.getCategory_name() + " (ID: " + category.getCategory_id() + ")");
            }
            displayGenres.addAll(categories);
        }
        
        genreAdapter.setGenres(displayGenres);
        
        // Reset to "TODOS" when genres are updated
        currentGenre = "TODOS";
        currentCategoryId = "";
        genreAdapter.setSelectedPosition(0);
        
        Log.d(TAG, "Total genres displayed: " + displayGenres.size());
    }

    @Override
    public void onTabClick(String tab) {
        currentTab = tab;
        tabsAdapter.setSelectedTab(tab);
        
        switch (tab) {
            case "Filmes":
                moviesSeriesRecyclerView.setAdapter(movieAdapter);
                loadMoviesByCategory(currentCategoryId);
                loadGenresForCurrentTab();
                break;
            case "Séries":
                moviesSeriesRecyclerView.setAdapter(seriesAdapter);
                loadSeriesByCategory(currentCategoryId);
                loadGenresForCurrentTab();
                break;
        }
    }

    private void onGenreClick(Category genre) {
        currentGenre = genre.getCategory_name();
        currentCategoryId = genre.getCategory_id() != null ? genre.getCategory_id() : "";
        
        Log.d(TAG, "Genre clicked: " + currentGenre + ", Category ID: " + currentCategoryId + ", Current Tab: " + currentTab);
        
        // Show loading while fetching new data
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        if (currentTab.equals("Filmes")) {
            loadMoviesByCategory(currentCategoryId);
        } else if (currentTab.equals("Séries")) {
            loadSeriesByCategory(currentCategoryId);
        }
    }

    private void refreshCurrentCategory() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        if (currentTab.equals("Filmes")) {
            loadMoviesByCategory(currentCategoryId);
        } else if (currentTab.equals("Séries")) {
            loadSeriesByCategory(currentCategoryId);
        }
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
        refreshCurrentCategory();
    }
    
    private void clearSearch() {
        searchView.setQuery("", false);
        refreshCurrentCategory();
    }
    
    private void performSearch(String query) {
        if (query.isEmpty()) {
            refreshCurrentCategory();
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
        Credential credential = sharedViewModel.getCredential();
        if (credential == null) {
            Toast.makeText(requireContext(), "Credenciais não disponíveis", Toast.LENGTH_SHORT).show();
            return;
        }

        // Iniciar SeriesDetailsActivity para mostrar detalhes da série com credenciais
        Intent intent = new Intent(requireContext(), SeriesDetailsActivity.class);
        intent.putExtra("series", series);
        intent.putExtra("server", credential.getServer());
        intent.putExtra("username", credential.getUsername());
        intent.putExtra("password", credential.getPassword());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Object item, boolean isFavorite) {
        // Implementation for favorites
        String message = isFavorite ? "Removido dos favoritos" : "Adicionado aos favoritos";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void observeViewModel() {
        // Observar mudanças na configuração de conteúdo adulto
        sharedViewModel.getAdultContentSettingChanged().observe(getViewLifecycleOwner(), changed -> {
            if (changed != null && changed) {
                // Atualizar as listas quando a configuração de conteúdo adulto mudar
                if (allGenres != null) {
                    genreAdapter.setGenres(allGenres);
                }
                if (currentTab.equals("Filmes") && allMovies != null) {
                    movieAdapter.setMovies(allMovies);
                } else if (currentTab.equals("Séries") && allSeries != null) {
                    seriesAdapter.setSeries(allSeries);
                }
            }
        });
    }

}