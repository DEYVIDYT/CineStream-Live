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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChannelsFragment extends Fragment implements
        CategoryAdapter.OnCategoryClickListener,
        ChannelAdapter.OnChannelClickListener,
        HistoryAdapter.OnHistoryItemClickListener {

    private SharedViewModel sharedViewModel;
    private VideoView videoView;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView channelsRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ChannelAdapter channelAdapter;
    private HistoryAdapter historyAdapter;
    private FavoritesManager favoritesManager;
    private HistoryManager historyManager;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channels, container, false);

        favoritesManager = new FavoritesManager(requireContext());
        historyManager = new HistoryManager(requireContext());

        initViews(view);
        setupRecyclerViews();
        setupSearch();
        setupVideoView();
        observeViewModel();

        return view;
    }

    private void initViews(View view) {
        videoView = view.findViewById(R.id.videoView);
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        channelsRecyclerView = view.findViewById(R.id.channelsRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        searchContainer = view.findViewById(R.id.searchContainer);
        searchIcon = view.findViewById(R.id.searchIcon);
        clearSearchIcon = view.findViewById(R.id.clearSearchIcon);
        fullscreenButton = view.findViewById(R.id.fullscreenButton);
        videoLoadingProgressBar = view.findViewById(R.id.videoLoadingProgressBar);
        
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
        if (getActivity() != null && getActivity().findViewById(R.id.navigation_tabs) != null) {
            getActivity().findViewById(R.id.navigation_tabs).setVisibility(View.GONE);
        }
        if (getActivity() instanceof HostActivity) {
            if (((HostActivity) getActivity()).getSupportActionBar() != null) {
                ((HostActivity) getActivity()).getSupportActionBar().hide();
            }
        }

        // Hide other views within the fragment
        if (getView() != null) {
            getView().findViewById(R.id.categoriesRecyclerView).setVisibility(View.GONE);
            getView().findViewById(R.id.channelsRecyclerView).setVisibility(View.GONE);
            getView().findViewById(R.id.searchContainer).setVisibility(View.GONE);
        }

        // Set video view to fullscreen
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = FrameLayout.LayoutParams.MATCH_PARENT;
        videoView.setLayoutParams(params);

        // Enter immersive fullscreen mode
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void exitFullscreen() {
        if (getActivity() != null && getActivity().findViewById(R.id.navigation_tabs) != null) {
            getActivity().findViewById(R.id.navigation_tabs).setVisibility(View.VISIBLE);
        }
        if (getActivity() instanceof HostActivity) {
            if (((HostActivity) getActivity()).getSupportActionBar() != null) {
                ((HostActivity) getActivity()).getSupportActionBar().show();
            }
        }

        // Show other views within the fragment
        if (getView() != null) {
            getView().findViewById(R.id.categoriesRecyclerView).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.channelsRecyclerView).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.searchContainer).setVisibility(View.VISIBLE);
        }

        // Restore video view size
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoView.getLayoutParams();
        params.width = FrameLayout.LayoutParams.MATCH_PARENT;
        params.height = (int) (250 * getResources().getDisplayMetrics().density);
        videoView.setLayoutParams(params);

        // Exit immersive fullscreen mode
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // The toggleFullscreen method already handles the state and calls the appropriate method.
        // No need to call enterFullscreen/exitFullscreen directly here.
    }
    
    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(requireContext(), this);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoriesRecyclerView.setAdapter(categoryAdapter);
        
        channelAdapter = new ChannelAdapter(requireContext(), this, favoritesManager);
        historyAdapter = new HistoryAdapter(requireContext(), this, favoritesManager);
        
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
            Toast.makeText(requireContext(), "Erro ao reproduzir vídeo", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void playStream(String streamUrl) {
        if (streamUrl != null && !streamUrl.isEmpty()) {
            videoLoadingProgressBar.setVisibility(View.VISIBLE);
            videoView.setVideoURI(Uri.parse(streamUrl));
            videoView.start();
        } else {
            Toast.makeText(requireContext(), "URL do canal não disponível", Toast.LENGTH_SHORT).show();
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

    private void observeViewModel() {
        sharedViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                allCategories = categories;
                Category allCategory = new Category("", "TODOS", "");
                Category favoritesCategory = new Category("", "FAVORITOS", "");
                Category historyCategory = new Category("", "HISTÓRICO", "");

                List<Category> displayCategories = new ArrayList<>();
                displayCategories.add(allCategory);
                displayCategories.add(favoritesCategory);
                displayCategories.add(historyCategory);
                displayCategories.addAll(categories);

                categoryAdapter.setCategories(displayCategories);
                channelAdapter.setCategoriesForFilter(displayCategories);
                categoryAdapter.setSelectedPosition(0);
                currentCategory = allCategory.getCategory_name();
            }
        });

        sharedViewModel.getChannels().observe(getViewLifecycleOwner(), channels -> {
            if (channels != null) {
                allChannels = channels;
                channelAdapter.setChannels(channels);
                if (channels.isEmpty()) {
                    Toast.makeText(requireContext(), "Nenhum canal encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sharedViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                videoLoadingProgressBar.setVisibility(View.VISIBLE);
            } else {
                videoLoadingProgressBar.setVisibility(View.GONE);
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
        Credential credential = sharedViewModel.getCredential();
        if (credential == null) {
            Toast.makeText(requireContext(), "Credenciais não disponíveis, por favor reinicie.", Toast.LENGTH_SHORT).show();
            return;
        }

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
            Toast.makeText(requireContext(), "URL do canal não disponível", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onFavoriteClick(Channel channel, boolean isFavorite) {
        if (isFavorite) {
            favoritesManager.removeFavorite(channel);
            Toast.makeText(requireContext(), "Removido dos favoritos", Toast.LENGTH_SHORT).show();
        } else {
            favoritesManager.addFavorite(channel);
            Toast.makeText(requireContext(), "Adicionado aos favoritos", Toast.LENGTH_SHORT).show();
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
    public void onResume() {
        super.onResume();
        
        // Refresh current view to update favorites and history
        if (currentCategory.equals("FAVORITOS")) {
            channelAdapter.filterByCategory("FAVORITOS");
        } else if (currentCategory.equals("HISTÓRICO")) {
            showHistory();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }

    // onBackPressed is handled by the Activity, not the Fragment directly
    // private void showSpecificCategory(String categoryName) is now public
    public void showSpecificCategory(String categoryName) {
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
