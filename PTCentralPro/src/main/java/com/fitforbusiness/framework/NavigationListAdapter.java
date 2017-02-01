package com.fitforbusiness.framework;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Ratnesh on 7/29/2014.
 */
public class NavigationListAdapter extends ArrayAdapter<HashMap<String, Object>> {
    private Context context;
    private int rowResourceId;
    private int imageViewResourceId;
    private int textViewLabelResourceId;
    private int textViewCountResourceId;
    private ArrayList<HashMap<String, Object>> mapArrayList;

    public NavigationListAdapter(Context context, int rowResourceId, int imageViewResourceId, int textViewLabelResourceId, int textViewCountResourceId,
                                 ArrayList<HashMap<String, Object>> mapArrayList) {
        super(context, rowResourceId, mapArrayList);
        this.mapArrayList = new ArrayList<HashMap<String, Object>>();
        this.context = context;
        this.rowResourceId = rowResourceId;
        this.imageViewResourceId = imageViewResourceId;
        this.textViewCountResourceId = textViewCountResourceId;
        this.textViewLabelResourceId = textViewLabelResourceId;
        this.mapArrayList = mapArrayList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(rowResourceId, null);
        if (convertView != null) {
            holder = new Holder();
            ImageView image = (ImageView) convertView.findViewById(imageViewResourceId);
            TextView textLabel = (TextView) convertView.findViewById(textViewLabelResourceId);
            TextView textCount = (TextView) convertView.findViewById(textViewCountResourceId);
            holder.imageView = image;
            holder.textViewCount = textCount;
            holder.textViewLabel = textLabel;

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        HashMap<String, Object> map = mapArrayList.get(position);
        holder.textViewLabel.setText(map.get("title").toString());

        if (map.get("count").toString().equalsIgnoreCase("No")) {
            holder.textViewCount.setVisibility(View.GONE);
        } else {
            if (Integer.parseInt(map.get("count").toString()) > 99){
                holder.textViewCount.setText("99+");
            }else {
                holder.textViewCount.setText(map.get("count").toString());
            }
        }
        try {
            holder.imageView.setImageDrawable((Drawable) map.get("icon"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;


    }

    public class Holder {
        ImageView imageView;
        TextView textViewLabel;
        TextView textViewCount;
    }


}
