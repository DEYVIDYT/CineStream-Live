package com.example.cinestreamlive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cinestreamlive.model.Channel;
import com.example.cinestreamlive.model.Credential;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;
    private TextView loadingText;
    private RecyclerView recyclerViewChannels;
    private ChannelAdapter channelAdapter;
    private List<Channel> channelList = new ArrayList<>();
    private XtreamCodesService xtreamService;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loading_text);
        recyclerViewChannels = findViewById(R.id.recyclerViewChannels);

        xtreamService = new XtreamCodesService();

        setupRecyclerView();
        loadCredentialsAndChannels();
    }

    private void showLoading(boolean isLoading, String message) {
        mainThreadHandler.post(() -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
                loadingText.setText(message != null ? message : getString(R.string.loading_channels));
                recyclerViewChannels.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
                recyclerViewChannels.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadCredentialsAndChannels() {
        showLoading(true, "Carregando credenciais...");
        executorService.execute(() -> {
            try {
                Credential credential = xtreamService.fetchAndSelectRandomCredential();
                if (credential != null) {
                    Log.d(TAG, "Credencial selecionada: " + credential.getServer() + " User: " + credential.getUsername());
                    mainThreadHandler.post(() -> showLoading(true, "Carregando canais de: " + credential.getServer()));
                    List<Channel> channels = xtreamService.fetchLiveStreams(credential);
                    Log.d(TAG, "Canais carregados: " + channels.size());
                    mainThreadHandler.post(() -> {
                        channelList.clear();
                        channelList.addAll(channels);
                        channelAdapter.updateChannels(channels); // Notifica o adapter
                        showLoading(false, null);
                        if (channels.isEmpty()){
                            Toast.makeText(MainActivity.this, "Nenhum canal ao vivo encontrado.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    throw new Exception("Nenhuma credencial pôde ser selecionada.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar dados: ", e);
                mainThreadHandler.post(() -> {
                    showLoading(false, null);
                    Toast.makeText(MainActivity.this, "Erro ao carregar canais: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadingText.setText("Falha ao carregar: " + e.getMessage());
                    loadingText.setVisibility(View.VISIBLE); // Mostrar texto de erro
                });
            }
        });
    }

    private void setupRecyclerView() {
        // O layout manager já está definido no XML, mas podemos confirmar aqui se quisermos
        // recyclerViewChannels.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        channelAdapter = new ChannelAdapter(this, channelList, channel -> {
            String streamUrl = xtreamService.getChannelStreamUrl(channel);
            if (streamUrl != null) {
                Log.d(TAG, "Iniciando player para: " + channel.getName() + " URL: " + streamUrl);
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("channel_url", streamUrl);
                startActivity(intent);
            } else {
                Log.e(TAG, "URL do stream não pôde ser gerada para o canal: " + channel.getName());
                Toast.makeText(this, "Não foi possível obter a URL para este canal.", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewChannels.setAdapter(channelAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
