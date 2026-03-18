package com.bryanwijaya.spaceshooters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashMap;
import java.util.Random;
import java.util.TimerTask;
import java.util.Timer;

public class MenuView extends View {
    private final int ENEMY_SPEED = 10, INTERVAL = 20;
    private int MENU_AREA_WIDTH, MENU_AREA_HEIGHT;
    private HashMap<String, Bitmap> images;
    private boolean loaded;
    private Enemy enemy;
    private TimerTask timerTask;
    private Timer timer;

    public MenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.post(() -> {
            MENU_AREA_WIDTH = getWidth();
            MENU_AREA_HEIGHT = getHeight();
            images = Utils.GetAllImages((Activity)context);
            enemy = null;
            Random rn = new Random();
            int prob = rn.nextInt(100) + 1;
            if (prob >= 1 && prob <= 40) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("enemy1").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("enemy1").getWidth(), images.get("enemy1").getHeight(), true, false, R.drawable.enemy1);
            }
            else if (prob >= 41 && prob <= 60) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("enemy2").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("enemy2").getWidth(), images.get("enemy2").getHeight(), true, false, R.drawable.enemy2);
            }
            else if (prob >= 61 && prob <= 70) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("enemy3").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("enemy3").getWidth(), images.get("enemy3").getHeight(), true, false, R.drawable.enemy3);
            }
            else if (prob >= 71 && prob <= 79) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("enemy4").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("enemy4").getWidth(), images.get("enemy4").getHeight(), true, false, R.drawable.enemy4);
            }
            else if (prob >= 80 && prob <= 87) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("enemy5").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("enemy5").getWidth(), images.get("enemy5").getHeight(), true, false, R.drawable.enemy5);
            }
            else if (prob >= 88 && prob <= 94) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("enemy6").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("enemy6").getWidth(), images.get("enemy6").getHeight(), true, false, R.drawable.enemy6);
            }
            else if (prob >= 95 && prob <= 97) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("boss1").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("boss1").getWidth(), images.get("boss1").getHeight(), true, false, R.drawable.boss1);
            }
            else if (prob >= 98 && prob <= 99) {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("boss2").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("boss2").getWidth(), images.get("boss2").getHeight(), true, false, R.drawable.boss2);
            }
            else {
                int posX = rn.nextInt(MENU_AREA_WIDTH - images.get("boss3").getWidth() + 1);
                enemy = new Enemy(posX, 0, images.get("boss3").getWidth(), images.get("boss3").getHeight(), true, false, R.drawable.boss3);
            }
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    EnemyMove();
                }
            };
            timer = new Timer(true);
            timer.schedule(timerTask, 0, INTERVAL);
            loaded = true;
        });
    }

    private void EnemyMove() {
        final int LIMIT_X = MENU_AREA_WIDTH - enemy.getWidth(), LIMIT_Y = MENU_AREA_HEIGHT - enemy.getHeight();
        if (enemy.isRight()) {
            enemy.setDx(ENEMY_SPEED);
            enemy.setDy(0);
            if (enemy.getX() + enemy.getDx() >= LIMIT_X) {
                enemy.setDx(-ENEMY_SPEED);
                enemy.setRight(false);
                if (enemy.isDown()) {
                    if (enemy.getY() + enemy.getDy() >= LIMIT_Y) {
                        enemy.setDy(-ENEMY_SPEED * 2);
                        enemy.setDown(false);
                    }
                    else {
                        enemy.setDy(ENEMY_SPEED * 2);
                    }
                }
                else {
                    if (enemy.getY() + enemy.getDy() <= 0) {
                        enemy.setDy(ENEMY_SPEED * 2);
                        enemy.setDown(true);
                    }
                    else {
                        enemy.setDy(-ENEMY_SPEED * 2);
                    }
                }
            }
        }
        else {
            enemy.setDx(-ENEMY_SPEED);
            enemy.setDy(0);
            if (enemy.getX() + enemy.getDx() <= 0) {
                enemy.setDx(ENEMY_SPEED);
                enemy.setRight(true);
                if (enemy.isDown()) {
                    if (enemy.getY() + enemy.getDy() >= LIMIT_Y) {
                        enemy.setDy(-ENEMY_SPEED * 2);
                        enemy.setDown(false);
                    }
                    else {
                        enemy.setDy(ENEMY_SPEED * 2);
                    }
                }
                else {
                    if (enemy.getY() + enemy.getDy() <= 0) {
                        enemy.setDy(ENEMY_SPEED * 2);
                        enemy.setDown(true);
                    }
                    else {
                        enemy.setDy(-ENEMY_SPEED * 2);
                    }
                }
            }
        }
        enemy.setX(Math.min(Math.max(0, enemy.getX() + enemy.getDx()), LIMIT_X));
        enemy.setY(Math.min(Math.max(0, enemy.getY() + enemy.getDy()), LIMIT_Y));
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (loaded) {
            Bitmap enemyBm = BitmapFactory.decodeResource(getContext().getResources(), enemy.getImgId());
            canvas.drawBitmap(enemyBm, enemy.getX(), enemy.getY(), new Paint());
        }
    }
}
