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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;

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
    private GestureDetector gestureDetector;

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
        
        // Configurar detector de gestos para duplo toque no player
        setupVideoGestures();
    }

    private void toggleFullscreen() {
        if (vlcVideoPlayer == null) {
            Toast.makeText(requireContext(), "Nenhum canal em reprodução", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Debug toast
        Toast.makeText(requireContext(), isFullscreen ? "Saindo do fullscreen" : "Entrando em fullscreen", Toast.LENGTH_SHORT).show();
        
        if (isFullscreen) {
            exitFullScreen();
        } else {
            // Pequeno delay para garantir que a UI esteja estável antes de aplicar fullscreen
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                enterFullScreen();
                // Aplicar novamente após um delay para garantir que pegou
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isFullscreen) {
                        enforceFullscreenMode();
                    }
                }, 1000);
            }, 100);
        }
    }

    private void enterFullScreen() {
        isFullscreen = true;
        
        // Salvar estado do player antes de mudar orientação
        boolean wasPlaying = vlcVideoPlayer != null && vlcVideoPlayer.isPlaying();
        String currentUrl = null;
        if (wasPlaying) {
            // Não parar o player, apenas salvar estado
            currentUrl = "playing"; // Flag para indicar que estava reproduzindo
        }
        
        // Aplicar configurações da Window primeiro
        if (getActivity() != null) {
            // Definir flags da window
            getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            
            // Aplicar system UI flags mais agressivos
            View decorView = getActivity().getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            
            decorView.setSystemUiVisibility(uiOptions);
            
            // Forçar aplicação imediata
            decorView.requestApplyInsets();
        }
        
        // Esconder elementos da UI do app
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
        }
        
        // Expandir container do vídeo APÓS esconder outros elementos
        if (videoContainer != null) {
            // Usar LinearLayout.LayoutParams porque o videoContainer está dentro de um LinearLayout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            videoContainer.setLayoutParams(params);
            
            // Configurações adicionais para manter vídeo ativo
            videoContainer.setKeepScreenOn(true);
            videoContainer.requestLayout();
        }
        
        // Configurar VLC para fullscreen após layout
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.setAspectRatio(null); // Aspect ratio automático
            vlcVideoPlayer.setScale(0); // Scale automático
        }
        
        // Mudar orientação por último para evitar parar o vídeo
        if (getActivity() != null) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        
        // Atualizar ícone do botão
        if (fullscreenButton != null) {
            fullscreenButton.setImageResource(R.drawable.ic_fullscreen_exit);
        }
        
        // Configurar listener para reforçar o modo imersivo
        if (getActivity() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if (isFullscreen && (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // Reaplicar flags após 1 segundo
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isFullscreen && getActivity() != null) {
                            enforceFullscreenMode();
                        }
                    }, 1000);
                }
            });
        }
    }

    private void enforceFullscreenMode() {
        if (getActivity() != null && isFullscreen) {
            // Reaplicar window flags
            getActivity().getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            
            // Reaplicar system UI flags
            View decorView = getActivity().getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    
    private void exitFullScreen() {
        isFullscreen = false;
        
        // Remover listener de sistema UI primeiro
        if (getActivity() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(null);
            
            // Limpar window flags
            getActivity().getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
            
            // Restaurar barras do sistema
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        
        // Voltar para portrait
        if (getActivity() != null) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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
        }

        // Restaurar tamanho original do container do vídeo
        if (videoContainer != null) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.player_height)
            );
            videoContainer.setLayoutParams(params);
            
            // Desativar keep screen on
            videoContainer.setKeepScreenOn(false);
        }
        
        // Restaurar configurações do VLC
        if (vlcVideoPlayer != null) {
            vlcVideoPlayer.setAspectRatio(null);
            vlcVideoPlayer.setScale(0);
        }
        
        // Atualizar ícone do botão
        if (fullscreenButton != null) {
            fullscreenButton.setImageResource(R.drawable.ic_fullscreen);
        }
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
        
        // Não parar o vídeo durante mudanças de orientação
        // Apenas ajustar a UI conforme necessário
        
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Se entrou em landscape mas não está em fullscreen
            if (!isFullscreen && vlcVideoPlayer != null && vlcVideoPlayer.isPlaying()) {
                // Opcionalmente sugerir fullscreen, mas não forçar
                // showFullscreenSuggestion();
            }
            // Se já está em fullscreen, reforçar configurações
            else if (isFullscreen) {
                // Pequeno delay para garantir que a rotação foi concluída
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    enforceFullscreenMode();
                    adjustPlayerForOrientation(Configuration.ORIENTATION_LANDSCAPE);
                }, 500);
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Se rotacionou para portrait em fullscreen, sair do fullscreen
            if (isFullscreen) {
                // Delay pequeno para evitar conflitos
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    exitFullScreen();
                }, 300);
            }
        }
    }
    
    private void adjustPlayerForOrientation(int orientation) {
        if (vlcVideoPlayer != null && vlcVideoPlayer.isPlaying()) {
            // Ajustar aspect ratio baseado na orientação
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // Em landscape, usar configurações otimizadas para tela larga
                vlcVideoPlayer.setScale(0); // Auto-scale
            } else {
                // Em portrait, manter proporções adequadas
                vlcVideoPlayer.setScale(0); // Auto-scale
            }
        }
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

    private void setupVideoGestures() {
        gestureDetector = new GestureDetector(requireContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Duplo toque no player para alternar fullscreen
                if (vlcVideoPlayer != null && vlcVideoPlayer.isPlaying()) {
                    toggleFullscreen();
                    return true;
                }
                return false;
            }
            
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Toque simples para mostrar/esconder controles em fullscreen
                if (isFullscreen) {
                    toggleFullscreenControls();
                    return true;
                }
                return false;
            }
        });
        
        // Aplicar o detector de gestos ao container do vídeo
        if (videoContainer != null) {
            videoContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
    }
    
    private void toggleFullscreenControls() {
        if (fullscreenButton != null) {
            if (fullscreenButton.getVisibility() == View.VISIBLE) {
                // Esconder controles
                fullscreenButton.setVisibility(View.GONE);
                if (networkSpeedTextView != null) {
                    networkSpeedTextView.setVisibility(View.GONE);
                }
                
                // Mostrar controles novamente após 3 segundos
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isFullscreen && fullscreenButton != null) {
                        fullscreenButton.setVisibility(View.VISIBLE);
                        if (networkSpeedTextView != null) {
                            networkSpeedTextView.setVisibility(View.VISIBLE);
                        }
                    }
                }, 3000);
            } else {
                // Mostrar controles
                fullscreenButton.setVisibility(View.VISIBLE);
                if (networkSpeedTextView != null) {
                    networkSpeedTextView.setVisibility(View.VISIBLE);
                }
            }
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
