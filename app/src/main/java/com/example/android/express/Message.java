package com.example.android.express;

import org.json.JSONObject;

/**
 * Created by pico on 1/08/2016.
 */
public class Message {
    private String emit;
    private JSONObject message;

    public Message(String emit, JSONObject message){
        this.emit = emit;
        this.message = message;
    }

    public JSONObject getMessage() {
        return message;
    }

    public String getEmit() {
        return emit;
    }

    @Override
    public String toString() {
        return "{ emit: " + emit +", message: " + message.toString() + "}";
    }
}
