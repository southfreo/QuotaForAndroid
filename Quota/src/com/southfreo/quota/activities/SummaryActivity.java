package com.southfreo.quota.activities;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.*;
import com.southfreo.R;
import com.southfreo.R.drawable;
import com.southfreo.R.id;
import com.southfreo.R.layout;
import com.southfreo.R.string;
import com.southfreo.quota.Dialogs.HelpDialog;
import com.southfreo.quota.billing.Billing;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.control.ProviderManager.NotifyUpdate;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.widgets.TextProgressBar;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SummaryActivity extends ListActivity implements Observer {
  private EfficientAdapter adap;
  private static final String TAG = "Application";
  public static String GOTO_NEXT = "NEXT_PROVIDER";
  private ProviderManager pm;
  private SlotManager sm;
  private ActionBar actionBar;
  private UIManager ui;
  
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    
    setContentView(R.layout.activity_summary);
    
    // Title Bar
    actionBar = (ActionBar) findViewById(R.id.actionbar);
    actionBar.setTitle("Summary");
    
    OnClickListener sh = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	// Refresh Provider
            UIManager.getInstance().showWelcomeMessage(true,SummaryActivity.this);
        }
      };
      
    actionBar.addAction(new IntentAction(this, AppSettingsConfigure.createIntent(this), R.drawable.info));           
      
    actionBar.setHomeAction(new ClickAction(this, sh, R.drawable.quota64));
    
    actionBar.addAction(new IntentAction(this, createAddIntent(), R.drawable.add2));
    
    OnClickListener li = new OnClickListener() {
        @Override
        public void onClick(View v) {
        	// Refresh Provider
            RefreshAllProviders();
        }
      };
      
    actionBar.addAction(new ClickAction(this, li, R.drawable.refresh));
 
    
    // List Setup
    adap = new EfficientAdapter(this);
    setListAdapter(adap);
    
    pm = ProviderManager.getInstance();
 
    sm = SlotManager.getInstance();
    ui = UIManager.getInstance();
    
    registerForContextMenu(getListView());
    
    // Show Welcome or Die
    UIManager.getInstance().checkValid();
    ui.showWelcomeMessage(false,this);
    

}

  @Override 
  protected void onResume(){
      super.onResume();
      // Refresh ListView
      pm.addObserver(this);
      adap.notifyDataSetChanged();
      UpdateTitle();
      
      // Check PIN Screen
      if (SlotManager.getInstance().showPinScreen()) {
          Intent i = new Intent(this,PinEntry.class); 
          i.putExtra(PinEntry.MODE_OP, PinEntry.MODE_VALIDATE);
          startActivityForResult(i,0);
      }
      
 }
 

	
  @Override
   public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {
  
    if (v==getListView()) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
      menu.setHeaderTitle("Provider Options");
  
      menu.add(0, 0, 0, "Delete");
      menu.add(1, 1, 1, "Force refresh");
       
    }
  
  }

  private void RefreshAllProviders() {
  	
  	if (!NetworkUtils.isOnline(this)) {
  		UIManager.getInstance().MsgBoxInfo(this, "Network", "You do not appear to be connected to the internet");
  	} else {
  		// Ensure no error message Showing
  		for (int i=0;i<sm.NoSlots();i++) {
  			Provider p = sm.slotArray.get(i);
  			if (p.hasCacheExpired()) {
  				pm.Queue_ScheduleUpdate(p);
  			}
  		}
  	}
  	
  }
  
  public boolean onContextItemSelected(MenuItem item) {
     int menuItemIndex = item.getItemId();

	  switch (menuItemIndex) {
	  case 0: {
		  // Delete
			if (!UIManager.getInstance().CheckPerformanceFull()) {
				UIManager.getInstance().MsgBoxInfo(this, "Delete Provider", "In the Lite version, you can only setup a single provider.\n\nTo change this provider, Tap the provider then tap i and select a new Provider");
			} else {
			    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			    SlotManager sm = SlotManager.getInstance();
			    sm.deleteSlotSaveAll(info.position);
			    adap.notifyDataSetChanged();
			}
			break;
	  }
	  case 1: {
		  // Force Refresh
  	      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		  Provider p = sm.slotArray.get(info.position);
   		  pm.Queue_ScheduleUpdate(p);
		  break;
	  }
	  }
	  
		  

    return true;
  }

  public static Intent createIntent(Context context) {
      Intent i = new Intent(context, SummaryActivity.class);
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      return i;
  }
  
  private Intent createShareIntent() {
	  
      final Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
      return Intent.createChooser(intent, "Share");
  }
  
  private Intent createAddIntent() {
	    
	  // Change to Load the ChooseProviderActivity Direct, on return it should choose the next Intent
	  
	  if (UIManager.getInstance().CheckPerformanceFull()) {
		  Intent i = new Intent(this,ChooseProviderActivity.class);
		  i.putExtra("addscreen", true);
		  return i;
	  } else {
		  Intent i = new Intent(this,Billing.class);
		  return i;
	  }
	  
  }
  
  private void showDetail(int row) {
	  Intent i = DetailActivity.CreateDetailScreen(this, row);
	  startActivityForResult(i, 0);
  }
  
  
  
  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);
	  showDetail(position);
  }
  
  @Override 
  protected void onPause(){
  	super.onPause();
  	pm.deleteObserver(this);
//	if (UIManager.isApplicationBroughtToBackground(this)) {
//		// Being pushed to background
//		SlotManager.getInstance().backgroundapp=true;
//	}
  }
  
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
	  // Check for Next Functionality
	  if (data!=null) {
			int gotoNext = data.getIntExtra(GOTO_NEXT,-1);
			
			if (gotoNext==1) {
				// Update Current Row
				int cr = ProviderManager.getInstance().edit_provider_slot;
				if (cr<SlotManager.getInstance().NoSlots()-1) {
					cr++;
				} else {
					cr=0;
				}
				showDetail(cr);
			}
		  
	  }
		
	  
  }
      
  // Icons bound to the rows.
