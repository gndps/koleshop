package com.kolshop.kolshopmaterial.viewholders.inventory;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.network.volley.VolleyUtil;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 14/10/15.
 */
public class InventoryProductViewHolder extends RecyclerView.ViewHolder {

    private int IMAGE_SIZE_XXXDPI = 160;//px
    private static final int VIEW_TYPE_HEADER = 0x01;
    private static final int VIEW_TYPE_CONTENT = 0x00;

    public TextView textViewTitleProductMasterList; // will also be used as header text
    public TextView textViewSubtitleProductMasterList;
    public CheckBox checkBoxProductMasterList;
    public CircleImageView circleImageViewProductMasterList;
    private String imageUrl;
    private String uniqueTag;

    public InventoryProductViewHolder(View view, int viewType) {
        super(view);
        if(viewType == VIEW_TYPE_HEADER) {
            this.textViewTitleProductMasterList = (TextView) view.findViewById(R.id.tv_inventory_product_header);
        } else {
            this.textViewTitleProductMasterList = (TextView) view.findViewById(R.id.textViewTitleProductMasterListItem);
            this.textViewSubtitleProductMasterList = (TextView) view.findViewById(R.id.textViewSubtitleProductMasterListItem);
            this.checkBoxProductMasterList = (CheckBox) view.findViewById(R.id.checkboxProductMasterListItem);
            this.circleImageViewProductMasterList = (CircleImageView) view.findViewById(R.id.circleImageViewProductMasterListItem);
        }
    }

    public void setTitle(String title) {
        textViewTitleProductMasterList.setText(title);
    }

    public void setSubtitle(String subtitle) {
        textViewSubtitleProductMasterList.setText(subtitle);
    }

    public void setChecked(boolean checked) {
        checkBoxProductMasterList.setChecked(checked);
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
}
