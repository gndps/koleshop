package com.kolshop.kolshopmaterial.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 14/10/15.
 */
public class ProductMasterListViewHolder extends RecyclerView.ViewHolder {

    public TextView textViewTitleProductMasterList;
    public TextView textViewSubtitleProductMasterList;
    public CheckBox checkBoxProductMasterList;
    public CircleImageView circleImageViewProductMasterList;

    public ProductMasterListViewHolder(View view) {
        super(view);
        this.textViewTitleProductMasterList = (TextView) view.findViewById(R.id.textViewTitleProductMasterListItem);
        this.textViewSubtitleProductMasterList = (TextView) view.findViewById(R.id.textViewSubtitleProductMasterListItem);
        this.checkBoxProductMasterList = (CheckBox) view.findViewById(R.id.checkboxProductMasterListItem);
        this.circleImageViewProductMasterList = (CircleImageView) view.findViewById(R.id.circleImageViewProductMasterListItem);
    }
}
