package com.koleshop.appkoleshop.ui.seller.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.koleshop.appkoleshop.ui.seller.fragments.orders.CompleteOrdersFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.IncomingOrdersFragment;
import com.koleshop.appkoleshop.ui.seller.fragments.orders.PendingOrdersFragment;

/**
 * Created by Gundeep on 22/01/16.
 */
public class SellerOrderTabsAdapter extends FragmentStatePagerAdapter {

    public SellerOrderTabsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return IncomingOrdersFragment.newInstance();
            case 1:
                return PendingOrdersFragment.newInstance();
            case 2:
                return CompleteOrdersFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Incoming";
            case 1:
                return "Pending";
            case 2:
                return "Complete";
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

}
