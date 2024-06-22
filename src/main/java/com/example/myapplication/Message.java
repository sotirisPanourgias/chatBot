package com.example.myapplication;

public class Message {
    private String message;
    private boolean fromUser;

    public Message(String message, boolean fromUser) {
        this.message = message;
        this.fromUser = fromUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromUser() {
        return fromUser;
    }
}

