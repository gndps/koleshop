package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kolshop.kolshopmaterial.model.Product;
import com.kolshop.kolshopmaterial.viewholders.ProductItemViewHolder;

import java.util.Collections;
import java.util.List;

/**
 * Created by Gundeep on 23/02/15.
 */
public class ProductListAdapter extends RecyclerView.Adapter<ProductItemViewHolder> {

    private LayoutInflater inflator;
    private List<Product> data = Collections.emptyList();

    public ProductListAdapter(Context context, List<Product> data)
    {
        inflator = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public ProductItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflator.inflate(com.kolshop.kolshopmaterial.R.layout.product_list_item, viewGroup, false);
        ProductItemViewHolder holder = new ProductItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ProductItemViewHolder viewHolder, int i) {
        //set details for each product here
        //Product product = data.get(i);
        //viewHolder.name.setText(product.getName());
        //viewHolder.description.setText(product.getDesc());
    }

    @Override
    public int getItemCount() {
        return 100;
        //return data.size();
    }


    public void deleteItem(int position)
    {
        data.remove(position);
        notifyItemRemoved(position);
    }
}
