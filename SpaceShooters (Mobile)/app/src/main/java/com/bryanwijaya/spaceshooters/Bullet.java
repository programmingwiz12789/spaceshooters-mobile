package com.bryanwijaya.spaceshooters;

public class Bullet {
    private int x, y, width, height, damage, type, color, imgId;

    public Bullet(int x, int y, int width, int height, int damage, int type, int color, int imgId) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.damage = damage;
        this.type = type;
        this.color = color;
        this.imgId = imgId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDamage() {
        return damage;
    }

    public int getType() {
        return type;
    }

    public int getColor() {
        return color;
    }

    public int getImgId() {
        return imgId;
    }
}
