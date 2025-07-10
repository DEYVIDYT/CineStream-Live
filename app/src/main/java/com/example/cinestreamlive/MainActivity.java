package com.example.cinestreamlive;

import androidx.appcompat.app.AppCompatActivity;
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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ProgressBar progressBar;
    private TextView loadingText;
    private XtreamCodesService xtreamService;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // O layout activity_main.xml será simplificado para não ter RecyclerView
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loading_text);

        // Garantir que o RecyclerView não esteja mais sendo referenciado se ainda existir no layout antigo
        // View recyclerViewChannels = findViewById(R.id.recyclerViewChannels);
        // if (recyclerViewChannels != null) {
        //     recyclerViewChannels.setVisibility(View.GONE);
        // }


        xtreamService = new XtreamCodesService();
        loadInitialDataAndLaunchPlayer();
    }

    private void showLoading(boolean isLoading, String message) {
        mainThreadHandler.post(() -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
                loadingText.setText(message != null ? message : getString(R.string.loading_channels));
            } else {
                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
            }
        });
    }

    private void loadInitialDataAndLaunchPlayer() {
        showLoading(true, "Carregando credenciais...");
        executorService.execute(() -> {
            try {
                Credential credential = xtreamService.fetchAndSelectRandomCredential();
                if (credential == null) {
                    throw new Exception("Nenhuma credencial pôde ser selecionada.");
                }

                Log.d(TAG, "Credencial selecionada: " + credential.getServer() + " User: " + credential.getUsername());
                mainThreadHandler.post(() -> showLoading(true, "Carregando lista inicial de canais..."));

                List<Channel> channels = xtreamService.fetchLiveStreams(credential); // Pega todos os canais por enquanto

                if (channels == null || channels.isEmpty()) {
                    throw new Exception("Nenhum canal encontrado.");
                }

                Channel firstChannel = channels.get(0);
                // Corrigido para passar a credencial
                String streamUrl = xtreamService.getChannelStreamUrl(credential, firstChannel);

                if (streamUrl == null) {
                    throw new Exception("Não foi possível obter a URL de stream para o primeiro canal.");
                }

                Log.d(TAG, "Iniciando PlayerActivity com o canal: " + firstChannel.getName() + " URL: " + streamUrl);
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("initial_channel_url", streamUrl);
                intent.putExtra("credential", credential); // Credential é Serializable
                startActivity(intent);
                finish(); // Finaliza a MainActivity para que não fique na pilha

            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar dados iniciais: ", e);
                mainThreadHandler.post(() -> {
                    showLoading(false, null);
                    Toast.makeText(MainActivity.this, "Erro ao iniciar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    loadingText.setText("Falha ao carregar: " + e.getMessage() + "\nPor favor, reinicie o aplicativo.");
                    loadingText.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
