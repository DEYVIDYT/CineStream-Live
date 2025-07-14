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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

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
            // TODO: Implement navigation to history category via ViewModel
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        
        favoritesAction.setOnClickListener(v -> {
            // TODO: Implement navigation to favorites category via ViewModel
            Toast.makeText(requireContext(), "Funcionalidade em desenvolvimento", Toast.LENGTH_SHORT).show();
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
        SharedPreferences prefs = requireContext().getSharedPreferences("CineStreamPrefs", requireContext().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String sessionToken = prefs.getString("session_token", null);

        if (userId == -1 || sessionToken == null) {
            // Redirecionar para o login se não houver dados de sessão
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
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
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Erro de rede.", Toast.LENGTH_SHORT).show());
                }
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

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                emailText.setText(email);
                                expirationText.setText("Data de expiração " + planExpiration);
                            });
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Sessão inválida. Faça login novamente.", Toast.LENGTH_SHORT).show();
                                // Limpar prefs e redirecionar para o login
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.clear();
                                editor.apply();
                                startActivity(new Intent(requireContext(), LoginActivity.class));
                                requireActivity().finish();
                            });
                        }
                    }
                } catch (org.json.JSONException e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Resposta inválida do servidor.", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
        
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
                    logout();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logout() {
        SharedPreferences prefs = requireContext().getSharedPreferences("CineStreamPrefs", requireContext().MODE_PRIVATE);
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
        SharedPreferences prefs = requireContext().getSharedPreferences("CineStreamPrefs", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                getActivity().finishAffinity();
                System.exit(0);
            });
        }
    }
    
    // onBackPressed is handled by the Activity, not the Fragment directly
    
    public static boolean isAdultContentEnabled(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        return prefs.getBoolean(ADULT_CONTENT_KEY, true);
    }
}