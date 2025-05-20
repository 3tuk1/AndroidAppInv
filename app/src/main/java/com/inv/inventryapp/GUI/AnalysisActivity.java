package com.inv.inventryapp.GUI;

import android.os.Bundle;
import com.inv.inventryapp.R;

public class AnalysisActivity extends commonActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nomain);
        setTitle("Analysis");
        settings();
        initCommonActivity(savedInstanceState);
        // Initialize your views and other components here
    }

    @Override
    public void onBackStackChanged() {

    }
}
