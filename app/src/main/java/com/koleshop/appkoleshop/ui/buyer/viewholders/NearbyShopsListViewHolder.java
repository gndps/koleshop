package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.constant.Constants;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CommonUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;
import com.koleshop.appkoleshop.util.PreferenceUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Gundeep on 02/02/16.
 */
public class NearbyShopsListViewHolder extends RecyclerView.ViewHolder {

    SellerSettings sellerInfo;
    Context mContext;
    private BuyerAddress buyerAddress;

    @Bind(R.id.iv_vnsi_avatar)
    CircleImageView imageViewAvatar;
    @Bind(R.id.iv_vnsi_online)
    ImageView imageViewOnline;
    @Bind(R.id.tv_vnsi_name)
    TextView textViewName;
    @Bind(R.id.tv_vnsi_timings)
    TextView textViewTimings;
    @Bind(R.id.tv_vnsi_distance)
    TextView textViewDistance;

    public NearbyShopsListViewHolder(View itemView, Context context, BuyerAddress buyerAddress) {
        super(itemView);
        this.mContext = context;
        this.buyerAddress = buyerAddress;
        ButterKnife.bind(this, itemView);
    }

    public void bindData(SellerSettings sellerInfo) {
        this.sellerInfo = sellerInfo;
        loadSellerInfoIntoUi();
    }

    private void loadSellerInfoIntoUi() {
        if (sellerInfo != null) {

            //01. SET SHOP NAME
            textViewName.setText(sellerInfo.getAddress().getName());


            //02. SET SHOP DISTANCE FROM USER
            float[] results = new float[3];
            Double userLat = buyerAddress.getGpsLat();
            Double userLong = buyerAddress.getGpsLong();
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
                final String thumbnailImageUrl = KoleshopUtils.getThumbnailImageUrl(sellerInfo.getImageUrl());
                final String sellerName = sellerInfo.getAddress().getName();
                Picasso.with(mContext)
                        .load(thumbnailImageUrl)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(KoleshopUtils.getTextDrawable(mContext, sellerName, 40, true))
                        .into(imageViewAvatar, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(mContext)
                                        .load(thumbnailImageUrl)
                                        .placeholder(KoleshopUtils.getTextDrawable(mContext, sellerName, 40, true))
                                        .error(KoleshopUtils.getTextDrawable(mContext, sellerName, 40, true))
                                        .into(imageViewAvatar);
                            }
                        });
            } else {
                imageViewAvatar.setImageDrawable(KoleshopUtils.getTextDrawable(mContext, sellerInfo.getAddress().getName(), 40, true));
            }


            //04. SET SHOP OPEN STATUS and DELIVERY/PICKUP INFO
            String deliveryPickupInfo;
            if (sellerInfo.isHomeDelivery()) {
                if (KoleshopUtils.doesSellerDeliverToBuyerLocation(sellerInfo)) {
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

    /*
     .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SellerInfo sellerInfo = getSellerInfo(mContext);
                if(sellerInfo!=null) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(sellerInfo.getName());
                }
                InventoryCategoryFragment myInventoryCategoryFragment = new InventoryCategoryFragment();
                Bundle bundleMyInventory = new Bundle();
                bundleMyInventory.putBoolean("myInventory", true);
                bundleMyInventory.putBoolean("customerView", true);
                myInventoryCategoryFragment.setArguments(bundleMyInventory);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, myInventoryCategoryFragment, "frag_my_shop").commit();
            }
        });*/

}
