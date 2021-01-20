package com.example.storeit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.storeit.data.clientContract;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;


public class ProductViewCursorAdapter extends CursorAdapter {
    Context context;
    private String firstName;
    private String location;
    public ProductViewCursorAdapter(Context context, Cursor c,String firstName, String location) {
        super(context, c, 0);
        this.context = context;
        this.firstName = firstName;
        this.location = location;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view =  LayoutInflater.from(context).inflate(R.layout.custom_product_view_listview,parent,false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        CheckBox cb = view.findViewById(R.id.checkbox_listView);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int position = (int)buttonView.getTag();
                if (isChecked){
                    //check whether its already selected or not
                    if (!ProductHomeView.selectedItemsPositions.contains(position))
                        ProductHomeView.selectedItemsPositions.add(position);
                } else {
                    //remove position if unchecked checked item
                    ProductHomeView.selectedItemsPositions.remove((Object) position);
                }
                if (ProductHomeView.mActionMode != null){
                ProductHomeView.mActionMode.setTitle(ProductHomeView.selectedItemsPositions.size()+" Items Selected..");
                }
            }
        });
        Log.i("TAG", "newView: called");
        return view;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final Intent toAddProductIntent = new Intent(context,AddNewClient.class);
        Log.i("TAG", "bindView: called");

        final ViewHolder holder = (ViewHolder) view.getTag();
        final TextView errorMessage;

        /** TextView Reference of the Custom ListView*/
        errorMessage = view.findViewById(R.id.error_message_tv);

        /** CheckBox, ImageView, LinearLayout, ImageButton error, Button reference of the custom listView */
        final RelativeLayout infoLayout = view.findViewById(R.id.layout_dropdown_relative);
        final Button dropDown = view.findViewById(R.id.expand_user_info_listView);

        holder.updateBtn.setFocusable(false);
        holder.updateBtn.setFocusableInTouchMode(false);
        /** Get the Data from DataBase */
        String date = cursor.getString(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PROFILE_DATE));
        final int profit =  cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PROFIT));
        final int selling = cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_SELLING_PRICE));
        final int actual = cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_ACTUAL_PRICE));
        final String productName = cursor.getString(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PRODUCT_NAME));
        final int pending = cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PENDING));
        final byte [] image = cursor.getBlob(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PRODUCT_IMAGE));
        int payMode = cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PAYMENT_MODE)); // this column will return 1 or 0 {@1 = COD and 0= Online} Payment done
        final String productSize = " ("+cursor.getString(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PRODUCT_SIZE))+") ";
        final String[] dateTime = date.split(" ");
        // Set payment mode of the user.
        final String paymentMode;
        if (payMode == 1){
            paymentMode = "COD";
        }else{
            paymentMode = "ONLINE";
        }

        if(image == null){
            holder.noImageTv.setVisibility(View.VISIBLE);
            holder.productImage.setImageResource(R.drawable.selimage);
        }else if (image.length > 0){
            toAddProductIntent.putExtra("image byte",image);
            holder.noImageTv.setVisibility(View.GONE);
            // convert from byte array to bitmap.
            // this will run in background and then update the UI with the image of the product.
            Thread imageThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
//                         handler is use to insert the task in the mainQueue {i.e UI thread}
//                         to complete this  we us Looper to get the MainUi thread queue and insert it in the Ui.
                    }finally {
                        Handler handler = new Handler(Looper.getMainLooper());
                        final Bitmap finalBitmap = bitmap;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                holder.productImage.setImageBitmap(finalBitmap);
                            }
                        });
                    }
                }
            });
            imageThread.setName("Image Thread");
            imageThread.start();

        }

        holder.titleTv.setText(productName);
        holder.pendingTv.setText("Pending : "+pending);
        holder.actualTv.setText("Actual Price : "+actual);
        holder.sellingTv.setText("Selling Price : "+selling);
        holder.profitTv.setText("Profit : "+profit);
        holder.dateTv.setText(dateTime[0]);
        holder. productSizeTv.setText(productSize);
        holder.paymentModeTv.setText(paymentMode);



        /** If pending fees is left then it will show a backgrounf of black , red with a error icon saying "Pending fees"*/
        if (pending > 0){
            holder.linearLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.error_message));
            holder.errorButton.setVisibility(View.VISIBLE);
            holder.errorButton.setFocusable(false);
            holder.errorButton.setFocusableInTouchMode(false);
            holder.errorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (errorMessage.getVisibility() == View.GONE){
                        TransitionManager.beginDelayedTransition(holder.cv_singleItem,new AutoTransition());
                        errorMessage.setVisibility(View.VISIBLE);
                    }else{
                        TransitionManager.beginDelayedTransition(holder.cv_singleItem,new AutoTransition());
                        errorMessage.setVisibility(View.GONE);
                    }
                }
            });
        }else{
            holder.linearLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.layout_background_custom));
            holder.errorButton.setVisibility(View.GONE);
        }


        /** Reference of the Card View of custom Listview, And dropDown for dropping down the linear layout view*/
        holder.cv_singleItem = view.findViewById(R.id.cv_custom_single_product);
        dropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoLayout.getVisibility() == View.GONE){
                    TransitionManager.beginDelayedTransition(holder.cv_singleItem,new AutoTransition());
                    infoLayout.setVisibility(View.VISIBLE);
                    dropDown.setBackgroundResource(R.drawable.ic_sumup);
                }else{
                    Log.i("THIS IS TEST","DEMO VIEW "+ infoLayout.getVisibility());
                    TransitionManager.beginDelayedTransition(holder.cv_singleItem,new AutoTransition());
                    infoLayout.setVisibility(View.GONE);
                    dropDown.setBackgroundResource(R.drawable.ic_dropdown);
                }
            }
        });

        /** This is use to identity when to make check box vissible and when to not.*/
        if (ProductHomeView.isActionMode){
            holder.materialCheckBox.setVisibility(View.VISIBLE);
        }else{
            holder.materialCheckBox.setVisibility(View.INVISIBLE);
        }
        holder.materialCheckBox.setTag(cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PK_ID)));
        if (ProductHomeView.selectedItemsPositions.contains(cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PK_ID))))
            holder.materialCheckBox.setChecked(true);
        else
            holder.materialCheckBox.setChecked(false);

        Button updateProduct = view.findViewById(R.id.btn_update_single_item);
        updateProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAddProductIntent.putExtra("product name", productName);
                toAddProductIntent.putExtra("pending fees", pending);
                toAddProductIntent.putExtra("actual price", actual);
                toAddProductIntent.putExtra("selling price", selling);
                toAddProductIntent.putExtra("profit",profit);
                toAddProductIntent.putExtra("product size", productSize);
                toAddProductIntent.putExtra("payment mode",paymentMode);
                toAddProductIntent.putExtra("cursorAdapter","ProductViewCursor");
                toAddProductIntent.putExtra("first name", firstName);
                toAddProductIntent.putExtra("location",location);
                toAddProductIntent.putExtra("primaryKey",cursor.getInt(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PK_ID)));
                Log.d("TAG", "onClick: "+productName);
                context.startActivity(toAddProductIntent);
            }
        });

    }

    static class ViewHolder{
        TextView dateTv;
        TextView profitTv;
        TextView sellingTv;
        TextView actualTv;
        TextView pendingTv;
        TextView titleTv;
        TextView productSizeTv;
        TextView paymentModeTv;
        TextView noImageTv;
        CardView cv_singleItem;
        LinearLayout linearLayout;
        ImageButton errorButton;
        Button updateBtn;
        MaterialCheckBox materialCheckBox;
        ImageView productImage;
        ViewHolder(View view){
            dateTv = view.findViewById(R.id.tv_date_dropdown_listView);
            profitTv = view.findViewById(R.id.tv_profit_dropdown_listView);
            sellingTv = view.findViewById(R.id.tv_sellingPrice_dropdown_listView);
            actualTv = view.findViewById(R.id.tv_actualPrice_dropDown_listView);
            pendingTv = view.findViewById(R.id.tv_pending_dropdown_listView);
            titleTv = view.findViewById(R.id.tv_product_name_listView);
            productSizeTv = view.findViewById(R.id.tv_product_size_listView);
            paymentModeTv = view.findViewById(R.id.tv_product_payment_mode_listView);
            noImageTv = view.findViewById(R.id.tv_no_image_available);
            cv_singleItem = view.findViewById(R.id.cv_custom_single_product);
            linearLayout = view.findViewById(R.id.parent_liner_layout);
            errorButton = view.findViewById(R.id.error_message_ib);
            updateBtn = view.findViewById(R.id.btn_update_single_item);
            materialCheckBox = view.findViewById(R.id.checkbox_listView);
            productImage = view.findViewById(R.id.iv_product_image_listView);
        }
    }


}
