package com.kolshop.kolshopmaterial.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.views.ViewProductEdit;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;

import org.w3c.dom.Text;

/**
 * Created by Gundeep on 28/11/15.
 */
public class ProductEditViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    TextView textViewNumber;
    ImageButton buttonOverFlow;
    ImageView imageView;
    Switch switchStock;
    EditText editTextQuantity, editTextPrice;
    TextInputLayout tilQuantity, tilPrice;
    int position;
    InventoryProduct inventoryProduct;
    InventoryProductVariety inventoryProductVariety;
    Context mContext;
    int viewType;
    ViewProductEdit viewProductEdit;
    long categoryId;

    private static String TAG = "ProductEditViewHolder";
    private static int VIEW_TYPE_BASIC_INFO = 0x01;
    private static int VIEW_TYPE_VARIETY = 0x02;

    public ProductEditViewHolder(View itemView, Context context, int viewType) {
        super(itemView);
        mContext = context;
        this.viewType = viewType;
        findViews(itemView);
    }

    private void findViews(View v) {
        if(viewType == VIEW_TYPE_VARIETY) {
            textViewNumber = (TextView) v.findViewById(R.id.tv_product_edit_number);
            buttonOverFlow = (ImageButton) v.findViewById(R.id.btn_product_edit_overflow);
            imageView = (ImageView) v.findViewById(R.id.iv_product_edit);
            switchStock = (Switch) v.findViewById(R.id.switch_product_edit);
            editTextQuantity = (EditText) v.findViewById(R.id.et_product_edit_variety_quantity);
            editTextPrice = (EditText) v.findViewById(R.id.et_product_edit_variety_price);
            tilQuantity = (TextInputLayout) v.findViewById(R.id.til_product_edit_variety_quantity);
            tilPrice = (TextInputLayout) v.findViewById(R.id.til_product_edit_variety_price);
        } else {
            viewProductEdit = new ViewProductEdit(mContext, (CardView) v);
        }
    }

    public void bindData(int position, InventoryProduct product, InventoryProductVariety variety, long categoryId) {
        this.position = position;
        this.categoryId = categoryId;
        if(viewType==VIEW_TYPE_BASIC_INFO) {
            inventoryProduct = product;
        } else {
            inventoryProductVariety = variety;
        }
        loadData();
    }

    private void loadData() {
        if(viewType == VIEW_TYPE_VARIETY) {
            textViewNumber.setText(position + 1 + ".");
            //todo picasso shit //imageView.setImageSource()
            switchStock.setSelected(inventoryProductVariety.getSelected());
            editTextQuantity.setText(inventoryProductVariety.getQuantity());
            editTextPrice.setText(inventoryProductVariety.getPrice() + "");
            buttonOverFlow.setOnClickListener(this);
        } else {
            viewProductEdit.setBasicInfo(inventoryProduct, categoryId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_product_edit_overflow:
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_product_variety_seller, popup.getMenu());
                popup.show();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete_product_variety:
                deleteProductVariety();
                return true;
            default:
                return false;
        }
    }

    private void deleteProductVariety() {
        Log.d(TAG, "Broadcasting delete variety at position " + position);
        Intent intent = new Intent(Constants.ACTION_DELETE_VARIETY);
        intent.putExtra("position", position);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
