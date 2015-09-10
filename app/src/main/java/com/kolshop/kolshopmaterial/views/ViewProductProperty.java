package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.kolshop.kolshopmaterial.R;
import com.kolshop.kolshopmaterial.model.MeasuringUnit;
import com.kolshop.kolshopmaterial.model.android.VarietyAttribute;
import com.kolshop.kolshopmaterial.model.android.AttributeValue;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Gundeep on 22/03/15.
 */
public class ViewProductProperty extends LinearLayout{

    EditText editTextProperty, editTextPropertyValue;
    Spinner spinnerPropertyUnit;
    ImageButton imageButtonProductPropertyOptions;
    String propertyName;
    final Context context;
    List<MeasuringUnit> measuringUnitList;
    int currentMeasuringUnitId;
    String productVarietyAttributeId,productVarietyAttributeValueId,productVarietyId;
    boolean aboutToDelete;

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        editTextProperty.setText(propertyName);
    }

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

    public void setOnOptionsClickListener(OnClickListener onOptionsClickListener) {
        imageButtonProductPropertyOptions.setOnClickListener(onOptionsClickListener);
    }

    private void loadMeasuringUnits()
    {
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


        for(MeasuringUnit mu:measuringUnits) {
            measuringUnitList.add(new MeasuringUnit(mu.getId(), mu.getUnitDimensions(), mu.getUnit(), mu.isBaseUnit(), mu.getConversionRate(), mu.getUnitFullName()));
            spinnerList.add(mu.getUnit());
        }

        ArrayAdapter<String> measuringUnitsArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerList);
        spinnerPropertyUnit.setAdapter(measuringUnitsArrayAdapter);
        spinnerPropertyUnit.setSelection(0);
    }

    private void bindSpinner()
    {
        spinnerPropertyUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int measuringUnitId = measuringUnitList.get(position).getId();
                currentMeasuringUnitId = measuringUnitId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }

    public VarietyAttribute getVarietyAttribute()
    {
        VarietyAttribute varietyAttribute = new VarietyAttribute();
        varietyAttribute.setId(productVarietyAttributeId);
        varietyAttribute.setName(editTextProperty.getText().toString());
        varietyAttribute.setMeasuringUnitId(currentMeasuringUnitId);
        return varietyAttribute;
    }

    public AttributeValue getAttributeValue()
    {
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setId(productVarietyAttributeValueId);
        attributeValue.setProductVarietyAttributeId(productVarietyAttributeId);
        attributeValue.setProductVarietyId(productVarietyId);
        attributeValue.setValue(editTextPropertyValue.getText().toString());
        return attributeValue;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener)
    {
        editTextProperty.setOnFocusChangeListener(onFocusChangeListener);
        editTextPropertyValue.setOnFocusChangeListener(onFocusChangeListener);
    }

    public void setTextWatcher(TextWatcher textWatcher)
    {
        editTextProperty.addTextChangedListener(textWatcher);
        editTextPropertyValue.addTextChangedListener(textWatcher);
    }

    public boolean isEmptyProperty()
    {
        return editTextProperty.getText().toString().trim().isEmpty()&&editTextPropertyValue.getText().toString().trim().isEmpty();
    }

    public boolean validateForSave()
    {
        if(isEmptyProperty())
        {
            return false;
        }
        else
        {
            if(editTextProperty.getText().toString().trim().isEmpty())
            {
                editTextProperty.setError("Can't be empty");
                return false;
            }
            else if(editTextPropertyValue.getText().toString().trim().isEmpty())
            {
                editTextPropertyValue.setError("Can't be empty");
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    public void setAboutToDelete()
    {
        aboutToDelete = true;
    }

    public boolean isAboutToDelete()
    {
        return aboutToDelete;
    }

    public boolean isCurrentlyFocused()
    {
        return (editTextProperty.isFocused() || editTextPropertyValue.isFocused());
    }

    private void addEditorActionListener()
    {
        editTextProperty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == KeyEvent.ACTION_DOWN) {
                    editTextPropertyValue.requestFocus();
                    handled = true;
                }
                return handled;
            }
        });
    }

}