//if (id != 0x0) {
//  mIcon1 = BitmapFactory.decodeResource(context.getResources(), id);
//}
//holder.iconLine.setImageBitmap(mIcon1);

  

  public static class EfficientAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater mInflater;
    private Context context;
    private ProviderManager pm;
    private SlotManager sm;
    private UIManager ui;
    
    public EfficientAdapter(Context context) {
      // Cache the LayoutInflate to avoid asking for a new one each time.
      mInflater = LayoutInflater.from(context);
      this.context = context;
      pm = ProviderManager.getInstance();
      ui = UIManager.getInstance();
      sm=SlotManager.getInstance();
      
    }
    
    private void ProgressBarsOff(ViewHolder vh) {
    	vh.p1red.setVisibility(View.GONE);
       	vh.p1gray.setVisibility(View.GONE);
       	vh.p2red.setVisibility(View.GONE);
       	vh.p2gray.setVisibility(View.GONE);
    }
    

  private void setupProgress(kbProgressBar pb, ViewHolder vh, int index) {
    	
    	TextProgressBar bar;
    	
        if (pb.overIdeal()) {
        	// Over Usage
        	if (index==1) {
         		vh.p1red.setVisibility(View.VISIBLE);
         		vh.p1gray.setVisibility(View.GONE);
        		bar = vh.p1red;
        	} else {
        		vh.p2red.setVisibility(View.VISIBLE);
        		vh.p2gray.setVisibility(View.GONE);
        		bar = vh.p2red;
        	}
        	bar.setSecondaryProgress((int)pb.percentage);
        	bar.setProgress((int)pb.ideal);
        } else {
        	// Normal
          	if (index==1) {
          		vh.p1gray.setVisibility(View.VISIBLE);
          		vh.p1red.setVisibility(View.GONE);
          		bar = vh.p1gray;
        	} else {
        		vh.p2gray.setVisibility(View.VISIBLE);
        		vh.p2red.setVisibility(View.GONE);
          		bar = vh.p2gray;
        	}
          	bar.setSecondaryProgress((int)pb.ideal);
          	bar.setProgress((int)pb.percentage);
        }
        
        bar.setMax(100);
        bar.setText(pb.percentage_msg);  
    
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
        convertView = mInflater.inflate(R.layout.summaryitem, parent,false);

        // Creates a ViewHolder and store references to the two children
        // views
        // we want to bind data to.
        holder = new ViewHolder();
        holder.textLine = (TextView) convertView.findViewById(R.id.headerlabel);
        holder.textLine2 = (TextView) convertView.findViewById(R.id.headersublabel);
               
        holder.iconLine = (ImageView) convertView.findViewById(R.id.logo);
        
        // Summary stuff
        holder.summarypanel = (LinearLayout) convertView.findViewById(R.id.summarypanel);
        holder.p1red = (TextProgressBar) convertView.findViewById(R.id.bar_1_red);
        holder.p1gray = (TextProgressBar) convertView.findViewById(R.id.bar_1_gray);
        holder.p2red= (TextProgressBar) convertView.findViewById(R.id.bar_2_red);
        holder.p2gray= (TextProgressBar) convertView.findViewById(R.id.bar_2_gray);
        holder.summaryText = (TextView) convertView.findViewById(R.id.txtSummary);

        convertView.setTag(holder);
      } else {
        // Get the ViewHolder back to get fast access to the TextView
        // and the ImageView.
        holder = (ViewHolder) convertView.getTag();
      }
      
      Provider p = sm.getSlot(position);
     
      ui.setIconForProvider(holder.iconLine, p,p.provider_definition.loadedFrom);
      holder.textLine.setText(p.disk_planname);
      
   	  if (p.loadstatus!=Provider.provider_status_idle) {
   		 holder.textLine2.setText("Update in progress...");
   		 holder.summarypanel.setVisibility(View.INVISIBLE);
  	  } else {
  		 if (!p.loadsuccess) {
  		  	 holder.textLine2.setText(p.getFailureCode());
		 } else {
	  		 holder.textLine2.setText(p.lastUpdated());
		 }
   		 holder.summarypanel.setVisibility(View.VISIBLE);
   		 
   		 // Update based on type
   		 if (!p.notSetup() && p.hasData()) {
   			 if (p.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
   				 
   				 // Turn off text
   				 holder.summaryText.setVisibility(View.GONE);
   		
   				 
   				// Get Progress Bars
   			   	if (p.progress !=null && p.progress.size()>0) {
   	            	kbProgressBar pb1 = p.progress.get(0);
   	            	setupProgress(pb1,holder,1);
   	        	}
  			   	if (p.progress !=null && p.progress.size()==2) {
   	            	kbProgressBar pb2 = p.progress.get(1);
   	            	setupProgress(pb2,holder,2);
   	        	} else {
   	        		// Hide it
   	        		holder.p2red.setVisibility(View.GONE);
  	        		holder.p2gray.setVisibility(View.GONE);
   	        	}
   				
   			 } else if (p.provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT){
   				 // Turn off Progress
   				ProgressBarsOff(holder);
   				
   				holder.summaryText.setVisibility(View.VISIBLE);
   				
   				holder.summaryText.setText(p.AccountSummaryText());
   			 }
   			 
   			 
   		 } else {
   	 		 holder.summarypanel.setVisibility(View.INVISIBLE);
   		 }
   		 
  	  }
      
      return convertView;
    }
    
    

    static class ViewHolder {
      TextView textLine;
      TextView textLine2;
      ImageView iconLine;
      Button buttonLine;
      
      LinearLayout summarypanel;
      TextProgressBar p1red;
      TextProgressBar p1gray;
      TextProgressBar p2red;
      TextProgressBar p2gray;
      TextView summaryText;
      
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
      return SlotManager.getInstance().NoSlots();
    }

    @Override
    public Object getItem(int position) {
      // TODO Auto-generated method stub
      return SlotManager.getInstance().getSlot(position);
    }

  }

  private void inProgress(boolean p) {
  	if (p) {
          actionBar.setProgressBarVisibility(View.VISIBLE);          						    		
  	} else {
          actionBar.setProgressBarVisibility(View.INVISIBLE);          						
  	}
  }
  
  private void UpdateTitle() {
	if (pm.q_pendingcount>0 || pm.q_activecount>0) {
       inProgress(true);
       actionBar.setTitle(String.format("Updating : %d (%d)",pm.q_activecount,pm.q_pendingcount));
	} else {
	    inProgress(false);
	    actionBar.setTitle("Summary"); 
	}
	
  }
  
public void updateUI(Observable observable, Object data) {

	NotifyUpdate nu = (NotifyUpdate)data;
	
	if (nu.msg == ProviderManager.PM_EVENT_ASKQUESTION) {
		nu.p.questionAnswered=true;
		nu.p.loadmsg="Action required in detail screen";
	}

	adap.notifyDataSetChanged();
	UpdateTitle();
}

@Override
public void update(final Observable observable, final Object data) {
    
	runOnUiThread(
        new Runnable() {
             public void run() {
            	  updateUI(observable,data);
             }
         }
     );
	
	
}


}

