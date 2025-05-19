package com.inv.inventryapp.GUI;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.inv.inventryapp.R;

public class SettingsActivity extends commonActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nomain);

        // Initialize the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set the title of the activity
        setTitle("Settings");
    }

}
