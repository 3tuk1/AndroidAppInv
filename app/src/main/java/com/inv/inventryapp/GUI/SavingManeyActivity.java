package com.inv.inventryapp.GUI;

import android.os.Bundle;
import android.widget.TextView;
import com.google.android.material.appbar.MaterialToolbar;
import com.inv.inventryapp.R;

public class SavingManeyActivity extends commonActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nomain);
        setTitle("Saving Money");
        settings();
        initCommonActivity(savedInstanceState);



    }

}
