package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.parcel.SellerSearchResults;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.ui.buyer.views.SingleProductInMultiSellerSearchView;
import com.koleshop.appkoleshop.ui.seller.views.ViewInventoryProductExpanded;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 07/02/16.
 */
public class MultiSellerSearchViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_title_msst)
    TextView textViewTitle;
    @Bind(R.id.tv_subtitle_msst)
    TextView textViewTimings;
    @Bind(R.id.tv_distance_msst)
    TextView textViewDistance;
    @Bind(R.id.civ_avatar_msst)
    CircleImageView imageViewAvatar;
    @Bind(R.id.button_more_results_msst)
    Button buttonShowMoreResults;
    @Bind(R.id.iv_status_msst)
    ImageView imageViewOnline;
    @Bind(R.id.ll_search_results_vmsst)
    LinearLayout linearLayoutSearchResults;

    Context mContext;
    int settingsPosition;
    BuyerAddress defaultUserAddress;


    private SellerSearchResults results;

    public MultiSellerSearchViewHolder(Context context, View itemView) {
        super(itemView);
        this.mContext = context;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(SellerSearchResults results, boolean expandProduct, int settingsPosition, int expandedProductPosition, BuyerAddress defaultUserAddress) {
        this.results = results;
        this.settingsPosition = settingsPosition;
        this.defaultUserAddress = defaultUserAddress;
        loadSellerInfoIntoUi();
        loadProductsIntoUi(expandProduct, expandedProductPosition);
        int size = results.getProducts().size();
        buttonShowMoreResults.setText(size + " ITEM" + (size > 1 ? "S" : ""));
        buttonShowMoreResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //replace current frag with single seller frag
                Intent openSingleSellerIntent = new Intent(Constants.ACTION_OPEN_SINGLE_SELLER_RESULTS);
                openSingleSellerIntent.putExtra("single_seller_position", getAdapterPosition());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(openSingleSellerIntent);
            }
        });
    }

    private void loadSellerInfoIntoUi() {
        if (results.getSellerSettings() != null) {

            SellerSettings sellerInfo = results.getSellerSettings();

            //01. SET SHOP NAME
            textViewTitle.setText(sellerInfo.getAddress().getName());


            //02. SET SHOP DISTANCE FROM USER
            float[] results = new float[3];
            Double userLat = defaultUserAddress.getGpsLat();
            Double userLong = defaultUserAddress.getGpsLong();
            Location.distanceBetween(userLat, userLong, sellerInfo.getAddress().getGpsLat(), sellerInfo.getAddress().getGpsLong(), results);
            float userDistanceFromShopInMeters = results[0];
            if (results != null && results.length > 0) {
                String distance = CommonUtils.getReadableDistanceFromMetres(userDistanceFromShopInMeters);
                textViewDistance.setText(distance);
            } else {
                textViewDistance.setText("-");
            }


            //03. SET SHOP IMAGE
            if (sellerInfo.getImageUrl() != null && !sellerInfo.getImageUrl().isEmpty()) {
                String thumbnailImageUrl = sellerInfo.getImageUrl().replace("/profile/", "/profile_thumb/");
                final String sellerName = sellerInfo.getAddress().getName();
                Picasso.with(mContext)
                        .load(thumbnailImageUrl)
                        .placeholder(KoleshopUtils.getTextDrawable(mContext, sellerName, 40, true))
                        .into(imageViewAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                imageViewAvatar.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, sellerName, 40, true));
                            }
                        });
            } else {
                imageViewAvatar.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, sellerInfo.getAddress().getName(), 40, true));
            }


            //04. SET SHOP OPEN STATUS and DELIVERY/PICKUP INFO
            String deliveryPickupInfo;
            if (sellerInfo.isHomeDelivery()) {
                if ((sellerInfo.getMaximumDeliveryDistance() + Constants.DELIVERY_DISTANCE_APPROXIMATION_ERROR) >= userDistanceFromShopInMeters) {
                    //home delivery is available to this location
                    deliveryPickupInfo = KoleshopUtils.getDeliveryTimeStringFromOpenAndCloseTime(sellerInfo.getDeliveryStartTime(), sellerInfo.getDeliveryEndTime());
                    if (KoleshopUtils.willSellerDeliverNow(sellerInfo.getDeliveryEndTime())) {
                        //seller will delivery to the user - GREEN ICON
                        imageViewOnline.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_green_dot_24dp));
                    } else {
                        //seller will not delivery to user at this time - ORANGE ICON
                        imageViewOnline.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_orange_dot_24dp));
                    }
                } else {
                    //seller don't delivery at this location - ORANGE ICON
                    deliveryPickupInfo = "No delivery to your location";
                    imageViewOnline.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_orange_dot_24dp));
                }
            } else {
                //only pickup available - ORANGE ICON
                deliveryPickupInfo = "Pickup Only";
                imageViewOnline.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_orange_dot_24dp));
            }

            textViewTimings.setText(deliveryPickupInfo);
            if (!sellerInfo.isShopOpen()) {
                //seller is offline - GREY ICON
                imageViewOnline.setImageDrawable(AndroidCompatUtil.getDrawable(mContext, R.drawable.ic_grey_dot_24dp));
            }

        }
    }

    private void loadProductsIntoUi(boolean expandProduct, int expandedProductPosition) {
        linearLayoutSearchResults.removeAllViews();
        SellerSettings sellerInfo = results.getSellerSettings();
        if (sellerInfo != null) {
            List<EditProduct> firstTwoProducts = new ArrayList<>();
            firstTwoProducts.add(results.getProducts().get(0));
            if (results.getProducts().size() > 1) {
                firstTwoProducts.add(results.getProducts().get(1));
            }

            int currentPosition = 0;
            for (EditProduct product : firstTwoProducts) {
                if (expandProduct && currentPosition == expandedProductPosition) {
                    ViewInventoryProductExpanded view = new ViewInventoryProductExpanded(mContext, null, true, sellerInfo);
                    view.setProduct(KoleshopUtils.getProductFromEditProduct(product), null, currentPosition);
                    view.setSettingsPositionInMultiSellerSearchViewMode(settingsPosition);
                    linearLayoutSearchResults.addView(view);
                } else {
                    SingleProductInMultiSellerSearchView view = new SingleProductInMultiSellerSearchView(mContext, product, sellerInfo, settingsPosition, currentPosition);
                    linearLayoutSearchResults.addView(view);
                }
                currentPosition++;
            }
        }
    }
}
