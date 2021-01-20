package com.example.storeit;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.storeit.data.clientContract;

import java.util.ArrayList;

public class HomeCursorAdapter extends CursorAdapter {
    ArrayList<String> names = new ArrayList<String>();
    public HomeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.main_listview,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvName = view.findViewById(R.id.tv_client_name);
        TextView tvLocation = view.findViewById(R.id.tv_client_location);

        String name = cursor.getString(cursor.getColumnIndex(clientContract.ClientEntry.COLUMN_FIRST_NAME));
        String location = cursor.getString(cursor.getColumnIndex(clientContract.ClientEntry.COLUMN_LOCATION));

        if(!names.contains(name)){
            names.add(name);
        }
        tvName.setText(name);
        tvLocation.setText(location);
    }
}
