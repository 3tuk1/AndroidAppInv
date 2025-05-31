package com.inv.inventryapp.GUI;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inv.inventryapp.R;
import com.inv.inventryapp.fragments.ShoppingListFragment;
import com.inv.inventryapp.fragments.AnalysisDashboardFragment;
import com.inv.inventryapp.fragments.ConsumptionHistoryFragment;

public class AnalysisActivity extends commonActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private final String[] tabTitles = new String[]{"ダッシュボード", "買い物リスト", "消費履歴"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_with_tabs);
        settings();
        initCommonActivity(savedInstanceState);

        viewPager = findViewById(R.id.analysisViewPager);
        tabLayout = findViewById(R.id.analysisTabLayout);

        viewPager.setAdapter(new AnalysisPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    @Override
    public void onBackStackChanged() {
        // Custom logic for onBackStackChanged can be added here if needed.
    }

    private static class AnalysisPagerAdapter extends FragmentStateAdapter {
        public AnalysisPagerAdapter(@NonNull AnalysisActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AnalysisDashboardFragment();
                case 1:
                    return new ShoppingListFragment();
                case 2:
                    return new ConsumptionHistoryFragment();
                default:
                    return new AnalysisDashboardFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
