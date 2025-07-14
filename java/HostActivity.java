package com.cinestream.live;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

public class HostActivity extends AppCompatActivity {

    private SharedViewModel sharedViewModel;
    private FragmentManager fragmentManager;
    private Fragment channelsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        fragmentManager = getSupportFragmentManager();

        // Start loading data
        sharedViewModel.loadData();

        LinearLayout liveTab = findViewById(R.id.liveTab);
        LinearLayout guideTab = findViewById(R.id.guideTab); // Assuming you might have a guide tab
        LinearLayout profileTab = findViewById(R.id.profileTab);

        if (savedInstanceState == null) {
            channelsFragment = new ChannelsFragment();
            profileFragment = new ProfileFragment();
            // Add fragments and hide them initially
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, profileFragment, "2").hide(profileFragment)
                    .add(R.id.fragment_container, channelsFragment, "1").commit();
            activeFragment = channelsFragment;
        } else {
            channelsFragment = fragmentManager.findFragmentByTag("1");
            profileFragment = fragmentManager.findFragmentByTag("2");
            // Find the active fragment by its visibility state, or default to channelsFragment
            if (profileFragment != null && profileFragment.isVisible()) {
                activeFragment = profileFragment;
            } else {
                activeFragment = channelsFragment;
            }
        }

        liveTab.setOnClickListener(v -> switchFragment(channelsFragment));
        profileTab.setOnClickListener(v -> switchFragment(profileFragment));
        // Set guideTab listener if you have a GuideFragment
    }

    private void switchFragment(Fragment fragment) {
        if (fragment != activeFragment) {
            fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
            activeFragment = fragment;
        }
    }
}