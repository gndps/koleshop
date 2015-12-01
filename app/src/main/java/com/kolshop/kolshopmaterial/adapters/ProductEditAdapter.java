package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.viewholders.ProductEditViewHolder;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;

/**
 * Created by Gundeep on 28/11/15.
 */
public class ProductEditAdapter extends RecyclerView.Adapter<ProductEditViewHolder> {

    private Context mContext;
    private InventoryProduct product;
    private static int VIEW_TYPE_BASIC_INFO = 0x01;
    private static int VIEW_TYPE_VARIETY = 0x02;
    private long categoryId;

    public ProductEditAdapter(Context context, InventoryProduct product, long categoryId) {
        this.mContext = context;
        this.product = product;
        this.categoryId = categoryId;
    }

    @Override
    public ProductEditViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;

        if(viewType == VIEW_TYPE_BASIC_INFO) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rv_product_edit, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_rv_product_variety_edit, parent, false);
        }

        ProductEditViewHolder productEditViewHolder = new ProductEditViewHolder(v, mContext, viewType);
        return productEditViewHolder;
    }

    @Override
    public void onBindViewHolder(ProductEditViewHolder holder, int position) {
        if(position == 0) {
            holder.bindData(0, product, null, categoryId);
        } else {
            int varietyPosition = position-1;
            holder.bindData(varietyPosition, null, product.getVarieties().get(varietyPosition), categoryId);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position==0?VIEW_TYPE_BASIC_INFO:VIEW_TYPE_VARIETY;
    }

    @Override
    public int getItemCount() {
        return product.getVarieties().size() + 1;
    }


}
