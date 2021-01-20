package com.example.storeit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.storeit.data.clientContract;

import java.util.ArrayList;
import java.util.Random;

import androidx.core.content.ContextCompat;

public class DateCursorAdapter extends CursorAdapter {
    public  ArrayList<String> names;
    public DateCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        names = new ArrayList<>();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(R.layout.custom_date_listview,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
                int position = cursor.getPosition();
                int dateColumnIndex = cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PROFILE_DATE);
                TextView productNameTv = view.findViewById(R.id.productName);
                TextView nameTv = view.findViewById(R.id.name);
                TextView dateTv = view.findViewById(R.id.date);
                String product = cursor.getString(cursor.getColumnIndex(clientContract.ClientInfo.COLUMN_PRODUCT_NAME));
                String dates = cursor.getString(dateColumnIndex);
                String name = cursor.getString(cursor.getColumnIndex(clientContract.ClientEntry.COLUMN_FIRST_NAME));
                String[] dateSplit = dates.split(" ");
                String date = dateSplit[0].substring(8);
                if (!names.contains(name)){
                    names.add(name);
                }
                if (position - 1 >= 0){
                    TextView headerTv = view.findViewById(R.id.header);
                    String currentDate = cursor.getString(dateColumnIndex);
                    cursor.moveToPosition(position - 1);
                    String previousDate = cursor.getString(dateColumnIndex);
                    String [] currentDateSplit = currentDate.split(" ");
                    String [] previousDateSplit = previousDate.split(" ");
                    Log.d("TAG", "bindView: "+currentDateSplit[0]+", previous "+previousDateSplit[0]);
                    if (!currentDateSplit[0].substring(0,8).equals(previousDateSplit[0].substring(0,8))){
                        String [] dateMonth = currentDateSplit[0].split("-");
                        String headerText = getMonth(Integer.parseInt(dateMonth[1]))+" "+dateMonth[0];
                        headerTv.setText(headerText);
                        headerTv.setVisibility(View.VISIBLE);
                    }else{
                        headerTv.setVisibility(View.GONE);
                    }
                }else{
                    TextView headerTv = view.findViewById(R.id.header);
                    String [] dateMonth = dateSplit[0].split("-");
                    String headerText = getMonth(Integer.parseInt(dateMonth[1]))+" "+dateMonth[0];
                    headerTv.setText(headerText);
                    headerTv.setVisibility(View.VISIBLE);

                }

                // Set the proper background color on the date circle.
                // Fetch the background from the TextView, which is a GradientDrawable.
                GradientDrawable dateCircle = (GradientDrawable) dateTv.getBackground();
                // Get the appropriate background color random color
                int circleColor = getRandomColor(context);
                // Set the color on the Date circle
                dateCircle.setColor(circleColor);
                nameTv.setText(name);
                productNameTv.setText(product);
                dateTv.setText(date);

    }

    private String getMonth(int monthNum){
        String month="";
        switch (monthNum){
            case 1 : month = "January"; break;
            case 2 : month = "February"; break;
            case 3 : month = "March"; break;
            case 4 : month = "April"; break;
            case 5 : month = "May"; break;
            case 6 : month = "June"; break;
            case 7 : month = "July"; break;
            case 8 : month = "August"; break;
            case 9 : month = "September"; break;
            case 10 : month = "October"; break;
            case 11 : month = "November"; break;
            case 12 : month = "December"; break;
        }
        return month;
    }

    private int getRandomColor(Context context){
        int [] dateColors = {R.color.magnitude1, R.color.magnitude2, R.color.magnitude3,
                             R.color.magnitude4, R.color.magnitude5, R.color.magnitude6,
                             R.color.magnitude7, R.color.magnitude8, R.color.magnitude9,
                             R.color.magnitude10plus};
        Random randomColor = new Random();
        int color = dateColors[randomColor.nextInt(dateColors.length)]+1;

        return ContextCompat.getColor(context,color);
    }

}
