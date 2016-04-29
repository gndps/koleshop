package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.ui.seller.viewholders.InventoryCategoryViewHolder;

import java.util.List;

/**
 * Created by Gundeep on 14/10/15.
 */
public class InventoryCategoryAdapter extends RecyclerView.Adapter<InventoryCategoryViewHolder> {

    private LayoutInflater inflater;
    List<ProductCategory> categories;
    Context context;
    OnItemClickListener mListener;


    public InventoryCategoryAdapter(Context context, List<ProductCategory> categories, OnItemClickListener onItemClickListener)
    {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.categories = categories; //null
        this.mListener = onItemClickListener;

    }

    @Override
    public InventoryCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(com.koleshop.appkoleshop.R.layout.item_rv_inventory_category, parent, false);
        InventoryCategoryViewHolder holder = new InventoryCategoryViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InventoryCategoryViewHolder holder, int position) {
        if(categories!=null) {
            ProductCategory productCategory = categories.get(position);
            //holder.setTitle(position+1 + ". " + productCategory.getName()); ...this line adds the item number in front of category name
            holder.setTitle(productCategory.getName());
            holder.setSubtitle(productCategory.getDesc());
            holder.setImageUrl(productCategory.getImageUrl());
            holder.sendImageFetchRequest(context);
            holder.setProductCategory(productCategory);
            holder.setOnItemClickListener(mListener);
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
    
    public void setData(List<ProductCategory> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public Long getInventoryCategoryId(int position) {
        if(categories!=null && categories.size()>0) {
            ProductCategory cat = categories.get(position);
            if(cat!=null) {
                return cat.getId();
            } else {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    public String getInventoryCategoryName(int position) {
        if(categories!=null) {
            ProductCategory cat = categories.get(position);
            if(cat!=null) {
                return cat.getName();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    /*@Override
    public void onViewRecycled(InventoryCategoryViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cancelImageFetchRequest();
    }*/

    public interface OnItemClickListener {
        void onItemClick(ProductCategory item);
    }

}
