package com.kolshop.kolshopmaterial.fragments.product;


import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.android.ProductVariety;
import com.kolshop.kolshopmaterial.model.android.VarietyAttribute;
import com.kolshop.kolshopmaterial.model.android.AttributeValue;
import com.kolshop.kolshopmaterial.views.ViewProductProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductVarietyDetailsFragment extends Fragment implements View.OnClickListener,PopupMenu.OnMenuItemClickListener
{

    private static String TAG = "ProductVarietyDetailFragment";
    private int index;
    private int numberOfVarieties;

    //UI variables
    TextView textViewHeading;
    EditText editTextProductName;
    EditText editTextStock;
    EditText editTextPrice;
    ImageButton buttonVarietyOptions;
    Switch switchStock;
    Spinner spinnerPrice;
    LinearLayout linearLayoutProperties;

    //ProductVariety Property variables
    private String id;
    private String name;
    private int limitedStock;
    private boolean valid;
    private String imageUrl;
    private Date dateAdded;
    private Date dateModified;
    RealmList<VarietyAttribute> listVarietyAttributes;
    RealmList<AttributeValue> listAttributeValues;
    Context context;
    List<MeasuringUnit> priceUnitList;
    int currentPriceUnitId;


    public ProductVarietyDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_product_variety_details, container, false);
        textViewHeading = (TextView) v.findViewById(R.id.variety_details_heading);
        linearLayoutProperties = (LinearLayout) v.findViewById(R.id.linear_layout_product_variety_properties_container);
        buttonVarietyOptions = (ImageButton) v.findViewById(R.id.button_variety_options);
        buttonVarietyOptions.setOnClickListener(this);
        editTextProductName = (EditText) v.findViewById(R.id.edittext_product_variety_name);
        editTextStock = (EditText) v.findViewById(R.id.edittext_stock);
        editTextPrice = (EditText) v.findViewById(R.id.edittext_product_price);
        switchStock = (Switch) v.findViewById(R.id.switch_product_available);
        spinnerPrice = (Spinner) v.findViewById(R.id.spinner_price_unit);
        context = v.getContext();


        addInitialProperty();

        loadPriceSpinner();
        bindSpinner();
        bindStockSwitch();
        addEditTextHandlers();

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
        productVariety.setListVarietyAttributes(getVarietyAttributesList());
        productVariety.setListAttributeValues(getAttributeValuesList());
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

    private RealmList<VarietyAttribute> getVarietyAttributesList()
    {
        listVarietyAttributes = new RealmList<>();
        for(int i=0; i<linearLayoutProperties.getChildCount(); i++)
        {
            ViewProductProperty viewProductProperty = (ViewProductProperty) linearLayoutProperties.getChildAt(i);
            VarietyAttribute va = viewProductProperty.getVarietyAttribute();
            if(!listVarietyAttributes.contains(va))
            {
                listVarietyAttributes.add(va);
            }
        }
        return listVarietyAttributes;
    }

    private RealmList<AttributeValue> getAttributeValuesList()
    {
        listAttributeValues = new RealmList<>();
        for(int i=0; i<linearLayoutProperties.getChildCount(); i++)
        {
            ViewProductProperty viewProductProperty = (ViewProductProperty) linearLayoutProperties.getChildAt(i);
            AttributeValue av = viewProductProperty.getAttributeValue();
            listAttributeValues.add(av);
        }
        return listAttributeValues;
    }

    private void loadPriceSpinner()
    {
        Realm realm = Realm.getInstance(context);
        RealmQuery<MeasuringUnit> query = realm.where(MeasuringUnit.class);
        query.beginsWith("unitDimensions", "price");
        RealmResults<MeasuringUnit> priceUnits = query.findAll();

        priceUnitList = new ArrayList<MeasuringUnit>();
        List<String> spinnerList = new ArrayList<>();


        for(MeasuringUnit mu:priceUnits) {
            priceUnitList.add(new MeasuringUnit(mu.getId(), mu.getUnitDimensions(), mu.getUnit(), mu.isBaseUnit(), mu.getConversionRate(), mu.getUnitFullName()));
            spinnerList.add(mu.getUnit());
        }

        ArrayAdapter<String> measuringUnitsArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerList);
        spinnerPrice.setAdapter(measuringUnitsArrayAdapter);
        spinnerPrice.setSelection(0);
    }

    private void bindSpinner()
    {
        spinnerPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int measuringUnitId = priceUnitList.get(position).getId();
                currentPriceUnitId = measuringUnitId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    private void bindStockSwitch()
    {
        switchStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked)
                {
                    editTextStock.setVisibility(View.GONE);
                }
                else
                {
                    //to be implemented later
                    //editTextStock.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setFocusChangeListenerToProperty(final ViewProductProperty viewProductProperty)
    {
        viewProductProperty.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(!hasFocus) {
                    int indexOfThisProperty = linearLayoutProperties.indexOfChild(viewProductProperty);
                    int lastPropertyIndex = linearLayoutProperties.getChildCount() - 1;
                    boolean thisIsTheLastProperty = indexOfThisProperty == lastPropertyIndex;
                    if (viewProductProperty.isEmptyProperty() && !thisIsTheLastProperty && !viewProductProperty.isAboutToDelete()) {
                        viewProductProperty.setAboutToDelete();
                        hideKeyboard(v);
                        linearLayoutProperties.removeViewAt(indexOfThisProperty);
                    }
                    else
                    {
                        hideKeyboard(v);
                    }
                    /*if (!viewProductProperty.isEmptyProperty() && thisIsTheLastProperty) {
                        addViewPropertyAtEnd();
                    }*/
                }
                else if(hasFocus && viewProductProperty.isAboutToDelete())
                {
                    hideKeyboard(v);
                }
            }
        });

        viewProductProperty.setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int indexOfThisProperty = linearLayoutProperties.indexOfChild(viewProductProperty);
                int lastPropertyIndex = linearLayoutProperties.getChildCount()-1;
                boolean thisIsTheLastProperty = indexOfThisProperty==lastPropertyIndex;
                /*if(viewProductProperty.isEmptyProperty() && !thisIsTheLastProperty)
                {
                    linearLayoutProperties.removeViewAt(indexOfThisProperty);
                }*/
                if(!viewProductProperty.isEmptyProperty() && thisIsTheLastProperty)
                {
                    addViewPropertyAtEnd();
                }
            }
        });

    }

    private void addInitialProperty()
    {
        if(linearLayoutProperties.getChildCount()==0)
        {
            addViewPropertyAtEnd();
        }
    }

    /*private void addEmptyPropertyAtEndIfNotAlreadyPresent()
    {
        boolean needToAddProperty = false;
        if(linearLayoutProperties.getChildCount()==0)
        {
            needToAddProperty = true;
        }
        else
        {
            List<Integer> removeIndices = new ArrayList<>();
            for(int i=0; i<linearLayoutProperties.getChildCount(); i++)
            {
                int lastPropertyIndex = linearLayoutProperties.getChildCount()-1;
                ViewProductProperty viewProductProperty = (ViewProductProperty) linearLayoutProperties.getChildAt(i);
                if(viewProductProperty.isEmptyProperty() && i!=lastPropertyIndex)
                {
                    removeIndices.add(i);
                }
                if(!viewProductProperty.isEmptyProperty() && i==lastPropertyIndex)
                {
                    needToAddProperty = true;
                }
            }

            if(!removeIndices.isEmpty())
            {
                ListIterator<Integer> li = removeIndices.listIterator(removeIndices.size());
                while(li.hasPrevious()) {
                    linearLayoutProperties.removeViewAt(li.previous());
                }
            }
        }

        if(needToAddProperty) {
            addViewPropertyAtEnd();
        }
    }*/

    private void addViewPropertyAtEnd()
    {
        ViewProductProperty v = new ViewProductProperty(getActivity());
        linearLayoutProperties.addView(v);
        setFocusChangeListenerToProperty(v);
    }

    private void addEditTextHandlers()
    {
        //to hide keyboard on click outside edittexts
        editTextProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        editTextStock.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
        editTextPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

}
