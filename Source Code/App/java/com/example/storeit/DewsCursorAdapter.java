package com.example.storeit;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class DewsCursorAdapter extends CursorAdapter {

    public DewsCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.custom_dews_listview,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // cursor column starts with 0th based indexing
        TextView clientNameTv = view.findViewById(R.id.tv_client_name); // at 1st position in cursor window
        TextView clientProductTv = view.findViewById(R.id.tv_client_product); // at 2nd position in cursor window
        TextView clientDews = view.findViewById(R.id.tv_client_dews); // at 3rd position in cursor window

        String name = cursor.getString(1);
        String product = cursor.getString(2);
        int dews = cursor.getInt(3);

        clientNameTv.setText(name);
        clientProductTv.setText(product);
        clientDews.setText("Pending : "+dews);
    }
}
