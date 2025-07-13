package com.cinestream.live;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;

public class HostActivity extends AppCompatActivity {

    private LinearLayout liveTab;
    private LinearLayout guideTab;
    private LinearLayout profileTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        liveTab = findViewById(R.id.liveTab);
        guideTab = findViewById(R.id.guideTab);
        profileTab = findViewById(R.id.profileTab);

        liveTab.setOnClickListener(v -> loadFragment(new ChannelsFragment()));
        guideTab.setOnClickListener(v -> {
            // Handle guide tab click, maybe show a Toast or load a GuideFragment
        });
        profileTab.setOnClickListener(v -> loadFragment(new ProfileFragment()));

        // Load default fragment
        if (savedInstanceState == null) {
            ChannelsFragment channelsFragment = new ChannelsFragment();
            channelsFragment.setArguments(getIntent().getExtras());
            loadFragment(channelsFragment);
        }
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}