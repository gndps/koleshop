package com.koleshop.appkoleshop.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.views.ViewProductEdit;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.squareup.picasso.Picasso;

/**
 * Created by Gundeep on 28/11/15.
 */
public class ProductEditViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    TextView textViewNumber;
    ImageButton buttonOverFlow;
    ImageView imageView;
    SwitchCompat switchStock;
    EditText editTextQuantity, editTextPrice;
    TextInputLayout tilQuantity, tilPrice;
    int position;
    EditProduct editProduct;
    EditProductVar editProductVariety;
    Context mContext;
    int viewType;
    ViewProductEdit viewProductEdit;
    long categoryId;
    ProgressBar progressBarImage;

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
        if (viewType == VIEW_TYPE_VARIETY) {
            textViewNumber = (TextView) v.findViewById(com.koleshop.appkoleshop.R.id.tv_product_edit_number);
            buttonOverFlow = (ImageButton) v.findViewById(com.koleshop.appkoleshop.R.id.btn_product_edit_overflow);
            imageView = (ImageView) v.findViewById(com.koleshop.appkoleshop.R.id.iv_product_edit);
            switchStock = (SwitchCompat) v.findViewById(com.koleshop.appkoleshop.R.id.switch_product_edit);
            editTextQuantity = (EditText) v.findViewById(com.koleshop.appkoleshop.R.id.et_product_edit_variety_quantity);
            editTextPrice = (EditText) v.findViewById(com.koleshop.appkoleshop.R.id.et_product_edit_variety_price);
            tilQuantity = (TextInputLayout) v.findViewById(com.koleshop.appkoleshop.R.id.til_product_edit_variety_quantity);
            tilPrice = (TextInputLayout) v.findViewById(com.koleshop.appkoleshop.R.id.til_product_edit_variety_price);
            progressBarImage = (ProgressBar) v.findViewById(R.id.pb_image_upload_product_edit);
        } else {
            viewProductEdit = new ViewProductEdit(mContext, (CardView) v);
        }
    }

    public void bindData(final int position, EditProduct product, EditProductVar variety, long categoryId, Bitmap localBitmap) {
        //position is the index of variety in product.varieties...i.e. first variety will have a position 0
        this.position = position;
        this.categoryId = categoryId;
        if (viewType == VIEW_TYPE_BASIC_INFO) {
            editProduct = product;
        } else {
            editProductVariety = variety;
        }
        loadData(localBitmap);
    }

    private void loadData(Bitmap localBitmap) {
        if (viewType == VIEW_TYPE_VARIETY) {
            textViewNumber.setText(position + 1 + ".");
            Drawable drawableCamera;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableCamera = mContext.getDrawable(R.drawable.ic_photo_camera_grey600_48dp);
            } else {
                drawableCamera = mContext.getResources().getDrawable(R.drawable.ic_photo_camera_grey600_48dp);
            }

            if (localBitmap != null) {
                //load local image
                if (imageView != null) {
                    imageView.setImageBitmap(localBitmap);
                } else {
                    Toast.makeText(mContext, "image view is null", Toast.LENGTH_SHORT).show();
                }
            } else {

                Picasso.with(mContext)
                        .load(editProductVariety.getImageUrl())
                        .placeholder(drawableCamera)
                        .error(drawableCamera)
                        .into(imageView);
            }

            if(editProductVariety.isShowImageProcessing()) {
                progressBarImage.setVisibility(View.VISIBLE);
            } else {
                progressBarImage.setVisibility(View.GONE);
            }

            switchStock.setSelected(editProductVariety.getLimitedStock()>0);
            editTextQuantity.setText(editProductVariety.getQuantity());
            editTextPrice.setText(editProductVariety.getPrice() + "");
            buttonOverFlow.setOnClickListener(this);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Constants.ACTION_CAPTURE_PRODUCT_VARIETY_PICTURE);
                    intent.putExtra("tag", editProductVariety.getTag());
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            });
        } else {
            viewProductEdit.setBasicInfo(editProduct, categoryId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.koleshop.appkoleshop.R.id.btn_product_edit_overflow:
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(com.koleshop.appkoleshop.R.menu.menu_product_variety_seller, popup.getMenu());
                popup.show();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case com.koleshop.appkoleshop.R.id.item_delete_product_variety:
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
        Toast.makeText(mContext, "will delete variety at position " + position, Toast.LENGTH_SHORT).show();
    }
}
