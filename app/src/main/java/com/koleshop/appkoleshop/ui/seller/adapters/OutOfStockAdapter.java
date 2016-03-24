package com.koleshop.appkoleshop.ui.seller.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.ProductSelectionRequest;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.services.CommonIntentService;
import com.koleshop.appkoleshop.ui.seller.viewholders.OutOfStockViewHolder;
import com.koleshop.appkoleshop.util.CommonUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 24/03/16.
 */
public class OutOfStockAdapter extends RecyclerView.Adapter<OutOfStockViewHolder> implements OutOfStockViewHolder.OutOfStockListener {

    List<Product> productsList;
    Context context;
    private LayoutInflater inflater;
    List<String> productSelectionRandomIdList = new ArrayList<>();
    List<Long> processingVarietyIdsList = new ArrayList<>();
    OutOfStockAdapterListener mListener;

    public OutOfStockAdapter(Context context, OutOfStockAdapterListener listener) {
        this.context = context;
        this.mListener = listener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public OutOfStockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(com.koleshop.appkoleshop.R.layout.item_rv_out_of_stock, parent, false);
        OutOfStockViewHolder holder = new OutOfStockViewHolder(view, context, this);
        return holder;
    }

    @Override
    public void onBindViewHolder(OutOfStockViewHolder holder, int position) {
        Product product = productsList.get(position);
        holder.bindData(product, processingVarietyIdsList.contains(product.getVarieties().get(0).getId()));
    }

    @Override
    public int getItemCount() {
        return productsList != null ? productsList.size() : 0;
    }

    public void setProductsList(List<Product> productsList) {
        this.productsList = productsList;
    }

    public List<String> getProductSelectionRandomIdList() {
        return productSelectionRandomIdList;
    }

    public void updateProductSelection(ProductSelectionRequest request, boolean success) {
        Long varietyId = request.getProductVarietyIds().get(0);
        productSelectionRandomIdList.remove(request.getRandomId());
        int position = getPositionOfVarietyId(varietyId);
        if(position<0) return;

        if (success) {
            //remove item with this variety id from products
            mListener.removeProductAtPosition(position);
            processingVarietyIdsList.remove(varietyId);
            notifyDataSetChanged();
        } else {
            //remove variety id from processing list
            if(processingVarietyIdsList.contains(varietyId)) {
                processingVarietyIdsList.remove(varietyId);
            }
            notifyItemChanged(position);
        }
    }

    private int getPositionOfVarietyId(Long varietyId) {
        if(productsList!=null) {
            int position = 0;
            for(Product product : productsList) {
                try {
                    if (product.getVarieties().get(0).getId().equals(varietyId)) {
                        return position;
                    }
                } catch (Exception e) {

                }
                position++;
            }
        }
        return -1;
    }

    @Override
    public void backInStockClicked(Long varietyId) {
        Intent intent = new Intent(context, CommonIntentService.class);
        intent.setAction(Constants.ACTION_UPDATE_INVENTORY_PRODUCT_SELECTION);
        ArrayList<Long> productSelection = new ArrayList<>();
        ProductSelectionRequest request = new ProductSelectionRequest();
        productSelection.add(varietyId);
        request.setWillSelectOnSuccess(true); //selection result - will select all the product varieties if the request is success
        request.setProductVarietyIds(productSelection); // pv ids
        String randomRequestId = CommonUtils.randomString(10);
        request.setRandomId(randomRequestId); //random id for request
        productSelectionRandomIdList.add(randomRequestId);
        processingVarietyIdsList.add(varietyId);
        Parcelable wrappedRequest = Parcels.wrap(request);
        intent.putExtra("request", wrappedRequest);
        intent.putExtra("stockMode", true);
        context.startService(intent);
        notifyItemChanged(getPositionOfVarietyId(varietyId));
    }

    public interface OutOfStockAdapterListener {
        void removeProductAtPosition(int position);
    }
}
