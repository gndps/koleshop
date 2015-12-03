package com.koleshop.appkoleshop.viewholders.inventory;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.koleshop.appkoleshop.network.volley.VolleyUtil;
import com.squareup.picasso.Picasso;

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
        this.textViewTitleInventoryCategory = (TextView) view.findViewById(com.koleshop.appkoleshop.R.id.textview_title_inventory_category);
        this.textViewSubtitleInventoryCategory = (TextView) view.findViewById(com.koleshop.appkoleshop.R.id.textview_subtitle_inventory_category);
        this.circleImageViewInventoryCategory = (CircleImageView) view.findViewById(com.koleshop.appkoleshop.R.id.circleiv_inventory_category);
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

    public void sendImageFetchRequest(Context context) {
        Picasso.with(context)
                .load(imageUrl)
                .into(circleImageViewInventoryCategory);
        /*ImageRequest request = new ImageRequest(imageUrl,
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
        VolleyUtil.getInstance().addToRequestQueue(request, uniqueTag);*/
    }

    public void cancelImageFetchRequest() {
        VolleyUtil.getInstance().cancelRequestsWithTag(uniqueTag);
    }

}
