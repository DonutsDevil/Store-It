package com.example.storeit.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class clientContract {

    /**string constant whose value is the same as that from the AndroidManifest*/
    public static final String CONTENT_AUTHORITY ="com.example.storeIt";
    //To make this a usable URI, we use the parse method which takes in a URI string and returns a Uri.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //This constants stores the path for each of the tables which will be appended to the base content URI.
    public static final String PATH_USER_INFO = "user_info";
    public static final String PATH_PRODUCT_INFO = "product_info";
    public static final String PATH_PRODUCT_INFO_TOTAL_ITEMS_PRICE = "product_info_total_items_price";
    public static final String PATH_PRODUCT_INFO_SINGLE_PRODUCT = "product_info_single_item";
    public static final String PATH_USER_INFO_SEARCH = "user_info_search";
    public static final String PATH_PRODUCT_NAME_SEARCH = "product_info_search";
    public static final String PATH_PRODUCT_INFO_INSERT_ITEM = "product_view_insert";
    public static final String PATH_USER_INFO_DELETE = "user_info_delete";

    // we don't want to create a instance of this class.
    private clientContract(){}

    // DataTypes for Database storeIt.db
    private static final String INTEGER_TYPE = "INTEGER";
    private static final String TEXT_TYPE = "TEXT";
    private static final String BLOB_TYPE = "BLOB";



    // contract class for the table user_info
    public static final class ClientEntry implements BaseColumns{
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of USER.
         */
        public static final String CONTENT_LIST_TYPE_USER_INFO = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_USER_INFO;
        public static final String CONTENT_LIST_TYPE_USER_INFO_SEARCH = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_USER_INFO_SEARCH;
        public static final String CONTENT_LIST_TYPE_USER_INFO_DELETE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_USER_INFO_DELETE;
        public static final Uri CONTENT_URI_INFO_DELETE = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_USER_INFO_DELETE);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_USER_INFO);
        public static final Uri CONTENT_URI_SEARCH_NAME = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_USER_INFO_SEARCH);
        public static final String TABLE_NAME = "user_info";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FIRST_NAME = "fname";
        public  static final String COLUMN_LOCATION = "location";

        // create table  user_info
        public static final String SQL_CREATE_USER_INFO_TABLE = "create table "+TABLE_NAME+"("+_ID+" "+INTEGER_TYPE+" PRIMARY KEY AUTOINCREMENT, "
                                                            +COLUMN_FIRST_NAME+" "+TEXT_TYPE+", "+COLUMN_LOCATION+" "+TEXT_TYPE+");";
    }

    // contract class for the table product_info
    public static final class ClientInfo{
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of PRODUCT.
         */
        public static final String CONTENT_LIST_TYPE_PRODUCT_INFO = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PRODUCT_INFO;
        public static final String CONTENT_LIST_TYPE_PRODUCT_INFO_TOTAL_ITEMS_PRICE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PRODUCT_INFO_TOTAL_ITEMS_PRICE;
        public static final String CONTENT_LIST_TYPE_PRODUCT_SINGLE_ITEM_VIEW = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PRODUCT_INFO_SINGLE_PRODUCT;
        public static final String CONTENT_LIST_TYPE_PRODUCT_INSERT_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PRODUCT_INFO_INSERT_ITEM;
        public static final String CONTENT_LIST_TYPE_PRODUCT_SEARCH_PRODUCT = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_PRODUCT_NAME_SEARCH;
        public static final Uri  CONTENT_URI_PRODUCT_INFO_SINGLE_ITEM = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PRODUCT_INFO_SINGLE_PRODUCT);
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT_INFO);
        public static final Uri CONTENT_URI_PRODUCT_INFO_TOTAL_ITEMS_PRICE = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PRODUCT_INFO_TOTAL_ITEMS_PRICE);
        public static final Uri CONTENT_URI_PRODUCT_INFO_INSERT_ITEM = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PRODUCT_INFO_INSERT_ITEM);
        public static final Uri CONTENT_URI_SEARCH_PRODUCT = Uri.withAppendedPath(BASE_CONTENT_URI,PATH_PRODUCT_NAME_SEARCH);
        public static final String TABLE_NAME = "product_info";
        public static final String COLUMN_PK_ID = "_id";
        public static final String COLUMN_FK_ID = "id";
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_ACTUAL_PRICE = "actual_price";
        public static final String COLUMN_SELLING_PRICE = "selling_price";
        public static final String COLUMN_PROFIT = "profit";
        public static final String COLUMN_PROFILE_DATE = "buying_date";
        public static final String COLUMN_PAYMENT_MODE = "payment";
        public static final String COLUMN_PRODUCT_IMAGE = "image";
        public static final String COLUMN_PENDING = "pending_payment";
        public static final String COLUMN_PRODUCT_SIZE = "product_size";

        // create table product_info.
        public static final String SQL_CREATE_PRODUCT_INFO_TABLE = "create table "+TABLE_NAME+" ( "
                                                                +COLUMN_PK_ID + " "+INTEGER_TYPE+" PRIMARY KEY AUTOINCREMENT, "
                                                                +COLUMN_FK_ID+" "+INTEGER_TYPE+", "
                                                                +COLUMN_PRODUCT_NAME+" "+TEXT_TYPE+", "
                                                                +COLUMN_PRODUCT_SIZE+" "+TEXT_TYPE+", "
                                                                +COLUMN_ACTUAL_PRICE+" "+INTEGER_TYPE+", "
                                                                +COLUMN_SELLING_PRICE+" "+INTEGER_TYPE+", "
                                                                +COLUMN_PROFIT+" "+INTEGER_TYPE+", "
                                                                +COLUMN_PROFILE_DATE+" "+TEXT_TYPE+", "
                                                                +COLUMN_PAYMENT_MODE+" "+INTEGER_TYPE+", "
                                                                +COLUMN_PRODUCT_IMAGE+" "+BLOB_TYPE+", "
                                                                +COLUMN_PENDING+" "+INTEGER_TYPE+", "
                                                                +"FOREIGN KEY("+COLUMN_FK_ID+") REFERENCES "+ClientEntry.TABLE_NAME+" ("+ClientEntry._ID+"));";

        // payment is the field in the database there{1 is online } {0 is cod}
        public static final int PAYMENT_ONLINE = 0;
        public static final int PAYMENT_COD = 1;


    }
}
