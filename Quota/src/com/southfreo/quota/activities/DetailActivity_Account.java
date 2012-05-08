package com.southfreo.quota.activities;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.markupartist.android.widget.ActionBar.*;

import com.markupartist.android.widget.ActionBar;
import com.southfreo.R;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.account;
import com.southfreo.quota.model.accountlines;
import com.southfreo.quota.model.extraTableAdapter;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.model.rssItem;
import com.southfreo.R.drawable;
import com.southfreo.R.id;
import com.southfreo.R.layout;
import com.southfreo.quota.activities.SummaryActivity.EfficientAdapter.ViewHolder;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.control.ProviderManager.NotifyUpdate;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.widgets.*;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class DetailActivity_Account extends DetailActivity implements Observer {
	private static final String TAG = "Quota-DetailActivity_Account";
	
	private TextView textLine1;
	private TextView textLine2;
	private RelativeLayout rssView;
	private RelativeLayout summaryView;
	private ListView rssTable;
	
	// Used for Extras
	private RelativeLayout extraTable;
	private GridView gridview;
    
    protected void  updateDetail() {
        
    	super.updateDetail();
    	
    	try {
    		
    		
    		
    		textLine1.setText("");
       		textLine2.setText("");
       	     		
    		if (thisProvider.accountdata!=null) {
    			if (thisProvider.accountdata.isRssFeed()) {
    	       		rssView.setVisibility(View.VISIBLE);
    	          	rssTable = (ListView)findViewById(R.id.rssTableList);
    	          	rssTable.setAdapter(new rssAdapter(this,thisProvider.accountdata.rssData));
    	          	
    	          	rssTable.setClickable(true);
    	          	
    	          	rssTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {

    	          	  @Override
    	          	  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

    	          	    rssItem  rss = (rssItem)rssTable.getItemAtPosition(position);
    	          	    UIManager.getInstance().openWebsite(DetailActivity_Account.this, rss.link);
    	          	    
    	          	  }
    	          	});
    			} else if (thisProvider.accountdata.hasAccountData()) {
       	       		rssView.setVisibility(View.VISIBLE);
    	          	rssTable = (ListView)findViewById(R.id.rssTableList);
    	          	rssTable.setAdapter(new accountAdapter(this,thisProvider.accountdata.transactions));
    	          	
    	          	rssTable.setClickable(true);
    	          	
    	          	rssTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {

    	          	  @Override
    	          	  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

    	          	    accountlines  acc = (accountlines)rssTable.getItemAtPosition(position);
    	          	    // Show Alert
    	          	    UIManager.getInstance().MsgBoxInfo(DetailActivity_Account.this, "Detail", acc.date+"\n\n"+acc.description+"\n\n"+acc.amount);
    	          	    
    	          	  }
    	          	});
    				
    			} else if (thisProvider.hasExtras()) {
    				// Show Extra Table
    				extraTable = (RelativeLayout)findViewById(R.id.extratable);
    				if (extraTable!=null) {
    	   				extraTable.setVisibility(View.VISIBLE);

        		        gridview = (GridView) findViewById(R.id.gridview);
        		        if (gridview!=null) {
        		            gridview.setSelector(new ColorDrawable(Color.TRANSPARENT));   
        		            extraTableAdapter edAdapt = new extraTableAdapter(this);
        		        	gridview.setAdapter(edAdapt);
        		        	edAdapt.mData = thisProvider.extras;
        		        }
    				}
     				
    			}
    			
    			
    			if (thisProvider.accountdata.hidesummary) {
    				summaryView.setVisibility(View.GONE);
    			} else {
    	   	   		textLine1.setText(Utils.BlankString(thisProvider.accountdata.bal1name) + " " + Utils.BlankString(thisProvider.accountdata.bal1value));
          	   		textLine2.setText(Utils.BlankString(thisProvider.accountdata.bal2name) + " " + Utils.BlankString(thisProvider.accountdata.bal2value));
     			}
    		}
    	} catch (Exception e) {
    		Log.e(TAG,"Problem displaying detail"+e.toString());
    	}
        
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       	super.onCreate(savedInstanceState,R.layout.activity_detailview_account);
       	
       	// Get the Controls
		textLine1 = (TextView) findViewById(R.id.header1);
   		textLine2 = (TextView) findViewById(R.id.header2);
       	rssView = (RelativeLayout)findViewById(R.id.rssTable);
       	
       	summaryView =  (RelativeLayout)findViewById(R.id.accsummary);
    }
    
    // Account Adapter
    public static class accountAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater mInflater;
        private Context context;
        private ArrayList<accountlines> items;
        
        public accountAdapter(Context context,ArrayList<accountlines> items) {
          // Cache the LayoutInflate to avoid asking for a new one each time.
          mInflater = LayoutInflater.from(context);
          this.context = context;
          this.items = items;
          
          
        }
        
 
        /**
         * Make a view to hold each row.
         * 
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(final int position, View convertView, ViewGroup parent) {
          ViewHolder holder;


          if (convertView == null) {
            convertView = mInflater.inflate(R.layout.accountitem, parent,false);

            // Creates a ViewHolder and store references to the two children
            // views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.col1 = (TextView) convertView.findViewById(R.id.col1);
            holder.col2 = (TextView) convertView.findViewById(R.id.col2);
            holder.col3 = (TextView) convertView.findViewById(R.id.col3);
 
            convertView.setTag(holder);
          } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
          }
          

          // Get rssItem 
          
          accountlines acc = items.get(position);
          holder.col1.setText(acc.date);
          holder.col2.setText(acc.description);
          holder.col3.setText(acc.amount);
          
          return convertView;
        }
        
        static class ViewHolder {
          TextView col1;
          TextView col2;
          TextView col3;
        }
        

        @Override
        public Filter getFilter() {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public int getCount() {
          if (items==null) return 0;
          return items.size();
        }

        @Override
        public Object getItem(int position) {
          return items.get(position);
        }

      }
 
    // RSS Adapter
    public static class rssAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater mInflater;
        private Context context;
        private ArrayList<rssItem> items;
        
        public rssAdapter(Context context,ArrayList<rssItem> items) {
          // Cache the LayoutInflate to avoid asking for a new one each time.
          mInflater = LayoutInflater.from(context);
          this.context = context;
          this.items = items;
          
          
        }
        
 
        /**
         * Make a view to hold each row.
         * 
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(final int position, View convertView, ViewGroup parent) {
          ViewHolder holder;


          if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rssitem, parent,false);

            // Creates a ViewHolder and store references to the two children
            // views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.textLine = (TextView) convertView.findViewById(R.id.headerlabel);
            holder.bodyText = (TextView) convertView.findViewById(R.id.bodytext);
 
            convertView.setTag(holder);
          } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
          }
          

          // Get rssItem 
          
          rssItem rss = items.get(position);
          holder.textLine.setText(rss.title);
          holder.bodyText.setText(rss.description);
          
          return convertView;
        }
        
        

        static class ViewHolder {
          TextView textLine;
          TextView bodyText;
        }

        @Override
        public Filter getFilter() {
          // TODO Auto-generated method stub
          return null;
        }

        @Override
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public int getCount() {
          if (items==null) return 0;
          return items.size();
        }

        @Override
        public Object getItem(int position) {
          return items.get(position);
        }

      }
    
   
}