package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;

public class DeliveryTimeRemainingAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflater;
    String[] time = {"5 min", "10 min", "15 min",
            "20 min", "30 min", "45 min",
            "1 hr", "1:30 hr", "2 hr"};
    TextView gridText;

    public DeliveryTimeRemainingAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return time.length;
    }

    @Override
    public TextView getItem(int position) {
        return gridText;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.view_delivery_time_remaining_item, parent, false);
        convertView.setLayoutParams(new GridView.LayoutParams(200, 60));
        gridText = (TextView) convertView.findViewById(R.id.text_view_in_grid);
        gridText.setText(time[position]);
        gridText.setTag(R.id.text_view_in_grid, Integer.toString(position));
        return convertView;
    }
}


