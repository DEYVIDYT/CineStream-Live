package com.cinestream.live;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;

public class ProfileActivity extends AppCompatActivity {
    
    private TextView emailText;
    private TextView expirationText;
    private Button rechargeButton;
    private Button inviteButton;
    private LinearLayout historyAction;
    private LinearLayout favoritesAction;
    private LinearLayout reminderAction;
    private Switch adultContentSwitch;
    private LinearLayout informationAction;
    private LinearLayout exitAction;
    
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "ProfilePrefs";
    private static final String ADULT_CONTENT_KEY = "adult_content_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        initViews();
        setupClickListeners();
        loadUserData();
    }
    
    private void initViews() {
        emailText = findViewById(R.id.emailText);
        expirationText = findViewById(R.id.expirationText);
        rechargeButton = findViewById(R.id.rechargeButton);
        inviteButton = findViewById(R.id.inviteButton);
        historyAction = findViewById(R.id.historyAction);
        favoritesAction = findViewById(R.id.favoritesAction);
        reminderAction = findViewById(R.id.reminderAction);
        adultContentSwitch = findViewById(R.id.adultContentSwitch);
        informationAction = findViewById(R.id.informationAction);
        exitAction = findViewById(R.id.exitAction);

        // Initialize bottom navigation views
        findViewById(R.id.liveTab).setOnClickListener(v -> {
            Intent intent = new Intent(this, ChannelsActivity.class);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.guideTab).setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        // Profile tab is already on this activity, so no intent needed
        findViewById(R.id.profileTab).setOnClickListener(v -> {
            // Do nothing or refresh current activity if needed
        });
    }
    
    private void setupClickListeners() {
        rechargeButton.setOnClickListener(v -> {
            Toast.makeText(this, "Redirecionando para pagamento...", Toast.LENGTH_SHORT).show();
            // Aqui você pode implementar a lógica para abrir o site de pagamento
        });
        
        inviteButton.setOnClickListener(v -> {
            String inviteCode = "uk5wn64";
            String shareText = "Use meu código de convite: " + inviteCode + " para acessar o CineStream Live!";
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Compartilhar código de convite"));
        });
        
        historyAction.setOnClickListener(v -> {
            // Voltar para ChannelsActivity e mostrar histórico
            Intent intent = new Intent(this, ChannelsActivity.class);
            intent.putExtra("show_category", "HISTÓRICO");
            startActivity(intent);
            finish();
        });
        
        favoritesAction.setOnClickListener(v -> {
            // Voltar para ChannelsActivity e mostrar favoritos
            Intent intent = new Intent(this, ChannelsActivity.class);
            intent.putExtra("show_category", "FAVORITOS");
            startActivity(intent);
            finish();
        });
        
        reminderAction.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade de lembrete em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        adultContentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Salvar preferência de conteúdo adulto
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ADULT_CONTENT_KEY, isChecked);
            editor.apply();
            
            String message = isChecked ? "Conteúdo adulto habilitado" : "Conteúdo adulto desabilitado";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
        
        informationAction.setOnClickListener(v -> {
            showInformationDialog();
        });
        
        exitAction.setOnClickListener(v -> {
            showExitDialog();
        });
    }
    
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String sessionToken = prefs.getString("session_token", null);

        if (userId == -1 || sessionToken == null) {
            // Redirecionar para o login se não houver dados de sessão
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("session_token", sessionToken)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/profile.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Erro de rede.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body().string();
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(responseBody);
                    String status = jsonObject.getString("status");

                    if (status.equals("success")) {
                        String email = jsonObject.getString("email");
                        String planExpiration = jsonObject.getString("plan_expiration");

                        runOnUiThread(() -> {
                            emailText.setText(obfuscateEmail(email));
                            expirationText.setText("Data de expiração " + planExpiration);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(ProfileActivity.this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_SHORT).show();
                            // Limpar prefs e redirecionar para o login
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();
                            editor.apply();
                            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                            finish();
                        });
                    }
                } catch (org.json.JSONException e) {
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show());
                }
            }
        });
        
        // Carregar preferência de conteúdo adulto
        boolean adultContentEnabled = preferences.getBoolean(ADULT_CONTENT_KEY, true);
        adultContentSwitch.setChecked(adultContentEnabled);
    }
    
    private void showInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Informações do App")
                .setMessage("CineStream Live\n\nVersão: 1.0.0\n\nDesenvolvido para streaming de canais de TV ao vivo.\n\nPara suporte, entre em contato através do email oficial.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sair do App")
                .setMessage("Tem certeza que deseja sair do aplicativo?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String sessionToken = prefs.getString("session_token", null);

        if (userId != -1 && sessionToken != null) {
            OkHttpClient client = new OkHttpClient();
            RequestBody formBody = new FormBody.Builder()
                    .add("user_id", String.valueOf(userId))
                    .add("session_token", sessionToken)
                    .build();

            Request request = new Request.Builder()
                    .url("http://mybrasiltv.x10.mx/logout.php")
                    .post(formBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                    // Mesmo que o logout falhe, limpe os dados localmente
                    clearSessionAndExit();
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                    clearSessionAndExit();
                }
            });
        } else {
            clearSessionAndExit();
        }
    }

    private void clearSessionAndExit() {
        SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        runOnUiThread(() -> {
            finishAffinity();
            System.exit(0);
        });
    }
    
    @Override
    public void onBackPressed() {
        // Voltar para a tela principal
        Intent intent = new Intent(this, ChannelsActivity.class);
        startActivity(intent);
        finish();
    }
    
    public static boolean isAdultContentEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(ADULT_CONTENT_KEY, true);
    }

    private String obfuscateEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 3) {
            return name + "@" + domain;
        }
        return name.substring(0, 3) + "***@" + domain;
    }
}

