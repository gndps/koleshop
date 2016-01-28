package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;

import butterknife.Bind;

/**
 * Created by Gundeep on 26/01/16.
 */
public class OrderItemsListViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_vodis_name)
    TextView textViewName;
    @Bind(R.id.tv_vodis_count_x_price)
    TextView textViewCountAndPrice;
    @Bind(R.id.tv_vodis_price)
    TextView textViewPrice;
    @Bind(R.id.tv_vodis_count)
    TextView textViewCount;
    @Bind(R.id.button_vodis_minus)
    Button buttonMinus;
    @Bind(R.id.button_vodis_plus)
    Button buttonPlus;

    private Context mContext;

    public OrderItemsListViewHolder(View itemView, Context context) {
        super(itemView);
        this.mContext = context;
    }

    public void bindData() {

    }
}
