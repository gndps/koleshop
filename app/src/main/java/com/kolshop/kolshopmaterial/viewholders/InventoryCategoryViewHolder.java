package com.kolshop.kolshopmaterial.viewholders;

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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 19/10/15.
 */
public class InventoryCategoryViewHolder extends RecyclerView.ViewHolder {

    private int IMAGE_SIZE_XXXDPI = 160;//px

    public TextView textViewTitleInventoryCategory;
    public TextView textViewSubtitleInventoryCategory;
    public CircleImageView circleImageViewInventoryCategory;
    private String imageUrl;
    private String uniqueTag;

    public InventoryCategoryViewHolder(View view) {
        super(view);
        this.textViewTitleInventoryCategory = (TextView) view.findViewById(R.id.textview_title_inventory_category);
        this.textViewSubtitleInventoryCategory = (TextView) view.findViewById(R.id.textview_subtitle_inventory_category);
        this.circleImageViewInventoryCategory = (CircleImageView) view.findViewById(R.id.circleiv_inventory_category);
    }

    public void setTitle(String title) {
        textViewTitleInventoryCategory.setText(title);
    }

    public void setSubtitle(String subtitle) {
        textViewSubtitleInventoryCategory.setText(subtitle);
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void sendImageFetchRequest() {
        ImageRequest request = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        circleImageViewInventoryCategory.setImageBitmap(bitmap);
                    }
                }, IMAGE_SIZE_XXXDPI, IMAGE_SIZE_XXXDPI, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        circleImageViewInventoryCategory.setImageResource(R.drawable.image_just_gray);
                    }
                });
        uniqueTag = CommonUtils.randomString(10);
        VolleyUtil.getInstance().addToRequestQueue(request, uniqueTag);
    }

    public void cancelImageFetchRequest() {
        VolleyUtil.getInstance().cancelRequestsWithTag(uniqueTag);
    }

}
