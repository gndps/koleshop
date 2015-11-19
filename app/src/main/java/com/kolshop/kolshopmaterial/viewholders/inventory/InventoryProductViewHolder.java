package com.kolshop.kolshopmaterial.viewholders.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.extensions.InventoryProductClickListener;
import com.kolshop.kolshopmaterial.extensions.KolClickListener;
import com.kolshop.kolshopmaterial.network.volley.VolleyUtil;
import com.kolshop.kolshopmaterial.views.ViewInventoryProductExpanded;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProductVariety;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 14/10/15.
 */
public class InventoryProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private int IMAGE_SIZE_XXXDPI = 160;//px
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    public TextView textViewTitleProductMasterList; // will also be used as header text
    public TextView textViewSubtitleProductMasterList;
//    public CheckBox checkBoxProductMasterList;
    private ImageView imageViewCheckBox;
    private ProgressBar progressBarCheckBox;
    public CircleImageView circleImageViewProductMasterList;
    private String imageUrl;
    private String uniqueTag;
    private InventoryProductClickListener clickListener;
    private int viewType;
    private InventoryProduct product;
    private String title;
    private ViewInventoryProductExpanded viewInventoryProductExpanded;

    public InventoryProductViewHolder(View view, int viewType, Context context) {
        super(view);
        view.setOnClickListener(this);
        if(viewType == VIEW_TYPE_HEADER) {
            textViewTitleProductMasterList = (TextView) view.findViewById(R.id.tv_inventory_product_header);
        } else if(viewType == VIEW_TYPE_CONTENT) {
            textViewTitleProductMasterList = (TextView) view.findViewById(R.id.textViewTitleProductMasterListItem);
            textViewSubtitleProductMasterList = (TextView) view.findViewById(R.id.textViewSubtitleProductMasterListItem);
            //this.checkBoxProductMasterList = (CheckBox) view.findViewById(R.id.checkboxProductMasterListItem);
            circleImageViewProductMasterList = (CircleImageView) view.findViewById(R.id.circleImageViewProductMasterListItem);
            imageViewCheckBox = (ImageView) view.findViewById(R.id.iv_inventory_product);
            progressBarCheckBox = (ProgressBar) view.findViewById(R.id.pb_inventory_product);
        } else {
            viewInventoryProductExpanded = new ViewInventoryProductExpanded(context, view);
        }
    }

    public void setTitle(String title) {
        textViewTitleProductMasterList.setText(title);
    }

    public void setSubtitle(String subtitle) {
        textViewSubtitleProductMasterList.setText(subtitle);
    }

    public void setChecked(boolean checked) {
        if(checked) {
            imageViewCheckBox.setImageResource(R.drawable.ic_checkbox_true);
        } else {
            imageViewCheckBox.setImageResource(R.drawable.ic_checkbox_false_focused);
        }
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void sendImageFetchRequest(Context context) {
        Picasso.with(context)
                .load(imageUrl)
                .into(circleImageViewProductMasterList);
        /*ImageRequest request = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        circleImageViewProductMasterList.setImageBitmap(bitmap);
                    }
                }, IMAGE_SIZE_XXXDPI, IMAGE_SIZE_XXXDPI, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        circleImageViewProductMasterList.setImageResource(R.drawable.image_just_gray);
                    }
                });
        uniqueTag = CommonUtils.randomString(10);
        VolleyUtil.getInstance().addToRequestQueue(request, uniqueTag);*/
    }

    public void cancelImageFetchRequest() {
        VolleyUtil.getInstance().cancelRequestsWithTag(uniqueTag);
    }

    public void bindData(int viewType, String title, InventoryProduct product, boolean checkBoxProgress) {
        //bind view holder
        this.viewType = viewType;
        this.product = product;
        this.title = title;

        if(viewType == VIEW_TYPE_HEADER) {
            setTitle(title);
        } else if(viewType == VIEW_TYPE_CONTENT) {
            setTitle(product.getName());
            setSubtitle(product.getDescription());
            setChecked(product.getSelectedByUser());
            if(checkBoxProgress) {
                startCheckBoxProgress();
            } else {
                stopCheckBoxProgress();
            }
            List<InventoryProductVariety> varieties = product.getVarieties();
            if(varieties!=null) {
                String imageUrl = varieties.get(0).getImageUrl();
                String smallSizeImageUrl = imageUrl.replaceFirst("small", "prod-image/286X224");
                setImageUrl(smallSizeImageUrl);
                //todo launch this request when kolserver image server is working
                //holder.sendImageFetchRequest(context);
            }
        } else {
            viewInventoryProductExpanded.setProduct(product, checkBoxProgress);
        }
    }

    @Override
    public void onClick(View v) {
        clickListener.onItemClick(v);
    }

    public void setClickListener(InventoryProductClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCheckBoxOnClickListener(View.OnClickListener checkBoxClickListener) {
        if(imageViewCheckBox!=null) {
            imageViewCheckBox.setOnClickListener(checkBoxClickListener);
        }
    }

    public void startCheckBoxProgress() {
        imageViewCheckBox.setVisibility(View.GONE);
        progressBarCheckBox.setVisibility(View.VISIBLE);
    }

    private void stopCheckBoxProgress() {
        imageViewCheckBox.setVisibility(View.VISIBLE);
        progressBarCheckBox.setVisibility(View.GONE);
    }

    @Override
    public boolean onLongClick(View v) {
        return clickListener.onItemLongClick(v);
    }
}
