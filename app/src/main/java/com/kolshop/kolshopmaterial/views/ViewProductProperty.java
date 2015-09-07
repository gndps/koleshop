package com.kolshop.kolshopmaterial.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * Created by Gundeep on 22/03/15.
 */
public class ViewProductProperty extends LinearLayout{

    EditText editTextProperty, editTextPropertyValue;
    Spinner spinnerPropertyUnit;
    ImageButton imageButtonProductPropertyOptions;
    int index;
    String propertyName;

    public ViewProductProperty(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(com.kolshop.kolshopmaterial.R.layout.view_product_property, this, true);
        //editTextProperty = (EditText) findViewById(R.id.edittext_property);
        //editTextProperty.setText(propertyName);
        //editTextPropertyValue = (EditText) findViewById(R.id.edittext_property_value);
        //spinnerPropertyUnit = (Spinner) findViewById(R.id.spinner_property_unit);
        //imageButtonProductPropertyOptions = (ImageButton) findViewById(R.id.imagebutton_product_proprety_options);
    }

    public ViewProductProperty(Context context, String propertyName)
    {
        this(context);
        this.propertyName = propertyName;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setPropertyName(String propertyName) {
        editTextProperty.setText(propertyName);
    }

    public void setOnOptionsClickListener(OnClickListener onOptionsClickListener) {
        imageButtonProductPropertyOptions.setOnClickListener(onOptionsClickListener);
    }

}
