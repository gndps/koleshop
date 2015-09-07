package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.model.MenuInfo;

import java.util.Collections;
import java.util.List;

/**
 * Created by gundeepsingh on 01/02/15.
 */
public class GndpAdapter extends RecyclerView.Adapter<GndpAdapter.MyViewHolder> {

    private LayoutInflater inflator;
    private  List<MenuInfo> data = Collections.emptyList();

    public GndpAdapter(Context context, List<MenuInfo> data)
    {
        inflator = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflator.inflate(R.layout.draw_item, viewGroup, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, int i) {
        MenuInfo info = data.get(i);
        viewHolder.title.setText(info.title);
        viewHolder.icon.setImageResource(info.iconId);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView title;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById((R.id.listText));
            icon = (ImageView) itemView.findViewById((R.id.listIcon));

        }

    }

    public void deleteItem(int position)
    {
        data.remove(position);
        notifyItemRemoved(position);
    }
}
