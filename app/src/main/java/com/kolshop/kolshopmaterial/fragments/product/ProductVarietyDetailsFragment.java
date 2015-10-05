package com.kolshop.kolshopmaterial.fragments.product;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.common.util.ProductUtil;
import com.kolshop.kolshopmaterial.helper.VarietyAttributePool;
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.android.ProductVariety;
import com.kolshop.kolshopmaterial.model.android.VarietyAttribute;
import com.kolshop.kolshopmaterial.model.android.AttributeValue;
import com.kolshop.kolshopmaterial.model.android.extended.ProductVarietyProperty;
import com.kolshop.kolshopmaterial.views.ViewProductProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private int sortOrder;
    private BroadcastReceiver mMessageReceiver;

    //UI variables
    TextView textViewHeading;
    EditText editTextProductName;
    EditText editTextStock;
    EditText editTextPrice;
    ImageButton buttonVarietyOptions;
    Switch switchStock;
    Spinner spinnerPrice;
    LinearLayout linearLayoutProperties;

    //ProductVariety variables
    private String id;
    private String name;
    private int limitedStock;
    private boolean valid;
    private ProductVarietyProperty price;
    //todo handle image
    private String imageUrl;
    private Date dateAdded;
    private Date dateModified;
    Context context;
    List<MeasuringUnit> priceUnitList;
    private List<VarietyAttribute> listVarietyAttribute;
    private List<AttributeValue> listAttributeValue;

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

        loadPriceSpinner();
        spinnerPrice.setVisibility(View.GONE);//bindSpinner();
        bindStockSwitch();
        addEditTextHandlers();
        initializeFragment();
        initializeBroadcastReceivers();
        return v;
    }

    private void initializeBroadcastReceivers()
    {
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Constants.ACTION_PROPERTY_MODIFIED)) {
                    dateModified = new Date();
                }
            }
        };
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.ACTION_PROPERTY_MODIFIED));
    }

    private void initializeFragment()
    {
        valid = true;

        Bundle bundle=getArguments();
        if(bundle!=null && bundle.getString("id")!=null && !bundle.getString("id").isEmpty()) {
            id = bundle.getString("id");
            name = bundle.getString("name");
            limitedStock = bundle.getInt("stock");
            sortOrder = bundle.getInt("sortOrder");
            imageUrl = bundle.getString("imageUrl")!=null?bundle.getString("imageUrl"):"";
            dateAdded = (bundle.getLong("dateAdded")!=0)?(new Date(bundle.getLong("dateAdded"))):(new Date());
            dateModified = (bundle.getLong("dateModified")!=0)?(new Date(bundle.getLong("dateModified"))):(new Date());
            listAttributeValue = (ArrayList<AttributeValue>) bundle.getSerializable("listAttributeValue");
            listVarietyAttribute = (ArrayList<VarietyAttribute>) bundle.getSerializable("listVarietyAttribute");
            if(listVarietyAttribute!=null && listAttributeValue!=null) {
                price = ProductUtil.getProductVarietyPropertyFromList(listVarietyAttribute, listAttributeValue, Constants.PRICE_PROPERTY_NAME);
            } else {
                price  = ProductUtil.getDefaultPriceProductVarietyProperty(id);
            }
        }
        else {
            id = "random" + CommonUtils.randomString(8);
            name = "";
            limitedStock = -1; //-1 = unlimited
            imageUrl = "";
            dateAdded = new Date();
            dateModified = new Date();
            listAttributeValue = new ArrayList<>();
            listVarietyAttribute = new ArrayList<>();
            price = ProductUtil.getDefaultPriceProductVarietyProperty(id);
        }

        if(sortOrder==0) {
            textViewHeading.setText("PRODUCT DETAILS");
        } else {
            textViewHeading.setText("VARIETY DETAILS " + (sortOrder+1));
        }

        //fill up the UI details of this fragment

        editTextProductName.setText(name);
        editTextPrice.setText(price.getAttributeValue().getValue());
        if(limitedStock<=-1) {
            switchStock.setChecked(true);
        } else if(limitedStock >= 0) {
            switchStock.setChecked(false);
        }
        spinnerPrice.setSelection(getPriceSpinnerSelectionIndexFromPriceMeasuringUnitId());
        if(listAttributeValue.size()>0) {
            for(AttributeValue attributeValue : listAttributeValue) {
                VarietyAttribute varietyAttribute = ProductUtil.getVarietyAttributeFromList(listVarietyAttribute, attributeValue.getProductVarietyAttributeId());
                if(!varietyAttribute.getName().equalsIgnoreCase(Constants.PRICE_PROPERTY_NAME)) {
                    addNonEmptyProductProperty(varietyAttribute, attributeValue);
                }
            }
        }
        addEmptyProductProperty();

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
        Log.d("ProductInfoActivity", "Broadcasting variety deleted");
        Intent intent = new Intent(Constants.ACTION_DELETE_VARIETY);
        intent.putExtra("fragmentTag", getTag());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    public ProductVariety getProductVariety()
    {
        ProductVariety productVariety = new ProductVariety();
        productVariety.setId(id);
        productVariety.setName(getVarietyName());
        productVariety.setImageUrl(imageUrl);
        productVariety.setValid(valid);
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
            limitedStock = -1; //-1 if stock is unlimited
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
        RealmList<VarietyAttribute> realmListVarietyAttributes = new RealmList<>();
        VarietyAttributePool vaPool = VarietyAttributePool.getInstance();
        for(int i=0; i<linearLayoutProperties.getChildCount(); i++)
        {
            ViewProductProperty viewProductProperty = (ViewProductProperty) linearLayoutProperties.getChildAt(i);
            if(!viewProductProperty.isEmptyProperty()) {
                VarietyAttribute loopVa = viewProductProperty.getVarietyAttribute();
                VarietyAttribute poolVa = vaPool.getSimilarVarietyAttribute(loopVa);
                if(poolVa==null) {
                    //check in realm
                    VarietyAttribute realmVa = getRealmVa(loopVa);
                    if(realmVa == null) {
                        vaPool.addVarietyAttribute(loopVa);
                        realmListVarietyAttributes.add(loopVa);
                    } else {
                        vaPool.addVarietyAttribute(realmVa);
                        realmListVarietyAttributes.add(realmVa);
                        adjustVaIdsInAttributeValues(loopVa, realmVa);
                    }
                } else {
                    //set this existing va id in attribute values
                    adjustVaIdsInAttributeValues(loopVa, poolVa);
                }
            }
        }
        realmListVarietyAttributes.add(price.getVarietyAttribute());
        return realmListVarietyAttributes;
    }

    private void adjustVaIdsInAttributeValues(VarietyAttribute oldVa, VarietyAttribute newVa) {
        for(int j=0; j<linearLayoutProperties.getChildCount(); j++)
        {
            ViewProductProperty vpp = (ViewProductProperty) linearLayoutProperties.getChildAt(j);
            if(!vpp.isEmptyProperty()) {
                AttributeValue av = vpp.getAttributeValue();
                if(av.getProductVarietyAttributeId().equalsIgnoreCase(oldVa.getId()))
                {
                    vpp.setProductVarietyAttributeId(newVa.getId());
                }
            }
        }
    }

    private VarietyAttribute getRealmVa(VarietyAttribute va) {
        Realm realm = CommonUtils.getRealmInstance(context);
        RealmQuery<VarietyAttribute> realmQuery = realm.where(VarietyAttribute.class)
                .equalTo("name", va.getName())
                .equalTo("measuringUnitId", va.getMeasuringUnitId());
        VarietyAttribute realmVa = realmQuery.findFirst();
        return realmVa;
    }

    private RealmList<AttributeValue> getAttributeValuesList()
    {
        RealmList<AttributeValue> realmListAttributeValues = new RealmList<>();
        for(int i=0; i<linearLayoutProperties.getChildCount(); i++)
        {
            ViewProductProperty viewProductProperty = (ViewProductProperty) linearLayoutProperties.getChildAt(i);
            if(!viewProductProperty.isEmptyProperty()) {
                AttributeValue av = viewProductProperty.getAttributeValue();
                av.setSortOrder(i);
                realmListAttributeValues.add(av);
            }
        }
        price.getAttributeValue().setValue(editTextPrice.getText().toString());
        realmListAttributeValues.add(price.getAttributeValue());
        return realmListAttributeValues;
    }

    private void loadPriceSpinner()
    {
        Realm realm = Realm.getInstance(context);
        RealmQuery<MeasuringUnit> query = realm.where(MeasuringUnit.class);
        query.beginsWith("unitDimensions", "price");
        RealmResults<MeasuringUnit> priceUnits = query.findAll();

        priceUnitList = new ArrayList<>();
        List<String> spinnerList = new ArrayList<>();


        for(MeasuringUnit mu:priceUnits) {
            priceUnitList.add(new MeasuringUnit(mu.getId(), mu.getUnitDimensions(), mu.getUnit(), mu.isBaseUnit(), mu.getConversionRate(), mu.getUnitFullName()));
            spinnerList.add(mu.getUnit());
        }

        ArrayAdapter<String> measuringUnitsArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerList);
        measuringUnitsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrice.setAdapter(measuringUnitsArrayAdapter);
        spinnerPrice.setSelection(0);
    }

    private void bindSpinner()
    {
        spinnerPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int measuringUnitId = priceUnitList.get(position).getId();
                price.getVarietyAttribute().setMeasuringUnitId(measuringUnitId);
                dateModified = new Date();
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
                dateModified = new Date();
                /*if (!isChecked) {
                    editTextStock.setVisibility(View.GONE);
                } else {
                    //to be implemented in later versions of kolshop
                    //editTextStock.setVisibility(View.VISIBLE);
                }*/
            }
        });
    }

    private void setFocusAndTextChangeListenersToProperty(final ViewProductProperty viewProductProperty)
    {
        viewProductProperty.setPropertyOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(!hasFocus) {
                    int indexOfThisProperty = linearLayoutProperties.indexOfChild(viewProductProperty);
                    int lastPropertyIndex = linearLayoutProperties.getChildCount() - 1;
                    boolean thisIsTheLastProperty = indexOfThisProperty == lastPropertyIndex;
                    if (viewProductProperty.isEmptyProperty() && !thisIsTheLastProperty) {
                        deleteThisPropertyIn1Second(viewProductProperty);
                        hideKeyboard(v);
                    }
                    else
                    {
                        if(!viewProductProperty.isCurrentlyFocused()) {
                            hideKeyboard(v);
                        }
                    }
                }
            }
        });

        viewProductProperty.setPropertyValueOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    int indexOfThisProperty = linearLayoutProperties.indexOfChild(viewProductProperty);
                    int lastPropertyIndex = linearLayoutProperties.getChildCount() - 1;
                    boolean thisIsTheLastProperty = indexOfThisProperty == lastPropertyIndex;
                    if (viewProductProperty.isEmptyProperty() && !thisIsTheLastProperty) {
                        deleteThisPropertyIn1Second(viewProductProperty);
                        hideKeyboard(v);
                    } else {
                        if (!viewProductProperty.isCurrentlyFocused()) {
                            hideKeyboard(v);
                        }
                    }
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
                int lastPropertyIndex = linearLayoutProperties.getChildCount() - 1;
                boolean thisIsTheLastProperty = indexOfThisProperty == lastPropertyIndex;

                if (!viewProductProperty.isEmptyProperty() && thisIsTheLastProperty) {
                    addEmptyProductProperty();
                }
                dateModified = new Date();

            }
        });

    }

    private void addEmptyProductProperty()
    {
        ViewProductProperty v = new ViewProductProperty(getActivity(), null, null, id, linearLayoutProperties.getChildCount());
        linearLayoutProperties.addView(v);
        setFocusAndTextChangeListenersToProperty(v);
    }

    private boolean addNonEmptyProductProperty(VarietyAttribute varietyAttribute, AttributeValue attributeValue)
    {
        if(varietyAttribute!=null && attributeValue!=null) {
            ViewProductProperty v = new ViewProductProperty(getActivity(), varietyAttribute, attributeValue, "", 0);
            linearLayoutProperties.addView(v);
            setFocusAndTextChangeListenersToProperty(v);
            return true; // property added
        }
        return false; // property not added
    }

    private void addEditTextHandlers()
    {
        TextWatcher dateModifiedTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                dateModified = new Date();
            }
        };
        editTextProductName.addTextChangedListener(dateModifiedTextWatcher);
        editTextPrice.addTextChangedListener(dateModifiedTextWatcher);
        //editTextStock.addTextChangedListener(dateModifiedTextWatcher);

        //hide keyboard on taps outside edittext
        editTextProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboardIfNothingFocused(v);
                }
            }
        });

        editTextStock.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboardIfNothingFocused(v);
                }
            }
        });
        editTextPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboardIfNothingFocused(v);
                }
            }
        });
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void hideKeyboardIfNothingFocused(final View view)
    {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(!editTextStock.isFocused() && !editTextPrice.isFocused() && !editTextProductName.isFocused() && !isAnyPropertyFocused() && getActivity()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 200);

    }

    private boolean isAnyPropertyFocused()
    {
        for(int i=0; i<linearLayoutProperties.getChildCount();i++)
        {
            if(((ViewProductProperty)linearLayoutProperties.getChildAt(i)).isCurrentlyFocused())
            {
                return true;
            }
        }
        return false;
    }

    public void deleteThisPropertyIn1Second(final ViewProductProperty v)
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                if(v.isEmptyProperty() && !v.isCurrentlyFocused()) {
                    linearLayoutProperties.removeView(v);
                }
            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1000);
    }

    private int getPriceSpinnerSelectionIndexFromPriceMeasuringUnitId()
    {
        int i = 0;
        if(price == null || priceUnitList ==null)
        {
            return i;
        }
        for(MeasuringUnit mu : priceUnitList)
        {
            if(String.valueOf(mu.getId()).equalsIgnoreCase(price.getVarietyAttribute().getId()))
            {
                return i;
            }
            i++;
        }
        return 0;
    }

    public boolean isPriceEmpty() {
        boolean isPriceEmpty = editTextPrice.getText().toString().trim().isEmpty();
        if(isPriceEmpty) {
            editTextPrice.setError("Price can't be empty");
            editTextPrice.requestFocus();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    editTextPrice.setError(null);
                }
            }, 1500);
        }
        return isPriceEmpty;
    }

}
