package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.model.Product;
import com.kolshop.kolshopmaterial.viewholders.ProductItemViewHolder;
import com.kolshop.kolshopmaterial.viewholders.ProductMasterListViewHolder;

import java.util.Collections;
import java.util.List;

/**
 * Created by Gundeep on 14/10/15.
 */
public class ProductMasterListAdapter extends RecyclerView.Adapter<ProductMasterListViewHolder> {

    private LayoutInflater inflator;
    private List<Product> data = Collections.emptyList();

    public ProductMasterListAdapter(Context context, List<Product> data)
    {
        inflator = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public ProductMasterListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.view_product_master_list, parent, false);
        ProductMasterListViewHolder holder = new ProductMasterListViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ProductMasterListViewHolder holder, int position) {
        //data.remove(position);
        //notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return 100;
        //return data.size();
    }
}
