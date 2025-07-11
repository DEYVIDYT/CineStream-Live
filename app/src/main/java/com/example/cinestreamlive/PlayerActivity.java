package com.example.cinestreamlive;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView; // Adicionado
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog; // Adicionado (androidx)
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

public class PlayerActivity extends AppCompatActivity implements EpgChannelAdapter.OnEpgChannelClickListener {

    private VideoView videoView;
    private ProgressBar playerProgressBar;

    // IDs para o novo painel EPG lateral (de epg_panel_left.xml)
    private FrameLayout epgPanelContainer;
    private TextView epgCategoryNameTextView;
    private RecyclerView epgChannelsRecyclerView;

    private CategoryAdapter categoryAdapterForDialog;
    private EpgChannelAdapter epgChannelAdapter;

    private List<Category> allCategoriesList = new ArrayList<>();
    private List<Channel> currentEpgChannelList = new ArrayList<>();

    private String currentChannelUrl;
    private Credential credential;
    private XtreamCodesService xtreamService;

    private boolean isEpgPanelVisible = false; // Nome da variável de estado do EPG atualizado
    private boolean categoriesLoaded = false;
    private int currentSelectedCategoryPositionInDialog = 0; // Para o diálogo

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private int reloadAttempts = 0;
    private static final int MAX_RELOAD_ATTEMPTS = 3;

    private AlertDialog categoriesDialog; // AlertDialog do androidx

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        videoView = findViewById(R.id.videoView);
        playerProgressBar = findViewById(R.id.playerProgressBar);

        epgPanelContainer = findViewById(R.id.epg_panel_container);
        epgCategoryNameTextView = findViewById(R.id.epg_category_name_panel);
        epgChannelsRecyclerView = findViewById(R.id.epg_channels_list_panel);

        currentChannelUrl = getIntent().getStringExtra("initial_channel_url");
        credential = (Credential) getIntent().getSerializableExtra("credential");

