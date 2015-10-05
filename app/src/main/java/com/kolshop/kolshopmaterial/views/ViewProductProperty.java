package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.common.constant.Constants;
import com.kolshop.kolshopmaterial.common.util.CommonUtils;
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.android.AttributeValue;
import com.kolshop.kolshopmaterial.model.android.VarietyAttribute;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 22/03/15.
 */
public class ViewProductProperty extends LinearLayout {

    EditText editTextProperty, editTextPropertyValue;
    Spinner spinnerPropertyUnit;
    ImageButton imageButtonProductPropertyOptions;
    String propertyName;
    final Context context;
    List<MeasuringUnit> measuringUnitList;
    int currentMeasuringUnitId;
    String productVarietyAttributeId, productVarietyAttributeValueId, productVarietyId;
    int sortOrder;
    static String TAG = "Kolshop - ViewProductProperty";

    public ViewProductProperty(Context context) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.view_product_property, this, true);
        editTextProperty = (EditText) v.findViewById(R.id.edittext_property);
        editTextProperty.setText(propertyName);
        editTextPropertyValue = (EditText) v.findViewById(R.id.edittext_property_value);
        spinnerPropertyUnit = (Spinner) v.findViewById(R.id.spinner_product_property_unit);
        loadMeasuringUnits();
        bindSpinner();
        addEditorActionListener();
    }

    public ViewProductProperty(Context context, VarietyAttribute varietyAttribute, AttributeValue attributeValue, String productVarietyId, int sortOrder) {
        this(context);
        if (varietyAttribute != null && attributeValue != null) {
            String info = varietyAttribute.getName();
            String value = attributeValue.getValue();
            editTextProperty.setText(info != null ? info : "");
            editTextPropertyValue.setText(value != null ? value : "");
            productVarietyAttributeId = varietyAttribute.getId();
            //just to double check
            if (productVarietyAttributeId == null || productVarietyAttributeId.isEmpty()) {
                productVarietyAttributeId = getRandomId();
            }
            productVarietyAttributeValueId = attributeValue.getId();
            if (productVarietyAttributeValueId == null || productVarietyAttributeValueId.isEmpty()) {
                productVarietyAttributeValueId = getRandomId();
            }
            this.productVarietyId = attributeValue.getProductVarietyId();
            currentMeasuringUnitId = varietyAttribute.getMeasuringUnitId();
            spinnerPropertyUnit.setSelection(currentMeasuringUnitId == 0 ? 0 : getSpinnerSelectionFromUnitId(currentMeasuringUnitId));
            this.sortOrder = attributeValue.getSortOrder();
        } else {
            String info = "";
            String value = "";
            editTextProperty.setText(info);
            editTextPropertyValue.setText(value);
            productVarietyAttributeId = getRandomId();
            productVarietyAttributeValueId = getRandomId();
            this.productVarietyId = productVarietyId;
            currentMeasuringUnitId = 0;
            spinnerPropertyUnit.setSelection(0);
            this.sortOrder = sortOrder;
        }
    }

    private String getRandomId() {
        return "random" + CommonUtils.randomString(8);
    }

    private int getSpinnerSelectionFromUnitId(int unitId) {
        if (measuringUnitList != null) {
            for (int i = 0; i < measuringUnitList.size(); i++) {
                MeasuringUnit measuringUnit = measuringUnitList.get(i);
                if (measuringUnit != null && measuringUnit.getId() == unitId) {
                    return i;
                }
            }
        }
        return 0;
    }

    public void setOnOptionsClickListener(OnClickListener onOptionsClickListener) {
        imageButtonProductPropertyOptions.setOnClickListener(onOptionsClickListener);
    }

    private void loadMeasuringUnits() {
        Realm realm = Realm.getInstance(context);
        RealmQuery<MeasuringUnit> query = realm.where(MeasuringUnit.class);
        query.equalTo("unitDimensions", "default");
        RealmResults<MeasuringUnit> result = query.findAll();
        RealmList<MeasuringUnit> measuringUnits = new RealmList<>();
        measuringUnits.addAll(result);

        query = realm.where(MeasuringUnit.class);
        query.equalTo("unitDimensions", "mass");
        measuringUnits.addAll(query.findAll());

        query = realm.where(MeasuringUnit.class);
        query.equalTo("unitDimensions", "volume");
        measuringUnits.addAll(query.findAll());

        query = realm.where(MeasuringUnit.class);
        query.equalTo("unitDimensions", "length");
        measuringUnits.addAll(query.findAll());

        measuringUnitList = new ArrayList<MeasuringUnit>();
        List<String> spinnerList = new ArrayList<>();


        for (MeasuringUnit mu : measuringUnits) {
            measuringUnitList.add(new MeasuringUnit(mu.getId(), mu.getUnitDimensions(), mu.getUnit(), mu.isBaseUnit(), mu.getConversionRate(), mu.getUnitFullName()));
            spinnerList.add(mu.getUnit());
        }

        ArrayAdapter<String> measuringUnitsArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerList);
        measuringUnitsArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPropertyUnit.setAdapter(measuringUnitsArrayAdapter);
        spinnerPropertyUnit.setSelection(0);
    }

    private void bindSpinner() {
        spinnerPropertyUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int measuringUnitId = measuringUnitList.get(position).getId();
                currentMeasuringUnitId = measuringUnitId;
                Log.d(TAG, "Broadcasting property unit modified");
                Intent intent = new Intent(Constants.ACTION_PROPERTY_MODIFIED);
                intent.putExtra("productVarietyId", productVarietyId);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public VarietyAttribute getVarietyAttribute() {
        String attributeName = editTextProperty.getText().toString();
        VarietyAttribute varietyAttribute = new VarietyAttribute();
        varietyAttribute.setId(productVarietyAttributeId);
        varietyAttribute.setName(attributeName);
        varietyAttribute.setMeasuringUnitId(currentMeasuringUnitId);
        if (productVarietyAttributeId.startsWith("random")) {
            Realm realm = CommonUtils.getRealmInstance(context);
            RealmQuery<VarietyAttribute> realmQuery = realm.where(VarietyAttribute.class)
                    .equalTo("name", attributeName)
                    .equalTo("measuringUnitId", currentMeasuringUnitId);
            VarietyAttribute varAtt = realmQuery.findFirst();
            if (varAtt != null) {
                varietyAttribute.setId(varAtt.getId());
            } else {

            }
        }
        return varietyAttribute;
    }

    public AttributeValue getAttributeValue() {
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setId(productVarietyAttributeValueId);
        attributeValue.setProductVarietyAttributeId(productVarietyAttributeId);
        attributeValue.setProductVarietyId(productVarietyId);
        attributeValue.setValue(editTextPropertyValue.getText().toString());
        attributeValue.setSortOrder(sortOrder);
        return attributeValue;
    }

    public void setPropertyOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        editTextProperty.setOnFocusChangeListener(onFocusChangeListener);
    }

    public void setPropertyValueOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        editTextPropertyValue.setOnFocusChangeListener(onFocusChangeListener);
    }

    public void setTextWatcher(TextWatcher textWatcher) {
        editTextProperty.addTextChangedListener(textWatcher);
        editTextPropertyValue.addTextChangedListener(textWatcher);
    }

    public boolean isEmptyProperty() {
        return editTextProperty.getText().toString().trim().isEmpty() && editTextPropertyValue.getText().toString().trim().isEmpty();
    }

    public boolean validateForSave() {
        if (isEmptyProperty()) {
            return false;
        } else {
            if (editTextProperty.getText().toString().trim().isEmpty()) {
                editTextProperty.setError("Can't be empty");
                return false;
            } else if (editTextPropertyValue.getText().toString().trim().isEmpty()) {
                editTextPropertyValue.setError("Can't be empty");
                return false;
            } else {
                return true;
            }
        }
    }

    public void setProductVarietyAttributeId(String productVarietyAttributeId) {
        this.productVarietyAttributeId = productVarietyAttributeId;
    }

    public boolean isCurrentlyFocused() {
        boolean isPropertyFocused = editTextProperty.isFocused();
        boolean isPropertyValueFocused = editTextPropertyValue.isFocused();
        //Log.d(TAG, "isPropertyFocused = " + String.valueOf(isPropertyFocused) + " and isPropertyValueFocused = " + String.valueOf(isPropertyValueFocused));
        return (isPropertyFocused || isPropertyValueFocused);
    }

    private void addEditorActionListener() {
        editTextProperty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                /*//Log event
                if(event!=null && event.getAction()==KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "event.action = action down");
                } else if(event!=null && event.getAction()==KeyEvent.ACTION_UP) {
                    Log.d(TAG, "event.action = action up");
                } else if(event!=null && event.getAction()==EditorInfo.IME_ACTION_DONE) {
                    Log.d(TAG, "event.action = ime action done");
                } else if(event!=null) {
                    Log.d(TAG, "event is not null. event.action = " + event.getAction());
                } else {
                    Log.d(TAG, "event is null");
                }
                //Log actionId
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    Log.d(TAG, "actionId is ime action next");
                } else if (actionId == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "actionId is action down");
                }*/


                if (event != null && event.getAction() != KeyEvent.ACTION_DOWN && event.getAction() != EditorInfo.IME_ACTION_NEXT && event.getAction() != KeyEvent.ACTION_UP) {
                    Log.d(TAG, "will return false");
                    return false;
                }
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    editTextPropertyValue.requestFocus();
                    handled = true;
                } else if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "--------testing 1--------");
                    //editTextProperty.clearFocus();
                    editTextPropertyValue.requestFocus();
                    handled = true;
                }
                Log.d(TAG, "will return " + String.valueOf(handled) + " from end");
                return handled;
            }
        });

        editTextPropertyValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;

                /*Log.d(TAG, "editor action listener for property value");
                //Log event
                if(event!=null && event.getAction()==KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "event.action = action down");
                } else if(event!=null && event.getAction()==KeyEvent.ACTION_UP) {
                    Log.d(TAG, "event.action = action up");
                } else if(event!=null && event.getAction()==EditorInfo.IME_ACTION_DONE) {
                    Log.d(TAG, "event.action = ime action done");
                } else if(event!=null) {
                    Log.d(TAG, "event is not null. event.action = " + event.getAction());
                } else {
                    Log.d(TAG, "event is null");
                }
                //Log actionId
                if(actionId == EditorInfo.IME_ACTION_NEXT) {
                    Log.d(TAG, "actionId is ime action next");
                } else if (actionId == KeyEvent.ACTION_DOWN) {
                    Log.d(TAG, "actionId is action down");
                }*/


                if (event != null && event.getAction() != KeyEvent.ACTION_DOWN && event.getAction() != EditorInfo.IME_ACTION_DONE && event.getAction() != KeyEvent.ACTION_UP) {
                    Log.d(TAG, "will return false");
                    return false;
                }
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.ACTION_DOWN) {
                    editTextPropertyValue.clearFocus();
                    handled = true;
                }
                Log.d(TAG, "will return " + String.valueOf(handled) + " from end");
                return handled;
            }
        });

    }

}
