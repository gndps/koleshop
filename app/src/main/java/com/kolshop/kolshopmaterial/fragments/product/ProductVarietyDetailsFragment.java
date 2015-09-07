package com.kolshop.kolshopmaterial.fragments.product;


import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.model.android.ProductVariety;
import com.kolshop.kolshopmaterial.model.android.ProductVarietyAttribute;
import com.kolshop.kolshopmaterial.views.ViewProductProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductVarietyDetailsFragment extends Fragment implements View.OnClickListener,PopupMenu.OnMenuItemClickListener
{

    private int index;
    private int numberOfVarieties;

    //UI variables
    TextView textViewHeading;
    EditText editTextProductName;
    EditText editTextStock;
    ImageButton buttonVarietyOptions;
    Switch switchStock;
    List<ViewProductProperty> productVarietyPropertyViewsList;

    //ProductVariety Property variables
    private String id;
    private String name;
    private int limitedStock;
    private boolean valid;
    private String imageUrl;
    private Date dateAdded;
    private Date dateModified;
    RealmList<ProductVarietyAttribute> listProductVarietyAttributes;


    public ProductVarietyDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_product_variety_details, container, false);
        textViewHeading = (TextView) v.findViewById(R.id.variety_details_heading);
        LinearLayout parentLinearLayout = (LinearLayout) v.findViewById(R.id.linear_layout_product_variety_properties_container);
        buttonVarietyOptions = (ImageButton) v.findViewById(R.id.button_variety_options);
        buttonVarietyOptions.setOnClickListener(this);
        editTextProductName = (EditText) v.findViewById(R.id.edittext_product_variety_name);
        editTextStock = (EditText) v.findViewById(R.id.edittext_stock);
        switchStock = (Switch) v.findViewById(R.id.switch_product_available);


        productVarietyPropertyViewsList = new ArrayList<ViewProductProperty>();

        ViewProductProperty v1 = new ViewProductProperty(getActivity(), "size");
        productVarietyPropertyViewsList.add(v1);
        parentLinearLayout.addView(v1);
        ViewProductProperty v2 = new ViewProductProperty(getActivity(), "price");
        productVarietyPropertyViewsList.add(v2);
        parentLinearLayout.addView(v2);

        initializeFragment();
        return v;
    }

    private void initializeFragment()
    {
        if(numberOfVarieties==1) {
            textViewHeading.setText("PRODUCT DETAILS");
        }
        else
        {
            textViewHeading.setText("VARIETY DETAILS " + (index+1));
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getNumberOfVarieties() {
        return numberOfVarieties;
    }

    public void setNumberOfVarieties(int numberOfVarieties) {
        this.numberOfVarieties = numberOfVarieties;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_variety_options:
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.setOnMenuItemClickListener(this);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_product_variety_seller, popup.getMenu());
                popup.show();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete_product_variety:
                deleteProductVariety();
                return true;
            default:
                return false;
        }
    }

    private void deleteProductVariety()
    {
        Log.d("ProductInfoActivity", "Broadcasting empty variety added");
        Intent intent = new Intent(Constants.ACTION_DELETE_VARIETY);
        intent.putExtra("varietyIndex", index);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    public void setProductVariety(ProductVariety productVariety)
    {
        id = productVariety.getId();
        name = productVariety.getName();
        limitedStock = productVariety.getLimitedStock();
        valid = productVariety.isValid();
        imageUrl = productVariety.getImageUrl();
        dateAdded = productVariety.getDateAdded();
        dateModified = productVariety.getDateModified();
    }

    public ProductVariety getProductVariety()
    {
        ProductVariety productVariety = new ProductVariety();
        productVariety.setId(id);
        productVariety.setName(getVarietyName());
        productVariety.setImageUrl(imageUrl);
        productVariety.setLimitedStock(getStockValue());
        productVariety.setDateAdded(dateAdded);
        productVariety.setDateModified(dateModified);
        productVariety.setListProductVarietyAttributes(getPropertiesList());
        return productVariety;
    }

    private String getVarietyName()
    {
        name = editTextProductName.getText().toString();
        return name;
    }

    private int getStockValue()
    {
        if(!switchStock.isEnabled())
        {
            limitedStock = 0;
        }
        else
        {
            limitedStock = -1;
            String stockString = editTextStock.getText().toString();
            if(stockString!=null && !stockString.isEmpty())
            {
                if(android.text.TextUtils.isDigitsOnly(stockString))
                {
                    limitedStock = Integer.parseInt(stockString);
                }
                else
                {
                    editTextStock.setError("This should be a number");
                }
            }
        }

        return limitedStock;
    }

    private RealmList<ProductVarietyAttribute> getPropertiesList()
    {
        return null;
    }

}
