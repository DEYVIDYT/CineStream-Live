package com.cinestream.live;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Intent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.FormBody;

public class HostActivity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private FragmentManager fragmentManager;
    private Fragment channelsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    private LinearLayout liveTab;
    private LinearLayout profileTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        fragmentManager = getSupportFragmentManager();

        // Start loading data
        Intent intent = getIntent();
        String xtreamServer = intent.getStringExtra("xtream_server");
        String xtreamUsername = intent.getStringExtra("xtream_username");
        String xtreamPassword = intent.getStringExtra("xtream_password");

        if (xtreamServer != null && xtreamUsername != null && xtreamPassword != null) {
            Credential credential = new Credential();
            credential.setServer(xtreamServer);
            credential.setUsername(xtreamUsername);
            credential.setPassword(xtreamPassword);
            sharedViewModel.getXtreamClient().setCredential(credential);
        }

        sharedViewModel.loadData();

        liveTab = findViewById(R.id.liveTab);
        profileTab = findViewById(R.id.profileTab);
        // Assuming you might have a guide tab
        // LinearLayout guideTab = findViewById(R.id.guideTab);

        if (savedInstanceState == null) {
            channelsFragment = new ChannelsFragment();
            profileFragment = new ProfileFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, profileFragment, "2").hide(profileFragment)
                    .add(R.id.fragment_container, channelsFragment, "1").commit();
            activeFragment = channelsFragment;
        } else {
            channelsFragment = fragmentManager.findFragmentByTag("1");
            profileFragment = fragmentManager.findFragmentByTag("2");
            activeFragment = (profileFragment != null && profileFragment.isVisible()) ? profileFragment : channelsFragment;
        }

        liveTab.setOnClickListener(v -> switchFragment(channelsFragment));
        profileTab.setOnClickListener(v -> switchFragment(profileFragment));

        updateTabAppearance();
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != activeFragment) {
            fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
            activeFragment = fragment;
            updateTabAppearance();
        }
    }

    private void updateTabAppearance() {
        // Reset all tabs to default color
        setTabColor(liveTab, R.color.text_secondary);
        setTabColor(profileTab, R.color.text_secondary);

        // Set the active tab to the accent color
        if (activeFragment == channelsFragment) {
            setTabColor(liveTab, R.color.accent_color);
        } else if (activeFragment == profileFragment) {
            setTabColor(profileTab, R.color.accent_color);
        }
    }

    private void setTabColor(LinearLayout tab, int colorResId) {
        ImageView icon = (ImageView) tab.getChildAt(0);
        TextView text = (TextView) tab.getChildAt(1);
        int color = ContextCompat.getColor(this, colorResId);
        icon.setColorFilter(color);
        text.setTextColor(color);
    }

    private void startSessionCheck() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSession();
                startSessionCheck(); // Agendar a próxima verificação
            }
        }, 60000); // Verificar a cada 1 minuto
    }

    private void checkSession() {
        SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (userId == -1) {
            return; // Não há usuário logado
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("device_id", deviceId)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/check_session.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                // Não fazer nada em caso de falha de rede
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body().string();
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(responseBody);
                    String status = jsonObject.getString("status");

                    if (status.equals("error")) {
                        // Duplo login detectado, desconectar o usuário
                        runOnUiThread(() -> {
                            Toast.makeText(HostActivity.this, "Você foi desconectado porque outra sessão foi iniciada.", Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();
                            editor.apply();
                            startActivity(new Intent(HostActivity.this, LoginActivity.class));
                            finish();
                        });
                    }
                } catch (org.json.JSONException e) {
                    // Não fazer nada em caso de resposta inválida
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startSessionCheck();
        checkBanStatus();
    }

    private void checkBanStatus() {
        SharedPreferences prefs = getSharedPreferences("CineStreamPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (userId == -1) {
            return; // Não há usuário logado
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("user_id", String.valueOf(userId))
                .add("device_id", deviceId)
                .build();

        Request request = new Request.Builder()
                .url("http://mybrasiltv.x10.mx/check_ban_status.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                // Não fazer nada em caso de falha de rede
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                String responseBody = response.body().string();
                try {
                    org.json.JSONObject jsonObject = new org.json.JSONObject(responseBody);
                    String status = jsonObject.getString("status");

                    if (status.equals("banned")) {
                        // Usuário ou dispositivo banido, desconectar e fechar o app
                        runOnUiThread(() -> {
                            Toast.makeText(HostActivity.this, "Você foi banido.", Toast.LENGTH_LONG).show();
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();
                            editor.apply();
                            // Fechar o app após um pequeno atraso para o usuário ler a mensagem
                            new android.os.Handler().postDelayed(
                                () -> finishAffinity(),
                                3000
                            );
                        });
                    }
                } catch (org.json.JSONException e) {
                    // Não fazer nada em caso de resposta inválida
                }
            }
        });
    }
}