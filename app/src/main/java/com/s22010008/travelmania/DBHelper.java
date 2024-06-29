package com.s22010008.travelmania;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "DiscussionMessages.db";
    private static DBHelper instance;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DiscussionContract.MessageEntry.TABLE_NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE + " TEXT," +
                    DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME + " TEXT," +
                    DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL + " TEXT," +
                    DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID + " TEXT," +
                    DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_ID + " TEXT," +
                    DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME + " TEXT)";

    private static final String SQL_CREATE_PLACES_TABLE =
            "CREATE TABLE Places (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    "PlaceId TEXT," +
                    "PlaceName TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DiscussionContract.MessageEntry.TABLE_NAME;
    private static final String SQL_DELETE_PLACES_TABLE =
            "DROP TABLE IF EXISTS Places";

    private Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + DiscussionContract.MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME + " TEXT");
                db.execSQL("ALTER TABLE " + DiscussionContract.MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL + " TEXT");
            case 2:
                db.execSQL("ALTER TABLE " + DiscussionContract.MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID + " TEXT");
            case 3:
                db.execSQL("ALTER TABLE " + DiscussionContract.MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_ID + " TEXT");
            case 4:
                db.execSQL("ALTER TABLE " + DiscussionContract.MessageEntry.TABLE_NAME +
                        " ADD COLUMN " + DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME + " TEXT");
                db.execSQL(SQL_CREATE_PLACES_TABLE);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean updateMessage(long messageId, String newMessageText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE, newMessageText);

        String selection = DiscussionContract.MessageEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(messageId) };

        int count = db.update(
                DiscussionContract.MessageEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return count > 0;
    }

    public boolean deleteMessage(long messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = DiscussionContract.MessageEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(messageId) };

        int count = db.delete(
                DiscussionContract.MessageEntry.TABLE_NAME,
                selection,
                selectionArgs);

        return count > 0;
    }

    public List<Message> getAllMessages(String placeId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Message> messages = new ArrayList<>();

        if (placeId == null) {
            Log.w("DBHelper", "getAllMessages() called with null placeId");
            return messages;
        }

        String selection = DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_ID + " = ?";
        String[] selectionArgs = { placeId };

        int dbVersion = db.getVersion();
        String[] projection = {
                DiscussionContract.MessageEntry._ID,
                DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE,
                DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME,
                DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL,
                DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME
        };
        if (dbVersion >= 3) {
            projection = new String[]{
                    DiscussionContract.MessageEntry._ID,
                    DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE,
                    DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME,
                    DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL,
                    DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID,
                    DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME
            };
        }

        Cursor cursor = db.query(
                DiscussionContract.MessageEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            long messageId = cursor.getLong(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry._ID));
            String messageText = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE));
            String senderName = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME));
            String senderPhotoUrl = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_PHOTO_URL));
            String placeName = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME));

            Message message;
            if (dbVersion >= 3) {
                String senderId = cursor.getString(cursor.getColumnIndexOrThrow(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID));
                message = new Message(messageText, senderName, senderPhotoUrl, messageId, senderId, placeId, placeName);
            } else {
                message = new Message(messageText, senderName, senderPhotoUrl, messageId, "", placeId, placeName);
            }
            messages.add(message);
        }
        cursor.close();
        return messages;
    }

    public long insertMessage(String messageText, String placeId, String placeName, String senderId, String senderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DiscussionContract.MessageEntry.COLUMN_NAME_MESSAGE, messageText);
        values.put(DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_ID, placeId);
        values.put(DiscussionContract.MessageEntry.COLUMN_NAME_PLACE_NAME, placeName);
        values.put(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_ID, senderId);
        values.put(DiscussionContract.MessageEntry.COLUMN_NAME_SENDER_NAME, senderName); // Store senderName

        return db.insert(DiscussionContract.MessageEntry.TABLE_NAME, null, values);
    }

    public long insertPlace(String placeId, String placeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("PlaceId", placeId);
        values.put("PlaceName", placeName);

        return db.insert("Places", null, values);
    }

    public String getPlaceIdForName(String placeName) {
        SQLiteDatabase db = getReadableDatabase();
        String placeId = null;

        String[] projection = { "PlaceId" };
        String selection = "PlaceName = ?";
        String[] selectionArgs = { placeName };

        Cursor cursor = db.query(
                "Places",
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            placeId = cursor.getString(cursor.getColumnIndexOrThrow("PlaceId"));
        }

        cursor.close();
        return placeId;
    }

    public String generateAndInsertPlaceId(String placeName) {
        String existingPlaceId = getPlaceIdForName(placeName);
        if (existingPlaceId != null) {
            return existingPlaceId;
        }

        String newPlaceId = UUID.randomUUID().toString();
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("PlaceId", newPlaceId);
        values.put("PlaceName", placeName);

        db.insert("Places", null, values);
        return newPlaceId;
    }
}
