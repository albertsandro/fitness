package com.fitforbusiness.framework;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.fitforbusiness.nafc.session.GoalSummaryActivity;

import java.util.ArrayList;
import java.util.HashMap;


public class SessionCustomAdapter extends ArrayAdapter<HashMap<String, Object>> {

    private ArrayList<HashMap<String, Object>> mapArrayList;
    private Context context;
    private int customRowResourceId;
    private int button1ResourceId;
    private int textView1ResourceId;
    private int textView2ResourceId;
    private int textView3ResourceId;

    public SessionCustomAdapter(Context context, int customRowResourceId, int button1ResourceId,
                                int textView1ResourceId, int textView2ResourceId, int textView3ResourceId,
                                ArrayList<HashMap<String, Object>> mapList) {
        super(context, customRowResourceId, mapList);
        this.customRowResourceId = customRowResourceId;
        this.context = context;
        this.button1ResourceId = button1ResourceId;
        this.textView1ResourceId = textView1ResourceId;
        this.textView2ResourceId = textView2ResourceId;
        this.textView3ResourceId = textView3ResourceId;
        this.mapArrayList = new ArrayList<HashMap<String, Object>>();
        this.mapArrayList.addAll(mapList);
    }

    private class ViewHolder {
        TextView textView1;
        TextView textView2;
        TextView textView3;
        Button button1;
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
            holder.button1 = (Button) convertView
                    .findViewById(button1ResourceId);
            holder.textView1 = (TextView) convertView
                    .findViewById(textView1ResourceId);
            holder.textView2 = (TextView) convertView
                    .findViewById(textView2ResourceId);
            holder.textView3 = (TextView) convertView
                    .findViewById(textView3ResourceId);
            convertView.setTag(holder);

            holder.button1.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    context.startActivity(new Intent(context,
                            GoalSummaryActivity.class).putExtra("_id",
                             mapArrayList.get(position).get("_id").toString()));
                }
            });

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, Object> map = mapArrayList.get(position);

        holder.textView1.setText(map.get("firstLabel").toString() != null ? map.get("firstLabel").toString() : "");
        holder.textView2.setText(map.get("secondLabel").toString() != null ? map.get("secondLabel").toString() : "");
        holder.textView3.setText(map.get("thirdLabel").toString() != null ? map.get("thirdLabel").toString() : "");


        return convertView;

    }
}
