package com.koleshop.appkoleshop.ui.buyer.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.koleshop.appkoleshop.model.demo.SellerInfo;
import com.koleshop.appkoleshop.model.parcel.SellerSettings;
import com.koleshop.appkoleshop.ui.buyer.fragments.NearbyShopsListFragment;
import com.koleshop.appkoleshop.ui.buyer.fragments.NearbyShopsMapFragment;

import java.util.List;

/**
 * Created by Gundeep on 03/02/16.
 */
public class NearbyShopsFragmentPagerAdapter extends FragmentPagerAdapter {

    List<SellerSettings> sellers;
    NearbyShopsListFragment listFragment;
    NearbyShopsMapFragment mapFragment;

    public NearbyShopsFragmentPagerAdapter(FragmentManager fm, List<SellerSettings> sellers) {
        super(fm);
        this.sellers = sellers;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            listFragment = NearbyShopsListFragment.newInstance(sellers);
            return listFragment;
        } else if (position ==1) {
            mapFragment = NearbyShopsMapFragment.newInstance(sellers);
            return mapFragment;
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "LIST";
        } else {
            return "MAP";
        }
    }

    public void moreSellersFetched(List<SellerSettings> moreSellers) {
        listFragment.moreSellersFetched(moreSellers);
        mapFragment.moreSellersFetched(moreSellers);
    }

    public void couldNotLoadMoreSellers() {
        listFragment.couldNotLoadMoreSellers();
        mapFragment.couldNotLoadMoreSellers();
    }
}
