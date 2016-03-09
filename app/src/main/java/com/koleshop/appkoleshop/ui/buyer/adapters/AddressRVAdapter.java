package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.koleshop.appkoleshop.ui.buyer.viewholders.AddressRvViewHolder;
import com.koleshop.appkoleshop.util.RealmUtils;

import java.util.List;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressRvAdapter extends RecyclerView.Adapter<AddressRvViewHolder> {

    Context mContext;
    List<BuyerAddress> addresses;
    boolean activateMaps;
    AddressesRvAdapterListener mListener;

    public AddressRvAdapter(Context context, List<BuyerAddress> addresses, AddressesRvAdapterListener listener) {
        mContext = context;
        this.addresses = addresses;
        this.mListener = listener;
    }

    public void setActivateMaps(boolean activateMaps) {
        this.activateMaps = activateMaps;
    }

    @Override
    public AddressRvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_address_tile, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        AddressRvViewHolder addressRvViewHolder = new AddressRvViewHolder(view, mContext, new AddressRvViewHolder.AddressItemClickListener() {
            @Override
            public void onAddressSelected(int position) {
                BuyerAddress selectedAddress = addresses.get(position);
                selectedAddress.setDefaultAddress(true);
                RealmUtils.setAddressAsSelected(selectedAddress);
                addresses = RealmUtils.getBuyerAddresses();
                notifyDataSetChanged();
            }

            @Override
            public void onAddressDeleted(int position) {
                BuyerAddress deletedAddress = addresses.get(position);
                RealmUtils.deleteBuyerAddress(deletedAddress);
                mListener.refreshAddresses();
            }
        });
        return addressRvViewHolder;
    }

    @Override
    public void onBindViewHolder(AddressRvViewHolder holder, int position) {
        BuyerAddress address = addresses.get(position);
        holder.bindAddressData(address, activateMaps, position);
    }

    @Override
    public int getItemCount() {
        return addresses!=null?addresses.size():0;
    }

    public interface AddressesRvAdapterListener {
        void refreshAddresses();
    }

}
