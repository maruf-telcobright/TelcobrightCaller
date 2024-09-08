package com.example.telcobrightcaller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SQLiteCallFragmentHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "CallDetails.db";
    private static final String TABLE_NAME = "callList";
    private static final int DB_VERSION = 2;

    private static final String ID = "_id";
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String PHOTO = "photo";
    private SQLiteDatabase sqliteDatabase;

    private Context context;

    public SQLiteCallFragmentHelper(@Nullable Context context) {
        super(context != null ? context.getApplicationContext() : null, DB_NAME, null, DB_VERSION);
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null");
        }
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (context == null) {
            Log.e("SQLiteCallFragmentHelp", "Context is null in onCreate");
            return;
        }

        try {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NAME + " VARCHAR(255), " +
                    NUMBER + " VARCHAR(255), " +
                    PHOTO + " VARCHAR(255));");
//            showToast("Table Created Successfully");
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelp", "Error creating table", e);
            showToast("Exception Caught: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
            showToast("Database upgraded successfully");
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelp", "Error upgrading table", e);
            showToast("Exception Caught onUpgrade: " + e.getMessage());
        }
    }

    public long insertData(String name, String number, String photo) {
        long rowId = -1;
        try {
            sqliteDatabase = this.getWritableDatabase();
            sqliteDatabase.beginTransaction();  // Start transaction
            ContentValues contentValues = new ContentValues();
            contentValues.put(NAME, name);
            contentValues.put(NUMBER, number);
            contentValues.put(PHOTO, photo);
            rowId = sqliteDatabase.insertOrThrow(TABLE_NAME, null, contentValues);
            sqliteDatabase.setTransactionSuccessful();  // Mark transaction as successful
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelp", "Error inserting data", e);
            showToast("Error inserting data: " + e.getMessage());
        } finally {
            if (sqliteDatabase != null) {
                sqliteDatabase.endTransaction();  // End transaction
                if (sqliteDatabase.isOpen()) {
                    sqliteDatabase.close();
                }
            }
        }
        return rowId;
    }

    public List<CallRecord> getAllRecords() {
        List<CallRecord> returnList = new ArrayList<>();
        Cursor cursor = null;

        try {
            sqliteDatabase = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + ID + " DESC";
            cursor = sqliteDatabase.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(ID);
                    int nameIndex = cursor.getColumnIndex(NAME);
                    int numberIndex = cursor.getColumnIndex(NUMBER);
                    int photoIndex = cursor.getColumnIndex(PHOTO);

                    if (idIndex == -1 || nameIndex == -1 || numberIndex == -1 || photoIndex == -1) {
                        Log.e("SQLiteCallFragmentHelp", "Invalid column index");
                        continue;
                    }

                    String id = cursor.getString(idIndex);
                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex);
                    String photo = cursor.getString(photoIndex);

                    CallRecord callRecord = new CallRecord(id, name, number, photo);
                    returnList.add(callRecord);
                } while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            Log.e("SQLiteCallFragmentHelp", "Error reading data", e);
            showToast("Error reading data: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqliteDatabase != null && sqliteDatabase.isOpen()) {
                sqliteDatabase.close();
            }
        }

        return returnList;
    }

    private void showToast(String message) {
        if (context != null) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}
