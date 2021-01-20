package com.example.storeit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.storeit.data.clientContract;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;


public class ProductHomeView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    /** To debug this activity */
    private static final String TAG = ProductHomeView.class.getSimpleName();
    /** This is to display the items in a listView*/
    private ListView listView;
    /** CursorAdapter to help ListView to load views in the listView*/
    private  ProductViewCursorAdapter cursorAdapter;
    /** Unique ID for LoaderCallBacks*/
    private final int LOADER_GET_SINGLE_ITEM = 1;
    /** This has the _id when a user clicks to get the information about a particular Client from the MainActivity
     * _id is parse from the uri received through intent*/
    private long user_id;
    private String clientFirstName;
    private String Location;
    /** Reference use to show Checkbox in selected mode*/
    public static ActionMode mActionMode;
    public static boolean isActionMode;
    /** This is use to store the Product id which is selected to be deleted by the user */
    public static  List<Integer> selectedItemsPositions = new ArrayList<>();
    /** Use to expand and collapse the toolbar when a product is selected*/
    private AppBarLayout appBarLayout;
    /** This is for searching the product of the user in the database. With editText to enter the text abd search btn to search it.*/
    private EditText etSearchBox;
    private ImageView ivSearchBtn;
    private ImageView no_product_image;

    private boolean isClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_home_view);
        // Reference to the textView of to add name and location of the client
        TextView nameAndLocation = findViewById(R.id.tv_mainText_Name_Location);
        etSearchBox = findViewById(R.id.et_search);
        ivSearchBtn = findViewById(R.id.Image_btnSearch);
        no_product_image = findViewById(R.id.no_product_image);

        appBarLayout = findViewById(R.id.appbar);
        final String PROFILE = " Profile\n";
        final String RESIDING = "Residing in ";
        // get the intent from the link {@MainActivity}
        // this will give the uri, Name and Location  of the item selected.
        Intent mainActivityIntent = getIntent();
        final Uri currentUserUri = mainActivityIntent.getData();
        String fName = mainActivityIntent.getStringExtra("firstName");
        String location = mainActivityIntent.getStringExtra("location");
        clientFirstName = fName;
        Location = location;
        String toolbarMainText = fName+PROFILE+RESIDING+location+"...";
        nameAndLocation.setText(toolbarMainText);

        // To send the user to the AddActivity to add a new Product in the database.
        FloatingActionButton fab = findViewById(R.id.add_new_product_homeView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addProductIntent = new Intent(ProductHomeView.this,AddNewClient.class);
                addProductIntent.setData(currentUserUri);
                addProductIntent.putExtra("FirstName",clientFirstName);
                addProductIntent.putExtra("Location", Location);
                startActivity(addProductIntent);
            }
        });

        // {@setToolbar} will set the toolbar with the title and also add a up button in toolbar.
        setToolbar();
        // this will display client name on the toolbar with his/her cation.
        displayClientToolbar(currentUserUri);

        //Reference to the ListView to show all the items in the table for a specific Client
        listView = findViewById(R.id.listView_user);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(modeListener);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        cursorAdapter = new ProductViewCursorAdapter(this,null,clientFirstName,Location);
        listView.setAdapter(cursorAdapter);
        getSupportLoaderManager().initLoader(LOADER_GET_SINGLE_ITEM,null,this);
        searchProduct();
    }

    private void searchProduct(){

        etSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isClicked){
                    isClicked = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0){
                    no_product_image.setVisibility(View.GONE);
                    Log.d("TAG", "onTextChanged: empty"+s);
                    initLoader();
                }
                else if (!isClicked) {
                    Log.d("TAG", "onTextChanged: "+s);
                    String searchFor = s.toString();
                    productSearch("%" + searchFor + "%");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ivSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchFor = etSearchBox.getText().toString();
                if (!searchFor.isEmpty()) {
                    isClicked = true;
                    productSearch(searchFor);

                }
            }
        });

    }

    private void productSearch(String searchFor){
        String [] selectionArgs = new String[]{searchFor.toLowerCase().trim()};
        String selection = "lower("+clientContract.ClientInfo.COLUMN_PRODUCT_NAME+") LIKE ? ";
        Cursor cursor = getContentResolver().query(clientContract.ClientInfo.CONTENT_URI_SEARCH_PRODUCT,null,selection,selectionArgs,null);
        if (cursor != null && cursor.getCount() != 0 && searchFor.contains("%")){
            no_product_image.setVisibility(View.GONE);
            cursorAdapter.swapCursor(cursor);
        }else if (!searchFor.contains("%")){
            if (cursor != null && cursor.getCount() != 0) {
                no_product_image.setVisibility(View.GONE);
                cursorAdapter.swapCursor(cursor);
            }else{
                cursorAdapter.swapCursor(null);
                if (no_product_image.getVisibility() == View.GONE){
                    no_product_image.setVisibility(View.VISIBLE);
                }
                cursorAdapter.notifyDataSetInvalidated();
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private void setToolbar(){
        /* Reference to the toolbar in the toolbar.
            And setting the title color to white and for then setting back icon for going back to the mainActivity */
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_arrow));
        /* Setting the action bar as toolbar and then displaying the home back button
         and then call the onBackPressed to move towards the MainActivity*/
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    // This will separate _id from the Uri {@currentUserUri}
    // This will display clientName and location also total items and total profit made from one person.
    private void displayClientToolbar(Uri currentUserUri){
        final Uri currentUser = currentUserUri;
                long _id = ContentUris.parseId(currentUser);
                user_id = _id;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getProductAndPriceTotal(user_id);
                    }
                }).start();
    }

    // This will Connect to the database and give us a single row which as count of total items  and total profit of  that particular client.
    // PARAM{clientId} : is the _id of the user use in  product_info table from uri through intent
    private void getProductAndPriceTotal(long clientId){

        String [] projections = {"count("+clientContract.ClientInfo.COLUMN_FK_ID+")", "sum("+clientContract.ClientInfo.COLUMN_PROFIT+")"};
        String selection = clientContract.ClientInfo.COLUMN_FK_ID+" = "+clientId;

        final Cursor product_info_total_ItemsAndProfit_cursor = getContentResolver().query(clientContract.ClientInfo.CONTENT_URI_PRODUCT_INFO_TOTAL_ITEMS_PRICE
                                                                                    ,projections
                                                                                    ,selection
                                                                                    ,null,null);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(product_info_total_ItemsAndProfit_cursor != null) {
                    setItemsAndProfit(product_info_total_ItemsAndProfit_cursor);
                }
            }
        });
    }

    // Set the TextView of Toolbar with total profit made and total items purchased.
    @SuppressLint("SetTextI18n")
    private void setItemsAndProfit(Cursor cursor_product_info){
        cursor_product_info.moveToFirst();
        int itemsCount = cursor_product_info.getInt(0);
        int totalProfit = cursor_product_info.getInt(1);
        TextView tv_subMainToolbar = findViewById(R.id.tv_subMain_profit_location);
        tv_subMainToolbar.setText("Total orders "+itemsCount+"\nand\n Profit made Rs. "+totalProfit);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String [] projection = {clientContract.ClientInfo.COLUMN_PK_ID,
                                clientContract.ClientInfo.COLUMN_FK_ID,
                                clientContract.ClientInfo.COLUMN_PROFILE_DATE,
                                clientContract.ClientInfo.COLUMN_PROFIT,
                                clientContract.ClientInfo.COLUMN_SELLING_PRICE,
                                clientContract.ClientInfo.COLUMN_ACTUAL_PRICE,
                                clientContract.ClientInfo.COLUMN_PENDING,
                                clientContract.ClientInfo.COLUMN_PRODUCT_IMAGE,
                                clientContract.ClientInfo.COLUMN_PRODUCT_NAME,
                                clientContract.ClientInfo.COLUMN_PRODUCT_SIZE,
                                clientContract.ClientInfo.COLUMN_PAYMENT_MODE};

        String selection = clientContract.ClientInfo.COLUMN_FK_ID+" = "+user_id;
        return new CursorLoader(this,clientContract.ClientInfo.CONTENT_URI_PRODUCT_INFO_SINGLE_ITEM,projection,selection,null,null);

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        cursorAdapter.notifyDataSetChanged();
        listView.setAdapter(cursorAdapter);
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: is called");
        restartLoader();
    }


    AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
        private int statusBarColor;
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            Log.d(TAG, "onItemCheckedStateChanged: "+position);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.i(TAG, "onCreateActionMode: called");
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.contextual_action,menu);
            //hold current color of status bar
            statusBarColor = getWindow().getStatusBarColor();
            mActionMode = mode;
            isActionMode = true;
            appBarLayout.setExpanded(false,true);
            mActionMode.setTitle("0 Items Selected..");
            //set your color
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_action_mode));
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.delete_product_menu :{
                    try {
                        deleteItems(mode);
                        throw new ReturnTrue();
                    }catch (ReturnTrue e){
                        e.isReturn();
                    }
                }
                default: {
                    return false;
                }

            }
        }

        private void deleteItems(final ActionMode mode){
            appBarLayout.setExpanded(false, true);
            Thread deleteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        deleteProduct();
                        selectedItemsPositions.clear();
                    } finally {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mode.finish();
                                restartLoader();
                                Toast.makeText(ProductHomeView.this, "Reocrds Deleted..", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            deleteThread.setName("Delete Thread");
            deleteThread.start();
        }

        class ReturnTrue extends  Exception{
            boolean isReturn = true;
            public boolean isReturn() {
                return isReturn;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isActionMode = false;
            selectedItemsPositions.clear();
            appBarLayout.setExpanded(true,true);
            //return to "old" color of status bar
            getWindow().setStatusBarColor(statusBarColor);
        }

    };
    /** This will delete selected Items from the database */
    private void deleteProduct (){
        if (cursorAdapter.getCount() != selectedItemsPositions.size()) {
            Log.d("TAG", "run: " + selectedItemsPositions.size());
            final int lastElement  = selectedItemsPositions.get(selectedItemsPositions.size() - 1);
            for (int id : selectedItemsPositions) {
                    Uri uri = ContentUris.withAppendedId(clientContract.ClientInfo.CONTENT_URI, id);
                    String selection = clientContract.ClientInfo.COLUMN_PK_ID + " = " + id;
                    getContentResolver().delete(uri, selection, null);
                }
        }else{
            Uri uri = ContentUris.withAppendedId(clientContract.ClientEntry.CONTENT_URI_INFO_DELETE, user_id);
            Uri uriProduct = ContentUris.withAppendedId(clientContract.ClientInfo.CONTENT_URI, user_id);
            String selectionProduct = clientContract.ClientInfo.COLUMN_FK_ID + " = " + user_id;
            String selection = clientContract.ClientEntry._ID + " = " + user_id;
            getContentResolver().delete(uri, selection, null);
            getContentResolver().delete(uriProduct, selectionProduct, null);
        }
    }

    /** This will restart loader */
    private void restartLoader(){
        getSupportLoaderManager().restartLoader(LOADER_GET_SINGLE_ITEM,null,this);

    }
    
    private void initLoader(){
        getSupportLoaderManager().initLoader(LOADER_GET_SINGLE_ITEM,null,this);
    }
}