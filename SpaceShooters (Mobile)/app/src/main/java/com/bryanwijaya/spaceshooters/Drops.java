package com.bryanwijaya.spaceshooters;

public class Drops {
    private String name;
    private int x, y, width, height, type, duration, imgId;

    public Drops() {}

    public Drops(String name, int x, int y, int width, int height, int type, int duration, int imgId) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.duration = duration;
        this.imgId = imgId;
    }

    public String getName() {
        return name;
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

    public int getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getImgId() {
        return imgId;
    }
}
