package com.kolshop.kolshopmaterial.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Gundeep on 23/02/15.
 */
public class ProductItemViewHolder extends RecyclerView.ViewHolder
{

    public TextView name;
    public TextView description;
    public TextView flavors;
    public TextView sizes;
    public ImageView imageView;

    public ProductItemViewHolder(View itemView) {
        super(itemView);
        description = (TextView) itemView.findViewById((com.kolshop.kolshopmaterial.R.id.product_list_item_txt_product_description));
        flavors = (TextView) itemView.findViewById((com.kolshop.kolshopmaterial.R.id.product_list_item_txt_product_flavors));
        sizes = (TextView) itemView.findViewById((com.kolshop.kolshopmaterial.R.id.product_list_item_txt_product_sizes));
        imageView = (ImageView) itemView.findViewById((com.kolshop.kolshopmaterial.R.id.product_list_item_img_product_image));
    }

}
