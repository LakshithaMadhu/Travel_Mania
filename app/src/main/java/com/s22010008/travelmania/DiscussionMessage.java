package com.s22010008.travelmania;

public class DiscussionMessage {
    private long id;
    private String message;

    // Constructor
    public DiscussionMessage(String message) {
        this.message = message;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
