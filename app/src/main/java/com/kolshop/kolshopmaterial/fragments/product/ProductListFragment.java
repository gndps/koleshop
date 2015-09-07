package com.kolshop.kolshopmaterial.fragments.product;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.adapters.ProductListAdapter;
import com.kolshop.kolshopmaterial.extensions.KolClickListener;
import com.kolshop.kolshopmaterial.extensions.KolRecyclerTouchListener;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;


public class ProductListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductListAdapter productListAdapter;

    public ProductListFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_product_list, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.fragment_product_list_recycler_view);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .margin(getResources().getDimensionPixelSize(R.dimen.recycler_view_left_margin),
                        getResources().getDimensionPixelSize(R.dimen.recycler_view_right_margin))
                .build());
        productListAdapter = new ProductListAdapter(getActivity(), null);
        recyclerView.setAdapter(productListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new KolRecyclerTouchListener(getActivity(), recyclerView, new KolClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View v, int position) {
                Toast.makeText(getActivity(), "item clicked " + position, Toast.LENGTH_LONG).show();

            }
        }));
        return layout;
    }

}
