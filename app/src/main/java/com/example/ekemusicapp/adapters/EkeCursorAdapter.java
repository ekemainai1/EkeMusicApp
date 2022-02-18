package com.example.ekemusicapp.adapters;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.ekemusicapp.database.EkeDbContract;
import com.example.ekemusicapp.activities.EkeDialogActivity;
import com.example.ekemusicapp.uitils.EkeUIStates;
import com.example.ekemusicapp.R;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.FragmentActivity;

/**
 * Created by Ekemini on 9/8/2017.
 */

public class EkeCursorAdapter extends CursorAdapter {

    public TextView songTitle;
    public TextView songSize;
    public TextView songDuration;
    public TextView songArtist;
    public CircleImageView albumArt;
    public ImageView imageButton_share;
    public String songName;

    // Initialize the array
    private SparseBooleanArray selectionArray = new SparseBooleanArray();

    public EkeCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Inflate a list item view using the layout specified in song.xml
        return LayoutInflater.from(context).inflate(R.layout.song, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        albumArt = view.findViewById(R.id.thumbnail);
        songTitle = view.findViewById(R.id.song_title);
        songSize = view.findViewById(R.id.song_size);
        songDuration = view.findViewById(R.id.song_duration);
        songArtist = view.findViewById(R.id.song_artist);
        imageButton_share = view.findViewById(R.id.spinner);

        // Find the columns of songs attributes that we're interested in
        int titleColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_TITLE);
        int sizeColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_SIZE);
        int durColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_DURATION);
        int artistColumnIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_ARTIST);
        int songFileIndex = cursor.getColumnIndex(EkeDbContract.EkeMusicInfoEntry.COLUMN_SONG_FILE_PATH);

        // Read the songs attributes from the Cursor for the current song
        songName = cursor.getString(titleColumnIndex);
        String songSized = cursor.getString(sizeColumnIndex);
        String songTimed = cursor.getString(durColumnIndex);
        String songOwner = cursor.getString(artistColumnIndex);
        String songFilePath = cursor.getString(songFileIndex);

        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(songFilePath);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        byte[] datamat = mediaMetadataRetriever.getEmbeddedPicture();
        if (datamat != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(datamat, 0, datamat.length);
            albumArt.setImageBitmap(bitmap);
        } else {
            Picasso.with(context)
                    .load(songFilePath)
                    .placeholder(R.drawable.listiconface_35px)   // optional
                    .into(albumArt);
        }

        songTitle.setText(songName);
        songSize.setText(songSized);
        songDuration.setText(songTimed);
        songArtist.setText(songOwner);

        int position = cursor.getPosition();
        boolean isSelected = selectionArray.get(position);
        final int pos = position;

        EkeUIStates ekeUIStates = new EkeUIStates(context);
        int buttonCol = ekeUIStates.loadButtonColor();

        if(buttonCol == 0) {

            if (isSelected) {
                songTitle.setTextColor(Color.BLUE);
                songSize.setTextColor(Color.BLUE);
                songDuration.setTextColor(Color.BLUE);
                songArtist.setTextColor(Color.BLUE);
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 1) {
            if (isSelected) {
                songTitle.setTextColor(Color.RED);
                songSize.setTextColor(Color.RED);
                songDuration.setTextColor(Color.RED);
                songArtist.setTextColor(Color.RED);
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 2) {
            if (isSelected) {
                songTitle.setTextColor(Color.YELLOW);
                songSize.setTextColor(Color.YELLOW);
                songDuration.setTextColor(Color.YELLOW);
                songArtist.setTextColor(Color.YELLOW);
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 3){
                if (isSelected) {
                    songTitle.setTextColor(Color.BLACK);
                    songSize.setTextColor(Color.BLACK);
                    songDuration.setTextColor(Color.BLACK);
                    songArtist.setTextColor(Color.BLACK);
                    //view.setBackgroundColor(Color.YELLOW);
                } else {
                    songTitle.setTextColor(Color.WHITE);
                    songSize.setTextColor(Color.WHITE);
                    songDuration.setTextColor(Color.WHITE);
                    songArtist.setTextColor(Color.WHITE);
                    //view.setBackgroundColor(Color.TRANSPARENT);
                }
        }else if(buttonCol == 4){
            if (isSelected) {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 5){
            if (isSelected) {
                songTitle.setTextColor(Color.GREEN);
                songSize.setTextColor(Color.GREEN);
                songDuration.setTextColor(Color.GREEN);
                songArtist.setTextColor(Color.GREEN);
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 6){
            if (isSelected) {
                songTitle.setTextColor(Color.parseColor("#f908f1"));
                songSize.setTextColor(Color.parseColor("#f908f1"));
                songDuration.setTextColor(Color.parseColor("#f908f1"));
                songArtist.setTextColor(Color.parseColor("#f908f1"));
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 7){
            if (isSelected) {
                songTitle.setTextColor(Color.parseColor("#08f7ef"));
                songSize.setTextColor(Color.parseColor("#08f7ef"));
                songDuration.setTextColor(Color.parseColor("#08f7ef"));
                songArtist.setTextColor(Color.parseColor("#08f7ef"));
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }else if(buttonCol == 8){
            if (isSelected) {
                songTitle.setTextColor(Color.parseColor("#7709ae"));
                songSize.setTextColor(Color.parseColor("#7709ae"));
                songDuration.setTextColor(Color.parseColor("#7709ae"));
                songArtist.setTextColor(Color.parseColor("#7709ae"));
                //view.setBackgroundColor(Color.YELLOW);
            } else {
                songTitle.setTextColor(Color.WHITE);
                songSize.setTextColor(Color.WHITE);
                songDuration.setTextColor(Color.WHITE);
                songArtist.setTextColor(Color.WHITE);
                //view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        imageButton_share.setTag(cursor.getString(1));
        imageButton_share.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
            String sPos = (String) view.getTag();
            Toast.makeText(context, "File:" +sPos, Toast.LENGTH_LONG).show();
            new EkeUIStates(context).storeShareFile(sPos);
            new EkeUIStates(context).storeSearchTitlePos(sPos);
            showDialog();
            }
        });

    }

    private void showDialog() {
        int mStackLevel = 0;
        mStackLevel++;

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = ((FragmentActivity)mContext).getFragmentManager().beginTransaction();
        Fragment prev = ((FragmentActivity)mContext).getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = EkeDialogActivity.newInstance(mStackLevel);
        newFragment.show(ft, "Dialog");
    }

    // Method to mark items in selection
    public void setSelected(int position, boolean isSelected) {
        selectionArray.put(position, isSelected);
    }

}
