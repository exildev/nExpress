package co.com.expressdelnorte.expressdelnorte.notix;

import org.json.JSONObject;


public class Message {
    private String emit;
    private JSONObject message;

    public Message(String emit, JSONObject message) {
        this.emit = emit;
        this.message = message;
    }

    JSONObject getMessage() {
        return message;
    }

    String getEmit() {
        return emit;
    }

    @Override
    public String toString() {
        return "{ emit: " + emit + ", message: " + message.toString() + "}";
    }
}
