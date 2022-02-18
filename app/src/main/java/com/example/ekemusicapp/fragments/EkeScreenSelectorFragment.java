package com.example.ekemusicapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.R;


public class EkeScreenSelectorFragment extends Fragment {
    public GridView gridView;
    public ImageView imageView_s;
    public Button button;

    Integer[] layoutBackgroundIDs = {
            R.drawable.player_ground,
            R.drawable.player_ground_a,
            R.drawable.player_ground_b,
            R.drawable.player_ground_c,
            R.drawable.player_ground_aa,
            R.drawable.player_ground_d,
            R.drawable.player_ground_e,
            R.drawable.background_a,
            R.drawable.background_b
    };

    // Store instance variables
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static EkeScreenSelectorFragment newInstance(int page, String title) {
        EkeScreenSelectorFragment fragmentFirst = new EkeScreenSelectorFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_eke_screen_selector, container, false);


        gridView = (rootView).findViewById(R.id.pics_for_screen);
        gridView.setAdapter(new EkeScreenImageAdapter(getActivity()));

        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                // Get selected image view
                new EkeUIStates(getContext()).storeLayoutBackgroundA(position);

                Toast.makeText(getContext(), "" + (position) + " selected",
                        Toast.LENGTH_SHORT).show();
                EkeUIStates ekeUIStates = new EkeUIStates(getContext());
                int backImageA =  ekeUIStates.loadLayoutBackgroundA();

                ImageView imageViewM = getActivity().findViewById(R.id.skin_view);


                if(backImageA == 0) {
                    imageView_s.setImageResource(R.drawable.player_ground);
                    imageViewM.setImageResource(R.drawable.player_ground);
                }else if(backImageA == 1){
                    imageView_s.setImageResource(R.drawable.player_ground_a);   //
                    imageViewM.setImageResource(R.drawable.player_ground_a);
                }else if(backImageA == 2){
                    imageView_s.setImageResource(R.drawable.player_ground_b);   // optional
                    imageViewM.setImageResource(R.drawable.player_ground_b);
                }else if(backImageA == 3){
                    imageView_s.setImageResource(R.drawable.player_ground_c);   // optional
                    imageViewM.setImageResource(R.drawable.player_ground_c);
                }else if(backImageA == 4){
                    imageView_s.setImageResource(R.drawable.player_ground_aa);   // optional
                    imageViewM.setImageResource(R.drawable.player_ground_aa);
                }else if (backImageA == 5) {
                    imageView_s.setImageResource(R.drawable.player_ground_d);   // optional
                    imageViewM.setImageResource(R.drawable.player_ground_d);
                }else if (backImageA == 6) {
                    imageView_s.setImageResource(R.drawable.player_ground_e);   // optional
                    imageViewM.setImageResource(R.drawable.player_ground_e);
                }else if (backImageA == 7){
                    imageView_s.setImageResource(R.drawable.background_a);  // optional
                    imageViewM.setImageResource(R.drawable.background_a);
                } else if (backImageA == 8) {
                    imageView_s.setImageResource(R.drawable.background_b);
                    imageViewM.setImageResource(R.drawable.background_b);
                }

            }
        });

        imageView_s = (ImageView) rootView.findViewById(R.id.selector_image);


        return rootView;




    }

    public class EkeScreenImageAdapter extends BaseAdapter {

        private Context context;

        public EkeScreenImageAdapter(Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return layoutBackgroundIDs.length;
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
                imageView.setLayoutParams(new GridView.LayoutParams(185, 185));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(5, 5, 5, 5);
            } else {
                imageView = (ImageView) view;
            }
            imageView.setImageResource(layoutBackgroundIDs[position]);
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
            imageView_s.setImageResource(R.drawable.player_ground);

        }else if(backImageA == 1){
            imageView_s.setImageResource(R.drawable.player_ground_a);   //

        }else if(backImageA == 2){
            imageView_s.setImageResource(R.drawable.player_ground_b);   // optional

        }else if(backImageA == 3){
            imageView_s.setImageResource(R.drawable.player_ground_c);   // optional

        }else if(backImageA == 4){
            imageView_s.setImageResource(R.drawable.player_ground_aa);   // optional

        }else if (backImageA == 5) {
            imageView_s.setImageResource(R.drawable.player_ground_d);   // optional

        }else if (backImageA == 6) {
            imageView_s.setImageResource(R.drawable.player_ground_e);   // optional

        }else if (backImageA == 7){
            imageView_s.setImageResource(R.drawable.background_a);  // optional

        } else if (backImageA == 8) {
            imageView_s.setImageResource(R.drawable.background_b);

        }


    }

}
