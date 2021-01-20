package com.example.storeit;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class ProfitCursorAdapter extends CursorAdapter {

    public ProfitCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.main_listview,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView clientNameTv = view.findViewById(R.id.tv_client_name);
        TextView clientTotalProfit = view.findViewById(R.id.tv_client_location);
        String name = cursor.getString(1);
        int overallProfit = cursor.getInt(2);
        clientNameTv.setText(name);
        clientTotalProfit.setText("Profit Made : "+overallProfit);
    }
}
