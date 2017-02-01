package com.fitforbusiness.framework;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fitforbusiness.nafc.R;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarAdapter extends BaseAdapter {
    static final int FIRST_DAY_OF_WEEK = 0; // Sunday = 0, Monday = 1
    // references to our items
    public String[] days;
    private Context mContext;
    private Calendar month;
    private Calendar selectedDate;
    private ArrayList<String> items;

    public CalendarAdapter(Context c, Calendar monthCalendar) {
        month = monthCalendar;
        //selectedDate = (Calendar) monthCalendar.clone();
        selectedDate = Calendar.getInstance();
        mContext = c;
        month.set(Calendar.DAY_OF_MONTH, 1);
        this.items = new ArrayList<String>();
        refreshDays();
    }

    public void setItems(ArrayList<String> items) {
        for (int i = 0; i != items.size(); i++) {
            if (items.get(i).length() == 1) {
                items.set(i, "0" + items.get(i));
            }
        }
        this.items = items;
    }

    public int getCount() {
       /* if (days.length == 28)
            return days.length;
        else if (days.length > 28 && days.length < 35)
            return 35;
        else if (days.length == 35)
            return days.length;
        else*/
            return days.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new view for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        TextView dayView;
        if (convertView == null) { // if it's not recycled, initialize some
            // attributes
            LayoutInflater vi = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.calendar_item, null);

        }
        dayView = (TextView) v.findViewById(R.id.date);

        // disable empty days from the beginning
        if (position < days.length) {
            if (days[position].equals("")) {
                dayView.setClickable(false);
                dayView.setFocusable(false);
              //  v.setBackgroundResource(R.drawable.custom_btn_gray);
            } else {
                // mark current day as focused
                if (month.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                        && month.get(Calendar.MONTH) == selectedDate
                        .get(Calendar.MONTH)
                        && days[position].equals(""
                        + selectedDate.get(Calendar.DAY_OF_MONTH))) {
                    dayView.setBackgroundResource(R.drawable.custom_dark_circle);
                    dayView.setTextColor(Color.WHITE);
                } else {
                    dayView.setTextColor(Color.BLACK);
                  //  v.setBackgroundResource(R.drawable.custom_btn_green);
                }
            }
            dayView.setText(days[position]);

            // create date string for comparison
            String date = days[position];

            if (date.length() == 1) {
                date = "0" + date;
            }
            String monthStr = "" + (month.get(Calendar.MONTH) + 1);
            if (monthStr.length() == 1) {
                monthStr = "0" + monthStr;
            }

            // show icon if date is not empty and it exists in the items array
            LinearLayout ll = (LinearLayout) v.findViewById(R.id.booked_item);
            ImageView iw = (ImageView) v.findViewById(R.id.date_icon);
            if (date.length() > 0 && items != null && items.contains(date)) {
                iw.setVisibility(View.VISIBLE);
                // ll.setBackgroundResource(R.drawable.booked);//(R.drawable.booked);
            } else {
                //iw.setVisibility(View.INVISIBLE);
            }
        } else {
            dayView.setText("");//(position - days.length + 1) +
            dayView.setClickable(false);
            dayView.setFocusable(false);
           // v.setBackgroundResource(R.drawable.custom_btn_gray);
        }
        return v;
    }

    public void refreshDays() {
        // clear items
        items.clear();

        int lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH);
        Log.d("last day of week is", "" + lastDay);
        int firstDay = (int) month.get(Calendar.DAY_OF_WEEK);
        Log.d("last day of week is", "" + lastDay);
        // figure size of the array
        if (firstDay == 1) {
            Log.d("firstDay day of week is", "" + firstDay);
            days = new String[lastDay + (FIRST_DAY_OF_WEEK * 6)];
            //days=new String[42];
        } else {
            days = new String[lastDay + firstDay - (FIRST_DAY_OF_WEEK + 1)];
            //days=new String[42];
        }

        int j = FIRST_DAY_OF_WEEK;

        // populate empty days before first real day
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(month.getTime());

        if (firstDay > 1) {
            for (j = 0; j < firstDay - FIRST_DAY_OF_WEEK; j++) {
                //calendar.add(Calendar.DAY_OF_MONTH,-(firstDay - FIRST_DAY_OF_WEEK));

                days[j] = "";
            }
        } else {
            for (j = 0; j < FIRST_DAY_OF_WEEK * 6; j++) {
                days[j] = "";
            }
            j = FIRST_DAY_OF_WEEK * 6 + 1; // sunday => 1, monday => 7
        }

        // populate days
        int dayNumber = 1;
        for (int i = j - 1; i < days.length; i++) {
            days[i] = "" + dayNumber;
            dayNumber++;
        }
        /*if(days.length<=42){
			for(int k = lastDay; k<42;k++){

				days[k] = "" ;
			}

		}*/
    }

}