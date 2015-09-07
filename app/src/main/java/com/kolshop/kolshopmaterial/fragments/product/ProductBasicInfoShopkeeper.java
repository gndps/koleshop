package com.kolshop.kolshopmaterial.fragments.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.model.ProductCategory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ProductBasicInfoShopkeeper extends Fragment implements View.OnClickListener {

    private EditText productName, productDescription;
    private Switch productAvailableSwitch;
    Spinner spinnerCategory,spinnerSubcategory;
    private List<ProductCategory> parentCategories,subCategories;
    private TextView textViewSubcategory;
    private TextView textViewNumberOfVarieties;
    private ImageButton buttonAddVariety;
    private int numberOfVarieties;
    public ProductBasicInfoShopkeeper() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_info, container, false);
        findViewsByIds(v);
        initializeVariables();
        loadCategories();
        loadAndBindSpinners();
        return v;
    }

    private void initializeVariables()
    {
        numberOfVarieties = 1;
    }

    private void findViewsByIds(View productBasicFragment)
    {
        productName = (EditText) productBasicFragment.findViewById(R.id.edittext_product_name);
        productDescription = (EditText) productBasicFragment.findViewById(R.id.edittext_product_description);
        productAvailableSwitch = (Switch) productBasicFragment.findViewById(R.id.switch_product_available);
        spinnerCategory = (Spinner) productBasicFragment.findViewById(R.id.spinner_product_category);
        spinnerSubcategory = (Spinner) productBasicFragment.findViewById(R.id.spinner_product_subcategory);
        textViewSubcategory = (TextView) productBasicFragment.findViewById(R.id.textview_product_subcategory);
        textViewNumberOfVarieties = (TextView) productBasicFragment.findViewById(R.id.textview_product_number_of_varieties);
        buttonAddVariety = (ImageButton) productBasicFragment.findViewById(R.id.button_add_variety);
        buttonAddVariety.setOnClickListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setTestingInfo()
    {
        try {
            productName.setText("Maggi Noodles");
            productDescription.setText("Delecious Maggi Noodles and Maggi atta noodle");
            productAvailableSwitch.setChecked(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadCategories()
    {
        Realm realm = Realm.getInstance(getActivity());
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", 0);

        final RealmResults<ProductCategory> productCategories = query.findAll();

        parentCategories = new ArrayList<ProductCategory>();
        List<String> categoriesList = new ArrayList<>();

        for(ProductCategory pc:productCategories) {
            parentCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            categoriesList.add(pc.getName());
        }

        final Context context = getActivity();

        ArrayAdapter<String> parentCategoryArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categoriesList);
        spinnerCategory.setAdapter(parentCategoryArrayAdapter);
        spinnerCategory.setSelection(0);

        /*ArrayAdapter<ProductCategory> parentCategoriesAdapter = new ArrayAdapter<ProductCategory>(context, android.R.layout.simple_spinner_item, parentCategories){
            @Override
            public ProductCategory getItem(int position) {
                return productCategories.get(position);
            }

            @Override
            public int getCount(){
                return parentCategories.size();
            }

            @Override
            public long getItemId(int position){
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView)getActivity().getLayoutInflater().inflate(android.R.layout.simple_spinner_item, null);
                //TextView tv = new TextView(getActivity());
                //tv.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                //tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tv.setText(productCategories.get(position).getName());
                return tv;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                TextView tv = (TextView)getActivity().getLayoutInflater().inflate(android.R.layout.simple_spinner_dropdown_item, null);
                //tv.setLayoutParams(new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
                //tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tv.setText(productCategories.get(position).getName());
                return tv;
            }
        };
        parentCategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(parentCategoriesAdapter);
        spinnerCategory.setSelection(0);*/

        //---load product subcategories

    }

    private void loadAndBindSpinners()
    {
        spinnerSubcategory.setVisibility(View.GONE);
        textViewSubcategory.setVisibility(View.GONE);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int parentCategoryId = parentCategories.get(position).getId();
                loadSubcategoriesForId(parentCategoryId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private void loadSubcategoriesForId(int parentCategoryId)
    {
        Realm realm = Realm.getInstance(getActivity());
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", parentCategoryId);

        final RealmResults<ProductCategory> productCategories = query.findAll();

        subCategories= new ArrayList<ProductCategory>();
        List<String> subcategoriesList = new ArrayList<>();

        for(ProductCategory pc:productCategories) {
            subCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            subcategoriesList.add(pc.getName());
        }

        final Context context = getActivity();

        ArrayAdapter<String> subcategoryArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, subcategoriesList);
        spinnerSubcategory.setAdapter(subcategoryArrayAdapter);
        spinnerSubcategory.setSelection(0);

        if(subcategoriesList.size() > 0) {
            spinnerSubcategory.setVisibility(View.VISIBLE);
            textViewSubcategory.setVisibility(View.VISIBLE);
        }
        else
        {
            spinnerSubcategory.setVisibility(View.GONE);
            textViewSubcategory.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_variety:
                addVariety();
                break;
        }
    }

    public void addVariety()
    {
        numberOfVarieties++;
        Log.d("ProductInfoActivity", "Broadcasting empty variety added");
        Intent intent = new Intent(Constants.ACTION_ADD_VARIETY);
        intent.putExtra("numberOfVarieties", numberOfVarieties);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        textViewNumberOfVarieties.setText(numberOfVarieties + "");
    }

}
