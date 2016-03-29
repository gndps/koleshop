package com.koleshop.appkoleshop.ui.seller.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.realm.Product;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.zl.reik.dilatingdotsprogressbar.DilatingDotsProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 24/03/16.
 */
public class OutOfStockViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.civ_avatar_oos)
    CircleImageView imageView;
    @Bind(R.id.tv_oos_title)
    TextView textViewTitle;
    @Bind(R.id.tv_oos_subtitle)
    TextView textViewSubtitle;
    @Bind(R.id.button_back_in_stock)
    Button buttonBackInStock;
    @Bind(R.id.pb_status_oos)
    DilatingDotsProgressBar progressBar;

    private final Context mContext;
    private Product product;
    private boolean processing;
    private OutOfStockListener mListener;

    public OutOfStockViewHolder(View itemView, Context context, OutOfStockListener listener) {
        super(itemView);
        mContext = context;
        this.mListener = listener;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(Product product, boolean processing) {
        this.product = product;
        this.processing = processing;
        loadDataIntoUi();
    }

    private void loadDataIntoUi() {
        String titleText = product.getBrand() + " - " + product.getName();
        final ProductVariety variety = product.getVarieties().get(0);
        String subtitleText = variety.getQuantity() + " - " + CommonUtils.getPriceStringFromFloat(variety.getPrice(), true);
        textViewTitle.setText(titleText);
        textViewSubtitle.setText(subtitleText);
        final String imageUrl = KoleshopUtils.getSmallImageUrl(variety.getImageUrl());
        if(!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(mContext)
                    .load(imageUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .centerCrop().fit()
                    .placeholder(R.drawable.ic_koleshop_grey_24dp)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(mContext)
                                    .load(imageUrl)
                                    .centerCrop().fit()
                                    .placeholder(R.drawable.ic_koleshop_grey_24dp)
                                    .error(R.drawable.ic_koleshop_grey_24dp)
                                    .into(imageView);
                        }
                    });
        }
        if(!processing) {
            buttonBackInStock.setVisibility(View.VISIBLE);
            buttonBackInStock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.backInStockClicked(variety.getId());
                }
            });
            progressBar.setVisibility(View.GONE);
            progressBar.hide();
        } else {
            buttonBackInStock.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.show();
        }
    }

    public interface OutOfStockListener {
        void backInStockClicked(Long varietyId);
    }
}
