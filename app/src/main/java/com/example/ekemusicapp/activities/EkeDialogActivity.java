package com.example.ekemusicapp.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
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
import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.R;

/**
 * Created by Ekemini on 10/7/2017.
 */


public class EkeDialogActivity extends DialogFragment {

        private ImageButton imageButtonEnqueue;
        private ImageButton imageButtonAdd;
        private ImageButton imageButtonRing;
        private ImageButton imageButtonArt;
        private ImageButton imageButtonShare;
        private ImageButton imageButtonDelete;
        public TextView dialogTitle;
        public Context context;
        public static final String Broadcast_SHARE_AUDIO = "com.example.ekemini.musicplayer.ShareAudio";

        int mNum;

        /**
         * Create a new instance of MyDialogFragment, providing "num"
         * as an argument.
         */
        public static EkeDialogActivity newInstance(int num) {
            EkeDialogActivity f = new EkeDialogActivity();

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
            View v = inflater.inflate(R.layout.activity_eke_share_dialog, container, false);
            imageButtonAdd = (ImageButton) v.findViewById(R.id.add);
            imageButtonArt = (ImageButton) v.findViewById(R.id.artwork);
            imageButtonDelete =(ImageButton) v.findViewById(R.id.delete);
            imageButtonEnqueue = (ImageButton) v.findViewById(R.id.enqueue);
            imageButtonRing = (ImageButton) v.findViewById(R.id.ringtone);
            imageButtonShare = (ImageButton) v.findViewById(R.id.share);

            // Watch for button clicks.
            imageButtonAdd.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // When button is clicked, call up to owning activity
                    Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                    dismiss();

                }
            });

            imageButtonDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // When button is clicked, call up to owning activity
                    Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                    dismiss();

                }
            });

            imageButtonRing.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // When button is clicked, call up to owning activity
                    Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                    dismiss();

                }
            });

            imageButtonShare.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    EkeUIStates ekeUIStates = new EkeUIStates(getActivity());
                    String filePos = ekeUIStates.loadShareFile();
                    Toast.makeText(getActivity().getApplicationContext(), "File Me:"+filePos, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EkeDialogActivity.Broadcast_SHARE_AUDIO);
                    intent.putExtra("songPos", filePos);
                    getActivity().getApplicationContext().sendBroadcast(intent);
                    dismiss();
                }
            });

            imageButtonArt.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    // When button is clicked, call up to owning activity
                    Toast.makeText(getActivity(), "Not Ready", Toast.LENGTH_LONG ).show();
                    dismiss();

                }
            });

            imageButtonEnqueue.setOnClickListener(new OnClickListener() {
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
