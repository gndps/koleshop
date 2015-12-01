package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.model.realm.Brand;
import com.kolshop.kolshopmaterial.model.realm.ProductCategory;
import com.kolshop.server.yolo.inventoryEndpoint.model.InventoryProduct;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 30/11/15.
 */
public class ViewProductEdit extends CardView {

    TextInputLayout tilProductName, tilProductBrand;
    @Bind(R.id.met_product_edit_name) MaterialEditText editTextProductName;
    @Bind(R.id.mactv_product_edit_brand) MaterialAutoCompleteTextView editTextBrand;
    @Bind(R.id.spinner_product_edit_1) MaterialBetterSpinner spinnerCategory;
    @Bind(R.id.spinner_product_edit_2) MaterialBetterSpinner spinnerSubcategory;
    @Bind(R.id.tv_spinner_subcategory_hint) TextView textViewSubcategoryHint;
    //@Bind(R.id.tv_spinner_category_hint) TextView textViewCategoryHint;

    private InventoryProduct product;
    private Realm realm;
    private ArrayList<ProductCategory> parentCategories;
    private ArrayList<ProductCategory> subCategories;
    private Context mContext;
    private boolean categorySelected;
    private boolean subcategorySelected;
    private long categoryIdProduct;
    private List<String> brands;
    View v;

    private static String TAG = "ViewProductEdit";

    public ViewProductEdit(Context context, View v) {
        super(context);
        this.v = v;
        this.mContext = context;
    }

    public void setBasicInfo(InventoryProduct product, long categoryIdProduct) {

        this.product = product;
        this.categoryIdProduct = categoryIdProduct;
        setupViews();
        loadCategories();
        loadAndBindSpinners();
        selectCategory(categoryIdProduct);
    }

    private void setupViews() {
        ButterKnife.bind(this, v);
        editTextProductName.setText(product.getName());
        editTextBrand.setText(product.getBrand());
        //Creating the instance of ArrayAdapter containing list of language names
        loadBrands();
        ArrayAdapter<String> adapter = new ArrayAdapter<> (mContext,android.R.layout.select_dialog_item,brands);
        editTextBrand.setAdapter(adapter);
        editTextBrand.setThreshold(1);
        spinnerCategory.setFocusable(true);
        spinnerCategory.setFocusableInTouchMode(true);
        spinnerSubcategory.setFocusable(true);
        spinnerSubcategory.setFocusableInTouchMode(true);
    }

    private void loadBrands() {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Brand> brandRealmQuery = realm.where(Brand.class);

        final RealmResults<Brand> brandRealmResults = brandRealmQuery.findAllSorted("name");
        brands = new ArrayList<String>();

        for (Brand b : brandRealmResults) {
            brands.add(b.getName());
        }
    }

    public void loadCategories() {

        realm = Realm.getDefaultInstance();

        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);
        query.equalTo("parentCategoryId", 0l);

        final RealmResults<ProductCategory> productCategories = query.findAllSorted("name");
        parentCategories = new ArrayList<>();
        List<String> categoriesList = new ArrayList<>();

        for (ProductCategory pc : productCategories) {
            parentCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            categoriesList.add(pc.getName());
        }

        ArrayAdapter<String> parentCategoryArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, categoriesList);
        parentCategoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(parentCategoryArrayAdapter);
        spinnerCategory.setSelection(0);

    }

    private void loadAndBindSpinners() {
        spinnerSubcategory.setVisibility(View.GONE);
        textViewSubcategoryHint.setVisibility(GONE);

        spinnerCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position >= 0) {
                    categorySelected = true;
                    long parentCategoryId = parentCategories.get(position).getId();
                    categoryIdProduct = parentCategoryId;
                    loadSubcategoriesForId(parentCategoryId);
                } else {
                    categorySelected = false;
                    categoryIdProduct = 0l;
                    spinnerSubcategory.setVisibility(View.GONE);
                    textViewSubcategoryHint.setVisibility(GONE);
                }
            }

        });

        spinnerSubcategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    subcategorySelected = true;
                    categoryIdProduct = subCategories.get(position).getId();
                } else {
                    subcategorySelected = false;
                    categoryIdProduct = 0l;
                }
            }
        });
    }

    private void loadSubcategoriesForId(long parentCategoryId) {
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class);

        query.equalTo("parentCategoryId", parentCategoryId);

        final RealmResults<ProductCategory> productCategories = query.findAllSorted("name");

        subCategories = new ArrayList<>();
        List<String> subcategoriesList = new ArrayList<>();

        for (ProductCategory pc : productCategories) {
            subCategories.add(new ProductCategory(pc.getId(), pc.getName(), pc.getImageUrl(), pc.getParentCategoryId()));
            subcategoriesList.add(pc.getName());
        }

        if (subcategoriesList.size() > 0) {
            categoryIdProduct = 0l;
            ArrayAdapter<String> subcategoryArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, subcategoriesList);
            subcategoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //productCategoryId = subCategories.get(0).getId();
            spinnerSubcategory.setAdapter(subcategoryArrayAdapter);
            spinnerSubcategory.setSelection(0);
            spinnerSubcategory.setVisibility(View.VISIBLE);
            textViewSubcategoryHint.setVisibility(VISIBLE);
        } else {
            spinnerSubcategory.setVisibility(View.GONE);
            textViewSubcategoryHint.setVisibility(GONE);
        }
    }

    private void selectCategory(long categoryId) {
        ProductCategory productCategory = null;
        RealmQuery<ProductCategory> query = realm.where(ProductCategory.class)
                .equalTo("id", categoryId);
        productCategory = query.findFirst();

        query.equalTo("id", categoryId);

        if (productCategory!=null && productCategory.getParentCategoryId() > 0l) {
            query = realm.where(ProductCategory.class);
            query.equalTo("id", productCategory.getParentCategoryId());
            final ProductCategory parentProductCategory = query.findFirst();
            int selectedCategoryIndex = getIndexOfCategoryInList(parentCategories, productCategory.getParentCategoryId());
            spinnerCategory.setText(parentCategories.get(selectedCategoryIndex).getName());
            //todo debug here
            loadSubcategoriesForId(productCategory.getParentCategoryId());
            int selectedSubcategoryIndex = getIndexOfCategoryInList(subCategories, productCategory.getId());
            spinnerSubcategory.setText(subCategories.get(selectedSubcategoryIndex).getName());
        } else {
            int ind = getIndexOfCategoryInList(parentCategories, productCategory.getId());
            spinnerCategory.setText(parentCategories.get(ind).getName());
        }
    }

    private int getIndexOfCategoryInList(List<ProductCategory> cats, long categoryId) {
        int index = 0;
        for(ProductCategory pc : cats) {
            if(pc.getId() == categoryId) {
                return index;
            }
            index++;
        }
        return 0;
    }
}
