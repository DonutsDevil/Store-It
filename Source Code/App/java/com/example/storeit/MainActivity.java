package com.example.storeit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.alphabetik.Alphabetik;
import com.example.storeit.data.clientContract;
import com.example.storeit.data.clientContract.ClientEntry;
import com.example.storeit.data.clientContract.ClientInfo;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVReader;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.RuleBasedCollator;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private int previousItemListView = 0;
    // LOG TAG for debugging
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    /** Adapter, dateCursorAdapter,profitCursorAdapter,dewsCursorAdapter for the Background task and displaying info on the screen*/
    private HomeCursorAdapter adapter;
    private DateCursorAdapter dateCursorAdapter;
    private ProfitCursorAdapter profitCursorAdapter;
    private DewsCursorAdapter dewsCursorAdapter;

    /** This is for the loader call back calls for getting all the user from user_info table*/
    private final int MAIN_QUERY_DISPLAY = 0;
    /** This is for the Loader call back for getting all the user for Date from user_info and product_info */
    private final int GET_PRODUCT_DATES = 1;
    /** This is for the Loader call back for getting all the users overall profit from user_info and product_info */
    private final int GET_OVERALL_PROFIT = 2;
    /** This is for the Loader call back for getting all the payment dews from product_info table and name of the client from the user_info*/
    private final int GET_CLIENT_DEWS = 3;

    /** ListView which is used by Cursor Adapter*/
    private ListView homeListView;
    /** Empty View cause if there's no data in the table then this view is displayed */
    private View emptyView;
    private View emptyViewDews;
    /** Search bard towards the right*/
    private Alphabetik alphabetik;
    /** When onTextSubmit i.e when we search and data is present in the table then we display this image. */
    private ImageView noDataFound;
    private BottomAppBar bottomAppBar;
    private BottomNavigationView bottomNavigationView;

    /**{timerTv} is the place where 5 sec countdown will be shown every time when ever user clicks on profit
     * {dismissBtn} is the buttong that will activate after 5 sec abd then only the alert box can be dismiss
     * {isValidToExit} to verify when to activate the {dismissBtn}*/
    private TextView timerTv;
    private Button dismissBtn;
    private boolean isValidToExit = false;
    /** This are used in deleting a product from the alert box*/
    private int foreignKey;
    private int productCount;

    private static final int STORAGE_REQUEST_CODE_EXPORT = 1;
    private static final int STORAGE_REQUEST_CODE_IMPORT = 2;
    private String [] storagePermission;

    private ProgressDialog backupDialog;
    private int progress = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        bottomNavigationView = findViewById(R.id.bottom);
        FloatingActionButton mFloatingBtn = findViewById(R.id.floating_btn);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        bottomNavigationView.setBackground(null);
        bottomNavigationView.getMenu().getItem(4).setEnabled(false);

        // switches the activity to { @ ADD NEW CLIENT } when floating button is pressed
        mFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,AddNewClient.class);
                startActivity(intent);
            }
        });

        //Find listView from the xml resource.
        homeListView = findViewById(R.id.listView);
        emptyView = findViewById(R.id.empty_view);
        emptyViewDews = findViewById(R.id.no_dews_left);
        emptyView.setVisibility(View.INVISIBLE);
        homeListView.setEmptyView(emptyView);
        emptyViewDews.setVisibility(View.INVISIBLE);
        noDataFound = findViewById(R.id.no_data_image);
        noDataFound.setVisibility(View.GONE);
        // vertical search bar
        alphabetik = findViewById(R.id.alphSectionIndex);

        // set up an adapter to create a list item for each row of the user and product data in the cursor
        // There is no data yet (Until the loader finishes) so pass the null value in the cursor
        adapter = new HomeCursorAdapter(this,null);
        dateCursorAdapter = new DateCursorAdapter(this,null);
        profitCursorAdapter = new ProfitCursorAdapter(this,null);
        dewsCursorAdapter = new DewsCursorAdapter(this,null);
        homeListView.setAdapter(adapter);
