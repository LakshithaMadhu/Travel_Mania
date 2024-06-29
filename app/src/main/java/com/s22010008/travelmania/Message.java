package com.s22010008.travelmania;

public class Message {
    private String messageText;
    private String senderName;
    private String senderPhotoUrl;
    private long id;
    private String senderId;
    private String placeId;
    private String placeName;


    public Message(String messageText, String senderName, String senderPhotoUrl,
                   long id, String senderId, String placeId, String placeName) {
        this.messageText = messageText;
        this.senderName = senderName;
        this.senderPhotoUrl = senderPhotoUrl;
        this.id = id;
        this.senderId = senderId;
        this.placeId = placeId;
        this.placeName = placeName; // Initialize the placeName field
    }
    public String getMessageText() {
        return messageText;
    }



    public String getPlaceId() {
        return placeId;
    }
    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhotoUrl() {
        return senderPhotoUrl;
    }

    public void setSenderPhotoUrl(String senderPhotoUrl) {
        this.senderPhotoUrl = senderPhotoUrl;
    }

    public long getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
