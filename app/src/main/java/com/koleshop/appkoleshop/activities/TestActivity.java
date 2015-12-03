package com.koleshop.appkoleshop.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.koleshop.appkoleshop.fragments.product.InventoryProductFragment;

public class TestActivity extends AppCompatActivity {

    private Button button;
    private final static String TAG_COUNTRIES_FRAGMENT = "tag_jse";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.koleshop.appkoleshop.R.layout.activity_test);
        if (savedInstanceState == null) {
            InventoryProductFragment fragment = new InventoryProductFragment();
            Bundle bundl = new Bundle();
            bundl.putLong("categoryId", 120l);
            fragment.setArguments(bundl);
            getSupportFragmentManager().beginTransaction()
                    .add(com.koleshop.appkoleshop.R.id.container, fragment, TAG_COUNTRIES_FRAGMENT)
                            .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(com.koleshop.appkoleshop.R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.koleshop.appkoleshop.R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.koleshop.appkoleshop.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void testing(final View v)
    {
        Toast.makeText(this, "testing working", Toast.LENGTH_LONG).show();
    }
}
