package com.koleshop.appkoleshop.fragments.productedit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.model.parcel.EditProduct;
import com.koleshop.appkoleshop.model.realm.Brand;
import com.koleshop.appkoleshop.model.realm.ProductCategory;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import fr.ganfra.materialspinner.MaterialSpinner;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class ProductEditFragment extends Fragment {

    @Bind(R.id.met_product_edit_name) MaterialEditText editTextProductName;
    @Bind(R.id.mactv_product_edit_brand) MaterialAutoCompleteTextView editTextBrand;
    @Bind(R.id.spinner_product_edit_1) MaterialBetterSpinner spinnerCategory;
    @Bind(R.id.spinner_product_edit_2) MaterialBetterSpinner spinnerSubcategory;

    private ArrayList<ProductCategory> parentCategories;
    private ArrayList<ProductCategory> subCategories;
    private Context mContext;
    private Realm realm;
    private List<String> brands;
    private long selectedParentCategoryId;
    private EditProduct product;
    private InteractionListener mListener;


    public ProductEditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContext = getActivity();
        View v = inflater.inflate(R.layout.fragment_product_edit, container, false);
        ButterKnife.bind(this, v);
        editTextBrand.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (product != null) {
                    product.setBrand(s.toString());
                }
            }
        });
        editTextProductName.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(product!=null) {
                    product.setName(s.toString());
                }
            }
        });
        loadBrands();
        loadCategories();
        loadAndBindSpinners();
        return v;
    }

    private void loadDataIntoViews() {
        editTextProductName.setText(product.getName());
        editTextBrand.setText(product.getBrand());
        spinnerCategory.setFocusable(true);
        spinnerCategory.setFocusableInTouchMode(true);
        spinnerSubcategory.setFocusable(true);
        spinnerSubcategory.setFocusableInTouchMode(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.product = mListener.getProductFromParent();
        loadDataIntoViews();
        selectCategory(product.getCategoryId());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InteractionListener) {
            mListener = (InteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProductEditFragmentInteractionListener");
        }
    }

    private void loadBrands() {
        realm = Realm.getDefaultInstance();
        RealmQuery<Brand> brandRealmQuery = realm.where(Brand.class);

        final RealmResults<Brand> brandRealmResults = brandRealmQuery.findAllSorted("name");
        brands = new ArrayList<String>();

        for (Brand b : brandRealmResults) {
            brands.add(b.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<> (mContext,android.R.layout.select_dialog_item,brands);
        editTextBrand.setAdapter(adapter);
        editTextBrand.setThreshold(1);
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

        spinnerCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position >= 0) {
                    //categorySelected = true;
                    long parentCategoryId = parentCategories.get(position).getId();
                    if (selectedParentCategoryId != parentCategoryId) {
                        product.setCategoryId(parentCategoryId);
                        selectedParentCategoryId = parentCategoryId;
                        loadSubcategoriesForId(parentCategoryId);
                    }
                } else {
                    //categorySelected = false;
                    product.setCategoryId(0l);
                    spinnerSubcategory.setVisibility(View.GONE);
                }
            }

        });

        spinnerSubcategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    //subcategorySelected = true;
                    product.setCategoryId(subCategories.get(position).getId());
                } else {
                    //subcategorySelected = false;
                    product.setCategoryId(0l);
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
            //product.setCategoryId(0l);
            ArrayAdapter<String> subcategoryArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_dropdown_item_1line, subcategoriesList);
            subcategoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            //productCategoryId = subCategories.get(0).getId();
            spinnerSubcategory.setAdapter(subcategoryArrayAdapter);
            spinnerSubcategory.setSelection(0);
            spinnerSubcategory.setVisibility(View.VISIBLE);
        } else {
            spinnerSubcategory.setVisibility(View.GONE);
        }
        spinnerSubcategory.setText(null);
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
            selectedParentCategoryId = productCategory.getParentCategoryId();
            int selectedCategoryIndex = getIndexOfCategoryInList(parentCategories, productCategory.getParentCategoryId());
            spinnerCategory.setText(parentCategories.get(selectedCategoryIndex).getName());
            //todo debug here
            loadSubcategoriesForId(selectedParentCategoryId);
            int selectedSubcategoryIndex = getIndexOfCategoryInList(subCategories, productCategory.getId());
            spinnerSubcategory.setText(subCategories.get(selectedSubcategoryIndex).getName());
        } else if(productCategory!=null) {
            int ind = getIndexOfCategoryInList(parentCategories, productCategory.getId());
            spinnerCategory.setSelection(ind);
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

    public void setProduct(EditProduct product) {
        this.product = product;
    }

    public interface InteractionListener {
        EditProduct getProductFromParent();
    }
}
