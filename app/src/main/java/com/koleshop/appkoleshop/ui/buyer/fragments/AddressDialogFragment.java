package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.Address;
import com.koleshop.appkoleshop.model.realm.BuyerAddress;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.parceler.Parcels;

/**
 * Created by Gundeep on 04/02/16.
 */
public class AddressDialogFragment extends DialogFragment {

    BuyerAddress address;
    private final int REQUEST_CODE = 1;

    public static AddressDialogFragment create(BuyerAddress address) {
        AddressDialogFragment addressDialogFragment = new AddressDialogFragment();
        Bundle bundle = new Bundle();
        Parcelable addressParcelable = Parcels.wrap(address);
        bundle.putParcelable("address", addressParcelable);
        addressDialogFragment.setArguments(bundle);
        return addressDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.address = Parcels.unwrap(getArguments().getParcelable("address"));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.view_address_dialog, null);
        final MaterialEditText metNickName = (MaterialEditText) view.findViewById(R.id.met_address_dialog_nickname);
        final MaterialEditText metName = (MaterialEditText) view.findViewById(R.id.met_address_dialog_name);
        final MaterialEditText metAddress = (MaterialEditText) view.findViewById(R.id.met_address_dialog_address);
        final MaterialEditText metPhone = (MaterialEditText) view.findViewById(R.id.met_address_dialog_phone);
        final Button buttonDone = (Button) view.findViewById(R.id.button_vad_done);
        final Button buttonCancel = (Button) view.findViewById(R.id.button_vad_cancel);
        metNickName.setText(address.getNickname());
        metName.setText(address.getName());
        metAddress.setText(address.getAddress());
        if(address.getPhoneNumber()!=null) {
            metPhone.setText(address.getPhoneNumber() + "");
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);
        final Dialog dialog = builder.create();
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(metName.getText())) {
                    metName.setError("Please enter a name");
                } else if (TextUtils.isEmpty(metAddress.getText())) {
                    metAddress.setError("Please enter an address");
                } else if (TextUtils.isEmpty(metPhone.getText()) || metPhone.getText().length()<=5) {
                    metPhone.setError("Please enter a valid phone");
                } else {
                    try {
                        Intent data = new Intent();
                        data.putExtra("id", address.getId());
                        data.putExtra("nickname", metNickName.getText().toString());
                        data.putExtra("name", metName.getText().toString());
                        String addressString = metAddress.getText().toString();
                        data.putExtra("addressString", addressString);
                        data.putExtra("phoneNumber", Long.parseLong(metPhone.getText().toString()));
                        data.putExtra("gpsLong", address.getGpsLong());
                        data.putExtra("gpsLat", address.getGpsLat());
                        getTargetFragment().onActivityResult(REQUEST_CODE, Activity.RESULT_OK, data);
                        dialog.cancel();
                    } catch (Exception e) {
                        Log.d("AddressDialogFragment", "exception while setting address", e);
                    }
                }
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        return dialog;
    }

}
