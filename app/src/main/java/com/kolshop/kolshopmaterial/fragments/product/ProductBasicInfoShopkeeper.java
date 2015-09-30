package com.kolshop.kolshopmaterial.fragments.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.model.ProductCategory;
import com.kolshop.kolshopmaterial.model.uipackage.BasicInfo;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ProductBasicInfoShopkeeper extends Fragment implements View.OnClickListener {

    private EditText editTextProductName, editTextDescription, editTextBrand;
    Spinner spinnerCategory,spinnerSubcategory;
    private List<ProductCategory> parentCategories,subCategories;
    private TextView textViewSubcategory;
    private TextView textViewNumberOfVarieties;
    private ImageButton buttonAddVariety;
    private int numberOfVarieties;
    private int brandId, productCategoryId;
    public ProductBasicInfoShopkeeper() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_info, container, false);
        findViewsByIds(v);
        loadCategories();
        loadAndBindSpinners();
        extractDataFromBundle();
        return v;
    }

    private void extractDataFromBundle()
    {
        Bundle basicInfoBundle = getArguments();
        String name = basicInfoBundle.getString("name", "");
        String brand = basicInfoBundle.getString("brand", "");
        int brandId = basicInfoBundle.getInt("brandId", 0);
        String description = basicInfoBundle.getString("description", "");
        productCategoryId = basicInfoBundle.getInt("categoryId", 0);
        numberOfVarieties = basicInfoBundle.getInt("numberOfVarieties", 1);

        //initialize User Interface
        editTextProductName.setText(name);
        editTextBrand.setText(brand);
        editTextDescription.setText(description);
        textViewNumberOfVarieties.setText(numberOfVarieties + "");
        selectCategory(productCategoryId);

    }

    private void findViewsByIds(View productBasicFragment)
    {
        editTextProductName = (EditText) productBasicFragment.findViewById(R.id.edittext_product_name);
        editTextDescription = (EditText) productBasicFragment.findViewById(R.id.edittext_product_description);
        editTextBrand = (EditText) productBasicFragment.findViewById(R.id.edittext_product_brand);
        spinnerCategory = (Spinner) productBasicFragment.findViewById(R.id.spinner_product_category);
        spinnerSubcategory = (Spinner) productBasicFragment.findViewById(R.id.spinner_product_subcategory);
        textViewSubcategory = (TextView) productBasicFragment.findViewById(R.id.textview_product_subcategory);
        textViewNumberOfVarieties = (TextView) productBasicFragment.findViewById(R.id.textview_product_number_of_varieties);
        buttonAddVariety = (ImageButton) productBasicFragment.findViewById(R.id.button_add_variety);
        buttonAddVariety.setOnClickListener(this);
        addEditTextHandlers();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void loadCategories()
    {
        Realm realm = Realm.getInstance(getActivity());
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", 0);

        final RealmResults<ProductCategory> productCategories = query.findAll();

        parentCategories = new ArrayList<>();
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
                productCategoryId = parentCategoryId;
                loadSubcategoriesForId(parentCategoryId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinnerSubcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                productCategoryId = subCategories.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadSubcategoriesForId(int parentCategoryId)
    {
        Realm realm = Realm.getInstance(getActivity());
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", parentCategoryId);

        final RealmResults<ProductCategory> productCategories = query.findAll();

        subCategories= new ArrayList<>();
        List<String> subcategoriesList = new ArrayList<>();

        for(ProductCategory pc:productCategories) {
            subCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            subcategoriesList.add(pc.getName());
        }

        final Context context = getActivity();

        ArrayAdapter<String> subcategoryArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, subcategoriesList);
        productCategoryId = subCategories.get(0).getId();
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

    private void selectCategory(int categoryId)
    {
        Realm realm = Realm.getInstance(getActivity());
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("id", categoryId);

        final ProductCategory productCategory = query.findFirst();
        if(productCategory.getParentCategoryId()>0)
        {
            query = realm.where(ProductCategory.class);
            query.equalTo("id", productCategory.getParentCategoryId());
            final ProductCategory parentProductCategory = query.findFirst();
            spinnerCategory.setSelection(parentCategories.indexOf(parentProductCategory));
            //todo debug here
            spinnerSubcategory.setSelection(subCategories.indexOf(productCategory));
        } else {
            spinnerCategory.setSelection(parentCategories.indexOf(productCategory));
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

    private void addEditTextHandlers()
    {
        //to clear focus from brand when done is pressed
        editTextBrand.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //Clear focus here from edittext
                    editTextBrand.clearFocus();
                }
                return false;
            }
        });

        //to hide keyboard on click outside edittexts
        editTextBrand.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        editTextProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        editTextDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String getName() {
        return editTextProductName.getText().toString();
    }

    private String getBrand() {
        return editTextBrand.getText().toString();
    }

    private int getBrandId() {
        return brandId;
    }

    private String getDescription() {
        return editTextDescription.getText().toString();
    }

    private int getProductCategoryId() {
        return productCategoryId;
    }

    public int getNumberOfVarieties()
    {
        return numberOfVarieties;
    }

    public BasicInfo getBasicInfo(boolean verifyForm)
    {
        if(verifyForm) {
            if(getName().isEmpty()) {
                putErrorOnEditText(editTextProductName, "Can't be empty");
            }
            if(getDescription().isEmpty()) {
                //todo do work here
                //put
            }
        }
        return null;
    }

    private void putErrorOnEditText(EditText editText, String errorMessage) {
        editText.setError(errorMessage);
    }

}
