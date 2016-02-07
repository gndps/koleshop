package com.koleshop.appkoleshop.ui.buyer.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.ui.buyer.views.AddressView;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressRvViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    AddressView addressView;
    Context mContext;
    Address address;
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

    public void bindAddressData(Address address, boolean currentlySelected, boolean activateMaps, int position) {
        this.address = address;
        this.position = position;
        addressView = new AddressView(mContext, address, currentlySelected, view, activateMaps);
    }

    @Override
    public void onClick(View v) {
        if(!(v instanceof Button) && !(v instanceof ImageButton)) {
            mListener.onAddressSelected(position);
        }
    }

    public interface AddressItemClickListener {
        void onAddressSelected(int position);
    }



}
