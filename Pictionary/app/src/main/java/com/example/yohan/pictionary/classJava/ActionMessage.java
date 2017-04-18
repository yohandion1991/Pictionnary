package com.example.yohan.pictionary.classJava;

/**
 * Created by Yohan on 06/03/2017.
 */

public class ActionMessage extends  Action{
    private String message;

    public String getMessage() {
        return message;
    }

    public ActionMessage(String message) {
        super("message");
        this.message=message;
    }
}
