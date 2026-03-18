package com.bryanwijaya.spaceshooters;

public class Enemy {
    private int life, maxLife, defense, type, x, y, dx, dy, width, height, imgId;
    private boolean right, down;

    public Enemy(int x, int y, int width, int height, boolean right, boolean down, int imgId) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.right = right;
        this.down = down;
        this.imgId = imgId;
    }

    public Enemy(int life, int defense, int type, int x, int y, int dx, int dy, int width, int height, int imgId) {
        this.life = life;
        maxLife = life;
        this.defense = defense;
        this.type = type;
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.width = width;
        this.height = height;
        this.imgId = imgId;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getMaxLife() {
        return maxLife;
    }

    public int getDefense() {
        return defense;
    }

    public int getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public int getImgId() {
        return imgId;
    }
}
