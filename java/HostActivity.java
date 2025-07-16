package com.cinestream.live;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HostActivity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

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
            sharedViewModel.getXtreamClient().setCredentials(credential);
        }

        sharedViewModel.loadData();

        viewPager = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_live) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.navigation_movies_series) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                viewPager.setCurrentItem(2);
                return true;
            }
            return false;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_live);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_movies_series);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                        break;
                }
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ChannelsFragment();
                case 1:
                    return new MoviesSeriesFragment();
                case 2:
                    return new ProfileFragment();
                default:
                    return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
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

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment currentFragment = fm.findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof ChannelsFragment) {
            ChannelsFragment cf = (ChannelsFragment) currentFragment;
            if (cf.onBackPressed()) {
                return; // O evento foi consumido pelo fragment
            }
        }

        // Comportamento padrão do botão voltar
        super.onBackPressed();
    }
}