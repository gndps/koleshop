package com.koleshop.appkoleshop.ui.buyer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.EditProductVar;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.model.realm.ProductVariety;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;
import com.koleshop.appkoleshop.util.CartUtils;
import com.koleshop.appkoleshop.util.KoleshopUtils;

import org.parceler.Parcels;

public class ProductVarietyDetailsFragment extends Fragment implements View.OnClickListener {
    TextView textViewCartCount;
    String cardCountString;
    ViewFlipper viewFlipper;
    ProductVariety productVariety;
    ImageView starImage;
    Button addCart;
    SellerSettings sellerSettings;
    int count;

    public ProductVarietyDetailsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EditProductVar editProductVar = Parcels.unwrap(getArguments().getParcelable("variety"));
        sellerSettings = Parcels.unwrap(getArguments().getParcelable("sellerSettings"));
        productVariety = KoleshopUtils.getProductVarietyFromEditProductVar(editProductVar);
        View view = inflater.inflate(R.layout.fragment_product_variety_details_tile, container, false);
        TextView price = (TextView) view.findViewById(R.id.price);
        starImage = (ImageView) view.findViewById(R.id.starimage);
        starImage.setOnClickListener(this);
        addCart = (Button) view.findViewById(R.id.add_to_cart);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.viewflipper);
        count = CartUtils.getCountOfVariety(productVariety, sellerSettings);

        Button decement = (Button) view.findViewById(R.id.decrement);
        Button increment = (Button) view.findViewById(R.id.increment);
        textViewCartCount = (TextView) view.findViewById(R.id.textview_cart_count);


        addCart.setOnClickListener(this);
        increment.setOnClickListener(this);
        decement.setOnClickListener(this);

        cardCountString = Integer.toString(count);
        if (count == 0) {
            viewFlipper.setDisplayedChild(0);
        } else if(!productVariety.isLimitedStock())
        {
            viewFlipper.setDisplayedChild(2);
        }

        else {
            textViewCartCount.setText(cardCountString);
            viewFlipper.setDisplayedChild(1);
        }
        price.setText(String.valueOf(productVariety.getPrice()));
        if (KoleshopUtils.isStarred(productVariety)) {
            starImage.setImageDrawable(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.ic_star_golden));
        } else {
            starImage.setImageDrawable(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.ic_star_outline_black_24dp));
        }
    return view;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.increment:
                count++;
                cardCountString = Integer.toString(count);
                textViewCartCount.setText(cardCountString);
                break;
            case R.id.decrement:
                count--;
                cardCountString = Integer.toString(count);
                textViewCartCount.setText(cardCountString);
                if (count <= 0) {
                    viewFlipper.setDisplayedChild(0);
                }
                break;
            case R.id.starimage:

                if (KoleshopUtils.isStarred(productVariety)) {
                    starImage.setImageDrawable(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.ic_star_outline_black_24dp));
                } else {
                    starImage.setImageDrawable(AndroidCompatUtil.getDrawable(getActivity(), R.drawable.ic_star_golden));
                }
                break;
            case R.id.add_to_cart:
                if (count == 0) {
                    count = 1;
                    addingcart(count);
                } else {
                    addingcart(count);
                }
                break;
        }
    }

    private void addingcart(int count) {

        viewFlipper.setDisplayedChild(1);
        cardCountString = Integer.toString(count);
        textViewCartCount.setText(cardCountString);
    }
}
