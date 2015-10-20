package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.viewholders.InventoryProductViewHolder;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.List;

/**
 * Created by Gundeep on 19/10/15.
 */
public class InventoryProductAdapter extends RecyclerView.Adapter<InventoryProductViewHolder> {

    private LayoutInflater inflator;
    List<InventoryCategory> categories;

    public InventoryProductAdapter(Context context, List<InventoryCategory> categories)
    {
        inflator = LayoutInflater.from(context);
        this.categories = categories; //null
    }

    @Override
    public InventoryProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.item_inventory_product, parent, false);
        InventoryProductViewHolder holder = new InventoryProductViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InventoryProductViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 20;
    }
}
