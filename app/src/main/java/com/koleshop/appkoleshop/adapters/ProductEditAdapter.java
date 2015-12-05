package com.koleshop.appkoleshop.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.viewholders.ProductEditViewHolder;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProduct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gundeep on 28/11/15.
 */
public class ProductEditAdapter extends RecyclerView.Adapter<ProductEditViewHolder> {

    private Context mContext;
    private InventoryProduct product;
    private static int VIEW_TYPE_BASIC_INFO = 0x01;
    private static int VIEW_TYPE_VARIETY = 0x02;
    private long categoryId;
    private Map<Integer, Bitmap> localBitmapMap;

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
            holder.bindData(0, product, null, categoryId, null);
        } else {
            int varietyPosition = position-1;
            Bitmap bitmap = null;
            if(localBitmapMap!=null && localBitmapMap.get(varietyPosition)!=null) {
                bitmap = localBitmapMap.get(varietyPosition);
            }
            holder.bindData(varietyPosition, null, product.getVarieties().get(varietyPosition), categoryId, bitmap);
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

    public void changeImage(int varietyPosition,  Bitmap bitmap) {
        if(localBitmapMap==null) {
            localBitmapMap = new HashMap<>();
        }
        localBitmapMap.put(varietyPosition, bitmap);
        int varietyPositionInRV = varietyPosition + 1;
        notifyItemChanged(varietyPositionInRV);
    }


}
