package com.kolshop.kolshopmaterial.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;

import com.kolshop.kolshopmaterial.fragments.product.InventoryProductFragment;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 29/10/15.
 */
public class InventoryCategoryViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<InventoryCategory> inventoryCategories = new ArrayList<>();

    public InventoryCategoryViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        InventoryProductFragment fragment = new InventoryProductFragment();
        Bundle bundl = new Bundle();
        bundl.putLong("categoryId", inventoryCategories.get(position).getId());
        fragment.setArguments(bundl);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return inventoryCategories.get(position).getName();
    }

    @Override
    public int getCount() {
        return inventoryCategories.size();
    }

    public void setInventoryCategories(List<InventoryCategory> list) {
        this.inventoryCategories = list;
    }

}
