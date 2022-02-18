package com.example.ekemusicapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import com.example.ekemusicapp.R;
import com.example.ekemusicapp.fragments.EkeButtonColorFragment;
import com.example.ekemusicapp.fragments.EkeScreenSelectorFragment;
import com.example.ekemusicapp.uitils.EkeUIStates;

public class EkeSkinActivity extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;
    public ImageView imageViewSkin;
    public PagerTabStrip tabsStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eke_skin);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_page);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        ViewPager vpPager = (ViewPager) findViewById(R.id.pager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        // Give the PagerSlidingTabStrip the ViewPager
        tabsStrip = (PagerTabStrip) findViewById(R.id.pager_header);
        // Attach the view pager to the tab strip
        tabsStrip.setTextColor(Color.BLACK);
        tabsStrip.setPadding(60, 0, 60, 0);
        vpPager.setAdapter(adapterViewPager);

        imageViewSkin = (ImageView)findViewById(R.id.skin_view);
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return EkeScreenSelectorFragment.newInstance(0, "Screen Background");
                case 1: // Fragment # 1 - This will show SecondFragment
                    return EkeButtonColorFragment.newInstance(1, "Buttons Color");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            String pageTitle = " ";
            if(position == 0){
                pageTitle = "Screen";
            }else if(position == 1){
                pageTitle = "Button";
            }
            return pageTitle;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        EkeUIStates ekeUIStates = new EkeUIStates(getApplicationContext());
        int backImageA = ekeUIStates.loadLayoutBackgroundA();
        //Toast.makeText(getApplicationContext(), "I am "+backImageA, Toast.LENGTH_LONG).show();

        if (backImageA == 0) {
            imageViewSkin.setImageResource(R.drawable.player_ground);     // optional

        } else if (backImageA == 1) {
            imageViewSkin.setImageResource(R.drawable.player_ground_a);   // optional

        } else if (backImageA == 2) {
            imageViewSkin.setImageResource(R.drawable.player_ground_b);   // optional

        } else if (backImageA == 3) {
            imageViewSkin.setImageResource(R.drawable.player_ground_c);   // optional

        } else if (backImageA == 4) {
            imageViewSkin.setImageResource(R.drawable.player_ground_aa);   // optional

        } else if (backImageA == 5) {
            imageViewSkin.setImageResource(R.drawable.player_ground_d);   // optional

        } else if (backImageA == 6) {
            imageViewSkin.setImageResource(R.drawable.player_ground_e);   // optional

        } else if (backImageA == 7) {
            imageViewSkin.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageViewSkin.setImageResource(R.drawable.background_b);    // optional

        }

        ekeUIStates = new EkeUIStates(getApplicationContext());
        int buttonCol = ekeUIStates.loadButtonColor();

        /**
         * All player button color settings
         */
        if(buttonCol == 0) {
            tabsStrip.setTabIndicatorColor(Color.BLUE);

        }else if(buttonCol == 1){
            tabsStrip.setTabIndicatorColor(Color.RED);

        }else if(buttonCol == 2){
            tabsStrip.setTabIndicatorColor(Color.YELLOW);
        }

    }


}
