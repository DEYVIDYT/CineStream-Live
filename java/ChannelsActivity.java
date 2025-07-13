package com.cinestream.live;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChannelsActivity extends AppCompatActivity implements 
        CategoryAdapter.OnCategoryClickListener, 
        ChannelAdapter.OnChannelClickListener,
        HistoryAdapter.OnHistoryItemClickListener {
    
    private VideoView videoView;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView channelsRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ChannelAdapter channelAdapter;
    private HistoryAdapter historyAdapter;
    private XtreamClient xtreamClient;
    private Handler handler;
    private FavoritesManager favoritesManager;
    private HistoryManager historyManager;
    
    // Search functionality
    private SearchView searchView;
    private LinearLayout searchContainer;
    private ImageView searchIcon;
    private ImageView clearSearchIcon;
    private ImageView fullscreenButton;
    private ProgressBar videoLoadingProgressBar;
    private boolean isSearchVisible = false;
    private String currentCategory = "TODOS";
    private List<Channel> allChannels;
    private List<Category> allCategories;
    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);
        
        // Initialize managers
        favoritesManager = new FavoritesManager(this);
        historyManager = new HistoryManager(this);
        xtreamClient = new XtreamClient();
        handler = new Handler(Looper.getMainLooper());
        
        initViews();
        setupRecyclerViews();
        setupSearch();
        setupVideoView();
        
        loadCredentialsAndData();
        
        // Check if we need to show a specific category
        String showCategory = getIntent().getStringExtra("show_category");
        if (showCategory != null) {
            // Delay to ensure data is loaded first
            handler.postDelayed(() -> showSpecificCategory(showCategory), 1000);
        }
    }
    
    private void initViews() {
        videoView = findViewById(R.id.videoView);
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        channelsRecyclerView = findViewById(R.id.channelsRecyclerView);
        searchView = findViewById(R.id.searchView);
        searchContainer = findViewById(R.id.searchContainer);
        searchIcon = findViewById(R.id.searchIcon);
        clearSearchIcon = findViewById(R.id.clearSearchIcon);
        fullscreenButton = findViewById(R.id.fullscreenButton);
        videoLoadingProgressBar = findViewById(R.id.videoLoadingProgressBar);
        
        findViewById(R.id.profileTab).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        
        findViewById(R.id.guideTab).setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        searchIcon.setOnClickListener(v -> toggleSearch());
        clearSearchIcon.setOnClickListener(v -> clearSearch());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }
    
    private void toggleFullscreen() {
        isFullscreen = !isFullscreen;
        if (isFullscreen) {
            enterFullscreen();
        } else {
            exitFullscreen();
        }
    }

    private void enterFullscreen() {
        // Hide other views
        findViewById(R.id.categoriesRecyclerView).setVisibility(View.GONE);
        findViewById(R.id.channelsRecyclerView).setVisibility(View.GONE);
        findViewById(R.id.searchContainer).setVisibility(View.GONE);

        // Set video view to fullscreen
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        videoView.setLayoutParams(params);

        // Enter immersive fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void exitFullscreen() {
        // Show other views
        findViewById(R.id.categoriesRecyclerView).setVisibility(View.VISIBLE);
        findViewById(R.id.channelsRecyclerView).setVisibility(View.VISIBLE);
        findViewById(R.id.searchContainer).setVisibility(View.VISIBLE);

        // Restore video view size
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = (int) (250 * getResources().getDisplayMetrics().density);
        videoView.setLayoutParams(params);

        // Exit immersive fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // The toggleFullscreen method already handles the state and calls the appropriate method.
        // No need to call enterFullscreen/exitFullscreen directly here.
    }
    
    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this, this);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoriesRecyclerView.setAdapter(categoryAdapter);
        
        channelAdapter = new ChannelAdapter(this, this, favoritesManager);
        historyAdapter = new HistoryAdapter(this, this, favoritesManager);
        
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        channelsRecyclerView.setAdapter(channelAdapter);
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
    
    private void setupVideoView() {
        videoView.setOnPreparedListener(mediaPlayer -> {
            videoLoadingProgressBar.setVisibility(View.GONE);
            // Player is ready
        });
        
        videoView.setOnErrorListener((mediaPlayer, what, extra) -> {
            videoLoadingProgressBar.setVisibility(View.GONE);
            Toast.makeText(ChannelsActivity.this, "Erro ao reproduzir vídeo", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void playStream(String streamUrl) {
        if (streamUrl != null && !streamUrl.isEmpty()) {
            videoLoadingProgressBar.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(streamUrl));
            videoView.start();
        } else {
            Toast.makeText(this, "URL do canal não disponível", Toast.LENGTH_SHORT).show();
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
        if (allCategories != null && !allCategories.isEmpty()) {
            categoryAdapter.setSelectedPosition(0); // Reset to "TODOS"
            currentCategory = allCategories.get(0).getCategory_name();
        }
    }
    
    private void hideSearch() {
        isSearchVisible = false;
        searchContainer.setVisibility(View.GONE);
        searchView.setQuery("", false);
        searchView.clearFocus();
        
        // Restore previous category filter
        if (currentCategory.equals("FAVORITOS")) {
            channelAdapter.filterByCategory("FAVORITOS");
        } else if (currentCategory.equals("HISTÓRICO")) {
            showHistory();
        } else {
            channelAdapter.filterByCategory(currentCategory);
        }
    }
    
    private void clearSearch() {
        searchView.setQuery("", false);
        if (allChannels != null) {
            channelAdapter.setChannels(allChannels);
        }
    }
    
    private void performSearch(String query) {
        if (allChannels != null) {
            channelAdapter.setChannels(allChannels);
            channelAdapter.filterBySearch(query);
        }
    }
    
    private void loadCredentialsAndData() {
        xtreamClient.fetchCredentials(new XtreamClient.CredentialsCallback() {
            @Override
            public void onSuccess(Credential credential) {
                handler.post(() -> {
                    fetchCategories();
                    fetchChannels();
                });
            }
            
            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(ChannelsActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fetchCategories() {
        xtreamClient.fetchLiveCategories(new XtreamClient.CategoriesCallback() {
            @Override
            public void onSuccess(List<Category> categories) {
                handler.post(() -> {
                    allCategories = categories;
                    // Add custom categories at the beginning
                    Category allCategory = new Category("", "TODOS", "");
                    Category favoritesCategory = new Category("", "FAVORITOS", "");
                    Category historyCategory = new Category("", "HISTÓRICO", "");

                    List<Category> displayCategories = new java.util.ArrayList<>();
                    displayCategories.add(allCategory);
                    displayCategories.add(favoritesCategory);
                    displayCategories.add(historyCategory);
                    displayCategories.addAll(categories);

                    categoryAdapter.setCategories(displayCategories);
                    channelAdapter.setCategoriesForFilter(displayCategories);
                    // Select 'TODOS' by default
                    categoryAdapter.setSelectedPosition(0);
                    currentCategory = allCategory.getCategory_name();
                });
            }
            
            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(ChannelsActivity.this, "Erro ao carregar categorias: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fetchChannels() {
        xtreamClient.fetchLiveStreams(new XtreamClient.ChannelsCallback() {
            @Override
            public void onSuccess(List<Channel> channels) {
                handler.post(() -> {
                    allChannels = channels;
                    channelAdapter.setChannels(channels);
                    if (channels.isEmpty()) {
                        Toast.makeText(ChannelsActivity.this, "Nenhum canal encontrado", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                handler.post(() -> {
                    Toast.makeText(ChannelsActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    public void onCategoryClick(Category category) {
        if (isSearchVisible) {
            hideSearch();
        }
        
        int position = allCategories.indexOf(category) + 3; // +3 for TODOS, FAVORITOS, HISTÓRICO
        if (category.getCategory_name().equals("TODOS")) {
            position = 0;
        } else if (category.getCategory_name().equals("FAVORITOS")) {
            position = 1;
        } else if (category.getCategory_name().equals("HISTÓRICO")) {
            position = 2;
        }

        categoryAdapter.setSelectedPosition(position);
        currentCategory = category.getCategory_name();
        
        if (currentCategory.equals("FAVORITOS")) {
            channelsRecyclerView.setAdapter(channelAdapter);
            channelAdapter.filterByCategory("FAVORITOS");
        } else if (currentCategory.equals("HISTÓRICO")) {
            showHistory();
        } else {
            channelsRecyclerView.setAdapter(channelAdapter);
            channelAdapter.filterByCategory(currentCategory);
        }
    }
    
    private void showHistory() {
        channelsRecyclerView.setAdapter(historyAdapter);
        List<HistoryManager.HistoryItem> historyItems = historyManager.getHistory();
        historyAdapter.setHistoryItems(historyItems);
    }
    
    @Override
    public void onChannelClick(Channel channel) {
        if (xtreamClient.getCurrentCredential() == null) {
            Toast.makeText(this, "Credenciais não carregadas", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Credential credential = xtreamClient.getCurrentCredential();
        String streamUrl = channel.getStreamUrl(
                credential.getServer(),
                credential.getUsername(),
                credential.getPassword()
        );
        
        if (streamUrl != null) {
            // Add to history
            historyManager.addToHistory(channel);
            playStream(streamUrl);
        } else {
            Toast.makeText(this, "URL do canal não disponível", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onFavoriteClick(Channel channel, boolean isFavorite) {
        if (isFavorite) {
            favoritesManager.removeFavorite(channel);
            Toast.makeText(this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();
        } else {
            favoritesManager.addFavorite(channel);
            Toast.makeText(this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show();
        }
        
        // Update the current view
        if (currentCategory.equals("FAVORITOS")) {
            channelAdapter.filterByCategory("FAVORITOS");
        } else if (currentCategory.equals("HISTÓRICO")) {
            showHistory();
        } else {
            // Refresh current adapter to update favorite icons
            if (channelsRecyclerView.getAdapter() == channelAdapter) {
                channelAdapter.notifyDataSetChanged();
            } else if (channelsRecyclerView.getAdapter() == historyAdapter) {
                historyAdapter.notifyDataSetChanged();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Refresh current view to update favorites and history
        if (currentCategory.equals("FAVORITOS")) {
            channelAdapter.filterByCategory("FAVORITOS");
        } else if (currentCategory.equals("HISTÓRICO")) {
            showHistory();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    @Override
    public void onBackPressed() {
        if (isSearchVisible) {
            hideSearch();
        }
        else {
            super.onBackPressed();
        }
    }
    
    private void showSpecificCategory(String categoryName) {
        if (allCategories != null && categoryAdapter != null) {
            // Find the category and select it
            for (int i = 0; i < allCategories.size(); i++) {
                Category category = allCategories.get(i);
                if (category.getCategory_name().equals(categoryName)) {
                    // Adjust position for custom categories
                    int position = i;
                    if (categoryName.equals("TODOS")) {
                        position = 0;
                    } else if (categoryName.equals("FAVORITOS")) {
                        position = 1;
                    } else if (categoryName.equals("HISTÓRICO")) {
                        position = 2;
                    } else {
                        position = i + 3; // +3 for TODOS, FAVORITOS, HISTÓRICO
                    }
                    
                    categoryAdapter.setSelectedPosition(position);
                    currentCategory = categoryName;
                    
                    if (categoryName.equals("FAVORITOS")) {
                        channelsRecyclerView.setAdapter(channelAdapter);
                        channelAdapter.filterByCategory("FAVORITOS");
                    } else if (categoryName.equals("HISTÓRICO")) {
                        showHistory();
                    } else {
                        channelsRecyclerView.setAdapter(channelAdapter);
                        channelAdapter.filterByCategory(categoryName);
                    }
                    break;
                }
            }
        }
    }
}

