package com.s22010008.travelmania;

import android.provider.BaseColumns;

public final class DiscussionContract {
    private DiscussionContract() {}

    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "discussion";
        public static final String COLUMN_NAME_MESSAGE = "message";
        public static final String COLUMN_NAME_SENDER_NAME = "senderName";
        public static final String COLUMN_NAME_SENDER_PHOTO_URL = "senderPhotoUrl";
        public static final String COLUMN_NAME_SENDER_ID = "sender_id";
        public static final String COLUMN_NAME_PLACE_ID = "place_id";
        public static final String COLUMN_NAME_PLACE_NAME = "place_name";
    }
}
