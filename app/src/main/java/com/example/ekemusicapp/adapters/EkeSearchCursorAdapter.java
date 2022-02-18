package com.example.ekemusicapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.ekemusicapp.EkeSearchEmpty;
import com.example.ekemusicapp.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ekemini on 10/10/2017.
 */

public class EkeSearchCursorAdapter extends BaseAdapter {

    public Context context;
    private ArrayList<EkeSearchEmpty> songArrayList = new ArrayList<>();
    private LayoutInflater layoutInflater = null;


    public EkeSearchCursorAdapter(Context context, ArrayList<EkeSearchEmpty> songArrayList){
        this.context = context;
        this.songArrayList = songArrayList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return songArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return songArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private static class ViewHolder{
        CircleImageView albumArt;
        TextView songSearchTitle;
    }

    public ViewHolder viewHolder = null;


    // this method  is called each time for arraylist data size.
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        View vi = view;
        if(vi == null){

            // create  viewholder object for list_rowcell View.
            viewHolder = new ViewHolder();
            // inflate list_rowcell for each row
            vi = layoutInflater.inflate(R.layout.activity_eke_search_empty, viewGroup, false);

            viewHolder.albumArt = vi.findViewById(R.id.search_face);
            viewHolder.songSearchTitle = vi.findViewById(R.id.search_empty);

            vi.setTag(viewHolder);

        }else {

            /* We recycle a View that already exists */
            viewHolder= (ViewHolder) vi.getTag();
        }

        viewHolder.albumArt.setImageResource(R.drawable.listiconface_35px);
        viewHolder.songSearchTitle.setText(songArrayList.get(position).getsTitle());

        return vi;
    }


}
