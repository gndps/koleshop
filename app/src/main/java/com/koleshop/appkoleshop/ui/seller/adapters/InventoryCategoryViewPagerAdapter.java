package com.koleshop.appkoleshop.ui.seller.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.ui.seller.fragments.product.InventoryProductFragment;
import com.koleshop.api.yolo.inventoryEndpoint.model.InventoryCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gundeep on 29/10/15.
 */
public class InventoryCategoryViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<ProductCategory> inventoryCategories = new ArrayList<>();
    private static final String TAG = "InventoryCatViewPager";
    private boolean myInventory = false;

    public InventoryCategoryViewPagerAdapter(FragmentManager fm, boolean myInventory) {
        super(fm);
        this.myInventory = myInventory;
    }

    @Override
    public Fragment getItem(int position) {
        InventoryProductFragment fragment = new InventoryProductFragment();
        Bundle bundl = new Bundle();
        long categoryId = inventoryCategories.get(position).getId();
        bundl.putLong("categoryId", categoryId);
        bundl.putBoolean("myInventory", myInventory);
        fragment.setArguments(bundl);
        Log.d(TAG, "view pager load page with categoryId = " + categoryId);
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

    public void setInventoryCategories(List<ProductCategory> list) {
        this.inventoryCategories = list;
    }

}
