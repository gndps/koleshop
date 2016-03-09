package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.ui.buyer.views.AddressView;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressRvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AddressView.AddressViewListener {

    AddressView addressView;
    Context mContext;
    BuyerAddress address;
    View view;
    AddressItemClickListener mListener;
    int position;


    public AddressRvViewHolder(View view, Context context , AddressItemClickListener addressItemClickListener) {
        super(view);
        this.view = view;
        this.mContext = context;
        this.mListener = addressItemClickListener;
        view.setOnClickListener(this);
    }

    public void bindAddressData(BuyerAddress address, boolean activateMaps, int position) {
        this.address = address;
        this.position = position;
        addressView = new AddressView(mContext, address, view, activateMaps, this);
    }

    @Override
    public void onClick(View v) {
        if(!(v instanceof Button) && !(v instanceof ImageButton)) {
            mListener.onAddressSelected(position);
        }
    }

    @Override
    public void deleteAddress(BuyerAddress address) {
        mListener.onAddressDeleted(position);
    }

    public interface AddressItemClickListener {
        void onAddressSelected(int position);
        void onAddressDeleted(int position);
    }



}
