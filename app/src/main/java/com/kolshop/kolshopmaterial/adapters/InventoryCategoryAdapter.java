package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageRequest;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.model.Product;
import com.kolshop.kolshopmaterial.services.CommonIntentService;
import com.kolshop.kolshopmaterial.viewholders.InventoryCategoryViewHolder;
import com.kolshop.kolshopmaterial.viewholders.InventoryProductViewHolder;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.Collections;
import java.util.List;

/**
 * Created by Gundeep on 14/10/15.
 */
public class InventoryCategoryAdapter extends RecyclerView.Adapter<InventoryCategoryViewHolder> {

    private LayoutInflater inflator;
    List<InventoryCategory> categories;


    public InventoryCategoryAdapter(Context context, List<InventoryCategory> categories)
    {
        inflator = LayoutInflater.from(context);
        this.categories = categories; //null
    }

    @Override
    public InventoryCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.item_inventory_category, parent, false);
        InventoryCategoryViewHolder holder = new InventoryCategoryViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InventoryCategoryViewHolder holder, int position) {
        if(categories!=null) {
            InventoryCategory inventoryCategory = categories.get(position);
            holder.setTitle(inventoryCategory.getName());
            holder.setSubtitle(inventoryCategory.getDesc());
            holder.setImageUrl(inventoryCategory.getImageUrl());
            holder.sendImageFetchRequest();
        }
        //data.remove(position);
        //notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        if(categories!=null) {
            return categories.size();
        } else return 0;
    }
    
    public void setNewDataOnAdapter(List<InventoryCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(InventoryCategoryViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cancelImageFetchRequest();
    }
}
