package com.example.cinestreamlive;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinestreamlive.model.Category;
import com.example.cinestreamlive.model.Channel;
import com.example.cinestreamlive.model.Credential;
import com.example.cinestreamlive.model.EpgEvent; // Import EpgEvent

import java.util.ArrayList;
import java.util.List;
import java.util.Map; // For EPG data
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener, EpgChannelAdapter.OnEpgChannelClickListener {

    private static final String TAG = "PlayerActivity";

    private VideoView videoView;
    private ProgressBar playerProgressBar;
    private FrameLayout epgOverlayContainer;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView channelsEpgRecyclerView;
    private MediaController mediaController;

    private CategoryAdapter categoryAdapter;
    private EpgChannelAdapter epgChannelAdapter;

    private List<Category> categoryList = new ArrayList<>();
    private List<Channel> currentEpgChannelList = new ArrayList<>();

    private String currentChannelUrl;
    private Credential credential;
    private XtreamCodesService xtreamService;

    private boolean isEpgVisible = false;
    private boolean categoriesLoaded = false;
    private int currentSelectedCategoryPosition = 0;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        playerProgressBar = findViewById(R.id.playerProgressBar);
        epgOverlayContainer = findViewById(R.id.epg_overlay_container);
        categoriesRecyclerView = findViewById(R.id.categories_recyclerview);
        channelsEpgRecyclerView = findViewById(R.id.channels_epg_recyclerview);

        currentChannelUrl = getIntent().getStringExtra("initial_channel_url");
        credential = (Credential) getIntent().getSerializableExtra("credential");

        if (credential == null) {
            Log.e(TAG, "Credential não recebida. Fechando PlayerActivity.");
            Toast.makeText(this, "Erro: Credenciais não encontradas.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        xtreamService = new XtreamCodesService();

        if (currentChannelUrl != null && !currentChannelUrl.isEmpty()) {
            playVideo(currentChannelUrl);
        } else {
            Log.e(TAG, "URL do canal inicial não recebida. Fechando PlayerActivity.");
            Toast.makeText(this, "Erro: URL do canal não encontrada.", Toast.LENGTH_LONG).show();
            finish();
        }
        setupEpg();
    }

    public void playVideo(String url) {
        this.currentChannelUrl = url;
        Log.d(TAG, "playVideo: " + url);
        playerProgressBar.setVisibility(View.VISIBLE);
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);

        if (mediaController == null) {
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
        }
        videoView.setMediaController(mediaController);
        videoView.requestFocus();

        videoView.setOnPreparedListener(mp -> {
            playerProgressBar.setVisibility(View.GONE);
            mp.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            playerProgressBar.setVisibility(View.GONE);
            Log.e(TAG, "Erro no VideoView: o que=" + what + ", extra=" + extra + " para URL: " + currentChannelUrl);
            Toast.makeText(PlayerActivity.this, "Erro ao reproduzir o canal (" + what + ", " + extra + ")", Toast.LENGTH_LONG).show();
            return true;
        });
    }

    private void setupEpg() {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter(categoryList, this);
        categoriesRecyclerView.setAdapter(categoryAdapter);

        channelsEpgRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        epgChannelAdapter = new EpgChannelAdapter(this, currentEpgChannelList, this);
        channelsEpgRecyclerView.setAdapter(epgChannelAdapter);
    }

    private void toggleEpgVisibility() {
        isEpgVisible = !isEpgVisible;
        if (isEpgVisible) {
            epgOverlayContainer.setVisibility(View.VISIBLE);
            if (mediaController != null && mediaController.isShowing()) {
                mediaController.hide();
            }
            videoView.setMediaController(null);
            videoView.pause();
            if (!categoriesLoaded) {
                loadCategories();
            } else {
                 categoriesRecyclerView.requestFocus();
            }
        } else {
            epgOverlayContainer.setVisibility(View.GONE);
            videoView.setMediaController(mediaController);
            if (currentChannelUrl != null && !videoView.isPlaying()) {
                videoView.start();
            }
        }
    }

    private void loadCategories() {
        Log.d(TAG, "Carregando categorias...");
        executorService.execute(() -> {
            try {
                List<Category> fetchedCategories = xtreamService.getLiveCategories(credential);
                mainThreadHandler.post(() -> {
                    Log.d(TAG, "Categorias recebidas: " + fetchedCategories.size());
                    categoryList.clear();
                    categoryList.addAll(fetchedCategories);
                    categoryAdapter.updateCategories(fetchedCategories);
                    categoriesLoaded = true;
                    if (!categoryList.isEmpty()) {
                        onCategoryClick(categoryList.get(0), 0);
                        categoryAdapter.setSelectedPosition(0);
                        categoriesRecyclerView.requestFocus();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar categorias: ", e);
                mainThreadHandler.post(() -> Toast.makeText(PlayerActivity.this, "Erro ao carregar categorias: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadChannelsForCategory(String categoryId, String categoryName) {
        Log.d(TAG, "Carregando canais para categoria: " + categoryName + " (ID: " + categoryId + ")");
        Toast.makeText(this, "Carregando: " + categoryName, Toast.LENGTH_SHORT).show();
        currentEpgChannelList.clear(); // Limpa a lista de canais antes de carregar novos
        epgChannelAdapter.notifyDataSetChanged(); // Notifica o adapter para mostrar a lista vazia (ou um loader)


        executorService.execute(() -> {
            try {
                // 1. Buscar canais
                List<Channel> fetchedChannels = xtreamService.fetchLiveStreamsForCategory(credential, categoryId);

                // 2. Buscar dados de EPG para esses canais/categoria
                Map<String, List<EpgEvent>> epgDataMap = null;
                if (!fetchedChannels.isEmpty()) {
                    try {
                        // Usar o categoryId da categoria selecionada, não dos canais individuais,
                        // para fetchEpgDataForCategory, pois é mais eficiente.
                        epgDataMap = xtreamService.fetchEpgDataForCategory(credential, categoryId);
                    } catch (Exception epgEx) {
                        Log.e(TAG, "Erro ao buscar dados de EPG para categoria " + categoryName + ": ", epgEx);
                        // Continuar mesmo sem dados de EPG
                    }
                }

                // 3. Processar e combinar dados
                final Map<String, List<EpgEvent>> finalEpgDataMap = epgDataMap;
                for (Channel channel : fetchedChannels) {
                    if (finalEpgDataMap != null && channel.getEpgChannelId() != null) {
                        List<EpgEvent> eventsForChannel = finalEpgDataMap.get(channel.getEpgChannelId());
                        if (eventsForChannel != null && !eventsForChannel.isEmpty()) {
                            long currentTimeSeconds = System.currentTimeMillis() / 1000;
                            for (EpgEvent event : eventsForChannel) {
                                if (currentTimeSeconds >= event.getStartEpochSeconds() && currentTimeSeconds < event.getEndEpochSeconds()) {
                                    channel.setCurrentEpgEvent(event);
                                    break;
                                }
                            }
                        }
                    }
                }

                mainThreadHandler.post(() -> {
                    Log.d(TAG, "Canais recebidos para " + categoryName + ": " + fetchedChannels.size());
                    // currentEpgChannelList já foi limpa
                    currentEpgChannelList.addAll(fetchedChannels);
                    epgChannelAdapter.updateChannels(fetchedChannels); // Notifica o adapter com os canais (e seus EPGs)
                    if (!fetchedChannels.isEmpty()) {
                        channelsEpgRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(PlayerActivity.this, "Nenhum canal encontrado em " + categoryName, Toast.LENGTH_SHORT).show();
                        channelsEpgRecyclerView.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar canais para " + categoryName + ": ", e);
                mainThreadHandler.post(() -> Toast.makeText(PlayerActivity.this, "Erro ao carregar canais: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        Log.d(TAG, "Categoria clicada: " + category.getCategoryName());
        currentSelectedCategoryPosition = position;
        categoryAdapter.setSelectedPosition(position);
        loadChannelsForCategory(category.getCategoryId(), category.getCategoryName());
    }

    @Override
    public void onEpgChannelClick(Channel channel) {
        Log.d(TAG, "Canal do EPG clicado: " + channel.getName());
        String streamUrl = xtreamService.getChannelStreamUrl(credential, channel);
        if (streamUrl != null) {
            playVideo(streamUrl);
            toggleEpgVisibility();
        } else {
            Toast.makeText(this, "Não foi possível obter a URL para: " + channel.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isEpgVisible) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                toggleEpgVisibility();
                return true;
            }
            View focusedView = getCurrentFocus();
            if (focusedView != null) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (focusedView.getParent() == categoriesRecyclerView) {
                        if (!currentEpgChannelList.isEmpty()) {
                            channelsEpgRecyclerView.requestFocus();
                            return true;
                        }
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                     if (focusedView.getParent() == channelsEpgRecyclerView) {
                        categoriesRecyclerView.requestFocus();
                        return true;
                    }
                }
            }
        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                toggleEpgVisibility();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isEpgVisible && currentChannelUrl != null && !videoView.isPlaying() && videoView.canSeekForward()) {
             videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
