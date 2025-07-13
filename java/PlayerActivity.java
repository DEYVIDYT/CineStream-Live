package com.cinestream.live;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This activity will now be empty or serve a different purpose if needed.
        // All player and category logic has been moved to ChannelsActivity.
        // setContentView(R.layout.activity_player); // Remove this line if the layout is no longer needed
    }
}

