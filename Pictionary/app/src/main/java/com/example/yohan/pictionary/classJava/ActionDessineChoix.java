package com.example.yohan.pictionary.classJava;

/**
 * Created by Yohan on 10/03/2017.
 */

public class ActionDessineChoix extends Action {
    private Boolean dessine;

    public boolean getDessin() {
        return dessine;
    }

    public ActionDessineChoix(boolean dessine) {
        super("dessin");
        this.dessine=dessine;
    }

}
