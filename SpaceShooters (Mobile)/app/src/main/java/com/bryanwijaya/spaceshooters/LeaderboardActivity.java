package com.bryanwijaya.spaceshooters;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    private GridView leaderboardGv;
    private String[] data;
    private int ctr;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_activity);
        leaderboardGv = findViewById(R.id.leaderboardGv);
        Button backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(v -> {
            Intent i = new Intent(LeaderboardActivity.this, MenuActivity.class);
            startActivity(i);
        });
        String[] header = {"NAME", "HIGHSCORE", "DATE"};
        data = new String[1000];
        ctr = 0;
        for (String s : header) {
            data[ctr] = s;
            ctr++;
        }
        db = AppDatabase.getAppDatabase(this);
        new GetLeaderboardsTask().execute();
    }

    private class GetLeaderboardsTask extends AsyncTask<Void, Void, List<Leaderboard>> {
        @Override
        protected List<Leaderboard> doInBackground(Void... voids) {
            return db.leaderboardDao().getLeaderboards();
        }

        @Override
        protected void onPostExecute(List<Leaderboard> leaderboards) {
            super.onPostExecute(leaderboards);
            for (Leaderboard leaderboard : leaderboards) {
                data[ctr] = leaderboard.getName();
                ctr++;
                data[ctr] = "" + leaderboard.getHighscore();
                ctr++;
                data[ctr] = leaderboard.getDate();
                ctr++;
            }
            LeaderboardAdapter leaderboardAdapter = new LeaderboardAdapter(LeaderboardActivity.this, data);
            leaderboardGv.setAdapter(leaderboardAdapter);
            leaderboardGv.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        }
    }
}