package com.example.storeit.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class UserProvider extends ContentProvider {
    private static final String LOG_TAG = UserProvider.class.getSimpleName();
    private static final int INSERT_USER_INFO = 100;
    private static final int INSERT_PRODUCT_INFO = 101;
    private static final int GET_TOTAL_ITEMS_PRICE = 104;
    private static final int GET_PRODUCT_INFO_SINGLE_ITEM_LIST_VIEW = 105;
    private static final int INSERT_PRODUCT_INFO_ITEM = 106;
    private static final int DELETE_PRODUCT = 200;
    private static final int DELETE_USER = 201;
    private static final int SEARCH_USER = 300;
    private static final int SEARCH_PRODUCT = 301;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    static{
        /**100 and 101 code uri are for Inserting data into the tables in the user_info and product_info */
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_USER_INFO,INSERT_USER_INFO);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_PRODUCT_INFO,INSERT_PRODUCT_INFO);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_USER_INFO_SEARCH,SEARCH_USER);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_PRODUCT_INFO_TOTAL_ITEMS_PRICE,GET_TOTAL_ITEMS_PRICE);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_PRODUCT_INFO_SINGLE_PRODUCT,GET_PRODUCT_INFO_SINGLE_ITEM_LIST_VIEW);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_PRODUCT_INFO_INSERT_ITEM,INSERT_PRODUCT_INFO_ITEM);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_PRODUCT_INFO+"/#",DELETE_PRODUCT);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_USER_INFO_DELETE+"/#",DELETE_USER);
        sUriMatcher.addURI(clientContract.CONTENT_AUTHORITY,clientContract.PATH_PRODUCT_NAME_SEARCH,SEARCH_PRODUCT);
    }
    /*Database helper object */
    private UserDbHelper mDbHelper;

    /** Initialize the provider and the database object*/
    @Override
    public boolean onCreate() {
        mDbHelper = new UserDbHelper(getContext());
        return true;
    }

    /*Perform the query for the given URI*/
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);
        switch (match){
            case INSERT_USER_INFO:{
                String tableName = clientContract.ClientEntry.TABLE_NAME+" INNER JOIN "+clientContract.ClientInfo.TABLE_NAME;
                if (projection == null){
                    cursor = db.query(clientContract.ClientEntry.TABLE_NAME,null,null,null,null,null,null);
                }
                else if (projection.length == 3) {
                    String from = clientContract.ClientEntry.TABLE_NAME;
                    cursor = db.query(from, projection, selection, null, null, null, sortOrder);
                }else if (projection.length == 6 && sortOrder != null){
//                    String tableName = clientContract.ClientEntry.TABLE_NAME+" INNER JOIN "+clientContract.ClientInfo.TABLE_NAME;
                    cursor = db.query(tableName,projection,selection,null,null,null,sortOrder);
                }else if (projection.length == 6 && sortOrder == null){
//                    String tableName = clientContract.ClientEntry.TABLE_NAME+" INNER JOIN "+clientContract.ClientInfo.TABLE_NAME;
                    cursor = db.query(tableName,projection,selection,selectionArgs,null,null,null);
                }else if(projection.length == 4 && selectionArgs == null){
                    String groupBy = clientContract.ClientInfo.TABLE_NAME+"."+clientContract.ClientInfo.COLUMN_FK_ID;
                    cursor = db.query(tableName,projection,selection,null,groupBy,null,sortOrder);
                }else if (projection.length == 4 && selectionArgs != null){
                    String groupBy = clientContract.ClientInfo.TABLE_NAME+"."+clientContract.ClientInfo.COLUMN_FK_ID;
                    cursor = db.query(tableName,projection,selection,selectionArgs,groupBy,null,sortOrder);
                }else if (projection.length == 5 && selectionArgs == null){
                    cursor = db.query(tableName,projection,selection,null,null,null,sortOrder);
                }else if (projection.length == 5 && selectionArgs != null){
                    cursor = db.query(tableName,projection,selection,selectionArgs,null,null,sortOrder);
                }
                break;
            }
            case INSERT_PRODUCT_INFO:{
                if (projection == null){
                    cursor = db.query(clientContract.ClientInfo.TABLE_NAME,null,null,null,null,null,null);
                }
                else if (projection.length == 2){
                    cursor = db.query(clientContract.ClientInfo.TABLE_NAME, projection,selection,null,null,null,null);
                }
                else if(projection.length == 3){
                    cursor = db.query(clientContract.ClientInfo.TABLE_NAME, projection, null, null, null, null, null);
                }else {
                    cursor = db.query(clientContract.ClientInfo.TABLE_NAME, projection, selection, null, null, null, sortOrder);
                }
                break;
            }
            case SEARCH_USER:{
                    cursor = db.query(clientContract.ClientEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
                    break;
            }
            case GET_TOTAL_ITEMS_PRICE: {
                if (projection != null &&projection.length == 4){
                    String tableName = clientContract.ClientEntry.TABLE_NAME+" INNER JOIN "+clientContract.ClientInfo.TABLE_NAME;
                    cursor = db.query(tableName,projection,selection,null,null,null,null);
                }
                else if (projection != null)
                cursor = db.query(clientContract.ClientInfo.TABLE_NAME,projection,selection,null,null,null,null);
                else
                    cursor = db.query(clientContract.ClientInfo.TABLE_NAME,null,selection,null,null,null,null);
                break;
            }
            case GET_PRODUCT_INFO_SINGLE_ITEM_LIST_VIEW: {
                if (true) {
                    cursor = db.query(clientContract.ClientInfo.TABLE_NAME, projection, selection, null, null, null, null);
                    break;
                }
            }

            case SEARCH_PRODUCT: {
                cursor = db.query(clientContract.ClientInfo.TABLE_NAME,projection,selection,selectionArgs,null,null,null);
                break;
            }

            default:{
                Log.i(LOG_TAG,"TEST: Cannot query: Unknown URI "+ uri);
            }
        }
        /** Set notification URI on cursor
         * so we know what content URI the cursor was created for
         * if the data at tis URI changes, then we need to update the cursor*/
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        Log.i(LOG_TAG, " USERPROVIDER query: called SetNotification"+uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INSERT_USER_INFO :{
                return clientContract.ClientEntry.CONTENT_LIST_TYPE_USER_INFO;
            }
            case INSERT_PRODUCT_INFO :{
                return clientContract.ClientInfo.CONTENT_LIST_TYPE_PRODUCT_INFO;
            }
            case SEARCH_USER :{
                return clientContract.ClientEntry.CONTENT_LIST_TYPE_USER_INFO_SEARCH;
            }
            case GET_TOTAL_ITEMS_PRICE: {
                return clientContract.ClientInfo.CONTENT_LIST_TYPE_PRODUCT_INFO_TOTAL_ITEMS_PRICE;
            }
            case GET_PRODUCT_INFO_SINGLE_ITEM_LIST_VIEW: {
                return clientContract.ClientInfo.CONTENT_LIST_TYPE_PRODUCT_SINGLE_ITEM_VIEW;
            }
            case INSERT_PRODUCT_INFO_ITEM: {
                return clientContract.ClientInfo.CONTENT_LIST_TYPE_PRODUCT_INSERT_ITEM;
            }
            case DELETE_PRODUCT: {
                return clientContract.ClientInfo.CONTENT_LIST_TYPE_PRODUCT_INFO+"/#";
            }
            case DELETE_USER : {
                return clientContract.ClientEntry.CONTENT_LIST_TYPE_USER_INFO_DELETE;
            }
            case SEARCH_PRODUCT :{
                return clientContract.ClientInfo.CONTENT_LIST_TYPE_PRODUCT_SEARCH_PRODUCT;
            }

            default:{
                Log.i(LOG_TAG,"TEST: UNKNOWN URI "+ uri+" with match "+match);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match){
            case INSERT_USER_INFO :{
                return insertUserInfo(uri,values);
            }
            case INSERT_PRODUCT_INFO:{
                return insertProductInfo(uri,values);
            }
            case INSERT_PRODUCT_INFO_ITEM: {
                return insertProductInfo(uri,values);
            }
        }
        return null;
    }

    private Uri insertUserInfo(Uri uri, ContentValues values){
        // Get writeable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Insert the new user_info with the given values
        long id = db.insert(clientContract.ClientEntry.TABLE_NAME,null,values);
        // Notify all listeners that the data has changed fot the user_info content uri
        getContext().getContentResolver().notifyChange(uri,null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri,id);
    }

    private Uri insertProductInfo(Uri uri, ContentValues values){
        // Get writeable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Insert the new user product information with the given values
        long id = db.insert(clientContract.ClientInfo.TABLE_NAME,null,values);
        // Notify all listeners that the data has changed fot the product_info content uri
        getContext().getContentResolver().notifyChange(uri,null);
        Log.i(LOG_TAG, " USERPROVIDER insertProductInfo: insertURi: "+ContentUris.withAppendedId(uri,id));
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case DELETE_PRODUCT:{
                return deletedProductNumbers(uri,selection);
            }
            case DELETE_USER : {
                return deletedUser(uri,selection);
            }
        }
        return 0;
    }

    private int deletedUser(Uri uri, String selection){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri,null);
        int id =  db.delete(clientContract.ClientEntry.TABLE_NAME,selection,null);
        Log.d("TAG", "deletedProductNumbers: "+id);
        return id;
    }
    private int deletedProductNumbers(Uri uri, String selection){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        getContext().getContentResolver().notifyChange(uri,null);
        int id = db.delete(clientContract.ClientInfo.TABLE_NAME, selection,null);
        Log.d("TAG", "deletedProductNumbers: "+id);
        return id;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case INSERT_PRODUCT_INFO : {
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                return db.update(clientContract.ClientInfo.TABLE_NAME,values,selection,null);
            }
        }
        return 0;
    }
}
