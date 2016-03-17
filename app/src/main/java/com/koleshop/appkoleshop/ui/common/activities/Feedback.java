package com.koleshop.appkoleshop.ui.common.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.koleshop.appkoleshop.R;

public class Feedback extends AppCompatActivity {

    TextView headerTextView;
    EditText feedbackText;
    Button sendFeedback;
    Button callUs;
    private String screenSizeInDp;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(8.0f);
        }
        context=this;
        headerTextView= (TextView) findViewById(R.id.header_text_view);
        feedbackText= (EditText) findViewById(R.id.feedback_text);
        sendFeedback= (Button) findViewById(R.id.button_send_feedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDetails();
            }
        });
        callUs=(Button)findViewById(R.id.button_call_us);
        callUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:123456789"));
                startActivity(callIntent);

            }
        });

    }

    private void fetchDetails() {
        String fetchedData=feedbackText.getText().toString();
        String deviceModel = android.os.Build.MODEL;
        String deviceManufacturer= Build.MANUFACTURER;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        String OS=Build.VERSION.RELEASE+" API: "+Build.VERSION.SDK_INT;
        String dpHeight = displayMetrics.heightPixels / displayMetrics.density+"";
        String dpWidth = displayMetrics.widthPixels / displayMetrics.density+"";
        String screenSize=getScreenSize();
        Toast.makeText(this,fetchedData+"\n"+dpHeight+"\n"+OS+"\n"+dpWidth+"\n"+deviceModel+"\n"+deviceManufacturer+"\n"+screenSize,Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);
        double screenInches = Math.sqrt(x + y);
        return screenInches+"";
    }

    public String getScreenSizeInDp() {
        return screenSizeInDp;
    }
}
