package com.koleshop.appkoleshop.ui.seller.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;
import com.koleshop.appkoleshop.ui.seller.adapters.DeliveryTimeRemainingAdapter;
import com.koleshop.appkoleshop.util.AndroidCompatUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class DeliveryTimeRemainingDialogFragment extends DialogFragment {
    static Context contextDialog;
    int highlightedTextViewPosition = -1;
    String editTextValue = null;
    private DeliveryTimeDialogFragmentListener dataFetcher;
    DeliveryTimeRemainingAdapter customButtonAdapter;
    static int hourInDialog, minInDialog;
    TextView oldSelectedTextView;

    public static DeliveryTimeRemainingDialogFragment create(Context context, int hour, int min) {
        hourInDialog = hour;
        minInDialog = min;
        contextDialog = context;
        DeliveryTimeRemainingDialogFragment dialog = new DeliveryTimeRemainingDialogFragment();
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) contextDialog.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_fragment_delivery_time_remaining, null);
        final GridView gridView = (GridView) view.findViewById(R.id.buttons_grid);
        customButtonAdapter = new DeliveryTimeRemainingAdapter(getActivity());
        gridView.setAdapter(customButtonAdapter);

        final EditText editTextHour = (EditText) view.findViewById(R.id.show_hour);
        final EditText editTextMin = (EditText) view.findViewById(R.id.show_min);
        editTextHour.setText(Integer.toString(hourInDialog));
        editTextMin.setText(Integer.toString(minInDialog));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final TextView textView = (TextView) view.findViewById(R.id.text_view_in_grid);
                editTextValue = textView.getText().toString();
                if (highlightedTextViewPosition != -1) {
                    oldSelectedTextView.setBackgroundColor(Color.TRANSPARENT);
                    oldSelectedTextView.setTextColor(AndroidCompatUtil.getColor(getContext(), R.color.secondary_text));
                }
                textView.setBackgroundResource(R.drawable.dialog_time_button_pressed);
                textView.setTextColor(AndroidCompatUtil.getColor(getContext(), R.color.primary_text));

                ArrayList<String> putValuesEditText = new ArrayList<String>(Arrays.asList(editTextValue.split(" ")));
                if (putValuesEditText.contains("hr")) {

                    if (putValuesEditText.get(0).equals("1:30")) {
                        editTextHour.setText("1");
                        editTextMin.setText("30");
                    } else {
                        editTextHour.setText(putValuesEditText.get(0));
                        editTextMin.setText("0");
                    }
                } else {
                    editTextHour.setText("0");
                    editTextMin.setText(putValuesEditText.get(0));
                }
                highlightedTextViewPosition = position;
                oldSelectedTextView = textView;
            }
        });
        builder.setView(view);

        builder.setTitle("Estimated Delivery Time");

        Button deliverButton = (Button) view.findViewById(R.id.deliver_button);
        deliverButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 if (editTextHour.getText().toString().equals("")) {
                                                     editTextHour.setText("0");
                                                 }
                                                 if (editTextMin.getText().toString().equals("")) {
                                                     editTextMin.setText("0");
                                                 }
                                                 int hour = Integer.parseInt(editTextHour.getText().toString());
                                                 int min = Integer.parseInt(editTextMin.getText().toString());
                                                 min = hour*60 + min;
                                                 if(min == 0) {
                                                     Toast.makeText(contextDialog, "Please select estimated delivery time", Toast.LENGTH_SHORT).show();
                                                 } else {

                                                 }
                                                 dataFetcher.deliveryTimeRemainingSelected(min);
                                                 dismiss();
                                             }

                                         }

        );
        Button cancelButton = (Button) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener()

                                        {
                                            @Override
                                            public void onClick(View v) {
                                                dismiss();
                                            }
                                        }

        );

        final Dialog dialog = builder.create();
        return dialog;
    }


    @Override
    public void onAttach(Activity activity) {
        dataFetcher = (DeliveryTimeDialogFragmentListener) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        dataFetcher = null;
        super.onDetach();
    }

    public interface DeliveryTimeDialogFragmentListener {
        void deliveryTimeRemainingSelected(int minutes);
    }

}
