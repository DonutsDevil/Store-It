package com.example.storeit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.storeit.data.clientContract;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNewClient extends AppCompatActivity {
    private static final String LOG_TAG = AddNewClient.class.getSimpleName();
    /** {@user_info} {@order_info} is the child of the card view for expanding and collapsing in the card view */
    private LinearLayout user_info;
    private LinearLayout order_info;

    private Button btn_dropdown_userInfo;
    private Button btn_dropdown_orderInfo;

    private CardView cv_userInfo;
    private CardView cv_orderInfo;

    /** Reference to the ImageView in the add_new_client.xml file to display the captured image from camera*/
    private ImageView captureImage;

    /** {@CAMERA_CAPTURE_IMAGE_PERMISSION_CODE}
     * is used for requesting permission, its value can be any digit
     * other then 0 ,cause 0 is used internally to check Permission granted.
     * this is to verify that the user has granted the open camera permission*/
    private static final int CAMERA_CAPTURE_IMAGE_PERMISSION_CODE = 101;


    /**{@OPEN_CAMERA_REQUEST_CODE} , {@SELECT_IMAGE_REQUEST_CODE}
     * is used in Intent as result code so that when the camera clicks a picture  and is clicked on save or we select a image
     * this request code is passed to check whether the user accepted te image or denied the clicked image */
    private static final int OPEN_CAMERA_REQUEST_CODE = 102;
    public static final int SELECT_IMAGE_REQUEST_CODE = 105;

    /** this String has the path of the image clicked form app and can be used to store in database*/
    private String currentPhotoPath;

    /** Reference to the Payment Spinner for cod and Online payment options*/
    private Spinner mPaymentSpinner;

    private static int mPaymentMode = 1;
    /** {@firstName} is the name of the user client clicks on a add item in product view
     * {@location} is the location of the user which is store in the database
     * {@currentClientUri} is the uri of the user which is given after client clicks on the select item*/
    private String firstName = null;
    private String location = null;
    private Uri currentClientUri;
    /** This is for the payment method which is found when cursorAdapter sends intent to this activity */
    private String payMode = null;

    private boolean mHasProductInfoChanged = false;
    private int product_pk_id = -2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_client);
        /** Reference to xml*/
        user_info = findViewById(R.id.layout_user_info);
        order_info = findViewById(R.id.layout_order_info);
        btn_dropdown_userInfo = findViewById(R.id.expand_user_info);
        btn_dropdown_orderInfo = findViewById(R.id.expand_order_info);
        cv_userInfo = findViewById(R.id.cv_user_info);
        cv_orderInfo = findViewById(R.id.order_info);
        /** It has the uri of the user, where the client will like to add more product
         * Intent is {@LINK ProductHomeView} */
        Intent dataIntent = getIntent();
        if (dataIntent.getData() != null) {
            currentClientUri = dataIntent.getData();
            getSupportActionBar().setTitle("Add Product");
            firstName = dataIntent.getStringExtra("FirstName");
            location = dataIntent.getStringExtra("Location");
            EditText et_user_name = findViewById(R.id.et_client_fName);
            EditText et_user_location = findViewById(R.id.et_client_sName);
            et_user_name.setText(firstName);
            et_user_location.setText(location);
            et_user_location.setFocusable(false);
            et_user_name.setFocusable(false);
        }
        else if(dataIntent.getStringExtra("cursorAdapter") != null &&dataIntent.getStringExtra("cursorAdapter").equals("ProductViewCursor") ){
            getSupportActionBar().setTitle("Update Item");
            EditText et_user_name = findViewById(R.id.et_client_fName);
            EditText et_user_location = findViewById(R.id.et_client_sName);
            EditText et_product_name = findViewById(R.id.et_client_product);
            EditText et_product_size = findViewById(R.id.et_client_product_size);
            EditText et_pending_fees = findViewById(R.id.et_client_dews);
            EditText et_actual_price = findViewById(R.id.et_client_product_actualPrice);
            EditText et_selling_price = findViewById(R.id.et_client_product_sellingPrice);
            ImageView imageView = findViewById(R.id.image_view_final_selected_image);

            firstName = dataIntent.getStringExtra("first name");
            location = dataIntent.getStringExtra("location");
            String size = dataIntent.getStringExtra("product size");
            byte[] image = dataIntent.getByteArrayExtra("image byte");
            if (image != null && image.length > 0){
                Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
                ImageView ivSelectImage = findViewById(R.id.iv_product_image);
                ImageView ivTakeImage = findViewById(R.id.iv_product_take);
                ivTakeImage.setVisibility(View.GONE);
                ivSelectImage.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);

            }
            product_pk_id = dataIntent.getIntExtra("primaryKey",-2);
            et_product_name.setText(dataIntent.getStringExtra("product name"));
            et_product_size.setText(size.substring(2,size.length()-2));
            et_pending_fees.setText(dataIntent.getIntExtra("pending fees",0)+"");
            et_actual_price.setText(dataIntent.getIntExtra("actual price",0)+"");
            et_selling_price.setText(dataIntent.getIntExtra("selling price",0)+"");
            payMode= dataIntent.getStringExtra("payment mode");
            imageView.setOnTouchListener(mTouchListener);
            et_product_size.setOnTouchListener(mTouchListener);
            et_pending_fees.setOnTouchListener(mTouchListener);
            et_actual_price.setOnTouchListener(mTouchListener);
            et_selling_price.setOnTouchListener(mTouchListener);
            et_user_name.setText(firstName);
            et_user_location.setText(location);
            et_user_location.setFocusable(false);
            et_user_name.setFocusable(false);
            et_product_name.setFocusable(false);
        }
        else if(dataIntent.getStringExtra("alertDialogUpdate") != null && dataIntent.getStringExtra("alertDialogUpdate").equals("update") ){
            getSupportActionBar().setTitle("Update Item");
            EditText et_user_name = findViewById(R.id.et_client_fName);
            EditText et_user_location = findViewById(R.id.et_client_sName);
            EditText et_product_name = findViewById(R.id.et_client_product);
            EditText et_product_size = findViewById(R.id.et_client_product_size);
            EditText et_pending_fees = findViewById(R.id.et_client_dews);
            EditText et_actual_price = findViewById(R.id.et_client_product_actualPrice);
            EditText et_selling_price = findViewById(R.id.et_client_product_sellingPrice);
            ImageView imageView = findViewById(R.id.image_view_final_selected_image);

            firstName = dataIntent.getStringExtra("first name");
            location = dataIntent.getStringExtra("location");
            String size = dataIntent.getStringExtra("product size");
            byte[] image = dataIntent.getByteArrayExtra("image byte");
            if (image != null && image.length > 0){
                Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
                ImageView ivSelectImage = findViewById(R.id.iv_product_image);
                ImageView ivTakeImage = findViewById(R.id.iv_product_take);
                ivTakeImage.setVisibility(View.GONE);
                ivSelectImage.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
            }
            product_pk_id = (int)dataIntent.getLongExtra("primaryKey",-2);
            et_product_name.setText(dataIntent.getStringExtra("product name"));
            et_product_size.setText(size);
            et_pending_fees.setText(dataIntent.getIntExtra("pending fees",0)+"");
            et_actual_price.setText(dataIntent.getIntExtra("actual price",0)+"");
            et_selling_price.setText(dataIntent.getIntExtra("selling price",0)+"");
            payMode= dataIntent.getStringExtra("payment mode");
            imageView.setOnTouchListener(mTouchListener);
            et_product_size.setOnTouchListener(mTouchListener);
            et_pending_fees.setOnTouchListener(mTouchListener);
            et_actual_price.setOnTouchListener(mTouchListener);
            et_selling_price.setOnTouchListener(mTouchListener);
            et_user_name.setText(firstName);
            et_user_location.setText(location);
            et_user_location.setFocusable(false);
            et_user_name.setFocusable(false);
            et_product_name.setFocusable(false);
        }
        else{
            getSupportActionBar().setTitle("Add CLIENT");
        }

        btn_dropdown_userInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_info.getVisibility() == View.GONE){
                    TransitionManager.beginDelayedTransition(cv_userInfo,new AutoTransition());
                    user_info.setVisibility(View.VISIBLE);
                    btn_dropdown_userInfo.setBackgroundResource(R.drawable.ic_sumup);
                }else{
                    TransitionManager.beginDelayedTransition(cv_userInfo,new AutoTransition());
                    user_info.setVisibility(View.GONE);
                    btn_dropdown_userInfo.setBackgroundResource(R.drawable.ic_dropdown);
                }
            }
        });

        btn_dropdown_orderInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(order_info.getVisibility() == View.GONE){
                    TransitionManager.beginDelayedTransition(cv_orderInfo, new AutoTransition());
                    order_info.setVisibility(View.VISIBLE);
                    btn_dropdown_orderInfo.setBackgroundResource(R.drawable.ic_sumup);
                }else{
                    TransitionManager.beginDelayedTransition(cv_orderInfo, new AutoTransition());
                    order_info.setVisibility(View.GONE);
                    btn_dropdown_orderInfo.setBackgroundResource(R.drawable.ic_dropdown);
                }
            }
        });

        captureImage = findViewById(R.id.image_view_final_selected_image);
        mPaymentSpinner = findViewById(R.id.spinner_client_product_payment);
        // when button is clicked it takes us to the camera and allows us to click the image if permission is granted
        findViewById(R.id.btn_take_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();
            }
        });

        findViewById(R.id.btn_select_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(selectImage, SELECT_IMAGE_REQUEST_CODE);
            }
        });
        setUpSpinner();

    }

    /** This is use to set a listner to show whether the user wants to leave the activity in middle of editing or not*/
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mHasProductInfoChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog( DialogInterface.OnClickListener discardButtonClickListener ) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mHasProductInfoChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void setUpSpinner(){
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter paymentSpinnerAdapter =ArrayAdapter.createFromResource(this,R.array.array_gender_option,android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line, then apply adapter to the spinner
        paymentSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mPaymentSpinner.setAdapter(paymentSpinnerAdapter);

        // To set the spinner to the selected payment method by the user when clicked from cursor adapter.
        if (payMode != null) {
            if (payMode.equalsIgnoreCase("COD")) {
                mPaymentSpinner.setSelection(0);
            } else {
                mPaymentSpinner.setSelection(1);
            }
            mPaymentSpinner.setOnTouchListener(mTouchListener);
        }

            mPaymentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedPayment = (String)parent.getItemAtPosition(position);
                    if(!selectedPayment.isEmpty()){
                        if(selectedPayment.equals(getString(R.string.payment_cod))){
                            mPaymentMode = clientContract.ClientInfo.PAYMENT_COD; // COD =  1
                        }else{

                            mPaymentMode = clientContract.ClientInfo.PAYMENT_ONLINE; // ONLINE = 0
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

    }

    // it checks for the permission first whether user has given the permission for camera or not
    // if no then ask @CAMERA PERMISSION or else if given open  @CAMERA
    private void askCameraPermission(){
        /** {@PERMISSION_GRANTED} value is 0 that means if permission is granted then checkSelfPermission will inform us by int
         * and if {@checkSelfPermission} is not 0 that means permission is not granted so we need to ask the permission at run time
         * only for API above 23*/
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_CAPTURE_IMAGE_PERMISSION_CODE);
        }else{
            // if permission is granted then open camera directly
            dispatchTakePictureIntent();
        }
    }

    // checks whether the permission is accepted or denied when permission is asked in fun askCameraPermission()
    // {@ requestCode} is a int which has values of {@ CAMERA_CAPTURE_IMAGE_PERMISSION_CODE} if permission granted in @fun askCameraPermission()
    // {@ permissions} is a array of requested permission
    // {@ grantedResults} is a array of  requested permission request code  whether its granted or not.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if(requestCode == CAMERA_CAPTURE_IMAGE_PERMISSION_CODE){
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    dispatchTakePictureIntent();
                }else{
                    Toast.makeText(this,"Camera Permission is Required to Use Camera", Toast.LENGTH_LONG).show();
                }
            }
    }

    // {@ requestCode} is the result code form the startActivityForResult method
    // the image that is clicked it pass in form of Intent here its {@ DATA}
    // to display it in the image view we Use Bitmap
    // if requestCode is equal OPEN_CAMERA_RESULT_CODE  then only clicked image is displayed on ImageView i.e {@ captureImage}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_CAMERA_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK){
            // 2 imageView are made not visible and allowing other image view {ID : image_view_final_selected_image}
            // to display clicked image
            ImageView ivSelectImage = findViewById(R.id.iv_product_image);
            ImageView ivTakeImage = findViewById(R.id.iv_product_take);
            ivTakeImage.setVisibility(View.GONE);
            ivSelectImage.setVisibility(View.GONE);
             File f = new File(currentPhotoPath);
             captureImage.setImageURI(Uri.fromFile(f));
            }
        }
        if(requestCode == SELECT_IMAGE_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                // 2 imageView are made not visible and allowing other image view {ID : image_view_final_selected_image}
                // to display clicked image
                ImageView ivSelectImage = findViewById(R.id.iv_product_image);
                ImageView ivTakeImage = findViewById(R.id.iv_product_take);
                ivTakeImage.setVisibility(View.GONE);
                ivSelectImage.setVisibility(View.GONE);

                Uri contentUri = data.getData();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_"+timeStamp+"."+getExtension(contentUri);
                captureImage.setImageURI(contentUri);
            }
        }
    }

    private String getExtension(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    // creates a image file of the clicked image so that we can get full resolution pic
    // we can use to display it  in ImageView and also this images are delete if app gets uninstall.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        // get Storage for the image file and then create the image file with {@extension as jpg} it can be png too or any other.
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, OPEN_CAMERA_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_new_client_done,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        // data is stored in database.
        switch (item.getItemId()){
            // Respond to a click on the "done" menu option
            case R.id.menu_done_add_client : {
                boolean isValid = isInputValid();
                if (isValid && firstName == null && location == null) {
                    storeInputInDatabase();
                    finish();
                }
                if (isValid && firstName != null && location != null) {
                    storeInputInDatabase();
                    finish();
                }
                return true;
            }
            case android.R.id.home: {
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link MainActivity}.
                if (!mHasProductInfoChanged) {
                    NavUtils.navigateUpFromSameTask(AddNewClient.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddNewClient.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /** Stores data in storeIt.db in particular user_info and product_info */
    private void storeInputInDatabase(){
            // get the {@userInfoValues} as inputs of userInfo that needs to be inserted in the table {@ user_info}
            // get the {@productInfoValues} as inputs of user Product details that needs to be inserted in the table {@ product_info}
            // This is called when user clicks on add new client.
            if (firstName == null && location == null) {
                ContentValues userInfoValues = storeUserInfo();
                /**
                 * {@user_info_insertion_uri} {@product_info_insertion_uri} returns a Uri with the last inserted row number after the insertion of the content values,
                 * {@user_info_insertion_uri} last character which is {@ID} is then used by {@productInfoValues} for it's foreign key.
                 * ContentUris.parseId(uri) find the id from the uri and return it.
                 * */
                Uri user_info_insertion_uri = getContentResolver().insert(clientContract.ClientEntry.CONTENT_URI, userInfoValues);
                ContentValues productInfoValues = storeProductInfo(ContentUris.parseId(user_info_insertion_uri));
                Uri product_info_insertion_uri = getContentResolver().insert(clientContract.ClientInfo.CONTENT_URI,productInfoValues);
                Toast.makeText(AddNewClient.this,"Added",Toast.LENGTH_SHORT).show();

            }// when clients client buys a new item so this will add it to the product_info
            // currentClientUri is the Uri received  from MainActivity.
            else if( (firstName != null && location != null) && payMode == null && product_pk_id == -2) {
                ContentValues productInfoValues = storeProductInfo(ContentUris.parseId(currentClientUri));
                Uri uris = getContentResolver().insert(clientContract.ClientInfo.CONTENT_URI_PRODUCT_INFO_INSERT_ITEM,productInfoValues);
                Toast.makeText(AddNewClient.this,"Added",Toast.LENGTH_SHORT).show();
            }else if (firstName != null && location != null && payMode != null && product_pk_id != -2){
                ContentValues productInfoValues = storeProductInfo(product_pk_id);
                int rows = getContentResolver().update(clientContract.ClientInfo.CONTENT_URI,productInfoValues,clientContract.ClientInfo.COLUMN_PK_ID+"="+product_pk_id,null);
                if (rows == 0){
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Updated",Toast.LENGTH_SHORT).show();
                }
            }
    }

    /** Compresses the data that is to be inserted in the user_info table in the database */
    private ContentValues storeUserInfo(){
        EditText et_client_name = findViewById(R.id.et_client_fName);
        EditText et_client_location = findViewById(R.id.et_client_sName);

        String name = String.valueOf(et_client_name.getText()).trim();
        String location = String.valueOf(et_client_location.getText()).trim();
        ContentValues userInfoValues = new ContentValues();
        userInfoValues.put(clientContract.ClientEntry.COLUMN_FIRST_NAME,name);
        userInfoValues.put(clientContract.ClientEntry.COLUMN_LOCATION,location);
        return userInfoValues;
    }
    /** Compress the data that is to be inserted in the product_info table in the database*/
    private ContentValues storeProductInfo(long id) {
        // Reference to the Edit Text Field in the AddClient xml to the the user TYPE INPUT
        EditText et_product_name = findViewById(R.id.et_client_product);
        EditText et_product_dews = findViewById(R.id.et_client_dews);
        EditText et_product_actual_price = findViewById(R.id.et_client_product_actualPrice);
        EditText et_product_selling_price = findViewById(R.id.et_client_product_sellingPrice);
        EditText et_product_size = findViewById(R.id.et_client_product_size);
        String size = et_product_size.getText().toString().isEmpty()?"null":et_product_size.getText().toString();

        // this has the current date when the client as been has brought the product.
        String strTodayDate = getTodayDate();
        int actualPrice = Integer.parseInt(String.valueOf(et_product_actual_price.getText()).trim());
        int sellingPrice = Integer.parseInt(String.valueOf(et_product_selling_price.getText()).trim());
        // This content value is then inserted in the product info
        ContentValues productInfoValues = new ContentValues();
        productInfoValues.put(clientContract.ClientInfo.COLUMN_FK_ID,id);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_NAME,et_product_name.getText().toString().trim());
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_SIZE,size);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_ACTUAL_PRICE, actualPrice);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_SELLING_PRICE,sellingPrice);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PROFIT,sellingPrice-actualPrice);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PROFILE_DATE,strTodayDate);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PAYMENT_MODE,mPaymentMode);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_IMAGE,getImage());
        String strDews = et_product_dews.getText().toString().trim();
        int dews = 0;
        if(!strDews.isEmpty()){
            dews = Integer.parseInt(et_product_dews.getText().toString().trim());
        }
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PENDING,dews);
        return productInfoValues;
    }
    private ContentValues storeProductInfo(int id){
        // Reference to the Edit Text Field in the AddClient xml to the the user TYPE INPUT
        EditText et_product_dews = findViewById(R.id.et_client_dews);
        EditText et_product_actual_price = findViewById(R.id.et_client_product_actualPrice);
        EditText et_product_selling_price = findViewById(R.id.et_client_product_sellingPrice);
        EditText et_product_size = findViewById(R.id.et_client_product_size);
        String size = et_product_size.getText().toString().isEmpty()?"null":et_product_size.getText().toString();

        int actualPrice = Integer.parseInt(String.valueOf(et_product_actual_price.getText()).trim());
        int sellingPrice = Integer.parseInt(String.valueOf(et_product_selling_price.getText()).trim());
        // This content value is then inserted in the product info
        ContentValues productInfoValues = new ContentValues();
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_SIZE,size);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_ACTUAL_PRICE, actualPrice);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_SELLING_PRICE,sellingPrice);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PROFIT,sellingPrice-actualPrice);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PAYMENT_MODE,mPaymentMode);
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_IMAGE,getImage());
        String strDews = et_product_dews.getText().toString().trim();
        int dews = 0;
        if(!strDews.isEmpty()){
            dews = Integer.parseInt(et_product_dews.getText().toString().trim());
        }
        productInfoValues.put(clientContract.ClientInfo.COLUMN_PENDING,dews);
        return productInfoValues;
    }

    /** Converts the Bitmap of the image to {Byte[]} that will be store in the product_info table as blob
     * it will return the byte array of the image.*/
    public byte[] getImageBytes() throws IOException {
        captureImage.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) captureImage.getDrawable();
        Bitmap image = drawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 12, stream);
        byte[] byteArrayImage = stream.toByteArray();
        stream.close();
        return byteArrayImage;
    }

    /** returns the current date which will be stored in product_info*/
    private String getTodayDate(){
        // get current Date and Time for product_info table
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:MM", Locale.getDefault());
        return dateFormat.format(date);
    }

    // this will return the true pr false depending whether all the input given from user is valid.
    private boolean isInputValid(){
        boolean isUserInfoValid = userInfoCheck();
        boolean isProductInfoValid = productInfoCheck();
        ImageView iv_error_user_info = findViewById(R.id.iv_error_user_info);
        ImageView iv_error_product_info = findViewById(R.id.iv_error_product_info);
        if(!isUserInfoValid){
            iv_error_user_info.setVisibility(View.VISIBLE);
        }else{
            iv_error_user_info.setVisibility(View.INVISIBLE);
        }
        if (!isProductInfoValid){
            iv_error_product_info.setVisibility(View.VISIBLE);
        }else{
            iv_error_product_info.setVisibility(View.INVISIBLE);
        }

        return isUserInfoValid && isProductInfoValid;
    }

    // check in the card view of the user info whether the input from the user is valid or not
    // and if valid return true else return false
    // similar is the case for thr product info card view.
    private boolean userInfoCheck(){
        boolean isValid = true;
        EditText et_user_name = findViewById(R.id.et_client_fName);
        EditText et_user_location = findViewById(R.id.et_client_sName);
        if(et_user_name.getText().toString().trim().isEmpty()){
            et_user_name.setError("Name Cannot Be Empty");
            isValid = false;
        }
        if (et_user_location.getText().toString().trim().isEmpty()){
            et_user_location.setError("Location Cannot Be Empty");
            isValid = false;
        }
        return isValid;
    }

    private boolean productInfoCheck(){
        boolean isValid = true;
        EditText et_product_name = findViewById(R.id.et_client_product);
        EditText et_product_actualPrice = findViewById(R.id.et_client_product_actualPrice);
        EditText et_product_sellingPrice = findViewById(R.id.et_client_product_sellingPrice);
        EditText et_pending = findViewById(R.id.et_client_dews);

        if(et_product_name.getText().toString().trim().isEmpty()){
            et_product_name.setError("Field Cannot Be Empty");
            isValid = false;
        }
        if (!et_pending.getText().toString().isEmpty()){
            if(Integer.parseInt(et_pending.getText().toString().trim()) < 0){
                et_pending.setError("Cannot enter Negative Value");
                isValid = false;
            }
        }
        if(et_product_actualPrice.getText().toString().trim().isEmpty()){
            et_product_actualPrice.setError("Field Cannot Be Empty");
            isValid = false;
        }else{
            if(Integer.parseInt(et_product_actualPrice.getText().toString().trim())< 0){
                et_product_actualPrice.setError("Cannot enter Negative Value");
                isValid = false;
            }
        }
        if (et_product_sellingPrice.getText().toString().trim().isEmpty()){
            et_product_sellingPrice.setError("Field Cannot Be Empty");
            isValid = false;
        }else{
            if(Integer.parseInt(et_product_sellingPrice.getText().toString().trim())< 0){
                et_product_sellingPrice.setError("Cannot enter Negative Value");
                isValid = false;
            }
        }

        return isValid;
    }

    // check whether the user has given the image in the imageView.
    private boolean isImageGiven(){
        return captureImage.getDrawable() != null;
    }

    // helper method that returns the image in the byte form if the image is given else it will return null
    private byte[] getImage(){
        byte image[] = null;
        try {
            if(isImageGiven()) {
                image = getImageBytes();
            }
        }catch (IOException e){
            Toast.makeText(this,"Error in Creating the image "+e, Toast.LENGTH_SHORT).show();
        }
        return image;
    }

}