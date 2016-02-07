package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.ui.buyer.viewholders.AddressRvViewHolder;

import java.util.List;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressRvAdapter extends RecyclerView.Adapter<AddressRvViewHolder> {

    Context mContext;
    List<Address> addresses;
    Long selectedAddressId;
    boolean activateMaps;

    public AddressRvAdapter(Context context, List<Address> addresses, Long selectedAddressId) {
        mContext = context;
        this.addresses = addresses;
        this.selectedAddressId = selectedAddressId;
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
                Long selectedAddressId = addresses.get(position).getId();
                setSelectedAddressId(selectedAddressId);
            }
        });
        return addressRvViewHolder;
    }

    @Override
    public void onBindViewHolder(AddressRvViewHolder holder, int position) {
        Address address = addresses.get(position);
        boolean selected = false;
        if(address!=null && address.getId().equals(selectedAddressId)) {
            selected = true;
        }
        holder.bindAddressData(address, selected, activateMaps, position);
    }

    @Override
    public int getItemCount() {
        return addresses!=null?addresses.size():0;
    }

    public void setSelectedAddressId(Long selectedAddressId) {
        this.selectedAddressId = selectedAddressId;
        notifyDataSetChanged();
    }
}
