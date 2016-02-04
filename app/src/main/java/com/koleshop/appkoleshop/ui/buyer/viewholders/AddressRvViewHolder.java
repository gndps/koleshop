package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.ui.buyer.views.AddressView;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressRvViewHolder extends RecyclerView.ViewHolder {

    AddressView addressView;
    Context mContext;
    Address address;
    View view;

    public AddressRvViewHolder(View view, Context context) {
        super(view);
        this.view = view;
        this.mContext = context;
    }

    public void bindAddressData(Address address, boolean currentlySelected, boolean activateMaps) {
        this.address = address;
        addressView = new AddressView(mContext, address, currentlySelected, view, activateMaps);
    }



}
