package com.bryanwijaya.spaceshooters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TimerTask;
import java.util.Timer;

public class GameView extends View {
    private final int DEFAULT_BULLET_WIDTH = 4, DEFAULT_BULLET_HEIGHT = 12, DEFAULT_PLAYER_ATTACK = 3, BULLET_SPEED = 10, INTERVAL = 20, OFFSET = 5, SMALL_MULTIPLIER = 10, BIG_MULTIPLIER = 30, DELAY = 9, SMALL_TEXT_SIZE = 30, BIG_TEXT_SIZE = 60;
    private GameView gameView;
    private Activity activity;
    private int GAME_AREA_WIDTH, GAME_AREA_HEIGHT;
    private HashMap<String, Bitmap> images;
    private ConcurrentHashMap<String, Drops> powerUps;
    private CopyOnWriteArrayList<Drops> dropss;
    private boolean loaded, gameOver;
    public boolean start, pause;
    private TextView pauseTv, playerScoreTv, bossLifeTv;
    private Player player;
    private Enemy enemy;
    private ProgressBar playerLifeBar, bossLifeBar;
    private CopyOnWriteArrayList<Bullet> playerBullets, enemyBullets;
    private Bitmap playerExplosionImg, enemyExplosionImg;
    private int diffX, playerDamagedValTxtShownCtr, playerHealCtr, playerExtraLifeValTxtShownCtr, playerExplosionCtr, enemyCnt, enemySpeed, enemyMoveCtr, enemyShootCooldownCtr, enemyDamagedValTxtShownCtr, enemyExplosionCtr, bossFightTxtShownCtr;
    private String playerDamagedValTxt, playerExtraLifeValTxt, enemyDamagedValTxt, bossFightTxt, name;
    private HashMap<String, TimerTask> timerTasks;
    private HashMap<String, Timer> timers;
    private AppDatabase db;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gameView = this;
        activity = (Activity)this.getContext();
        gameView.post(() -> {
            GAME_AREA_WIDTH = getWidth();
            GAME_AREA_HEIGHT = getHeight();
            images = Utils.GetAllImages(activity);
            pauseTv = activity.findViewById(R.id.pauseTv);
            playerScoreTv = activity.findViewById(R.id.playerScoreTv);
            playerLifeBar = activity.findViewById(R.id.playerLifeBar);
            bossLifeTv = activity.findViewById(R.id.bossLifeTv);
            bossLifeBar = activity.findViewById(R.id.bossLifeBar);
            LoadGame();
            pauseTv.setOnClickListener(v -> {
                if (start) {
                    if (!gameOver && pause) {
                        pause = false;
                    }
                }
                else {
                    start = true;
                }
                pauseTv.setVisibility(View.GONE);
                SetTimersState(true);
            });
            gameView.setOnTouchListener((v, event) -> {
                performClick();
                if (start && !gameOver && !pause) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Rect touchRect = new Rect((int)event.getX(), (int)event.getY(), (int)event.getX() + OFFSET, (int)event.getY() + OFFSET);
                        Rect playerRect = new Rect(player.getX(), player.getY(), player.getX() + player.getWidth(), player.getY() + player.getHeight());
                        Rect pauseRect = new Rect(pauseTv.getLeft(), pauseTv.getTop(), pauseTv.getRight(), pauseTv.getBottom());
                        if (touchRect.intersect(playerRect)) {
                            diffX = (int)event.getX() - player.getX();
                        }
                        else if (touchRect.intersect(pauseRect)) {
                            pause = true;
                            SetTimersState(false);
                            pauseTv.setText("TAP HERE TO RESUME");
                            pauseTv.setVisibility(View.VISIBLE);
                        }
                    }
                    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (diffX != -1) {
                            player.setX(Math.min(Math.max(0, (int)event.getX() - diffX), GAME_AREA_WIDTH - player.getWidth() - SMALL_TEXT_SIZE * 2));
                        }
                    }
                    else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (diffX != -1) {
                            diffX = -1;
                            RemoveLasers();
                        }
                    }
                }
                return true;
            });
            db = AppDatabase.getAppDatabase(activity);
            loaded = true;
        });
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    public void LoadGame() {
        pauseTv.setText("TAP HERE TO START");
        pauseTv.setVisibility(View.VISIBLE);
        powerUps = new ConcurrentHashMap<>();
        powerUps.put("swarm", new Drops());
        powerUps.put("laser", new Drops());
        powerUps.put("nuke", new Drops());
        powerUps.put("shield", new Drops());
        dropss = new CopyOnWriteArrayList<>();
        start = false;
        pause = false;
        diffX = -1;
        player = new Player(0, 20, 1, GAME_AREA_WIDTH / 2 - images.get("player").getWidth() / 2, GAME_AREA_HEIGHT - images.get("player").getHeight(), images.get("player").getWidth(), images.get("player").getHeight(), R.drawable.player);
        playerScoreTv.setText("Score: " + player.getScore());
        playerLifeBar.setMax(player.getMaxLife());
        playerLifeBar.setProgress(playerLifeBar.getMax());
        playerBullets = new CopyOnWriteArrayList<>();
        playerDamagedValTxtShownCtr = -1;
        playerDamagedValTxt = "";
        playerHealCtr = 0;
        playerExtraLifeValTxtShownCtr = -1;
        playerExtraLifeValTxt = "";
        playerExplosionCtr = -1;
        playerExplosionImg = null;
        enemy = null;
        enemyCnt = 0;
        enemySpeed = 10;
        enemyMoveCtr = -1;
        enemyBullets = new CopyOnWriteArrayList<>();
        enemyShootCooldownCtr = 0;
        enemyDamagedValTxtShownCtr = -1;
        enemyDamagedValTxt = "";
        enemyExplosionCtr = -1;
        enemyExplosionImg = null;
        bossFightTxtShownCtr = -1;
        bossFightTxt = "";
        bossLifeTv.setVisibility(View.GONE);
        bossLifeBar.setVisibility(View.GONE);
        gameOver = false;
        LoadTimers();
        gameView.invalidate();
    }

    private void LoadTimers() {
        // TimerTasks
        timerTasks = new HashMap<>();
        timerTasks.put("PlayerShoot", new TimerTask() {
            @Override
            public void run() {
                PlayerShoot();
            }
        });
        timerTasks.put("PlayerBulletsMove", new TimerTask() {
            @Override
            public void run() {
                PlayerBulletsMove();
            }
        });
        timerTasks.put("PlayerBulletsOutOfArea", new TimerTask() {
            @Override
            public void run() {
                PlayerBulletsOutOfArea();
            }
        });
        timerTasks.put("PlayerBulletsDamage", new TimerTask() {
            @Override
            public void run() {
                PlayerBulletsDamage();
            }
        });
        timerTasks.put("PlayerDamagedCtr", new TimerTask() {
            @Override
            public void run() {
                PlayerDamagedCtr();
            }
        });
        timerTasks.put("PlayerExtraLifeCtr", new TimerTask() {
            @Override
            public void run() {
                PlayerExtraLifeCtr();
            }
        });
        timerTasks.put("PlayerHeal", new TimerTask() {
            @Override
            public void run() {
                PlayerHeal();
            }
        });
        timerTasks.put("PlayerExplosion", new TimerTask() {
            @Override
            public void run() {
                PlayerExplosion();
            }
        });
        timerTasks.put("EnemySpawn", new TimerTask() {
            @Override
            public void run() {
                EnemySpawn();
            }
        });
        timerTasks.put("EnemyMove", new TimerTask() {
            @Override
            public void run() {
                EnemyMove();
            }
        });
        timerTasks.put("EnemyShoot", new TimerTask() {
            @Override
            public void run() {
                EnemyShoot();
            }
        });
        timerTasks.put("EnemyBulletsMove", new TimerTask() {
            @Override
            public void run() {
                EnemyBulletsMove();
            }
        });
        timerTasks.put("EnemyBulletsOutOfArea", new TimerTask() {
            @Override
            public void run() {
                EnemyBulletsOutOfArea();
            }
        });
        timerTasks.put("EnemyBulletsDamage", new TimerTask() {
            @Override
            public void run() {
                EnemyBulletsDamage();
            }
        });
        timerTasks.put("EnemyDamagedCtr", new TimerTask() {
            @Override
            public void run() {
                EnemyDamagedCtr();
            }
        });
        timerTasks.put("EnemyExplosion", new TimerTask() {
            @Override
            public void run() {
                EnemyExplosion();
            }
        });
        timerTasks.put("BossFightDelayCtr", new TimerTask() {
            @Override
            public void run() {
                BossFightDelayCtr();
            }
        });
        timerTasks.put("DropsMove", new TimerTask() {
            @Override
            public void run() {
                DropsMove();
            }
        });
        timerTasks.put("DropsOutOfArea", new TimerTask() {
            @Override
            public void run() {
                DropsOutOfArea();
            }
        });
        timerTasks.put("DropsReceived", new TimerTask() {
            @Override
            public void run() {
                DropsReceived();
            }
        });
        timerTasks.put("PowerUpDuration", new TimerTask() {
            @Override
            public void run() {
                PowerUpDuration();
            }
        });
        timerTasks.put("RefreshScreen", new TimerTask() {
            @Override
            public void run() {
                gameView.invalidate();
            }
        });

        // Timers
        timers = new HashMap<>();
        for (Object key : timerTasks.keySet().toArray()) {
            timers.put((String)key, new Timer(true));
        }
    }

    private void SetTimersState(boolean state) {
        if (state) {
            LoadTimers();
            for (Object key : timerTasks.keySet().toArray()) {
                String k = (String)key;
                if (k.equals("PlayerShoot")) {
                    timers.get(k).schedule(timerTasks.get(k), 0, INTERVAL * BIG_MULTIPLIER);
                }
                else if (k.contains("Explosion")) {
                    timers.get(k).schedule(timerTasks.get(k), 0, 50);
                }
                else {
                    timers.get(k).schedule(timerTasks.get(k), 0, INTERVAL);
                }
            }
        }
        else {
            for (Object key : timerTasks.keySet().toArray()) {
                timerTasks.get((String)key).cancel();
                timers.get((String)key).cancel();
            }
        }
    }

    private class GetMaxHighscoreTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... voids) {
            return db.leaderboardDao().getMaxHighScore();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (player.getScore() > integer) {
                AlertDialog.Builder newHighScoreDialogBuilder = new AlertDialog.Builder(activity);
                newHighScoreDialogBuilder.setTitle("New Highscore");
                newHighScoreDialogBuilder.setMessage("Enter your name");
                final EditText input = new EditText(activity);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                newHighScoreDialogBuilder.setView(input);
                newHighScoreDialogBuilder.setPositiveButton("Ok", null);
                AlertDialog newHighScoreDialog = newHighScoreDialogBuilder.show();
                Button okButton = newHighScoreDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(view -> {
                    String name = input.getText().toString();
                    if (!name.trim().isEmpty()) {
                        newHighScoreDialog.dismiss();
                        Leaderboard leaderboard = new Leaderboard(name, player.getScore());
                        new InsertLeaderboardTask().execute(leaderboard);
                    }
                    else {
                        Toast.makeText(activity, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                AlertDialog.Builder playAgainDialog = new AlertDialog.Builder(activity);
                playAgainDialog.setTitle("Game Over");
                playAgainDialog.setMessage("Play again?");
                playAgainDialog.setPositiveButton("Yes", (dialog, which) -> LoadGame());
                playAgainDialog.setNegativeButton("No", (dialog, which) -> {
                    Intent i = new Intent(activity, MenuActivity.class);
                    activity.startActivity(i);
                });
                playAgainDialog.show();
            }
        }
    }

    private class InsertLeaderboardTask extends AsyncTask<Leaderboard, Void, Void> {
        @Override
        protected Void doInBackground(Leaderboard... leaderboards) {
            db.leaderboardDao().insert(leaderboards[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            AlertDialog.Builder playAgainDialog = new AlertDialog.Builder(activity);
            playAgainDialog.setTitle("Game Over");
            playAgainDialog.setMessage("Play again?");
            playAgainDialog.setPositiveButton("Yes", (dialog, which) -> LoadGame());
            playAgainDialog.setNegativeButton("No", (dialog, which) -> {
                Intent i = new Intent(activity, MenuActivity.class);
                activity.startActivity(i);
            });
            playAgainDialog.show();
        }
    }

    private void RemoveLasers() {
        for (Bullet bullet : playerBullets) {
            if (bullet.getType() == 1) {
                playerBullets.remove(bullet);
            }
        }
    }

    private void PlayerShoot() {
        if (start && !gameOver && player.getLife() > 0) {
            if (diffX != -1) {
                RemoveLasers();
                if (powerUps.get("swarm").getName() != null) {
                    Bullet bullet1 = new Bullet(player.getX() + (player.getWidth() / 2) - (DEFAULT_BULLET_WIDTH / 2), player.getY() - DEFAULT_BULLET_HEIGHT, DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, DEFAULT_PLAYER_ATTACK, 0, Color.rgb(173, 216, 230), -1);
                    Bullet bullet2 = new Bullet(player.getX() - (DEFAULT_BULLET_WIDTH / 2), player.getY() - DEFAULT_BULLET_HEIGHT, DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, DEFAULT_PLAYER_ATTACK, 0, Color.rgb(173, 216, 230), -1);
                    Bullet bullet3 = new Bullet(player.getX() + player.getWidth() - (DEFAULT_BULLET_WIDTH / 2), player.getY() - DEFAULT_BULLET_HEIGHT, DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, DEFAULT_PLAYER_ATTACK, 0, Color.rgb(173, 216, 230), -1);
                    playerBullets.add(bullet1);
                    playerBullets.add(bullet2);
                    playerBullets.add(bullet3);
                }
                else if (powerUps.get("laser").getName() != null) {
                    Bullet bullet = new Bullet(player.getX() + player.getWidth() / 2, 0, 1, GAME_AREA_HEIGHT - player.getHeight(), 15, 1, Color.rgb(173, 216, 230), -1);
                    playerBullets.add(bullet);
                }
                else if (powerUps.get("nuke").getName() != null) {
                    Bullet bullet = new Bullet(player.getX() + (player.getWidth() / 2) - (images.get("nuke_bullet").getWidth() / 2), player.getY() - images.get("nuke_bullet").getHeight(), images.get("nuke_bullet").getWidth(), images.get("nuke_bullet").getHeight(), 30, 2, Color.TRANSPARENT, R.drawable.nuke_bullet);
                    playerBullets.add(bullet);
                }
                else {
                    Bullet bullet = new Bullet(player.getX() + (player.getWidth() / 2) - (DEFAULT_BULLET_WIDTH / 2), player.getY() - DEFAULT_BULLET_HEIGHT, DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, DEFAULT_PLAYER_ATTACK, 0, Color.rgb(173, 216, 230), -1);
                    playerBullets.add(bullet);
                }
            }
        }
    }

    private void PlayerBulletsMove() {
        if (start && !gameOver) {
            for (Bullet bullet : playerBullets) {
                if (bullet.getType() != 1) {
                    bullet.setY(bullet.getY() - BULLET_SPEED);
                }
            }
        }
    }

    private void PlayerBulletsOutOfArea() {
        if (start && !gameOver) {
            for (Bullet bullet : playerBullets) {;
                if (bullet.getY() + bullet.getHeight() < 0) {
                    playerBullets.remove(bullet);
                }
            }
        }
    }

    private void PlayerBulletsDamage() {
        if (start && !gameOver && enemy != null && enemy.getLife() > 0) {
            Rect enemyRect = new Rect(enemy.getX(), enemy.getY(), enemy.getX() + enemy.getWidth(), enemy.getY() + enemy.getHeight());
            for (Bullet bullet : playerBullets) {
                Rect bulletRect = new Rect(bullet.getX(), bullet.getY(), bullet.getX() + bullet.getWidth(), bullet.getY() + bullet.getHeight());
                if (bulletRect.intersect(enemyRect)) {
                    int damage = Math.max(1, bullet.getDamage() - enemy.getDefense());
                    enemy.setLife(Math.max(0, enemy.getLife() - damage));
                    if (bossFightTxtShownCtr == -2) {
                        activity.runOnUiThread(() -> {
                            bossLifeBar.setProgress(enemy.getLife());
                        });
                    }
                    enemyDamagedValTxtShownCtr = 0;
                    enemyDamagedValTxt = "-" + damage;
                    if (bullet.getType() != 1) {
                        playerBullets.remove(bullet);
                    }
                }
            }
            if (enemy.getLife() <= 0) {
                Random rn = new Random();
                int prob = rn.nextInt(100) + 1;
                if (enemy.getType() == -1)
                {
                    player.setScore(player.getScore() + 1);
                }
                else if (enemy.getType() == -2)
                {
                    player.setScore(player.getScore() + 3);
                }
                else if (enemy.getType() == -3)
                {
                    player.setScore(player.getScore() + 5);
                }
                else if (enemy.getType() == -4)
                {
                    player.setScore(player.getScore() + 7);
                }
                else if (enemy.getType() == -5)
                {
                    player.setScore(player.getScore() + 9);
                }
                else if (enemy.getType() == -6)
                {
                    player.setScore(player.getScore() + 13);
                }
                else if (enemy.getType() == 1)
                {
                    player.setScore(player.getScore() + 100);
                }
                else if (enemy.getType() == 2)
                {
                    player.setScore(player.getScore() + 200);
                }
                else
                {
                    player.setScore(player.getScore() + 500);
                }
                if (bossFightTxtShownCtr == -2) {
                    bossFightTxtShownCtr = -1;
                    activity.runOnUiThread(() -> {
                        bossLifeBar.setVisibility(View.GONE);
                        bossLifeTv.setVisibility(View.GONE);
                    });
                    enemySpeed += 3;
                }
                activity.runOnUiThread(() -> playerScoreTv.setText("Score: " + player.getScore()));
                if (prob >= 1 && prob <= 10) {
                    String name = "life";
                    int left = enemy.getX(), right = enemy.getX() + enemy.getWidth();
                    int posX = ((left + right) / 2) - (images.get(name).getWidth() / 2), posY = enemy.getY() + enemy.getHeight();
                    dropss.add(new Drops(name, posX, posY, images.get(name).getWidth(), images.get(name).getHeight(),0, -1, R.drawable.life));
                }
                else if (prob >= 11 && prob <= 17) {
                    String name = "swarm";
                    int left = enemy.getX(), right = enemy.getX() + enemy.getWidth();
                    int posX = ((left + right) / 2) - (images.get(name).getWidth() / 2), posY = enemy.getY() + enemy.getHeight();
                    dropss.add(new Drops(name, posX, posY, images.get(name).getWidth(), images.get(name).getHeight(),1, 350, R.drawable.swarm));
                }
                else if (prob >= 18 && prob <= 22) {
                    String name = "laser";
                    int left = enemy.getX(), right = enemy.getX() + enemy.getWidth();
                    int posX = ((left + right) / 2) - (images.get(name).getWidth() / 2), posY = enemy.getY() + enemy.getHeight();
                    dropss.add(new Drops(name, posX, posY, images.get(name).getWidth(), images.get(name).getHeight(),2, 300, R.drawable.laser));
                }
                else if (prob >= 23 && prob <= 25) {
                    String name = "nuke";
                    int left = enemy.getX(), right = enemy.getX() + enemy.getWidth();
                    int posX = ((left + right) / 2) - (images.get("nuke").getWidth() / 2), posY = enemy.getY() + enemy.getHeight();
                    dropss.add(new Drops(name, posX, posY, images.get("nuke").getWidth(), images.get("nuke").getHeight(),4, 200, R.drawable.nuke));
                }
                else if (prob == 26) {
                    String name = "shield";
                    int left = enemy.getX(), right = enemy.getX() + enemy.getWidth();
                    int posX = ((left + right) / 2) - (images.get("shield").getWidth() / 2), posY = enemy.getY() + enemy.getHeight();
                    dropss.add(new Drops(name, posX, posY, images.get("shield").getWidth(), images.get("shield").getHeight(),5, 400, R.drawable.shield));
                }
                enemyMoveCtr = -1;
                enemyShootCooldownCtr = 0;
                if (enemyExplosionCtr == -2){
                    if (enemy.getType() < 0) {
                        enemyExplosionImg = images.get("small_explosion1");
                    }
                    else {
                        enemyExplosionImg = images.get("big_explosion1");
                    }
                    enemyExplosionCtr = 0;
                }
                if (enemyCnt >= 10) {
                    bossFightTxtShownCtr = 0;
                    bossFightTxt = "BOSS FIGHT";
                    enemyCnt = 0;
                }
            }
        }
    }

    private void PlayerDamagedCtr() {
        if (start && !gameOver) {
            if (playerDamagedValTxtShownCtr > DELAY) {
                playerDamagedValTxtShownCtr = -1;
                playerDamagedValTxt = "";
            }
            else if (playerDamagedValTxtShownCtr != -1) {
                playerDamagedValTxtShownCtr++;
            }
        }
    }

    private void PlayerExtraLifeCtr() {
        if (start && !gameOver) {
            if (playerExtraLifeValTxtShownCtr > DELAY) {
                playerExtraLifeValTxtShownCtr = -1;
                playerExtraLifeValTxt = "";
            }
            else if (playerExtraLifeValTxtShownCtr != -1) {
                playerExtraLifeValTxtShownCtr++;
            }
        }
    }

    private void PlayerHeal() {
        if (start && !gameOver && player.getLife() > 0) {
            if (playerHealCtr > 600) {
                playerHealCtr = 0;
                player.setLife(Math.min(player.getLife() + 1, player.getMaxLife()));
                activity.runOnUiThread(() -> {
                    playerLifeBar.setProgress(player.getLife());
                });
            }
        }
    }

    private void PlayerExplosion() {
        if (start && !gameOver && player.getLife() <= 0) {
            if (playerExplosionCtr > DELAY) {
                playerExplosionCtr = -1;
                playerExplosionImg = null;
                SetTimersState(false);
                gameOver = true;
                new GetMaxHighscoreTask().execute();
            }
            else if (playerExplosionCtr != -1) {
                playerExplosionCtr++;
                ConstraintLayout cl = activity.findViewById(R.id.cl);
                playerExplosionImg = images.get("small_explosion" + playerExplosionCtr);
            }
        }
    }

    private void EnemySpawn() {
        if (start && !gameOver) {
            if (enemyExplosionCtr == -1) {
                Random rn = new Random();
                int[] dirX = {0, 1, 1, 1, 0, -1, -1, -1};
                int[] dirY = {-1, -1, 0, 1, 1, 1, 0, -1};
                int index = rn.nextInt(8), dx = dirX[index] * enemySpeed, dy = dirY[index] * enemySpeed;
                int prob = rn.nextInt(100) + 1;
                enemyMoveCtr = 0;
                if (bossFightTxtShownCtr == -1) {
                    enemyExplosionCtr = -2;
                    if (prob >= 1 && prob <= 50) {
                        int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("enemy1").getWidth() - SMALL_TEXT_SIZE * 2);
                        enemy = new Enemy(10, 0, -1, posX, playerLifeBar.getHeight(), dx, dy, images.get("enemy1").getWidth(), images.get("enemy1").getHeight(), R.drawable.enemy1);
                    }
                    else if (prob >= 51 && prob <= 70) {
                        int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("enemy2").getWidth() - SMALL_TEXT_SIZE * 2);
                        enemy = new Enemy(20, 1, -2, posX, playerLifeBar.getHeight(), dx, dy, images.get("enemy2").getWidth(), images.get("enemy2").getHeight(), R.drawable.enemy2);
                    }
                    else if (prob >= 71 && prob <= 80) {
                        int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("enemy3").getWidth() - SMALL_TEXT_SIZE * 2);
                        enemy = new Enemy(20, 2, -3, posX, playerLifeBar.getHeight(), dx, dy, images.get("enemy3").getWidth(), images.get("enemy3").getWidth(), R.drawable.enemy3);
                    }
                    else if (prob >= 81 && prob <= 89) {
                        int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("enemy4").getWidth() - SMALL_TEXT_SIZE * 2);
                        enemy = new Enemy(30, 4, -4, posX, playerLifeBar.getHeight(), dx, dy, images.get("enemy4").getWidth(), images.get("enemy4").getHeight(), R.drawable.enemy4);
                    }
                    else if (prob >= 90 && prob <= 97) {
                        int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("enemy5").getWidth() - SMALL_TEXT_SIZE * 2);
                        enemy = new Enemy(30, 5, -5, posX, playerLifeBar.getHeight(), dx, dy, images.get("enemy5").getWidth(), images.get("enemy5").getHeight(), R.drawable.enemy5);
                    }
                    else {
                        int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("enemy6").getWidth() - SMALL_TEXT_SIZE * 2);
                        enemy = new Enemy(40, 6, -6, posX, playerLifeBar.getHeight(), dx, dy, images.get("enemy6").getWidth(), images.get("enemy6").getHeight(), R.drawable.enemy6);
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            enemy.getWidth(),
                            playerLifeBar.getHeight()
                    );
                    enemyCnt++;
                }
                else if (bossFightTxtShownCtr == -2) {
                    if (enemy == null || enemy.getLife() <= 0) {
                        enemyExplosionCtr = -2;
                        bossFightTxtShownCtr = -2;
                        bossFightTxt = "";
                        if (prob >= 1 && prob <= 70) {
                            int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("boss1").getWidth() - SMALL_TEXT_SIZE * 2);
                            enemy = new Enemy(200, 10, 1, posX,0, dx, dy, images.get("boss1").getWidth(), images.get("boss1").getHeight(), R.drawable.boss1);
                        }
                        else if (prob >= 71 && prob <= 90) {
                            int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("boss2").getWidth() - SMALL_TEXT_SIZE * 2);
                            enemy = new Enemy(300, 15, 2, posX, 0, dx, dy, images.get("boss2").getWidth(), images.get("boss2").getHeight(), R.drawable.boss2);
                        }
                        else {
                            int posX = rn.nextInt(GAME_AREA_WIDTH - images.get("boss3").getWidth() - SMALL_TEXT_SIZE * 2);
                            enemy = new Enemy(500, 20, 3, posX, 0, dx, dy, images.get("boss3").getWidth(), images.get("boss3").getHeight(), R.drawable.boss3);
                        }
                        activity.runOnUiThread(() -> {
                            bossLifeTv.setVisibility(View.VISIBLE);
                            bossLifeBar.setMax(enemy.getMaxLife());
                            bossLifeBar.setProgress(bossLifeBar.getMax());
                            bossLifeBar.setVisibility(View.VISIBLE);
                        });
                    }
                }
            }
        }
    }

    private void EnemyMove() {
        if (start && !gameOver && enemy != null && enemy.getLife() > 0) {
            final int MAX_X = GAME_AREA_WIDTH - enemy.getWidth() - SMALL_TEXT_SIZE * 2, MAX_Y = GAME_AREA_HEIGHT - enemy.getHeight() - player.getHeight() - OFFSET * 10;
            if (enemyMoveCtr > 50) {
                Random rn = new Random();
                int[] dirX = {0, 1, 1, 1, 0, -1, -1, -1};
                int[] dirY = {-1, -1, 0, 1, 1, 1, 0, -1};
                int index = rn.nextInt(8);
                enemy.setDx(dirX[index] * enemySpeed);
                enemy.setDy(dirY[index] * enemySpeed);
                enemyMoveCtr = 0;
            }
            else if (enemyMoveCtr != -1) {
                int MIN_Y = 0;
                if (enemy.getX() + enemy.getDx() < 0) {
                    enemy.setDx(enemySpeed);
                }
                if (enemy.getX() + enemy.getDx() > MAX_X) {
                    enemy.setDx(-enemySpeed);
                }
                if (enemy.getType() < 0) {
                    MIN_Y = playerLifeBar.getLayoutParams().height * 5;
                }
                if (enemy.getY() + enemy.getDy() < MIN_Y) {
                    enemy.setDy(enemySpeed);
                }
                if (enemy.getY() + enemy.getDy() > MAX_Y) {
                    enemy.setDy(-enemySpeed);
                }
                enemy.setX(Math.min(Math.max(0, enemy.getX() + enemy.getDx()), MAX_X));
                enemy.setY(Math.min(Math.max(MIN_Y, enemy.getY() + enemy.getDy()), MAX_Y));
                enemyMoveCtr++;
            }
        }
    }

    private void EnemyShoots() {
        Bullet bullet;
        int posX = enemy.getX() + enemy.getWidth() / 2;
        if (enemy.getType() == -1) {
            bullet = new Bullet(posX - DEFAULT_BULLET_WIDTH / 2, enemy.getY() + enemy.getHeight(), DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, 1, 3, Color.rgb(255, 215, 0), -1);
        }
        else if (enemy.getType() == -2) {
            bullet = new Bullet(posX - DEFAULT_BULLET_WIDTH / 2, enemy.getY() + enemy.getHeight(), DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, 3, 4, Color.LTGRAY, -1);
        }
        else if (enemy.getType() == -3) {
            bullet = new Bullet(posX - DEFAULT_BULLET_WIDTH / 2, enemy.getY() + enemy.getHeight(), DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, 4, 5, Color.rgb(192, 192, 192), -1);
        }
        else if (enemy.getType() == -4) {
            bullet = new Bullet(posX - DEFAULT_BULLET_WIDTH / 2, enemy.getY() + enemy.getHeight(), DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, 5, 6, Color.rgb(200, 69, 19), -1);
        }
        else if (enemy.getType() == -5) {
            bullet = new Bullet(posX - DEFAULT_BULLET_WIDTH / 2, enemy.getY() + enemy.getHeight(), DEFAULT_BULLET_WIDTH, DEFAULT_BULLET_HEIGHT, 6, 7, Color.BLUE, -1);
        }
        else if (enemy.getType() == -6) {
            bullet = new Bullet(posX - DEFAULT_BULLET_HEIGHT / 2, enemy.getY() + enemy.getHeight(), DEFAULT_BULLET_HEIGHT, DEFAULT_BULLET_WIDTH, 9, 8, Color.RED, -1);
        }
        else if (enemy.getType() == 1) {
            bullet = new Bullet(posX - images.get("boss1_bullet").getWidth() / 2, enemy.getY() + enemy.getHeight(), images.get("boss1_bullet").getWidth(), images.get("boss1_bullet").getHeight(), 30, 9, Color.TRANSPARENT, R.drawable.boss1_bullet);
        }
        else if (enemy.getType() == 2) {
            bullet = new Bullet(posX - images.get("boss2_bullet").getWidth() / 2, enemy.getY() + enemy.getHeight(), images.get("boss2_bullet").getWidth(), images.get("boss2_bullet").getHeight(), 50, 10, Color.TRANSPARENT, R.drawable.boss2_bullet);
        }
        else {
            bullet = new Bullet(posX - images.get("boss3_bullet").getWidth() / 2, enemy.getY() + enemy.getHeight(), images.get("boss3_bullet").getWidth(), images.get("boss3_bullet").getHeight(), 80, 11, Color.TRANSPARENT, R.drawable.boss3_bullet);
        }
        enemyBullets.add(bullet);
    }

    private void EnemyShoot() {
        if (start && !gameOver && enemy != null && enemy.getLife() > 0) {
            if (enemy.getType() < 0) {
                if (enemyShootCooldownCtr > BIG_MULTIPLIER) {
                    enemyShootCooldownCtr = 0;
                    EnemyShoots();
                }
                else {
                    enemyShootCooldownCtr++;
                }
            }
            else {
                if (enemyShootCooldownCtr > SMALL_MULTIPLIER) {
                    enemyShootCooldownCtr = 0;
                    EnemyShoots();
                }
                else {
                    enemyShootCooldownCtr++;
                }
            }
        }
    }

    private void EnemyBulletsMove() {
        if (start && !gameOver) {
            for (Bullet bullet : enemyBullets) {
                bullet.setY(bullet.getY() + BULLET_SPEED);
            }
        }
    }

    private void EnemyBulletsOutOfArea() {
        if (start && !gameOver) {
            for (Bullet bullet : enemyBullets) {
                if (bullet.getY() > GAME_AREA_HEIGHT) {
                    enemyBullets.remove(bullet);
                }
            }
        }
    }

    private void EnemyBulletsDamage() {
        if (start && !gameOver && player.getLife() > 0) {
            for (Bullet bullet : enemyBullets) {
                Rect bulletRect = new Rect(bullet.getX(), bullet.getY(), bullet.getX() + bullet.getWidth(), bullet.getY() + bullet.getHeight());
                Rect playerRect = new Rect(player.getX(), player.getY(), player.getX() + player.getWidth(), player.getY() + player.getHeight());
                if (bulletRect.intersect(playerRect)) {
                    enemyBullets.remove(bullet);
                    if (powerUps.get("shield").getName() == null) {
                        int damage = Math.max(1, bullet.getDamage() - player.getDefense());
                        player.setLife(Math.max(0, player.getLife() - damage));
                        activity.runOnUiThread(() -> playerLifeBar.setProgress(player.getLife()));
                        playerDamagedValTxtShownCtr = 0;
                        playerDamagedValTxt = "-" + damage;
                        if (player.getLife() <= 0) {
                            if (playerExplosionCtr == -1) {
                                playerExplosionImg = images.get("small_explosion1");
                                playerExplosionCtr = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private void EnemyDamagedCtr() {
        if (start && !gameOver) {
            if (enemyDamagedValTxtShownCtr > DELAY) {
                enemyDamagedValTxtShownCtr = -1;
                enemyDamagedValTxt = "";
            }
            else if (enemyDamagedValTxtShownCtr != -1) {
                enemyDamagedValTxtShownCtr++;
            }
        }
    }

    private void EnemyExplosion() {
        if (start && !gameOver && enemy != null && enemy.getLife() <= 0) {
            if (enemyExplosionCtr > DELAY) {
                enemyExplosionCtr = -1;
                enemyExplosionImg = null;
            }
            else if (enemyExplosionCtr >= 0) {
                enemyExplosionCtr++;
                if (enemy.getType() < 0) {
                    enemyExplosionImg = images.get("small_explosion" + enemyExplosionCtr);
                }
                else {
                    enemyExplosionImg = images.get("big_explosion" + enemyExplosionCtr);
                }
            }
        }
    }

    private void BossFightDelayCtr() {
        if (start && !gameOver) {
            if (bossFightTxtShownCtr >= 0) {
                if (bossFightTxtShownCtr <= 140) {
                    bossFightTxtShownCtr++;
                }
                else {
                    bossFightTxtShownCtr = -2;
                }
            }
        }
    }

    private void DropsMove() {
        if (start && !gameOver) {
            for (Drops drops : dropss) {
                drops.setY(drops.getY() + 10);
            }
        }
    }

    private void DropsOutOfArea() {
        if (start && !gameOver) {
            for (Drops drops : dropss) {
                if (drops.getY() > GAME_AREA_HEIGHT) {
                    dropss.remove(drops);
                }
            }
        }
    }

    private void DropsReceived() {
        if (start && !gameOver && player.getLife() > 0) {
            for (Drops drops : dropss) {
                Rect dropsRect = new Rect(drops.getX(), drops.getY(), drops.getX() + drops.getWidth(), drops.getY() + drops.getHeight());
                Rect playerRect = new Rect(player.getX(), player.getY(), player.getX() + player.getWidth(), player.getY() + player.getHeight());
                if (dropsRect.intersect(playerRect)) {
                    if (drops.getType() == 0) {
                        final int EXTRA_LIFE = 5;
                        player.setLife(Math.min(player.getLife() + EXTRA_LIFE, player.getMaxLife()));
                        activity.runOnUiThread(() -> {
                            playerLifeBar.setProgress(player.getLife());
                        });
                        playerExtraLifeValTxtShownCtr = 0;
                        playerExtraLifeValTxt = "+" + EXTRA_LIFE;
                    }
                    else {
                        powerUps.put(drops.getName(), drops);
                        if (drops.getType() >= 1 && drops.getType() <= 4) {
                            for (Object key : powerUps.keySet().toArray()) {
                                String k = (String)key;
                                if (!k.equals(drops.getName()) && !k.equals("shield") && powerUps.get(k).getName() != null) {
                                    powerUps.put(k, new Drops());
                                }
                            }
                        }
                    }
                    dropss.remove(drops);
                }
            }
        }
    }

    private void PowerUpDuration() {
        if (start && !gameOver && player.getLife() > 0) {
            for (Object key : powerUps.keySet().toArray()){
                String k = (String)key;
                if (powerUps.get(k).getName() != null) {
                    if (powerUps.get(k).getDuration() <= 0) {
                        powerUps.put(k, new Drops());
                    }
                    else {
                        powerUps.get(k).setDuration(powerUps.get(k).getDuration() - 1);
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (loaded) {
            Paint paint = new Paint();
            if (player.getLife() > 0) {
                int cx = player.getX() + player.getWidth() / 2, cy = player.getY() + player.getHeight() / 2;
                Bitmap playerBm = BitmapFactory.decodeResource(getResources(), player.getImgId());
                canvas.drawBitmap(playerBm, player.getX(), player.getY(), new Paint());
                if (powerUps.get("shield").getName() != null) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.rgb(173,216,230));
                    canvas.drawCircle(cx, cy, 200, paint);
                }
            }
            paint.setStyle(Paint.Style.FILL);
            for (Bullet bullet : playerBullets) {
                if (bullet.getType() < 2) {
                    paint.setColor(bullet.getColor());
                    canvas.drawRect(bullet.getX(), bullet.getY(), bullet.getX() + bullet.getWidth(), bullet.getY() + bullet.getHeight(), paint);
                }
                else {
                    Bitmap bulletBm = BitmapFactory.decodeResource(getResources(), bullet.getImgId());
                    canvas.drawBitmap(bulletBm, bullet.getX(), bullet.getY(), new Paint());
                }
            }
            if (!playerDamagedValTxt.isEmpty()) {
                float x = player.getX() + player.getWidth(), y = player.getY();
                paint.setColor(Color.RED);
                paint.setTextSize(SMALL_TEXT_SIZE);
                Button btn = activity.findViewById(R.id.backBtn);
                paint.setTypeface(btn.getTypeface());
                canvas.drawText(playerDamagedValTxt, x, y, paint);
            }
            if (!playerExtraLifeValTxt.isEmpty()) {
                float x = player.getX() + player.getWidth(), y = player.getY();
                paint.setColor(Color.rgb(144, 238, 144));
                paint.setTextSize(SMALL_TEXT_SIZE);
                Button btn = activity.findViewById(R.id.backBtn);
                paint.setTypeface(btn.getTypeface());
                canvas.drawText(playerExtraLifeValTxt, x, y, paint);
            }
            if (playerExplosionImg != null) {
                canvas.drawBitmap(playerExplosionImg, player.getX(), player.getY(), new Paint());
            }
            if (enemy != null && enemy.getLife() > 0) {
                Bitmap enemyBm = BitmapFactory.decodeResource(getResources(), enemy.getImgId());
                if (enemy.getType() < 0) {
                    int remainingLifeWidth = enemy.getLife() * enemy.getWidth() / enemy.getMaxLife();
                    paint.setColor(Color.RED);
                    canvas.drawRect(enemy.getX(), enemy.getY() - playerLifeBar.getLayoutParams().height * 5, enemy.getX() + remainingLifeWidth, enemy.getY(), paint);
                    paint.setColor(Color.DKGRAY);
                    canvas.drawRect(enemy.getX() + remainingLifeWidth, enemy.getY() - playerLifeBar.getLayoutParams().height * 5, enemy.getX() + enemy.getWidth(), enemy.getY(), paint);
                }
                canvas.drawBitmap(enemyBm, enemy.getX(), enemy.getY(), new Paint());
            }
            for (Bullet bullet : enemyBullets) {
                if (bullet.getType() < 9) {
                    paint.setColor(bullet.getColor());
                    canvas.drawRect(bullet.getX(), bullet.getY(), bullet.getX() + bullet.getWidth(), bullet.getY() + bullet.getHeight(), paint);
                }
                else {
                    Bitmap bulletBm = BitmapFactory.decodeResource(getResources(), bullet.getImgId());
                    canvas.drawBitmap(bulletBm, bullet.getX(), bullet.getY(), new Paint());
                }
            }
            if (!enemyDamagedValTxt.isEmpty()) {
                float x = enemy.getX() + enemy.getWidth(), y = enemy.getY();
                paint.setColor(Color.RED);
                paint.setTextSize(SMALL_TEXT_SIZE);
                Button btn = activity.findViewById(R.id.backBtn);
                paint.setTypeface(btn.getTypeface());
                canvas.drawText(enemyDamagedValTxt, x, y, paint);
            }
            if (enemyExplosionImg != null) {
                canvas.drawBitmap(enemyExplosionImg, enemy.getX(), enemy.getY(), new Paint());
            }
            if (!bossFightTxt.isEmpty()) {
                float x = (GAME_AREA_WIDTH / 2) - (44 * bossFightTxt.length() / 2), y = (GAME_AREA_HEIGHT / 2) - (BIG_TEXT_SIZE / 2);
                paint.setColor(Color.RED);
                paint.setTextSize(BIG_TEXT_SIZE);
                Button btn = activity.findViewById(R.id.backBtn);
                paint.setTypeface(Typeface.create(btn.getTypeface(), Typeface.BOLD));
                canvas.drawText(bossFightTxt, x, y, paint);
            }
            for (Drops drops : dropss) {
                Bitmap dropsBm = BitmapFactory.decodeResource(getResources(), drops.getImgId());
                canvas.drawBitmap(dropsBm, drops.getX(), drops.getY(), new Paint());
            }
            float imgTop = playerScoreTv.getBottom() + OFFSET * 10;
            for (Object key : powerUps.keySet().toArray()) {
                String k = (String)key;
                if (powerUps.get(k).getName() != null) {
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(SMALL_TEXT_SIZE);
                    Button btn = activity.findViewById(R.id.backBtn);
                    paint.setTypeface(btn.getTypeface());
                    float imgLeft = playerLifeBar.getRight() - powerUps.get(k).getWidth();
                    float imgRight = imgLeft + powerUps.get(k).getWidth();
                    float txtLeft = Math.max((imgLeft + imgRight) / 2 - paint.getTextSize() * 2, imgLeft);
                    float txtTop = imgTop + powerUps.get(k).getHeight() + OFFSET;
                    Bitmap powerUpsBm = BitmapFactory.decodeResource(getResources(), powerUps.get(k).getImgId());
                    String durationTxt = "" + (INTERVAL * powerUps.get(k).getDuration() / 1000);
                    canvas.drawBitmap(powerUpsBm, imgLeft, imgTop, new Paint());
                    canvas.drawText("00:" + String.format("%2s", durationTxt).replace(' ', '0'), txtLeft, txtTop, paint);
                    imgTop += powerUps.get(k).getHeight() + OFFSET * 5;
                }
            }
        }
    }
}
