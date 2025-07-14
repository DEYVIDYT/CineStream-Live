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
import android.content.SharedPreferences;
import android.content.Intent;
public class HostActivity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private FragmentManager fragmentManager;
    private Fragment channelsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    private LinearLayout liveTab;
    private LinearLayout profileTab;
    private FrameLayout fullscreenContainer;

    public FrameLayout getFullscreenContainer() {
        return fullscreenContainer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        fragmentManager = getSupportFragmentManager();

        // Start loading data
        sharedViewModel.loadData();

        liveTab = findViewById(R.id.liveTab);
        profileTab = findViewById(R.id.profileTab);
        fullscreenContainer = findViewById(R.id.fullscreen_container);
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

    private Handler sessionHandler = new Handler();
    private Runnable sessionRunnable = new Runnable() {
        @Override
        public void run() {
            checkUserSession();
            sessionHandler.postDelayed(this, 30000); // Check every 30 seconds
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        sessionHandler.post(sessionRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sessionHandler.removeCallbacks(sessionRunnable);
    }

    private void checkUserSession() {
        android.content.SharedPreferences sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String sessionToken = sessionPrefs.getString("session_token", null);

        if (sessionToken != null) {
            ApiClient apiClient = new ApiClient();
            apiClient.checkSession(sessionToken, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response) {
                    if (response != null && !response.contains("\"success\":true")) {
                        logout();
                    }
                }

                @Override
                public void onFailure(String message) {
                    // Could be a network error, decide if you want to log out
                }
            });
        } else {
            logout();
        }
    }

    private void logout() {
        android.content.SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = preferences.edit();
        editor.remove("session_token");
        editor.apply();

        Intent intent = new Intent(HostActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}