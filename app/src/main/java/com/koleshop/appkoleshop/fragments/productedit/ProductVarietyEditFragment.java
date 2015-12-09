package com.koleshop.appkoleshop.fragments.productedit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProductVarietyEditFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ProductVarietyEditFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private EditProductVar variety;
    @Bind(R.id.met_product_edit_variety_quantity) MaterialEditText editTextQuantity;
    @Bind(R.id.met_product_edit_variety_price) MaterialEditText editTextPrice;
    @Bind(R.id.switch_product_edit) SwitchCompat switchStock;
    @Bind(R.id.tv_product_edit_number) TextView textViewNumber;
    @Bind(R.id.btn_product_edit_overflow) ImageButton buttonOverFlow;
    @Bind(R.id.iv_product_edit) ImageView imageView;
    @Bind(R.id.pb_image_upload_product_edit) ProgressBar progressBarImage;

    public ProductVarietyEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_variety_edit, container, false);
        ButterKnife.bind(this, v);
        Bundle bundle = getArguments();
        if(bundle!=null) {
            Parcelable parcelableVariety = bundle.getParcelable("variety");
            variety = Parcels.unwrap(parcelableVariety);
            loadTheDataIntoUi();
        }
        return v;
    }

    private void loadTheDataIntoUi() {
        editTextQuantity.setText(variety.getQuantity());
        editTextPrice.setText(variety.getPrice() + "");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            /*throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
