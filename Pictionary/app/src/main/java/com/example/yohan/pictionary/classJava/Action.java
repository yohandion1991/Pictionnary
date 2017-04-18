package com.example.yohan.pictionary.classJava;

import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public abstract class  Action  {
        private String action;

    public Action(String action) {
        this.action = action;
    }
}
