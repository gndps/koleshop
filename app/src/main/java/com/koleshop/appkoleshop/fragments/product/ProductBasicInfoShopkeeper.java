package com.koleshop.appkoleshop.fragments.product;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.common.constant.Constants;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.koleshop.appkoleshop.model.uipackage.BasicInfo;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ProductBasicInfoShopkeeper extends Fragment implements View.OnClickListener {

    private EditText editTextProductName, editTextBrand;
    //MaterialSpinner spinnerCategory, spinnerSubcategory;
    MaterialBetterSpinner spinnerCategory, spinnerSubcategory;
    private List<ProductCategory> parentCategories, subCategories;
    //private TextView textViewNumberOfVarieties;
    //private ImageButton buttonAddVariety;
    boolean categorySelected, subcategorySelected;
    private Long brandId, productCategoryId;
    Context mContext;
    Realm realm;

    public ProductBasicInfoShopkeeper() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_product_info, container, false);
        mContext = getActivity();
        realm = Realm.getDefaultInstance();
        findViewsByIds(v);
        loadCategories();
        loadAndBindSpinners();
        extractDataFromBundle();
        return v;
    }

    private void extractDataFromBundle() {
        Bundle basicInfoBundle = getArguments();
        if (basicInfoBundle != null) {
            String name = basicInfoBundle.getString("name", "");
            String brand = basicInfoBundle.getString("brand", "");
            int brandId = basicInfoBundle.getInt("brandId", 0);
            String description = basicInfoBundle.getString("description", "");
            productCategoryId = basicInfoBundle.getLong("categoryId", 0);

            //initialize User Interface
            editTextProductName.setText(name);
            editTextBrand.setText(brand);
            //editTextDescription.setText(description);
            updateNumberOfVarieties();
            selectCategory(productCategoryId);
        }

    }

    private void findViewsByIds(View productBasicFragment) {
        editTextProductName = (EditText) productBasicFragment.findViewById(R.id.edittext_product_name);
        //editTextDescription = (EditText) productBasicFragment.findViewById(R.id.edittext_product_description);
        editTextBrand = (EditText) productBasicFragment.findViewById(R.id.edittext_product_brand);
        //spinnerCategory = (MaterialSpinner) productBasicFragment.findViewById(R.id.spinner_product_category);
        spinnerCategory = (MaterialBetterSpinner) productBasicFragment.findViewById(R.id.spinner_product_category);
        spinnerCategory.setFocusable(true);
        spinnerCategory.setFocusableInTouchMode(true);
        //spinnerSubcategory = (MaterialSpinner) productBasicFragment.findViewById(R.id.spinner_product_subcategory);
        spinnerSubcategory = (MaterialBetterSpinner) productBasicFragment.findViewById(R.id.spinner_product_subcategory);
        spinnerSubcategory.setFocusable(true);
        spinnerSubcategory.setFocusableInTouchMode(true);
        //textViewNumberOfVarieties = (TextView) productBasicFragment.findViewById(R.id.textview_product_number_of_varieties);
        //buttonAddVariety = (ImageButton) productBasicFragment.findViewById(R.id.button_add_variety);
        //buttonAddVariety.setOnClickListener(this);
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

    public void updateNumberOfVarieties()
    {
        //textViewNumberOfVarieties.setText(KoleshopSingleton.getSharedInstance().getNumberOfVarieties() + "");
    }

    public void loadCategories() {

        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", 0);

        final RealmResults<ProductCategory> productCategories = query.findAll();
        parentCategories = new ArrayList<>();
        List<String> categoriesList = new ArrayList<>();

        for (ProductCategory pc : productCategories) {
            parentCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            categoriesList.add(pc.getName());
        }

        final Context context = getActivity();

        ArrayAdapter<String> parentCategoryArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, categoriesList);
        parentCategoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

    private void loadAndBindSpinners() {
        spinnerSubcategory.setVisibility(View.GONE);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(position>=0) {
                    categorySelected = true;
                    Long parentCategoryId = parentCategories.get(position).getId();
                    productCategoryId = parentCategoryId;
                    loadSubcategoriesForId(parentCategoryId);
                } else {
                    categorySelected = false;
                    productCategoryId = 0l;
                    spinnerSubcategory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        spinnerSubcategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>=0) {
                    subcategorySelected = true;
                    productCategoryId = subCategories.get(position).getId();
                } else {
                    subcategorySelected = false;
                    productCategoryId = 0l;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadSubcategoriesForId(Long parentCategoryId) {
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", parentCategoryId);

        final RealmResults<ProductCategory> productCategories = query.findAll();

        subCategories = new ArrayList<>();
        List<String> subcategoriesList = new ArrayList<>();

        for (ProductCategory pc : productCategories) {
            subCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            subcategoriesList.add(pc.getName());
        }

        final Context context = getActivity();

        if (subcategoriesList.size() > 0) {
            productCategoryId = 0l;
            ArrayAdapter<String> subcategoryArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, subcategoriesList);
            subcategoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //productCategoryId = subCategories.get(0).getId();
            spinnerSubcategory.setAdapter(subcategoryArrayAdapter);
            spinnerSubcategory.setSelection(0);
            spinnerSubcategory.setVisibility(View.VISIBLE);
        } else {
            spinnerSubcategory.setVisibility(View.GONE);
        }
    }

    private void selectCategory(Long categoryId) {
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("id", categoryId);

        final ProductCategory productCategory = query.findFirst();
        if (productCategory.getParentCategoryId() > 0) {
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
        /*switch (v.getId()) {
            case R.id.button_add_variety:
                addVariety();
                break;
        }*/
    }

    public void addVariety() {
        Intent intent = new Intent(Constants.ACTION_ADD_VARIETY);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void addEditTextHandlers() {
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
        /*editTextDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });*/
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private String getName() {
        return editTextProductName.getText().toString();
    }

    private String getBrand() {
        return editTextBrand.getText().toString();
    }

    private Long getBrandId() {
        return brandId;
    }

    private String getDescription() {
        return "";//editTextDescription.getText().toString();
    }

    private Long getProductCategoryId() {
        return productCategoryId;
    }

    @Nullable
    public BasicInfo getBasicInfo() {
        if (isFormCorrect()) {
            return createBasicInfo();
        } else {
            return null;
        }
    }

    private BasicInfo createBasicInfo() {
        BasicInfo basicInfo = new BasicInfo();
        basicInfo.setName(getName());
        basicInfo.setBrand(getBrand());
        basicInfo.setBrandId(getBrandId());
        basicInfo.setDescription(getDescription());
        basicInfo.setProductCategoryId(getProductCategoryId());
        return basicInfo;
    }


    public boolean isFormCorrect() {
        if (getName().isEmpty()) {
            putErrorOnEditText(editTextProductName, "Can't be empty");
            return false;
        }
        if (getProductCategoryId() == 0) {
            if(!categorySelected) {
                spinnerCategory.setError("Please select a product category");
                spinnerCategory.clearFocus();
                spinnerCategory.requestFocus();
            } else if(!subcategorySelected) {
                spinnerSubcategory.setError("Please select a product subcategory");
                spinnerSubcategory.clearFocus();
                spinnerSubcategory.requestFocus();
            }
            return false;
        }
        return true;
    }

    private void putErrorOnEditText(final EditText editText, String errorMessage) {
        editText.setError(errorMessage);
        editText.requestFocus();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(editText!=null)
                editText.setError(null);
            }
        }, 1500);
    }

    private void putErrorOnTextView(final TextView textView, Spinner spinner, String errorMessage) {
        textView.setError(errorMessage);
        spinner.requestFocus();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(textView!=null)
                    textView.setError(null);
            }
        }, 1500);
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }
}
