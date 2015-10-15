package com.kolshop.kolshopmaterial.fragments.product;

import android.app.Activity;
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
import com.kolshop.kolshopmaterial.adapters.ProductMasterListAdapter;
import com.kolshop.kolshopmaterial.extensions.KolClickListener;
import com.kolshop.kolshopmaterial.extensions.KolRecyclerTouchListener;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

public class ProductMasterListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductMasterListAdapter productMasterListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_product_master_list, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view_product_masterlist);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .margin(getResources().getDimensionPixelSize(R.dimen.recycler_view_left_margin),
                        getResources().getDimensionPixelSize(R.dimen.recycler_view_right_margin))
                .build());
        productMasterListAdapter = new ProductMasterListAdapter(getActivity(), null);
        recyclerView.setAdapter(productMasterListAdapter);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
