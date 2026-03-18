package com.bryanwijaya.spaceshooters;

public class Player {
    private int score, life, maxLife, defense, x, y, width, height, imgId;

    public Player(int score, int life, int defense, int x, int y, int width, int height, int imgId) {
        this.score = score;
        this.life = life;
        maxLife = life;
        this.defense = defense;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imgId = imgId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public int getImgId() {
        return imgId;
    }
}
