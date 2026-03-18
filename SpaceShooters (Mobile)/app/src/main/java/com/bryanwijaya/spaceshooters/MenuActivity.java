package com.bryanwijaya.spaceshooters;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);
        Button playBtn = findViewById(R.id.playBtn);
        Button leaderBoardBtn = findViewById(R.id.leaderboardBtn);
        Button controlsBtn = findViewById(R.id.controlsBtn);
        playBtn.setOnClickListener(v -> {
            Intent i = new Intent(MenuActivity.this, GameActivity.class);
            startActivity(i);
        });
        leaderBoardBtn.setOnClickListener(v -> {
            Intent i = new Intent(MenuActivity.this, LeaderboardActivity.class);
            startActivity(i);
        });
        controlsBtn.setOnClickListener(v -> {
            Intent i = new Intent(MenuActivity.this, ControlsActivity.class);
            startActivity(i);
        });
    }
}