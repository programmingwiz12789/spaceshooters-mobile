package com.bryanwijaya.spaceshooters;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;
    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity);
        gameView = findViewById(R.id.gameView);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            if (!gameView.start) {
                AlertDialog.Builder backDialog = getBuilder("Back to main menu?");
                backDialog.setNegativeButton("No", null);
                backDialog.show();
            }
            else if (gameView.pause) {
                AlertDialog.Builder backDialog = getBuilder("Back to main menu? Your progress will not be saved.");
                backDialog.setNegativeButton("No", null);
                backDialog.show();
            }
        });
    }

    private AlertDialog.Builder getBuilder(String message) {
        AlertDialog.Builder backDialog = new AlertDialog.Builder(GameActivity.this);
        backDialog.setTitle("Back to main menu");
        backDialog.setMessage(message);
        backDialog.setPositiveButton("Yes", (dialog, which) -> {
            Intent i = new Intent(GameActivity.this, MenuActivity.class);
            startActivity(i);
        });
        return backDialog;
    }
}