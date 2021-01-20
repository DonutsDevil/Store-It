package com.example.storeit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "storeIt.db";
    private static final int DATABASE_VERSION = 1;


    public UserDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(clientContract.ClientEntry.SQL_CREATE_USER_INFO_TABLE);
        db.execSQL(clientContract.ClientInfo.SQL_CREATE_PRODUCT_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
