package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.viewholders.InventoryProductViewHolder;
import com.kolshop.server.productEndpoint.model.ProductVariety;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;

import java.util.List;

/**
 * Created by Gundeep on 19/10/15.
 */
public class InventoryProductAdapter extends RecyclerView.Adapter<InventoryProductViewHolder> {

    private LayoutInflater inflator;
    List<InventoryProduct> products;
    Context context;

    public InventoryProductAdapter(Context context) {
        this.context = context;
        inflator = LayoutInflater.from(context);
    }

    @Override
    public InventoryProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflator.inflate(R.layout.item_inventory_product, parent, false);
        InventoryProductViewHolder holder = new InventoryProductViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(InventoryProductViewHolder holder, int position) {
        InventoryProduct product = products.get(position);
        holder.setTitle(product.getName());
        holder.setSubtitle(product.getDescription());
        holder.setChecked(product.getSelectedByUser());
        List<InventoryProductVariety> varieties = product.getVarieties();
        String imageUrl = varieties.get(0).getImageUrl();
        String smallSizeImageUrl = imageUrl.replaceFirst("small", "prod-image/286X224");
        holder.setImageUrl(smallSizeImageUrl);
        holder.sendImageFetchRequest(context);
    }

    @Override
    public int getItemCount() {
        if(products!=null) {
            return products.size();
        } else {
            return 0;
        }
    }

    public void setProductsList(List<InventoryProduct> productsList) {
        products = productsList;
        notifyDataSetChanged();
    }
}