//        restartLoader(MAIN_QUERY_DISPLAY);
        homeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            float offset = 0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (previousItemListView < firstVisibleItem){
                    offset = bottomAppBar.getCradleVerticalOffset();
                    TransitionManager.beginDelayedTransition(bottomAppBar,new AutoTransition());
                    bottomAppBar.setVisibility(View.GONE);
                    bottomNavigationView.setVisibility(View.GONE);
                    previousItemListView = firstVisibleItem;
                }
                else if (previousItemListView > firstVisibleItem){
                    TransitionManager.beginDelayedTransition(bottomAppBar,new AutoTransition());
                    bottomAppBar.setVisibility(View.VISIBLE);
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    previousItemListView = firstVisibleItem;
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_dates :
                        emptyViewDews.setVisibility(View.INVISIBLE);
                        homeListView.setEmptyView(emptyView);
                        // query for getting data from the database sorted according to dates
//                        select u.fname, p.* from user_info u , product_info p where p.id = u._id  order by date(p.buying_date) desc;
                        homeListView.setAdapter(dateCursorAdapter);
                        restartLoader(GET_PRODUCT_DATES);
                        setSideSearchOff();
                        return true;
                    case R.id.menu_profit :

                        emptyViewDews.setVisibility(View.INVISIBLE);
                        homeListView.setEmptyView(emptyView);
                        homeListView.setAdapter(profitCursorAdapter);
                        restartLoader(GET_OVERALL_PROFIT);
                        if (profitCursorAdapter.getCount() != 0) {
                            createAlertDialog();
                        }
                        setSideSearchOff();
                        return true;
                    case R.id.menu_pending_payment :
                        emptyView.setVisibility(View.INVISIBLE);
                        homeListView.setEmptyView(emptyViewDews);
                        homeListView.setAdapter(dewsCursorAdapter);
                        restartLoader(GET_CLIENT_DEWS);
                        setSideSearchOff();
                        return true;
                    case R.id.menu_home:
                        emptyViewDews.setVisibility(View.INVISIBLE);
                        homeListView.setEmptyView(emptyView);
                        homeListView.setAdapter(adapter);
                        restartLoader(MAIN_QUERY_DISPLAY);
                        alphabetik.setVisibility(View.VISIBLE);
                        if (noDataFound.getVisibility() == View.VISIBLE){
                            noDataFound.setVisibility(View.GONE);
                        }
                        return true;
                    default:
                        Toast.makeText(MainActivity.this,"Error "+item.getItemId(),Toast.LENGTH_SHORT).show();
                        return false;

                }
            }
        });

        /*References to the search bar to the right of the screen in the main screen
         * and then when a letter is clicked it will take us to that letter*/
        String[] searchLetters = {"A","B","C","D",
                                  "E","F","G","H",
                                  "I","J","K","L",
                                  "M","N","O","P",
                                  "Q","R","S","T",
                                  "U","V","W","X","Y","Z"};
        alphabetik.setAlphabet(searchLetters);
        alphabetik.onSectionIndexClickListener(new Alphabetik.SectionIndexClickListener() {
            @Override
            public void onItemClick(View view, int position, String character) {
                String info = " Postion = "+position+" Char = "+character;
                Log.i("View: ",view+","+info);
                // returns the position of the letter from A-Z and takes the focus to the name with that letter
                homeListView.smoothScrollToPositionFromTop(getPositionFromData(character,homeListView),0);
            }
        });

        listViewOnClick();
        dateListItemListener();
        Log.i(LOG_TAG,"MainAcitivity is CALLED");
    }

    // this will check for the permission of the external Storage return true if its granted else it will return false.
    private boolean checkStoragePermission(){
        final boolean isReturn = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return isReturn;
    }

    private void requestStoragePermissionExport(){
        // request storage permission to export
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE_EXPORT);
    }

    private void requestStoragePermissionImport(){
        // request storage permission to Import
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE_IMPORT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // handle permission request
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case STORAGE_REQUEST_CODE_EXPORT : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permission granted
                    exportCsv();
                }else {
                    Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case STORAGE_REQUEST_CODE_IMPORT : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permission granted
                    importCsv();
                }else {
                    Toast.makeText(this, "Storage Permission Required", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }

    private void exportCsv(){
        backupDialog = new ProgressDialog(MainActivity.this);
        backupDialog.setTitle("Data Backup");
        backupDialog.setMax(100);
        backupDialog.setMessage("Please Wait, BackUp is being store in your phone");
        backupDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        backupDialog.setCancelable(false);
        backupDialog.show();
        File folder = new File(Environment.getExternalStorageDirectory()+"/"+"StoreItBackUp");
        final File imageFolder = new File(folder.toString()+"/"+"ImageBackup");
        boolean isFolderCreated = false;
        boolean isImageFolderCreated = false;
        isFolderCreated = folder.mkdir(); // it will create a folder if doesn't exists
        isImageFolderCreated = imageFolder.mkdir();
        // user_info_table_FileName,product_info_FileName are files in StoreItBackup file
            String user_info_table_FileName =  "logic.csv";
            String product_info_FileName = "product.csv";
            final String filePAthAndName1st = folder.toString()+"/"+user_info_table_FileName;
            final String filePathAndName2nd = folder.toString()+"/"+product_info_FileName;

            final Cursor cursorUserInfo = getContentResolver().query(ClientEntry.CONTENT_URI,null,null,null,null);
            final Cursor cursorProductInfo = getContentResolver().query(ClientInfo.CONTENT_URI,null,null,null,null);
            // Total dta to be backUp;
            final int totalValues = cursorUserInfo.getCount()+cursorProductInfo.getCount();
            cursorProductInfo.moveToFirst();
            cursorUserInfo.moveToFirst();
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                // write to the csv file
                if (cursorUserInfo.getCount() > 0 && cursorProductInfo.getCount() > 0) {
                    // write in logic.csv
                    FileWriter fw = new FileWriter(filePAthAndName1st);
                    // write in product.csv
                    FileWriter fwp = new FileWriter(filePathAndName2nd);

                    writeUser_info_Csv(fw, cursorUserInfo);
                    backupDialog.setProgress((progress/totalValues)*100);
                    progress++;
                    while (cursorUserInfo.moveToNext()) {
                        writeUser_info_Csv(fw, cursorUserInfo);
                        backupDialog.setProgress((progress/totalValues)*100);
                        progress++;
                    }
                    fw.flush();
                    fw.close();

                    writeProduct_info_Csv(fwp, cursorProductInfo,imageFolder);
                    backupDialog.setProgress((progress/totalValues)*100);
                    progress++;
                    while (cursorProductInfo.moveToNext()){
                        writeProduct_info_Csv(fwp, cursorProductInfo,imageFolder);
                        backupDialog.setProgress((progress/totalValues)*100);
                        progress++;
                    }
                    fwp.flush();
                    fwp.close();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Backup Completed", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.d(LOG_TAG, "exportCsv: " + filePAthAndName1st);
                }
                if (cursorUserInfo.getCount() == 0 && cursorProductInfo.getCount() == 0){
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "No Records To Back Up..", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (IOException e) {
                // if there was any error while doing so.
                    Log.d(LOG_TAG, "run: while backup error "+e.getLocalizedMessage());
            }finally {
                    backupDialog.dismiss();
                    progress = 1;
                }

                }
            });
        thread.setName("backup Thread");
        thread.start();
    }

    private void importCsv(){
    // use name path and file name to import
        backupDialog = new ProgressDialog(MainActivity.this);
        backupDialog.setTitle("Data Restoring");
        backupDialog.setMessage("Please Wait, Data is being Restore in your phone");
        backupDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        backupDialog.setCancelable(false);
        backupDialog.show();
        String filePathAndNameLogic = Environment.getExternalStorageDirectory()+"/StoreItBackUp/"+"logic.csv";
        String filePathAndNameProduct = Environment.getExternalStorageDirectory()+"/StoreItBackUp/"+"product.csv";

        File csvLogic = new File(filePathAndNameLogic);
        File csvProduct = new File(filePathAndNameProduct);

        // check if backup already exit
        if (csvLogic.exists() && csvProduct.exists()){
            try {
                CSVReader csvReaderLogic = new CSVReader(new FileReader(csvLogic.getAbsolutePath()));
                CSVReader csvReaderProduct = new CSVReader(new FileReader(csvProduct.getAbsolutePath()));
                String[] nextLine;
                while((nextLine = csvReaderLogic.readNext()) != null){
                    int id = Integer.parseInt(nextLine[0]);
                    String fname = nextLine[1];
                    String location = nextLine[2];
                    ContentValues userInfoValues = new ContentValues();
                    userInfoValues.put(ClientEntry._ID,id);
                    userInfoValues.put(clientContract.ClientEntry.COLUMN_FIRST_NAME,fname);
                    userInfoValues.put(clientContract.ClientEntry.COLUMN_LOCATION,location);
                    getContentResolver().insert(clientContract.ClientEntry.CONTENT_URI, userInfoValues);
                }

                String[] Line;
                ContentValues productInfoValues = new ContentValues();
                while ((Line = csvReaderProduct.readNext()) != null) {
                    int pkId = Integer.parseInt(Line[0]);
                    int fkId = Integer.parseInt(Line[1]);
                    String pName = Line[2];
                    String pSize = Line[3];
                    int actualPrice = Integer.parseInt(Line[4]);
                    int sellingPrice = Integer.parseInt(Line[5]);
                    int profit = Integer.parseInt(Line[6]);
                    String buyDate = Line[7];
                    int payment = Integer.parseInt(Line[8]);
                    String images = Line[9];
                    if (!images.equals("null")){
                        String imagePath = Line[9];
                        byte [] image = decodeByteFromFile(imagePath);
                        productInfoValues.put(ClientInfo.COLUMN_PRODUCT_IMAGE,image);
                    }else{
                        productInfoValues.put(ClientInfo.COLUMN_PRODUCT_IMAGE, (byte[]) null);
                    }
                    int dews = Integer.parseInt(Line[10]);
                    productInfoValues.put(ClientInfo.COLUMN_PK_ID,pkId);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_FK_ID,fkId);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_NAME,pName);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_PRODUCT_SIZE,pSize);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_ACTUAL_PRICE, actualPrice);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_SELLING_PRICE,sellingPrice);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_PROFIT,profit);
                    productInfoValues.put(ClientInfo.COLUMN_PROFILE_DATE,buyDate);
                    productInfoValues.put(clientContract.ClientInfo.COLUMN_PAYMENT_MODE,payment);
                    productInfoValues.put(ClientInfo.COLUMN_PENDING,dews);
                    getContentResolver().insert(clientContract.ClientInfo.CONTENT_URI_PRODUCT_INFO_INSERT_ITEM,productInfoValues);
                }
                Toast.makeText(this, "Restored!", Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Log.d(LOG_TAG, "importCsv: "+e.getMessage());
            }finally {
                backupDialog.dismiss();
            }
        }else{
            Toast.makeText(this, "No Backup Found....", Toast.LENGTH_SHORT).show();
        }

    }

    private byte[] decodeByteFromFile(String txtPath) throws IOException {
        // read from text file
        FileInputStream inputStream = new FileInputStream(txtPath);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int len = 2048;
        byte[] buffer = new byte[len];
        byte[] textData;
        int readLength;

        while ((readLength = inputStream.read(buffer, 0, len)) != -1) {
            out.write(buffer, 0, readLength);
        }

        textData = out.toByteArray();


//        byte[] data = Base64.getDecoder().decode(new String(textData));
        byte[] data = Base64.decode(new String(textData),Base64.DEFAULT);
        return data;
    }

    private void writeProduct_info_Csv(FileWriter fwp, Cursor cursorProductInfo, File imageFolder) throws IOException {
        int _id = cursorProductInfo.getInt(0);
        fwp.append(""+_id); // _id
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getInt(1)); // id
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getString(2)); // product_name
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getString(3)); // product_size
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getInt(4)); // actual_price
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getInt(5)); // selling_price
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getInt(6)); // profit
        fwp.append(",");
        String buyingDate = cursorProductInfo.getString(7);
        fwp.append(""+buyingDate); // buying_date
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getInt(8)); // payment
        fwp.append(",");
        // create separate files for images and store the path_location in the csv.
        if (cursorProductInfo.getBlob(9) != null){
            String imageFileName = _id+"_"+buyingDate+".txt";
            String imageFilePath = imageFolder.toString()+"/"+imageFileName;
            byte[] data = cursorProductInfo.getBlob(9);
            // encode it to a String using base64 encoding
            String encodedImage = Base64.encodeToString(data,Base64.DEFAULT);
            FileWriter fileWriter = new FileWriter(imageFilePath);
            fileWriter.write(encodedImage);
            fileWriter.close();
            fwp.append(imageFilePath);
        }else{
            fwp.append("null"); // image
        }
        fwp.append(",");
        fwp.append(""+cursorProductInfo.getInt(10)); // pending_payment
        fwp.append("\n");
    }

    private void writeUser_info_Csv(FileWriter fw, Cursor cursorUserInfo) throws IOException {
        fw.append(""+cursorUserInfo.getInt(0)); // _id
        fw.append(",");
        fw.append(""+cursorUserInfo.getString(1)); // fname
        fw.append(",");
        fw.append(""+cursorUserInfo.getString(2)); // location
        fw.append("\n");
    }
    // This will make the side search bar invisible
    private void setSideSearchOff(){
        alphabetik.setVisibility(View.GONE);
        if (noDataFound.getVisibility() == View.VISIBLE){
            noDataFound.setVisibility(View.GONE);
        }
    }

    // This will create a alertDialog when a user clicks on profit from the main screen.
    private void createAlertDialog(){
        View customView = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_alert_profit_dialog,null);
        getData(customView);
        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setView(customView).setCancelable(false).create();
        Timer timer = new Timer(5200,1000);
        timerTv = customView.findViewById(R.id.timer_tv);
        dismissBtn = customView.findViewById(R.id.btn_okay);
        if (!isValidToExit) {
            timer.start();
        }
        if (isValidToExit){
            timerTv.setVisibility(View.GONE);
            dismissBtn.setBackgroundColor(getResources().getColor(R.color.timer_button_color));
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidToExit) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void getData(final View view){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String []projection = {"sum("+ClientInfo.COLUMN_SELLING_PRICE+")",
                                        "sum("+ClientInfo.COLUMN_ACTUAL_PRICE+")",
                                        "sum("+ClientInfo.COLUMN_PROFIT+")"};
                Cursor cursor = getContentResolver().query(ClientInfo.CONTENT_URI,projection,null,null,null,null);
                final TextView buyingPriceTv = view.findViewById(R.id.total_buying_price_tv);
                final TextView sellingPRiceTv = view.findViewById(R.id.total_selling_price_tv);
                final TextView profitPRiceTv = view.findViewById(R.id.total_profit_tv);
                Log.d(LOG_TAG, "run: "+cursor.getColumnCount());
                cursor.moveToFirst();
                final int sellingPrice = cursor.getInt(0);
                final int buyingPrice = cursor.getInt(1);
                final int profitPrice = cursor.getInt(2);
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        buyingPriceTv.setText("TOTAL BUYING PRICE : "+buyingPrice);
                        sellingPRiceTv.setText("TOTAL SELLING PRICE : "+sellingPrice);
                        profitPRiceTv.setText("TOTAL PROFIT : "+profitPrice);
                    }
                });
            }
        });
        thread.setName("get Data for Alert Dialog");
        thread.start();
    }

    // when user clicks on the listView depending on the menu user is  this will provide the necessary feature for that particular view.
    private void listViewOnClick() {
        homeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bottomNavigationView.getSelectedItemId() == R.id.menu_home) {
                    TextView name = view.findViewById(R.id.tv_client_name);
                    TextView location = view.findViewById(R.id.tv_client_location);
                    // Get name and location from the TextView of the selected view.
                    String fName = name.getText().toString();
                    String fLocation = location.getText().toString();

                    // Create intent to go to {@link ProductHomeView}
                    Intent intent = new Intent(MainActivity.this, ProductHomeView.class);
                    //Form the content URI that represents  the specific user that was clicked on,
                    //by appending 'id' (passed as input to this method) onto the
                    //{@link ClientEntry#CONTENT_URI}
                    Uri currentUser = ContentUris.withAppendedId(ClientEntry.CONTENT_URI, id);
                    // Set the URI on the data filed of the intent
                    intent.setData(currentUser);
                    // Add name and location of the client and get it ready to send it to ProductHomeView
                    intent.putExtra("firstName", fName);
                    intent.putExtra("location", fLocation);
                    // Launch the {@link ProductHomeView} to display the data for the current pet.
                    startActivity(intent);
                }
                if (bottomNavigationView.getSelectedItemId() == R.id.menu_pending_payment){
                    createDialogForProduct(view, id, R.id.menu_pending_payment);
                }
            }
        });
    }

    private void dateListItemListener(){
        homeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
                if (R.id.menu_dates == bottomNavigationView.getSelectedItemId()) {
                    createDialogForProduct(view,id,R.id.menu_dates);
                }
                return false;
            }
        });
    }

    private boolean createDialogForProduct(View view, final long id, int menuId){
        final Intent toAddProductIntent = new Intent(MainActivity.this,AddNewClient.class);
        String selection = ClientInfo.COLUMN_PK_ID + " = "+id;
        Log.d(LOG_TAG, "onItemLongClick: date "+id);
        Cursor cursor = getContentResolver().query(ClientInfo.CONTENT_URI_PRODUCT_INFO_TOTAL_ITEMS_PRICE,null,selection,null,null);
        if (cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            final String productName = cursor.getString(cursor.getColumnIndex(ClientInfo.COLUMN_PRODUCT_NAME));
            final String productSize = cursor.getString(cursor.getColumnIndex(ClientInfo.COLUMN_PRODUCT_SIZE));
            final int actualPrice = cursor.getInt(cursor.getColumnIndex(ClientInfo.COLUMN_ACTUAL_PRICE));
            final int sellingPrice = cursor.getInt(cursor.getColumnIndex(ClientInfo.COLUMN_SELLING_PRICE));
            final int profit = cursor.getInt(cursor.getColumnIndex(ClientInfo.COLUMN_PROFIT));
            final int dews = cursor.getInt(cursor.getColumnIndex(ClientInfo.COLUMN_PENDING));
            byte image[] = cursor.getBlob(cursor.getColumnIndex(ClientInfo.COLUMN_PRODUCT_IMAGE));
            final int paymentMode = cursor.getInt(cursor.getColumnIndex(ClientInfo.COLUMN_PAYMENT_MODE));
            final View customAlertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.custom_alert_dialog, null);
            final Button cancelBtn = customAlertView.findViewById(R.id.btnCancel_alert);
            final Button updateBtn = customAlertView.findViewById(R.id.btnUpdate);
            final Button deleteBtn = customAlertView.findViewById(R.id.btnDelete_alert);
            final TextView nameTv = customAlertView.findViewById(R.id.productName_alert);
            final TextView buying = customAlertView.findViewById(R.id.buyingPrice_alert);
            final TextView selling = customAlertView.findViewById(R.id.selliingPrice_alert);
            final TextView dewsTv = customAlertView.findViewById(R.id.single_user_dews);
            final TextView profitTv = customAlertView.findViewById(R.id.profit_alert);
            final TextView paymentModeTv = customAlertView.findViewById(R.id.single_user_payment_mode);
            String payment= "";
            if (paymentMode == 0){
                 payment = "ONLINE";
             }else if(paymentMode == 1){
                 payment = "COD";
             }
            TextView usernameTv = customAlertView.findViewById(R.id.userNameTv);
            final ImageView productImage = customAlertView.findViewById(R.id.productImageAlert);
            if (image != null && image.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                productImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                productImage.setImageBitmap(bitmap);
                toAddProductIntent.putExtra("image byte", image);
            }
            nameTv.setText(productName+"( "+ productSize +" )");
            selling.setText(""+ sellingPrice);
            buying.setText(""+ actualPrice);
            dewsTv.setText("Pending : "+ dews);
            profitTv.setText("Profit : "+profit);
            String firstName = "";
            paymentModeTv.setText(payment);
            toAddProductIntent.putExtra("payment mode", payment);
            if (menuId == R.id.menu_dates) {
                TextView listViewNameTv = view.findViewById(R.id.name);
                usernameTv.setText(listViewNameTv.getText().toString());
                toAddProductIntent.putExtra("first name", usernameTv.getText().toString());
            }else if (menuId == R.id.menu_pending_payment){
                TextView listViewNameTv = view.findViewById(R.id.tv_client_name);
                firstName = listViewNameTv.getText().toString();
                usernameTv.setText(firstName);
                toAddProductIntent.putExtra("first name", usernameTv.getText().toString());
            }
            final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(customAlertView).setCancelable(false).create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    foreignKey = getUserForeignProductKey(id);
                    productCount = getCountOfProducts(foreignKey);
                }
            });
            thread.setName("DELETE PRODUCT ");
            thread.start();
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                }
            });
            updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //    select u.location,p.* from user_info u inner join product_info p where p._id = 1 and u._id = p.id;
                    String location = getUserLocation(id);
                    toAddProductIntent.putExtra("product name", productName);
                    toAddProductIntent.putExtra("pending fees", dews);
                    toAddProductIntent.putExtra("actual price", actualPrice);
                    toAddProductIntent.putExtra("selling price", sellingPrice);
                    toAddProductIntent.putExtra("profit",profit);
                    toAddProductIntent.putExtra("product size", productSize);
                    toAddProductIntent.putExtra("alertDialogUpdate","update");
                    toAddProductIntent.putExtra("location", location);
                    toAddProductIntent.putExtra("primaryKey",id);
                    dialog.dismiss();
                    startActivity(toAddProductIntent);
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (productCount == 1){
                        Uri uri = ContentUris.withAppendedId(clientContract.ClientEntry.CONTENT_URI_INFO_DELETE, foreignKey);
                        String selection = ClientEntry._ID+" = "+foreignKey;
                        getContentResolver().delete(uri,selection,null);
                    }else{
                        Uri uriProduct = ContentUris.withAppendedId(clientContract.ClientInfo.CONTENT_URI, id);
                        String selection = ClientInfo.COLUMN_PK_ID+" = "+id;
                        getContentResolver().delete(uriProduct,selection,null);
                    }
                    Toast.makeText(MainActivity.this,"Deleted ", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    onResume();
                }
            });
        }
        return true;
    }

    // this function will return the primary key of the user_info table with the help of the foregin key of the product_info table.
    private int getUserForeignProductKey(long id){
        String [] projection = {ClientInfo.COLUMN_PK_ID, ClientInfo.COLUMN_FK_ID};
        String selection = ClientInfo.COLUMN_PK_ID+" = "+id;
        Cursor cursor = getContentResolver().query(ClientInfo.CONTENT_URI,projection,selection,null,null);
        cursor.moveToFirst();
        return cursor.getInt(1);
    }

    // this function will return how may total products are there for that particular client.
    private int getCountOfProducts(int foreignKey){
        String [] projection = {ClientInfo.COLUMN_PK_ID, ClientInfo.COLUMN_FK_ID};
        String selection = ClientInfo.COLUMN_FK_ID+" = "+foreignKey;
        Cursor cursor = getContentResolver().query(ClientInfo.CONTENT_URI,projection,selection,null,null);
        return cursor.getCount();
    }
    // this function will return the location of the selected user on the alert dialog
    private String getUserLocation(long id){
        String user_table = ClientEntry.TABLE_NAME;
        String product_table = ClientInfo.TABLE_NAME;
        String [] projection = { user_table+"."+ClientEntry.COLUMN_LOCATION,product_table+"."+ClientInfo.COLUMN_PK_ID,product_table+"."+ClientInfo.COLUMN_FK_ID,product_table+"."+ClientInfo.COLUMN_PROFIT};
        String selection = product_table+"."+ClientInfo.COLUMN_PK_ID+" = "+id+" and "+user_table+"."+ClientEntry._ID+" = "+product_table+"."+ClientInfo.COLUMN_FK_ID;
        Cursor cursor = getContentResolver().query(ClientInfo.CONTENT_URI_PRODUCT_INFO_TOTAL_ITEMS_PRICE,projection,selection,null,null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    // Returns the position of the letter which is clicked from the list of the names.
    @SuppressLint("NonConstantResourceId")
    private int getPositionFromData(String character, final ListView homeListView) {
        int selectedMenuId = bottomNavigationView.getSelectedItemId();
        switch (selectedMenuId){
            case R.id.menu_home : {
                return searchVerticalMenuHome(character);
            }
        }
        return 0;
    }

    // this vertical search bar is used when user is in the home menu of the app.
    private int searchVerticalMenuHome(String character){
        int position = 0;
        for (String s : adapter.names) {
            Log.i(LOG_TAG,"TEST: String = "+s);
            String letter = "" + s.charAt(0);
            if (letter.equalsIgnoreCase("" + character)) {
                Log.i(LOG_TAG,"TEST: String = "+s+" = "+letter);
                return position;
            }
            position++;
        }
        homeListView.post(new Runnable() {
            @Override
            public void run() {
                homeListView.smoothScrollToPosition(homeListView.getCount()-1);
            }
        });
        return 0;
    }

    // Inflates from the menu resource dir. the search icon in toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager)MainActivity.this.getSystemService(Context.SEARCH_SERVICE);
        MenuItem ourSearchItem = menu.findItem(R.id.menu_search);
        // This is for the search view up button so when pressed we can get the original view of that particular screen.
        ourSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                int match = bottomNavigationView.getSelectedItemId();
                switch (match){
                    case R.id.menu_home : restartLoader(MAIN_QUERY_DISPLAY); break;
                    case R.id.menu_dates : restartLoader(GET_PRODUCT_DATES); break;
                    case R.id.menu_profit : restartLoader(GET_OVERALL_PROFIT); break;
                    case R.id.menu_pending_payment : restartLoader(GET_CLIENT_DEWS); break;
                }
                return true;
            }
        });

        SearchView searchView = null;
        if(ourSearchItem != null){
            // Assumes current activity is the searchable activity
            searchView = (SearchView) ourSearchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setQueryHint("Search Client Name");
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }

        assert searchView != null;
        searchView.setIconifiedByDefault(false);// Do not iconify the widget; expand it by default
        searchView.setSubmitButtonEnabled(true);

        searchManager.setOnCancelListener(new SearchManager.OnCancelListener() {
            @Override
            public void onCancel() {
                restartLoader(GET_OVERALL_PROFIT);
            }
        });
        // this will give display the whole table of user_info
        // if text is entered then it will display names  which is searched from the user_table
        /* Search view from the main activity*/
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {

                }else{
                    submitQuery(query.toLowerCase());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noDataFound.setVisibility(View.INVISIBLE);
                // this will give display the whole table of user_info
                if (newText.isEmpty()) {
                    switch(bottomNavigationView.getSelectedItemId()){
                        case R.id.menu_home : {
                            if (alphabetik.getVisibility() == View.INVISIBLE) {
                                alphabetik.setVisibility(View.VISIBLE);
                            }
                            restartLoader(MAIN_QUERY_DISPLAY);
                            break;
                        }
                        case R.id.menu_dates : {
                            restartLoader(GET_PRODUCT_DATES);
                            break;
                        }
                        case R.id.menu_profit : {
                            restartLoader(GET_OVERALL_PROFIT);
                            break;
                        }
                        case R.id.menu_pending_payment : {
                            restartLoader(GET_CLIENT_DEWS);
                            break;
                        }
                    }

                } else {
                    // if text is entered then it will display names  which is searched from the user_table
                    Log.d(LOG_TAG, "onQueryTextChange: "+newText);
                    showSearchResult(newText.toLowerCase());
                }

                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_backup :{
                Toast.makeText(MainActivity.this,"Backing Up", Toast.LENGTH_SHORT).show();
                if (checkStoragePermission()){
                    exportCsv();
                }else{
                    requestStoragePermissionExport();
                }
                return true;
            }
            case R.id.menu_restore : {
                Toast.makeText(MainActivity.this,"Restoring",Toast.LENGTH_SHORT).show();
                if (checkStoragePermission()){
                    importCsv();
                }else{
                    requestStoragePermissionImport();
                }
                return true;
            } default:{
                return false;
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {

        String user_info_table = ClientEntry.TABLE_NAME;
        String product_info_table = ClientInfo.TABLE_NAME;

        switch (loaderId) {
            // this is displayed on the mainscreen of the app its the home screen.
            case MAIN_QUERY_DISPLAY : {
                String[] projections = {user_info_table + "." + ClientEntry._ID,
                        user_info_table + "." + ClientEntry.COLUMN_FIRST_NAME,
                        user_info_table + "." + ClientEntry.COLUMN_LOCATION};
                String orderBy = "lower("+ClientEntry.COLUMN_FIRST_NAME + ") ASC";
                return new CursorLoader(this, ClientEntry.CONTENT_URI, projections,null, null, orderBy);
            }
            case GET_PRODUCT_DATES : {
                String[] projections = {user_info_table + "." + ClientEntry._ID +" iid ",
                        user_info_table + "." + ClientEntry.COLUMN_FIRST_NAME,
                        product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                        product_info_table+"."+ClientInfo.COLUMN_FK_ID,
                        product_info_table + "." + ClientInfo.COLUMN_PRODUCT_NAME,
                        product_info_table + "." + ClientInfo.COLUMN_PROFILE_DATE};
//                    String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID;
                String selection = "iid = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID;
                    String orderBy = "DATE("+ClientInfo.COLUMN_PROFILE_DATE+") DESC";
                    return new CursorLoader(this,ClientEntry.CONTENT_URI,projections,selection,null,orderBy);
            }
            case GET_OVERALL_PROFIT : {
                String [] projection = {
                        product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                        user_info_table+"."+ClientEntry.COLUMN_FIRST_NAME,
                        "sum("+product_info_table+"."+ClientInfo.COLUMN_PROFIT+") overallProfit",
                        product_info_table+"."+ClientInfo.COLUMN_FK_ID };
                String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID;
                String sort = "overallProfit DESC";
                return new CursorLoader(this,ClientEntry.CONTENT_URI,projection,selection,null,sort);
            }
            case GET_CLIENT_DEWS : {
                String [] projection = {
                        product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                        user_info_table+"."+ClientEntry.COLUMN_FIRST_NAME,
                        product_info_table+"."+ClientInfo.COLUMN_PRODUCT_NAME,
                        product_info_table+"."+ClientInfo.COLUMN_PENDING,
                        product_info_table+"."+ClientInfo.COLUMN_PAYMENT_MODE};
                String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+product_info_table+"."+ClientInfo.COLUMN_PENDING+" > 0";
                String sort = product_info_table+"."+ClientInfo.COLUMN_PENDING+" DESC";
                return new CursorLoader(this,ClientEntry.CONTENT_URI,projection,selection,null,sort);
            }
            default:{
                return null;
            }
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MAIN_QUERY_DISPLAY : {
                adapter.swapCursor(data);
                break;
            }
            case GET_PRODUCT_DATES : {
                dateCursorAdapter.swapCursor(data);
                break;
            }
            case GET_OVERALL_PROFIT : {
                profitCursorAdapter.swapCursor(data);
                break;
            }
            case GET_CLIENT_DEWS : {
                dewsCursorAdapter.swapCursor(data);
                break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        switch (loader.getId()) {
            case MAIN_QUERY_DISPLAY : {
                adapter.swapCursor(null);
                break;
            }
            case GET_PRODUCT_DATES : {
                dateCursorAdapter.swapCursor(null);
                break;
            }
            case GET_OVERALL_PROFIT : {
                profitCursorAdapter.swapCursor(null);
                break;
            }
            case GET_CLIENT_DEWS : {
                dewsCursorAdapter.swapCursor(null);
                break;
            }
        }
    }

    // This will call he content provider and fetch the data from the table and display it on the screen
    private void showSearchResult(String search) {
            noDataFound.setVisibility(View.INVISIBLE);
            alphabetik.setVisibility(View.VISIBLE);
            int menuId =  bottomNavigationView.getSelectedItemId();
            String selections = "lower(" + ClientEntry.COLUMN_FIRST_NAME + ") LIKE ? ";
            String[] selectionArgs = new String[]{"%"+search + "%"};
            switch (menuId){
                case R.id.menu_home : {
                    String[] projections = {ClientEntry._ID,
                            ClientEntry.COLUMN_FIRST_NAME,
                            ClientEntry.COLUMN_LOCATION,};
                    Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI_SEARCH_NAME, projections, selections, selectionArgs, null);
                    // this will swap the adapter with the newly return cursor if it as the searched item in it.
                    if (cursor != null && cursor.getCount() != 0) {
                        adapter.swapCursor(cursor);
                    }
                    break;
                }
                case R.id.menu_dates : {
                    alphabetik.setVisibility(View.GONE);
                    String user_info_table = ClientEntry.TABLE_NAME;
                    String product_info_table = ClientInfo.TABLE_NAME;
                    String[] projections = {user_info_table + "." + ClientEntry._ID +" iid",
                            user_info_table + "." + ClientEntry.COLUMN_FIRST_NAME,
                            product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                            product_info_table+"."+ClientInfo.COLUMN_FK_ID,
                            product_info_table + "." + ClientInfo.COLUMN_PRODUCT_NAME,
                            product_info_table + "." + ClientInfo.COLUMN_PROFILE_DATE};
                    String where = "iid = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+selections;
                    Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI, projections, where, selectionArgs, null);
                    // this will swap the adapter with the newly return cursor if it as the searched item in it.
                    if (cursor != null && cursor.getCount() != 0) {
                        Log.d(LOG_TAG, "showSearchResult: ");
                        dateCursorAdapter.swapCursor(cursor);
                    }
                    if (cursor == null || cursor.getCount() == 0){
                        Log.d(LOG_TAG, "showSearchResult: null cursor");
                    }
                    break;

                }
                case R.id.menu_profit : {
                    alphabetik.setVisibility(View.GONE);
                    String user_info_table = ClientEntry.TABLE_NAME;
                    String product_info_table = ClientInfo.TABLE_NAME;
                    String [] projection = {
                            product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                            user_info_table+"."+ClientEntry.COLUMN_FIRST_NAME,
                            "sum("+product_info_table+"."+ClientInfo.COLUMN_PROFIT+") overallProfit",
                            product_info_table+"."+ClientInfo.COLUMN_FK_ID };
                    String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+selections;
                    String sort = "overallProfit DESC";
                    Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI, projection, selection, selectionArgs, sort);
                    // this will swap the adapter with the newly return cursor if it as the searched item in it.
                    if (cursor != null && cursor.getCount() != 0) {
                        profitCursorAdapter.swapCursor(cursor);
                    }
                    break;
                }
                case R.id.menu_pending_payment : {
                    alphabetik.setVisibility(View.GONE);
                    String user_info_table = ClientEntry.TABLE_NAME;
                    String product_info_table = ClientInfo.TABLE_NAME;
                    String [] projection = {
                            product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                            user_info_table+"."+ClientEntry.COLUMN_FIRST_NAME,
                            product_info_table+"."+ClientInfo.COLUMN_PRODUCT_NAME,
                            product_info_table+"."+ClientInfo.COLUMN_PENDING,
                            product_info_table+"."+ClientInfo.COLUMN_PAYMENT_MODE};
                    String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+product_info_table+"."+ClientInfo.COLUMN_PENDING+" > 0 and "+selections;
                    String sort = product_info_table+"."+ClientInfo.COLUMN_PENDING+" DESC";
                    Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI,projection,selection,selectionArgs,sort);
                    // this will swap the adapter with the newly return cursor if it as the searched item in it.
                    if (cursor != null && cursor.getCount() != 0) {
                        dewsCursorAdapter.swapCursor(cursor);
                    }
                    break;
                }
            }

    }

    // This is helper function for onTextSubmitted of searchView,
    // It will display the user if found, else display a image saying no result found.
    @SuppressLint("NonConstantResourceId")
    private void submitQuery(String search) {
        int menuId = bottomNavigationView.getSelectedItemId();
        String selections = "lower(" + ClientEntry.COLUMN_FIRST_NAME + ") LIKE ? ";
        String[] selectionArgs = new String[]{search};
        switch (menuId){
            case R.id.menu_home: {
                String[] projections = {ClientEntry._ID,
                        ClientEntry.COLUMN_FIRST_NAME,
                        ClientEntry.COLUMN_LOCATION};
                String selection = "lower("+ClientEntry.COLUMN_FIRST_NAME+") = ?";
                Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI_SEARCH_NAME,projections,selection,selectionArgs,"demo");
                // Display the users found by the query.
                if (cursor != null && cursor.getCount() != 0) {
                    adapter.swapCursor(cursor);
                }else{
                    // if no users where found then display an image saying no data found.
                    // and swap the cursor to null to have a empty view
                    adapter.swapCursor(null);
                    emptyView.setVisibility(View.INVISIBLE);
                    noDataFound = findViewById(R.id.no_data_image);
                    noDataFound.setVisibility(View.VISIBLE);
                    alphabetik.setVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this,"No User Found",Toast.LENGTH_SHORT).show();
                }
                break;
            }

            case R.id.menu_dates :{
                String user_info_table = ClientEntry.TABLE_NAME;
                String product_info_table = ClientInfo.TABLE_NAME;
                String[] projections = {user_info_table + "." + ClientEntry._ID +" iid",
                        user_info_table + "." + ClientEntry.COLUMN_FIRST_NAME,
                        product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                        product_info_table+"."+ClientInfo.COLUMN_FK_ID,
                        product_info_table + "." + ClientInfo.COLUMN_PRODUCT_NAME,
                        product_info_table + "." + ClientInfo.COLUMN_PROFILE_DATE};
                String where = "iid = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+selections;
                Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI, projections, where, selectionArgs, null);
                // this will swap the adapter with the newly return cursor if it as the searched item in it.
                if (cursor != null && cursor.getCount() != 0) {
                    Log.d(LOG_TAG, "showSearchResult: ");
                    dateCursorAdapter.swapCursor(cursor);
                }
                if (cursor == null || cursor.getCount() == 0){
                    Log.d(LOG_TAG, "showSearchResult: null cursor");
                    dateCursorAdapter.swapCursor(null);
                    showNoResultsFound();
                }
                break;
            }

            case R.id.menu_profit : {
                alphabetik.setVisibility(View.GONE);
                String user_info_table = ClientEntry.TABLE_NAME;
                String product_info_table = ClientInfo.TABLE_NAME;
                String [] projection = {
                        product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                        user_info_table+"."+ClientEntry.COLUMN_FIRST_NAME,
                        "sum("+product_info_table+"."+ClientInfo.COLUMN_PROFIT+") overallProfit",
                        product_info_table+"."+ClientInfo.COLUMN_FK_ID };
                String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+selections;
                String sort = "overallProfit DESC";
                Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI, projection, selection, selectionArgs, sort);
                if (cursor != null && cursor.getCount() != 0) {
                    Log.d(LOG_TAG, "showSearchResult: ");
                    profitCursorAdapter.swapCursor(cursor);
                }
                if (cursor == null || cursor.getCount() == 0){
                    Log.d(LOG_TAG, "showSearchResult: null cursor");
                    profitCursorAdapter.swapCursor(null);
                    showNoResultsFound();
                }
            }
            case R.id.menu_pending_payment : {
                String user_info_table = ClientEntry.TABLE_NAME;
                String product_info_table = ClientInfo.TABLE_NAME;
                String [] projection = {
                        product_info_table+"."+ClientInfo.COLUMN_PK_ID,
                        user_info_table+"."+ClientEntry.COLUMN_FIRST_NAME,
                        product_info_table+"."+ClientInfo.COLUMN_PRODUCT_NAME,
                        product_info_table+"."+ClientInfo.COLUMN_PENDING,
                        product_info_table+"."+ClientInfo.COLUMN_PAYMENT_MODE};
                String selection = user_info_table+"."+ClientEntry._ID+" = "+product_info_table+"."+ClientInfo.COLUMN_FK_ID+" and "+product_info_table+"."+ClientInfo.COLUMN_PENDING+" > 0 and "+selections;
                String sort = product_info_table+"."+ClientInfo.COLUMN_PENDING+" DESC";
                Cursor cursor = getContentResolver().query(ClientEntry.CONTENT_URI,projection,selection,selectionArgs,sort);
                if (cursor != null && cursor.getCount() != 0) {
                    Log.d(LOG_TAG, "showSearchResult: ");
                    dewsCursorAdapter.swapCursor(cursor);
                }
                if (cursor == null || cursor.getCount() == 0){
                    Log.d(LOG_TAG, "showSearchResult: null cursor");
                    dewsCursorAdapter.swapCursor(null);
                    showNoResultsFound();
                }
            }
        }
    }

    private void showNoResultsFound(){
        emptyView.setVisibility(View.INVISIBLE);
        noDataFound = findViewById(R.id.no_data_image);
        noDataFound.setVisibility(View.VISIBLE);
        alphabetik.setVisibility(View.INVISIBLE);
        Toast.makeText(MainActivity.this,"No User Found",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int match = bottomNavigationView.getSelectedItemId();
        switch (match){
            case R.id.menu_home : restartLoader(MAIN_QUERY_DISPLAY); break;
            case R.id.menu_dates : restartLoader(GET_PRODUCT_DATES);  break;
            case R.id.menu_pending_payment : restartLoader(GET_CLIENT_DEWS);  break;
        }

    }

    // This is helper function for onTextChange of SearchView cause it will display the list of names from the table
    // when the search view text field is empty.
    private void restartLoader(int loaderNumber){
        getSupportLoaderManager().restartLoader(loaderNumber,null,this);
    }

    class Timer extends CountDownTimer{

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            timerTv.setText(l/1000+"");
        }

        @Override
        public void onFinish() {
            dismissBtn.setBackgroundColor(getResources().getColor(R.color.timer_button_color));
            isValidToExit = true;
        }
    }

}
