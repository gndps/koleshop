package com.koleshop.appkoleshop.ui.seller.fragments.settings;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.koleshop.appkoleshop.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class HomeDeliveryFragment extends Fragment {

    @Bind(R.id.button_home_delivery_settings_yes)
    Button buttonYes;
    @Bind(R.id.button_home_delivery_settings_no)
    Button buttonNo;


    public HomeDeliveryFragment() {
        // Required empty public constructor
    }

    public static HomeDeliveryFragment newInstance() {
        HomeDeliveryFragment fragment = new HomeDeliveryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_delivery, container, false);
        ButterKnife.bind(this, view);
        GradientDrawable drawable = (GradientDrawable) buttonNo.getBackground();
        drawable.setStroke(0, getResources().getColor(R.color.delivery_settings_stroke_color_no));
        GradientDrawable drawable2 = (GradientDrawable) buttonYes.getBackground();
        drawable2.setStroke(0, getResources().getColor(R.color.delivery_settings_stroke_color_yes));
        return view;
    }

    public void yesButtonClicked() {
        GradientDrawable drawable = (GradientDrawable) buttonYes.getBackground();
        drawable.setStroke(4, getResources().getColor(R.color.delivery_settings_stroke_color_yes));
        GradientDrawable drawable2 = (GradientDrawable) buttonNo.getBackground();
        drawable2.setStroke(0, getResources().getColor(R.color.delivery_settings_stroke_color_no));
    }

    public void noButtonClicked() {
        GradientDrawable drawable = (GradientDrawable) buttonNo.getBackground();
        drawable.setStroke(4, getResources().getColor(R.color.delivery_settings_stroke_color_no));
        GradientDrawable drawable2 = (GradientDrawable) buttonYes.getBackground();
        drawable2.setStroke(0, getResources().getColor(R.color.delivery_settings_stroke_color_yes));
    }
}
