package com.example.yohan.pictionary.classJava;

/**
 * Created by Yohan on 09/03/2017.
 */

public class ActionPicture extends Action{
    private float positionX;
    private float positionY;
    //True = touch
    //False =touch-move
    private Boolean start;
    private int color;

    public int getColor() {
        return color;
    }

    public Boolean getStart() {
        return start;
    }

    public float getPositionX() {
        return positionX;
    }

    public float getPositionY() {
        return positionY;
    }

    public ActionPicture(int color, float positionX, float positionY, Boolean start) {
        super("picture");
        this.color=color;
        this.positionX = positionX;
        this.positionY = positionY;
        this.start = start;
    }
}
