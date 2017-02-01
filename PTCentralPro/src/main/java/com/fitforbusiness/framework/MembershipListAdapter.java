package com.fitforbusiness.framework;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class MembershipListAdapter extends ArrayAdapter<HashMap<String, Object>> {

    private ArrayList<HashMap<String, Object>> mapArrayList;
    private Context context;
    private int customRowResourceId;
    private int textView1ResourceId;
    private int textView2ResourceId;
    private int textView3ResourceId;
    private int textView4ResourceId;

    public MembershipListAdapter(Context context, int customRowResourceId,
                                 int textView1ResourceId, int textView2ResourceId,
                                 int textView3ResourceId, int textView4ResourceId,
                                 ArrayList<HashMap<String, Object>> mapList) {
        super(context, customRowResourceId, mapList);
        this.customRowResourceId = customRowResourceId;
        this.context = context;
        this.textView1ResourceId = textView1ResourceId;
        this.textView2ResourceId = textView2ResourceId;
        this.textView3ResourceId = textView3ResourceId;
        this.textView4ResourceId = textView4ResourceId;
        this.mapArrayList = new ArrayList<HashMap<String, Object>>();
        this.mapArrayList.addAll(mapList);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Log.d("ConvertView", String.valueOf(position));
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(customRowResourceId, null);
            holder = new ViewHolder();
            assert convertView != null;
            holder.textView1 = (TextView) convertView
                    .findViewById(textView1ResourceId);
            holder.textView2 = (TextView) convertView
                    .findViewById(textView2ResourceId);
            holder.textView3 = (TextView) convertView
                    .findViewById(textView3ResourceId);
            holder.textView4 = (TextView) convertView
                    .findViewById(textView4ResourceId);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, Object> map = mapArrayList.get(position);
        holder.textView1.setText(map.get("firstLabel").toString() != null ? map.get("firstLabel").toString() : "");
        holder.textView2.setText(map.get("secondLabel").toString() != null ? map.get("secondLabel").toString() : "");
        holder.textView3.setText(map.get("thirdLabel").toString() != null ? map.get("thirdLabel").toString() : "");
        holder.textView4.setText(map.get("fourthLabel").toString() != null ? map.get("fourthLabel").toString() : "");
        return convertView;
    }

    private class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        TextView textView4;
    }
}