        if (credential == null) {
            Toast.makeText(this, "Erro: Credenciais não encontradas.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        xtreamService = new XtreamCodesService();

        if (currentChannelUrl != null && !currentChannelUrl.isEmpty()) {
            playVideo(currentChannelUrl);
        } else {
            Toast.makeText(this, "Erro: URL do canal não encontrada.", Toast.LENGTH_LONG).show();
            finish();
        }
        setupEpg();
    }

    public void playVideo(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL do canal inválida.", Toast.LENGTH_SHORT).show();
            if (playerProgressBar != null) playerProgressBar.setVisibility(View.GONE);
            return;
        }

        if (!url.equals(this.currentChannelUrl)) {
            this.reloadAttempts = 0;
        }

        this.currentChannelUrl = url;
        playerProgressBar.setVisibility(View.VISIBLE);
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {
            playerProgressBar.setVisibility(View.GONE);
            mp.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            playerProgressBar.setVisibility(View.GONE);
            videoView.stopPlayback();

            reloadAttempts++;
            if (reloadAttempts <= MAX_RELOAD_ATTEMPTS) {
                String reloadMsg = "Canal travou. Tentando recarregar... (" + reloadAttempts + "/" + MAX_RELOAD_ATTEMPTS + ")";
                Toast.makeText(PlayerActivity.this, reloadMsg, Toast.LENGTH_SHORT).show();
                mainThreadHandler.postDelayed(() -> {
                    if (currentChannelUrl != null && !isFinishing()) {
                        playVideo(currentChannelUrl);
                    }
                }, 2000);
            } else {
                Toast.makeText(PlayerActivity.this, "Falha ao carregar o canal após " + MAX_RELOAD_ATTEMPTS + " tentativas.", Toast.LENGTH_LONG).show();
            }
            return true;
        });
    }

    private void setupEpg() {
        if (epgChannelsRecyclerView != null) {
            epgChannelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            epgChannelAdapter = new EpgChannelAdapter(this, currentEpgChannelList, this);
            epgChannelsRecyclerView.setAdapter(epgChannelAdapter);
        }

        categoryAdapterForDialog = new CategoryAdapter(allCategoriesList, (category, position) -> {
            if (categoriesDialog != null && categoriesDialog.isShowing()) {
                categoriesDialog.dismiss();
            }
            selectCategory(category);
        });

        if (epgCategoryNameTextView != null) {
            epgCategoryNameTextView.setOnClickListener(v -> showCategorySelectionDialog());
        }
    }

    private void toggleEpgPanelVisibility() {
        isEpgPanelVisible = !isEpgPanelVisible; // Use a nova variável de estado
        if (epgPanelContainer == null) return;

        if (isEpgPanelVisible) {
            epgPanelContainer.setVisibility(View.VISIBLE);
            videoView.pause();
            if (!categoriesLoaded) {
                loadInitialCategoriesAndChannels();
            } else {
                if (epgChannelsRecyclerView != null && epgChannelAdapter != null && epgChannelAdapter.getItemCount() > 0) {
                    epgChannelsRecyclerView.requestFocus();
                } else if (epgCategoryNameTextView != null) {
                    epgCategoryNameTextView.requestFocus();
                }
            }
        } else {
            epgPanelContainer.setVisibility(View.GONE);
            if (currentChannelUrl != null && !videoView.isPlaying() && videoView.canSeekForward()) {
                videoView.start();
            }
        }
    }

    private void loadInitialCategoriesAndChannels() {
        executorService.execute(() -> {
            try {
                final List<Category> fetchedCategories = xtreamService.getLiveCategories(credential);
                mainThreadHandler.post(() -> {
                    allCategoriesList.clear();
                    allCategoriesList.addAll(fetchedCategories);
                    if (categoryAdapterForDialog != null) {
                        categoryAdapterForDialog.updateCategories(fetchedCategories);
                    }
                    categoriesLoaded = true;
                    if (!allCategoriesList.isEmpty()) {
                        selectCategory(allCategoriesList.get(0));
                    } else {
                        if(epgCategoryNameTextView != null) epgCategoryNameTextView.setText(getString(R.string.no_categories_found));
                    }
                });
            } catch (Exception e) {
                mainThreadHandler.post(() -> Toast.makeText(PlayerActivity.this, getString(R.string.error_loading_categories) + ": " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void selectCategory(Category category) {
        if (category == null) return;

        int foundPosition = -1;
        for(int i=0; i < allCategoriesList.size(); i++){
            if(allCategoriesList.get(i).getCategoryId().equals(category.getCategoryId())){
                foundPosition = i;
                break;
            }
        }
        if(foundPosition != -1) {
            currentSelectedCategoryPositionInDialog = foundPosition;
             if (categoryAdapterForDialog != null) categoryAdapterForDialog.setSelectedPosition(currentSelectedCategoryPositionInDialog);
        }

        if (epgCategoryNameTextView != null) {
            epgCategoryNameTextView.setText(category.getCategoryName().toUpperCase());
        }
        loadChannelsForCategory(category.getCategoryId(), category.getCategoryName());
    }

    private void showCategorySelectionDialog() {
        if (!categoriesLoaded || allCategoriesList.isEmpty()) {
            Toast.makeText(this, R.string.categories_not_loaded_yet, Toast.LENGTH_SHORT).show();
            if (!categoriesLoaded) loadInitialCategoriesAndChannels();
            return;
        }

        if (categoryAdapterForDialog == null) {
             categoryAdapterForDialog = new CategoryAdapter(allCategoriesList, (cat, pos) -> {
                if (categoriesDialog != null && categoriesDialog.isShowing()) {
                    categoriesDialog.dismiss();
                }
                selectCategory(cat);
            });
        } else {
            categoryAdapterForDialog.updateCategories(allCategoriesList);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_category_title);

        RecyclerView categoryDialogRecyclerView = new RecyclerView(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        categoryDialogRecyclerView.setLayoutManager(layoutManager);
        categoryDialogRecyclerView.setAdapter(categoryAdapterForDialog);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        categoryDialogRecyclerView.setPadding(padding, padding / 2, padding, padding / 2);

        builder.setView(categoryDialogRecyclerView);
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

        categoriesDialog = builder.create();
        categoriesDialog.show();

        if (currentSelectedCategoryPositionInDialog != RecyclerView.NO_POSITION && currentSelectedCategoryPositionInDialog < allCategoriesList.size()) {
            categoryAdapterForDialog.setSelectedPosition(currentSelectedCategoryPositionInDialog);
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(currentSelectedCategoryPositionInDialog, 0);
            }
        }
    }

    private void loadChannelsForCategory(String categoryId, String categoryName) {
        Toast.makeText(this, getString(R.string.loading_category_channels, categoryName), Toast.LENGTH_SHORT).show();
        currentEpgChannelList.clear();
        if(epgChannelAdapter != null) epgChannelAdapter.notifyDataSetChanged();

        executorService.execute(() -> {
            try {
                List<Channel> fetchedChannels = xtreamService.fetchLiveStreamsForCategory(credential, categoryId);

                Map<String, List<EpgEvent>> epgDataMap = null;
                if (!fetchedChannels.isEmpty()) {
                    try {
                        epgDataMap = xtreamService.fetchEpgDataForCategory(credential, categoryId);
                    } catch (Exception epgEx) {
                        // Não fazer nada, continuar sem EPG se falhar
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
                    currentEpgChannelList.addAll(fetchedChannels);
                    if (epgChannelAdapter != null) epgChannelAdapter.updateChannels(fetchedChannels);

                    if (epgChannelsRecyclerView != null) {
                        if (!fetchedChannels.isEmpty()) {
                            epgChannelsRecyclerView.setVisibility(View.VISIBLE);
                            if (isEpgPanelVisible) epgChannelsRecyclerView.requestFocus(); // Focar na lista de canais
                        } else {
                            Toast.makeText(PlayerActivity.this, "Nenhum canal encontrado em " + categoryName, Toast.LENGTH_SHORT).show();
                            epgChannelsRecyclerView.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                mainThreadHandler.post(() -> Toast.makeText(PlayerActivity.this, "Erro ao carregar canais: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onEpgChannelClick(Channel channel) {
        String streamUrl = xtreamService.getChannelStreamUrl(credential, channel);
        if (streamUrl != null) {
            playVideo(streamUrl);
            toggleEpgPanelVisibility();
        } else {
            Toast.makeText(this, "Não foi possível obter a URL para: " + channel.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isEpgPanelVisible) { // Usar a nova variável de estado
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                toggleEpgPanelVisibility();
                return true;
            }
            View focusedView = getCurrentFocus();
            if (focusedView == epgCategoryNameTextView && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (epgChannelsRecyclerView != null && epgChannelAdapter != null && epgChannelAdapter.getItemCount() > 0) {
                    epgChannelsRecyclerView.requestFocus();
                    return true;
                }
            }
            if (focusedView != null && epgChannelsRecyclerView != null && (focusedView == epgChannelsRecyclerView || focusedView.getParent() == epgChannelsRecyclerView) ) {
                 LinearLayoutManager lm = (LinearLayoutManager) epgChannelsRecyclerView.getLayoutManager();
                 if (lm != null && lm.findFirstCompletelyVisibleItemPosition() == 0 && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                     if (epgCategoryNameTextView != null) {
                        epgCategoryNameTextView.requestFocus();
                        return true;
                     }
                 }
            }

        } else {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                toggleEpgPanelVisibility();
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
        if (!isEpgPanelVisible && currentChannelUrl != null && !videoView.isPlaying() && videoView.canSeekForward()) {
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
        if (categoriesDialog != null && categoriesDialog.isShowing()) {
            categoriesDialog.dismiss();
        }
    }
}
