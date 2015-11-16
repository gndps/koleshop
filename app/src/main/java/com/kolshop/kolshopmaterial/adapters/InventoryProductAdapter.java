package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.viewholders.inventory.InventoryProductViewHolder;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 19/10/15.
 */
public class InventoryProductAdapter extends RecyclerView.Adapter<InventoryProductViewHolder> {

    private static final String TAG = "InventoryProductAdapter";

    private static final int VIEW_TYPE_HEADER = 0x01;

    private static final int VIEW_TYPE_CONTENT = 0x00;

    private LayoutInflater inflator;
    List<InventoryProduct> products;
    Context context;
    List<LineItem> mItems;

    public InventoryProductAdapter(Context context) {
        this.context = context;
        inflator = LayoutInflater.from(context);
    }

    public void setProductsList(List<InventoryProduct> productsList) {
        products = productsList;
        mItems = new ArrayList<>();

        //Insert headers into list of items.
        String lastHeader = "";
        int headerCount = 0;
        int sectionFirstPosition = 0;
        for (int i = 0; i < products.size(); i++) {
            String header = products.get(i).getBrand();
            if (!TextUtils.equals(lastHeader, header)) {
                // Insert new header view and update section data.
                sectionFirstPosition = i + headerCount;
                lastHeader = header;
                headerCount += 1;
                mItems.add(new LineItem(header, true, sectionFirstPosition, null));
            }
            mItems.add(new LineItem("", false, sectionFirstPosition, products.get(i)));
        }

        Log.d(TAG, "setting new product list with size " + productsList.size());

        //notifyDataSetChanged();
    }

    //Item viewholder methods

    @Override
    public InventoryProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_inventory_product_header, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_inventory_product, parent, false);
        }

        InventoryProductViewHolder holder = new InventoryProductViewHolder(view, viewType);
        Log.d(TAG, "on create view holder");
        return holder;
    }

    @Override
    public void onBindViewHolder(InventoryProductViewHolder holder, int position) {

        LineItem item = mItems.get(position);
        final View itemView = holder.itemView;

        holder.bindData(item.isHeader?VIEW_TYPE_HEADER:VIEW_TYPE_CONTENT, item.text, item.product);

        //sticky header shit
        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        // Overrides xml attrs, could use different layouts too.
        if (item.isHeader) {
            lp.headerDisplay = LayoutManager.LayoutParams.HEADER_STICKY | LayoutManager.LayoutParams.HEADER_INLINE;
            //Log.d("omgshit" , "" + (LayoutManager.LayoutParams.HEADER_STICKY | LayoutManager.LayoutParams.HEADER_INLINE));
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.headerEndMarginIsAuto = true;
            lp.headerStartMarginIsAuto = true;
        }
        lp.setSlm(LinearSLM.ID);
        //lp.setColumnWidth(96);
        lp.setFirstPosition(item.sectionFirstPosition);
        itemView.setLayoutParams(lp);
        Log.d(TAG, "on bind view holder position " + position);
    }

    @Override
    public int getItemCount() {
        if(mItems!=null) {
            //Log.d(TAG, "mItems.size()=" + mItems.size());
            return mItems.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
    }

    private static class LineItem {

        public int sectionFirstPosition;

        public boolean isHeader;

        public String text;

        public InventoryProduct product;

        public LineItem(String text, boolean isHeader,
                        int sectionFirstPosition, InventoryProduct product) {
            this.isHeader = isHeader;
            this.text = text;
            this.sectionFirstPosition = sectionFirstPosition;
            this.product = product;
        }
    }
}
