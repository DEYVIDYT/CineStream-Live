package com.cinestream.live;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        preferences = requireContext().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);
        
        initViews(view);
        setupClickListeners();
        loadUserData();
        
        return view;
    }
    
    private void initViews(View view) {
        emailText = view.findViewById(R.id.emailText);
        expirationText = view.findViewById(R.id.expirationText);
        rechargeButton = view.findViewById(R.id.rechargeButton);
        inviteButton = view.findViewById(R.id.inviteButton);
        historyAction = view.findViewById(R.id.historyAction);
        favoritesAction = view.findViewById(R.id.favoritesAction);
        reminderAction = view.findViewById(R.id.reminderAction);
        adultContentSwitch = view.findViewById(R.id.adultContentSwitch);
        informationAction = view.findViewById(R.id.informationAction);
        exitAction = view.findViewById(R.id.exitAction);
    }
    
    private void setupClickListeners() {
        rechargeButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Redirecionando para pagamento...", Toast.LENGTH_SHORT).show();
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
            if (getActivity() instanceof HostActivity) {
                HostActivity hostActivity = (HostActivity) getActivity();
                ChannelsFragment channelsFragment = new ChannelsFragment();
                Bundle args = new Bundle();
                args.putString("show_category", "HISTÓRICO");
                channelsFragment.setArguments(args);
                hostActivity.loadFragment(channelsFragment);
            }
        });
        
        favoritesAction.setOnClickListener(v -> {
            // Voltar para ChannelsActivity e mostrar favoritos
            if (getActivity() instanceof HostActivity) {
                HostActivity hostActivity = (HostActivity) getActivity();
                ChannelsFragment channelsFragment = new ChannelsFragment();
                Bundle args = new Bundle();
                args.putString("show_category", "FAVORITOS");
                channelsFragment.setArguments(args);
                hostActivity.loadFragment(channelsFragment);
            }
        });
        
        reminderAction.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Funcionalidade de lembrete em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        adultContentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Salvar preferência de conteúdo adulto
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ADULT_CONTENT_KEY, isChecked);
            editor.apply();
            
            String message = isChecked ? "Conteúdo adulto habilitado" : "Conteúdo adulto desabilitado";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Informações do App")
                .setMessage("CineStream Live\n\nVersão: 1.0.0\n\nDesenvolvido para streaming de canais de TV ao vivo.\n\nPara suporte, entre em contato através do email oficial.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sair do App")
                .setMessage("Tem certeza que deseja sair do aplicativo?")
                .setPositiveButton("Sair", (dialog, which) -> {
                    // Fechar o app completamente
                    if (getActivity() != null) {
                        getActivity().finishAffinity();
                        System.exit(0);
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }
    
    // onBackPressed is handled by the Activity, not the Fragment directly
    
    public static boolean isAdultContentEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean(ADULT_CONTENT_KEY, true);
    }
}