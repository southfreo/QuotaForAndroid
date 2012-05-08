package com.southfreo.quota.model;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;


public class extraTableAdapter extends BaseAdapter {

	private Context context;

	public ArrayList<extradata> mData;
	
    public extraTableAdapter(Context c) {
    	context = c;
    }
    
	@Override
	public int getCount() {
		if (mData!=null){
			return mData.size()*2;
		} else {
			return 0;
		}
	}

	
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		   TextView tv;
		   
		   int row      = position/2;
		   boolean name = (position%2)==0;
		   
		    if (convertView == null) {
		        tv = new TextView(context);
		        tv.setTextColor(Color.WHITE);
		        
		        //tv.setLayoutParams(new GridView.LayoutParams(85, 20));
		    }
		    else {
		        tv = (TextView) convertView;
		    }
		    extradata ed = mData.get(row);
		    if (name) {
			    tv.setText(ed.name);
		    } else {
			    tv.setText(ed.value);
		    }
		    return tv;
	}

	
}
