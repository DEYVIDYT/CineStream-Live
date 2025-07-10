package com.example.cinestreamlive;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
// import android.widget.MediaController; // Removido
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinestreamlive.model.Category;
import com.example.cinestreamlive.model.Channel;
import com.example.cinestreamlive.model.Credential;
import com.example.cinestreamlive.model.EpgEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener, EpgChannelAdapter.OnEpgChannelClickListener {

    private static final String TAG = "PlayerActivity";

    private VideoView videoView;
    private ProgressBar playerProgressBar;
    private FrameLayout epgOverlayContainer;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView channelsEpgRecyclerView;
    // private MediaController mediaController; // Removido

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

    // Variáveis para controle de recarregamento
    private int reloadAttempts = 0;
    private static final int MAX_RELOAD_ATTEMPTS = 3;

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
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "playVideo chamado com URL nula ou vazia.");
            Toast.makeText(this, "URL do canal inválida.", Toast.LENGTH_SHORT).show();
            if (playerProgressBar != null) playerProgressBar.setVisibility(View.GONE);
            return;
        }

        // Se a URL for diferente da atual, é um novo canal (ou o primeiro canal).
        // Então, resetar as tentativas de recarregamento.
        if (!url.equals(this.currentChannelUrl)) {
            this.reloadAttempts = 0;
            Log.d(TAG, "Novo canal selecionado, tentativas de recarregamento resetadas.");
        }

        this.currentChannelUrl = url;
        Log.d(TAG, "playVideo: " + url + " (Tentativa: " + (reloadAttempts + 1) + ")"); // Loga a tentativa atual
        playerProgressBar.setVisibility(View.VISIBLE);
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);

        // MediaController removido
        // if (mediaController == null) {
        //     mediaController = new MediaController(this);
        //     mediaController.setAnchorView(videoView);
        // }
        // videoView.setMediaController(mediaController);
        // videoView.requestFocus(); // Não mais necessário para MediaController

        videoView.setOnPreparedListener(mp -> {
            playerProgressBar.setVisibility(View.GONE);
            mp.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Erro no VideoView: o que=" + what + ", extra=" + extra + " para URL: " + currentChannelUrl + ". Tentativas: " + reloadAttempts);
            playerProgressBar.setVisibility(View.GONE); // Esconde a barra de progresso principal
            videoView.stopPlayback(); // Para o playback atual

            reloadAttempts++;
            if (reloadAttempts <= MAX_RELOAD_ATTEMPTS) {
                String reloadMsg = "Canal travou. Tentando recarregar... (" + reloadAttempts + "/" + MAX_RELOAD_ATTEMPTS + ")";
                Toast.makeText(PlayerActivity.this, reloadMsg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, reloadMsg + " URL: " + currentChannelUrl);
                // Postar com atraso para não sobrecarregar e dar tempo para a rede/stream se recuperar
                mainThreadHandler.postDelayed(() -> {
                    if (currentChannelUrl != null && !isFinishing()) { // Verifica se a activity ainda é válida
                        playVideo(currentChannelUrl);
                    }
                }, 2000); // Atraso de 2 segundos
            } else {
                Log.e(TAG, "Máximo de tentativas de recarregamento atingido para: " + currentChannelUrl);
                Toast.makeText(PlayerActivity.this, "Falha ao carregar o canal após " + MAX_RELOAD_ATTEMPTS + " tentativas.", Toast.LENGTH_LONG).show();
                // Não resetar reloadAttempts aqui, será resetado quando um *novo* canal for selecionado.
            }
            return true; // Indica que o erro foi tratado.
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
            // if (mediaController != null && mediaController.isShowing()) { // Removido
            //     mediaController.hide();
            // }
            // videoView.setMediaController(null); // Removido
            videoView.pause();
            if (!categoriesLoaded) {
                loadCategories();
            } else {
                 categoriesRecyclerView.requestFocus();
            }
        } else {
            epgOverlayContainer.setVisibility(View.GONE);
            // videoView.setMediaController(mediaController); // Removido
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
        currentEpgChannelList.clear();
        epgChannelAdapter.notifyDataSetChanged();


        executorService.execute(() -> {
            try {
                List<Channel> fetchedChannels = xtreamService.fetchLiveStreamsForCategory(credential, categoryId);

                Map<String, List<EpgEvent>> epgDataMap = null;
                if (!fetchedChannels.isEmpty()) {
                    try {
                        epgDataMap = xtreamService.fetchEpgDataForCategory(credential, categoryId);
                    } catch (Exception epgEx) {
                        Log.e(TAG, "Erro ao buscar dados de EPG para categoria " + categoryName + ": ", epgEx);
                    }
                }

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
                    currentEpgChannelList.addAll(fetchedChannels);
                    epgChannelAdapter.updateChannels(fetchedChannels);
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
                    // Se o foco está atualmente em algum lugar dentro do categoriesRecyclerView ou no próprio RecyclerView
                    if (focusedView == categoriesRecyclerView || (focusedView.getParent() instanceof RecyclerView && focusedView.getParent() == categoriesRecyclerView)) {
                        if (!currentEpgChannelList.isEmpty()) {
                            channelsEpgRecyclerView.requestFocus();
                            // Tentar definir o primeiro item como selecionado se nenhum estiver
                            if (epgChannelAdapter.getItemCount() > 0 &&
                                channelsEpgRecyclerView.findViewHolderForAdapterPosition(epgChannelAdapter.getSelectedPosition()) == null) {
                                // Se a posição selecionada não está visível ou é inválida, selecionar o primeiro.
                                // Ou, idealmente, o RecyclerView deveria tentar focar seu item selecionado.
                                // Por simplicidade, vamos garantir que o adapter tenha uma seleção válida.
                                if (epgChannelAdapter.getSelectedPosition() == RecyclerView.NO_POSITION) {
                                     epgChannelAdapter.setSelectedPosition(0);
                                }
                            }
                            return true;
                        }
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                     if (focusedView == channelsEpgRecyclerView || (focusedView.getParent() instanceof RecyclerView && focusedView.getParent() == channelsEpgRecyclerView)) {
                        categoriesRecyclerView.requestFocus();
                        // A categoria já deve estar selecionada visualmente via currentSelectedCategoryPosition
                        // categoryAdapter.setSelectedPosition(currentSelectedCategoryPosition); // Já é feito em onCategoryClick
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
