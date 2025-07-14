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
        // Carregar dados do usuário (simulado)
        emailText.setText("mfd***aro@msn.com");
        expirationText.setText("Data de expiração 29/11/2026");
        
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
                    // Fechar o app completamente
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
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
}

