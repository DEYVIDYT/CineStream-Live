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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.videolan.libvlc.util.VLCVideoLayout;

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
    private VLCVideoLayout vlcVideoLayout;
    private VlcVideoPlayer vlcVideoPlayer;
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
    private ProgressBar videoLoadingProgressBar;
    private ImageView fullscreenButton;
    private TextView networkSpeedTextView;
    private FrameLayout videoContainer;
    private LinearLayout headerContainer;
    private LinearLayout mainContentContainer;
    private NetworkSpeedMonitor networkSpeedMonitor;
    private boolean isSearchVisible = false;
    private String currentCategory = "TODOS";
    private List<Channel> allChannels;
    private List<Category> allCategories;
    private boolean isFullscreen = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        networkSpeedMonitor = new NetworkSpeedMonitor(new NetworkSpeedMonitor.SpeedListener() {
            @Override
            public void onSpeedChanged(double downloadSpeed, double uploadSpeed) {
                if (networkSpeedTextView != null) {
                    networkSpeedTextView.setText(String.format("%.2f KB/s", downloadSpeed / 1024));
                }
            }
        });
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
        observeViewModel();

        return view;
    }

    private void initViews(View view) {
        vlcVideoLayout = view.findViewById(R.id.vlcVideoLayout);
        videoContainer = view.findViewById(R.id.videoContainer);
        headerContainer = view.findViewById(R.id.headerContainer);
        mainContentContainer = view.findViewById(R.id.mainContentContainer);
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        channelsRecyclerView = view.findViewById(R.id.channelsRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        searchContainer = view.findViewById(R.id.searchContainer);
        searchIcon = view.findViewById(R.id.searchIcon);
        clearSearchIcon = view.findViewById(R.id.clearSearchIcon);
        videoLoadingProgressBar = view.findViewById(R.id.videoLoadingProgressBar);
        fullscreenButton = view.findViewById(R.id.fullscreenButton);
        networkSpeedTextView = view.findViewById(R.id.networkSpeedTextView);
        
        searchIcon.setOnClickListener(v -> toggleSearch());
        clearSearchIcon.setOnClickListener(v -> clearSearch());
        fullscreenButton.setOnClickListener(v -> toggleFullscreen());
    }

    private void toggleFullscreen() {
        if (vlcVideoPlayer == null) {
            Toast.makeText(requireContext(), "Nenhum canal em reprodução", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isFullscreen) {
            exitFullScreen();
        } else {
            enterFullScreen();
        }
    }

    private void enterFullScreen() {
        isFullscreen = true;
        
        // Mudar orientação para landscape
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Esconder elementos da UI
        if (headerContainer != null) {
            headerContainer.setVisibility(View.GONE);
        }
        if (mainContentContainer != null) {
            mainContentContainer.setVisibility(View.GONE);
        }
        
        // Esconder navegação da activity
        if (getActivity() != null) {
            View navigationTabs = getActivity().findViewById(R.id.navigation_tabs);
            if (navigationTabs != null) {
                navigationTabs.setVisibility(View.GONE);
            }
            
            // Esconder barras do sistema
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        // Expandir o container do vídeo para ocupar toda a tela
        if (videoContainer != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            videoContainer.setLayoutParams(params);
        }
        
        // Atualizar ícone do botão fullscreen
        fullscreenButton.setImageResource(R.drawable.ic_fullscreen_exit);
    }

    private void exitFullScreen() {
        isFullscreen = false;
        
        // Voltar para portrait
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Mostrar elementos da UI
        if (headerContainer != null) {
            headerContainer.setVisibility(View.VISIBLE);
        }
        if (mainContentContainer != null) {
            mainContentContainer.setVisibility(View.VISIBLE);
        }
        
        // Mostrar navegação da activity
        if (getActivity() != null) {
            View navigationTabs = getActivity().findViewById(R.id.navigation_tabs);
            if (navigationTabs != null) {
                navigationTabs.setVisibility(View.VISIBLE);
            }
            
            // Mostrar barras do sistema
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

        // Restaurar tamanho original do container do vídeo
        if (videoContainer != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.player_height)
            );
            videoContainer.setLayoutParams(params);
        }
        
        // Atualizar ícone do botão fullscreen
        fullscreenButton.setImageResource(R.drawable.ic_fullscreen);
    }

    // Método público para ser chamado pela HostActivity quando o botão voltar for pressionado
    public boolean onBackPressed() {
        if (isFullscreen) {
            exitFullScreen();
            return true; // Consome o evento de voltar
        }
        return false; // Permite que a activity trate o evento normalmente
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // A lógica agora é tratada pelos métodos enterFullScreen e exitFullScreen
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
    
    private void playStream(String streamUrl) {
        if (streamUrl != null && !streamUrl.isEmpty()) {
            videoLoadingProgressBar.setVisibility(View.VISIBLE);
            if (vlcVideoPlayer != null) {
                vlcVideoPlayer.stop();
            } else {
                vlcVideoPlayer = new VlcVideoPlayer(requireContext(), vlcVideoLayout);
            }
            vlcVideoPlayer.play(streamUrl);
            videoLoadingProgressBar.setVisibility(View.GONE);
            networkSpeedTextView.setVisibility(View.VISIBLE);
            networkSpeedMonitor.start();
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
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.release();
        }
        if (networkSpeedMonitor != null) {
            networkSpeedMonitor.stop();
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
