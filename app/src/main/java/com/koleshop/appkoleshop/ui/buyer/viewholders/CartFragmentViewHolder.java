package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.cart.ProductVarietyCount;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.ui.common.views.ItemCountView;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 04/03/16.
 */
public class CartFragmentViewHolder extends RecyclerView.ViewHolder implements ItemCountView.ItemCountListener {

    private final Context mContext;
    @Bind(R.id.civ_cart_item)
    CircleImageView circleImageView;
    @Bind(R.id.tv_name_cart_item)
    TextView textViewName;
    @Bind(R.id.tv_unit_price_cart_item)
    TextView textViewUnitPrice;
    @Bind(R.id.tv_total_cart_item)
    TextView textViewTotal;
    @Bind(R.id.rl_view_cart_item)
    RelativeLayout relativeLayoutItem;
    @Bind(R.id.icv_cart_item)
    ItemCountView itemCountView;

    private final SellerSettings sellerSettings;
    private ProductVarietyCount productVarietyCount;
    private int position;
    private CartFragmentAdapterViewHolderListener mListener;

    public CartFragmentViewHolder(View itemView, Context context, SellerSettings sellerSettings) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
        this.sellerSettings = sellerSettings;
    }

    public void bindData(ProductVarietyCount productVarietyCount, int position, CartFragmentAdapterViewHolderListener listener) {
        this.productVarietyCount = productVarietyCount;
        this.position = position;
        this.mListener = listener;
        if(!TextUtils.isEmpty(productVarietyCount.getProductVariety().getImageUrl())) {
            Picasso.with(mContext)
                    .load(productVarietyCount.getProductVariety().getImageUrl())
                    .centerCrop()
                    .fit()
                    .placeholder(R.drawable.ic_koleshop_grey_24dp)
                    .into(circleImageView);
        } else {
            Picasso.with(mContext)
                    .load(R.drawable.ic_koleshop_grey_24dp)
                    .centerCrop()
                    .fit()
                    .into(circleImageView);
        }

        itemCountView.setCount(productVarietyCount.getCartCount());
        itemCountView.setItemCountListener(this);
        textViewName.setText(productVarietyCount.getTitle() + "\n" + productVarietyCount.getProductVariety().getQuantity());
        float singleUnitPrice = productVarietyCount.getProductVariety().getPrice();
        textViewUnitPrice.setText(CommonUtils.getPriceStringFromFloat(singleUnitPrice, true));
        textViewTotal.setText(CommonUtils.getPriceStringFromFloat(singleUnitPrice*productVarietyCount.getCartCount(), true));
    }

    @Override
    public void onItemCountPlusClicked() {
        if(mListener!=null) {
            mListener.onItemCountPlusClicked(position);
        }
    }

    @Override
    public void onItemCountMinusClicked() {
        if(mListener!=null) {
            mListener.onItemCountMinusClicked(position);
        }
    }

    public interface CartFragmentAdapterViewHolderListener {
        void onItemCountPlusClicked(int position);
        void onItemCountMinusClicked(int position);
    }
}
