package com.example.ekemusicapp.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;

import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.R;


public class EkeButtonColorFragment extends Fragment {
    public GridView gridViewColor;
    public ImageView imageViewColor;

    Integer[] layoutButtonColorIDs = {
            R.drawable.ic_button_color_1,
            R.drawable.ic_button_color_2,
            R.drawable.ic_button_color_3,
            R.drawable.ic_button_color_4,
            R.drawable.ic_button_color_5,
            R.drawable.ic_button_color_6,
            R.drawable.ic_button_color_7,
            R.drawable.ic_button_color_8,
            R.drawable.ic_button_color_9
    };

    // Store instance variables
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static EkeButtonColorFragment newInstance(int page, String title) {
        EkeButtonColorFragment fragmentSecond = new EkeButtonColorFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentSecond.setArguments(args);
        return fragmentSecond;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 1);
        title = getArguments().getString("someTitle");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rView = (ViewGroup) inflater.inflate(
                R.layout.fragment_eke_button_color, container, false);

        gridViewColor = rView.findViewById(R.id.color_palette);
        gridViewColor.setAdapter(new EkeButtonColorAdapter(getActivity()));

        gridViewColor.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                // Get selected image view
                new EkeUIStates(getContext()).storeButtonColor(position);

                Toast.makeText(getContext(), "" + (position) + " selected", Toast.LENGTH_SHORT).show();
                EkeUIStates ekeUIStates = new EkeUIStates(getContext());
                int buttonCol = ekeUIStates.loadButtonColor();

                PagerTabStrip pagerTabStrip = getActivity().findViewById(R.id.pager_header);

                /**
                 * All player button color settings
                 */
                if(buttonCol == 0) {
                 pagerTabStrip.setTabIndicatorColor(Color.BLUE);

                 }else if(buttonCol == 1){
                 pagerTabStrip.setTabIndicatorColor(Color.RED);

                 }else if(buttonCol == 2){
                 pagerTabStrip.setTabIndicatorColor(Color.YELLOW);

                 }else if(buttonCol == 3) {
                    pagerTabStrip.setTabIndicatorColor(Color.BLACK);

                 }else if(buttonCol == 4){
                    pagerTabStrip.setTabIndicatorColor(Color.WHITE);

                 }else if(buttonCol == 5){
                    pagerTabStrip.setTabIndicatorColor(Color.GREEN);

                 }else if(buttonCol == 6) {
                    pagerTabStrip.setTabIndicatorColor(Color.parseColor("#f908f1"));
                 }else if(buttonCol == 7) {
                    pagerTabStrip.setTabIndicatorColor(Color.parseColor("#08f7ef"));
                 }else if(buttonCol == 8) {
                    pagerTabStrip.setTabIndicatorColor(Color.parseColor("#7709ae"));
                 }
            }
        });

        imageViewColor = (ImageView) rView.findViewById(R.id.selector_color);

        return rView;

        }

    public class EkeButtonColorAdapter extends BaseAdapter {

        private Context context;

        public EkeButtonColorAdapter(Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return layoutButtonColorIDs.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ImageView imageView;
            if (view == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(64, 64));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) view;
            }
            imageView.setImageResource(layoutButtonColorIDs[position]);
            return imageView;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        EkeUIStates ekeUIStates = new EkeUIStates(getContext());
        int backImageA =  ekeUIStates.loadLayoutBackgroundA();
        //Toast.makeText(getApplicationContext(), "I am "+backImageA, Toast.LENGTH_LONG).show();

        if(backImageA == 0) {
            imageViewColor.setImageResource(R.drawable.player_ground);

        }else if(backImageA == 1){
            imageViewColor.setImageResource(R.drawable.player_ground_a);   //

        }else if(backImageA == 2){
            imageViewColor.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageViewColor.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageViewColor.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageViewColor.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageViewColor.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageViewColor.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageViewColor.setImageResource(R.drawable.background_b);

        }


    }
}
