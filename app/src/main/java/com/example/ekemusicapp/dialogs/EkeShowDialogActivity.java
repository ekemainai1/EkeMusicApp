package com.example.ekemusicapp.dialogs;


import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.ekemusicapp.R;
import com.example.ekemusicapp.uitils.EkeUIStates;

/**
 * Created by Ekemini on 10/9/2017.
 */

public class EkeShowDialogActivity extends DialogFragment{

    private ImageButton imageButtonEnqueueSe;
    private ImageButton imageButtonAddSe;
    private ImageButton imageButtonRingSe;
    private ImageButton imageButtonArtSe;
    private ImageButton imageButtonShareSe;
    private ImageButton imageButtonDeleteSe;
    public TextView dialogTitleSe;

    int mNum;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static DialogFragment newInstance(int num) {
        EkeShowDialogActivity f = new EkeShowDialogActivity();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments().getInt("num");

        // Pick a style based on the num.
        int style = DialogFragment.STYLE_NORMAL, theme = 0;
        switch ((mNum-1)%6) {
            case 1: style = DialogFragment.STYLE_NO_TITLE; break;
            case 2: style = DialogFragment.STYLE_NO_FRAME; break;
            case 3: style = DialogFragment.STYLE_NO_INPUT; break;
            case 4: style = DialogFragment.STYLE_NORMAL; break;
            case 5: style = DialogFragment.STYLE_NORMAL; break;
            case 6: style = DialogFragment.STYLE_NO_TITLE; break;
            case 7: style = DialogFragment.STYLE_NO_FRAME; break;
            case 8: style = DialogFragment.STYLE_NORMAL; break;
        }
        switch ((mNum-1)%6) {
            case 4: theme = android.R.style.Theme_Holo; break;
            case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            case 6: theme = android.R.style.Theme_Holo_Light; break;
            case 7: theme = android.R.style.Theme_Holo_Light_Panel; break;
            case 8: theme = android.R.style.Theme_Holo_Light; break;
        }
        setStyle(style, theme);
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        window.setFlags(Window.FEATURE_CONTEXT_MENU, Window.FEATURE_CONTEXT_MENU);
        window.setGravity(Gravity.BOTTOM);
        EkeUIStates ekeUIStates = new EkeUIStates(getActivity());
        String string = ekeUIStates.loadSearchTitlePos();
        window.setTitle(string);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_eke_show_dialog, container, false);
        imageButtonAddSe = (ImageButton) v.findViewById(R.id.add_se);
        imageButtonArtSe = (ImageButton) v.findViewById(R.id.artwork_se);
        imageButtonDeleteSe =(ImageButton) v.findViewById(R.id.delete_se);
        imageButtonEnqueueSe = (ImageButton) v.findViewById(R.id.enqueue_se);
        imageButtonRingSe = (ImageButton) v.findViewById(R.id.ringtone_se);
        imageButtonShareSe = (ImageButton) v.findViewById(R.id.share_se);

        // Watch for button clicks.
        imageButtonAddSe.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity
                Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                dismiss();

            }
        });

        imageButtonDeleteSe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, call up to owning activity
                Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                dismiss();
            }
        });

        imageButtonRingSe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, call up to owning activity
                Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                dismiss();
            }
        });

        imageButtonShareSe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, call up to owning activity
                Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                dismiss();
            }
        });

        imageButtonArtSe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, call up to owning activity
                Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                dismiss();
            }
        });

        imageButtonEnqueueSe.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // When button is clicked, call up to owning activity
                Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                dismiss();
            }
        });


        return v;
    }


}
