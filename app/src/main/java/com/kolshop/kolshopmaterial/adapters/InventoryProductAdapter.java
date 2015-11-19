package com.kolshop.kolshopmaterial.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.extensions.InventoryProductClickListener;
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

    private static final int VIEW_TYPE_CONTENT_EXPANDED = 0x02;

    private LayoutInflater inflator;
    List<InventoryProduct> products;
    Context context;
    List<LineItem> mItems;
    int expandedItemPosition, expandedItemPositionOld;

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
        } else if(viewType == VIEW_TYPE_CONTENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_inventory_product, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_inventory_product_expanded, parent, false);
        }

        InventoryProductViewHolder holder = new InventoryProductViewHolder(view, viewType, context);
        Log.d(TAG, "on create view holder of type=" + viewType);
        return holder;
    }

    @Override
    public void onBindViewHolder(InventoryProductViewHolder holder, final int position) {

        LineItem item = mItems.get(position);
        final View itemView = holder.itemView;

        if(position == expandedItemPosition && position!=0) {
            holder.bindData(VIEW_TYPE_CONTENT_EXPANDED, null, item.product, item.checkBoxProgress);
        } else if(item.isHeader) {
            holder.bindData(VIEW_TYPE_HEADER, item.text, item.product, item.checkBoxProgress);
        } else {
            holder.bindData(VIEW_TYPE_CONTENT, item.text, item.product, item.checkBoxProgress);
        }
        holder.setClickListener(new InventoryProductClickListener() {
            @Override
            public void onItemClick(View v) {
                if(expandedItemPosition!=0 && expandedItemPosition == position) {
                    expandItemAtPosition(position);
                }
            }

            @Override
            public boolean onItemLongClick(View v) {
                //do nothing
                return false;
            }
        });

        holder.setCheckBoxOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRotatingShit(position);
            }
        });

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

    private void startRotatingShit(final int position) {
        mItems.get(position).checkBoxProgress = true;
        notifyItemChanged(position);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mItems.get(position).checkBoxProgress = false;
                mItems.get(position).product.setSelectedByUser(true);
                notifyItemChanged(position);
            }
        }, 2000);
    }

    private void expandItemAtPosition(int position) {
        if(!mItems.get(position).isHeader) {
            if(position == expandedItemPosition) {
                expandedItemPositionOld = expandedItemPosition;
                expandedItemPosition = 0;
            } else {
                expandedItemPositionOld = expandedItemPosition;
                expandedItemPosition = position;
            }
        }
            notifyItemChanged(expandedItemPosition);
            notifyItemChanged(expandedItemPositionOld);
    }

    @Override
    public int getItemViewType(int position) {
        if(position == expandedItemPosition && position!=0) {
            return VIEW_TYPE_CONTENT_EXPANDED;
        } else {
            return mItems.get(position).isHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_CONTENT;
        }
    }

    private static class LineItem {

        public int sectionFirstPosition;

        public boolean isHeader;

        public String text;

        public InventoryProduct product;

        public boolean checkBoxProgress;

        public LineItem(String text, boolean isHeader,
                        int sectionFirstPosition, InventoryProduct product) {
            this.isHeader = isHeader;
            this.text = text;
            this.sectionFirstPosition = sectionFirstPosition;
            this.product = product;
            checkBoxProgress = false;
        }
    }
}
