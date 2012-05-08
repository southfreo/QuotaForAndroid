package com.southfreo.quota.activities;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.markupartist.android.widget.ActionBar.*;

import com.markupartist.android.widget.ActionBar;
import com.southfreo.R;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.R.drawable;
import com.southfreo.R.id;
import com.southfreo.R.layout;
import com.southfreo.quota.control.CacheManager;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.control.ProviderManager.NotifyUpdate;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.widgets.*;

import android.app.Activity;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public abstract class DetailActivity extends Activity implements Observer {
	
	private static final String TAG = "Quota-DetailActivity";
	
    private ActionBar actionBar;
 
    private TextView headerLabel;
    private TextView headersubLabel;
    private ImageView providerLogo;
    private TextView errormessage;
    
    LinearLayout detailContent;
    LinearLayout errorContent;
    
    private SlotManager sm;
    private ProviderManager pm;
    private UIManager ui;
    
    protected Provider thisProvider;
    
    
    protected void updateProviderStatus(String s) {
    	headersubLabel.setText(s);
    }
    
    protected void UpdateHeader() {
      	headerLabel.setText(thisProvider.disk_planname);
      	if (thisProvider.loadstatus!=Provider.provider_status_idle) {
         	headersubLabel.setText("Update in progress...");
      	} else {
         	headersubLabel.setText(thisProvider.lastUpdated());
      	}
      	ui.setIconForProvider(providerLogo, thisProvider,thisProvider.provider_definition.loadedFrom);
    }
 
    
    @Override 
    protected void onResume(){
        super.onResume();
        pm.addObserver(this);
        updateDetail();
        if (!thisProvider.disk_manualrefresh) {
            RefreshProvider();
        }
        // Check PIN Screen
        if (SlotManager.getInstance().showPinScreen()) {
            Intent i = new Intent(this,PinEntry.class); 
            i.putExtra(PinEntry.MODE_OP, PinEntry.MODE_VALIDATE);
            startActivityForResult(i,0);
        }

   }
    
    @Override 
    protected void onPause(){
    	super.onPause();
    	pm.deleteObserver(this);
//    	if (UIManager.isApplicationBroughtToBackground(this)) {
//    		// Being pushed to background
//    		SlotManager.getInstance().backgroundapp=true;
//    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.mainmenu,menu);
        return true;
    }
    
    private void sendProblemReport() {
    	CacheManager cm = CacheManager.getInstance();
    	UIManager ui = UIManager.getInstance();
    	
    	String uf="";
       	String body = "";
           	
    	if (thisProvider.provider_definition.secure) {
    		uf = "\nUsage unavailable for this provider type\n";
    	} else {
        	uf = cm.UsageFile(thisProvider);
           	try {
            	String content =NetworkUtils.readFileAsString(cm.DebugFile(thisProvider));
            	body = body + "\n\n"+content;
        	} catch (Exception e) {
        	}
    	}
    	
    	String subject =  ui.AppName() + " for Android (" + UIManager.getInstance().getVersion(this) + ")  Problem report for "+thisProvider.provider_definition.providerName;
    	
  	
    	body = body + Utils.DeviceInfo(this)+"\n\n\nPlease write a short note to explain the problem/suggestion.";
    	
    	NetworkUtils.emailFile(this, "southfreo@me.com", "", subject, body, uf);
    	
    }
    
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.reportproblem:
        	sendProblemReport();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    protected void inProgress(boolean p) {
    	if (actionBar==null) return;			// Landscape mode
    	if (p) {
            actionBar.setProgressBarVisibility(View.VISIBLE);          						    		
    	} else {
            actionBar.setProgressBarVisibility(View.INVISIBLE);          						
    	}
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	// Have we come back from Edit Screen???
    	
    }

    
    protected void showErrorMessage(String s) {
    	detailContent.setVisibility(View.GONE);
    	errorContent.setVisibility(View.VISIBLE);
    	errormessage.setText(s);
    }
    
    protected void hideErrorScreen() {
    	errorContent.setVisibility(View.GONE);
    }
 
    
    protected void hideDetailScreen() {
       	detailContent.setVisibility(View.GONE);
    }
    
    protected void showDetailScreen() {
    	detailContent.setVisibility(View.VISIBLE);
      	errorContent.setVisibility(View.GONE);
    }
    
    	    
    
    
    protected void RefreshProvider() {
    	
    	if (thisProvider.notSetup()) {
    		return;
    	}
    	
    	if (!NetworkUtils.isOnline(this)) {
    		ui.MsgBoxInfo(this, "Network", "You do not appear to be connected to the internet");
    	} else {
    		// Ensure no error message Showing
    		hideErrorScreen();
    		if (!thisProvider.hasCacheExpired() ) {
    			headersubLabel.setText(String.format("Cached: %s (%s)",thisProvider.lastUpdated(),DateUtils.HHMM(thisProvider.CacheSeconds())));
    		} else {
    			// PM Schedule Update code
    			if (thisProvider.loadstatus==Provider.provider_status_idle) {
    				this.inProgress(true);
        			pm.Queue_ScheduleUpdate(thisProvider);
    			}
    		}
    	}
    	
    }
    
 
    public static Intent CreateDetailScreen(Context c,int slot) {
    	
    	Provider p = SlotManager.getInstance().getSlot(slot);
    	Intent i=null;
    	
		 if (p.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
			  	i = new Intent(c,DetailActivity_ISP.class);
   		  	i.putExtra("row", p.slotnumber);
   		  	return i;
		 } else if (p.provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT) {
	  		  	i = new Intent(c,DetailActivity_Account.class);
	  		  	i.putExtra("row", p.slotnumber);
	   		  	return i;
		 } else {
			 // Currently Unsupported..
			 UIManager.getInstance().ToastLong("Unssuported display type");
		 }
		 return null;
    }
    
    private Intent nextIntent() {
		int ns = SlotManager.getInstance().nextSlot();
		Intent i = DetailActivity.CreateDetailScreen(this, ns);
		return i;
    }


    
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState,int myID) {
       	super.onCreate(savedInstanceState);
        // Setup Screen
       	
        UIManager.getInstance().checkValid();
        
       	requestWindowFeature(Window.FEATURE_NO_TITLE);
       	
        setContentView(myID);
        
        // Handle to content
        detailContent = (LinearLayout) findViewById(R.id.detail_container);
        errorContent = (LinearLayout) findViewById(R.id.error_panel);
        
        errormessage  = (TextView)findViewById(R.id.errormessage);
        
       	// Header
       	headerLabel = (TextView) findViewById(R.id.headerlabel);
      	headersubLabel = (TextView) findViewById(R.id.headersublabel);
      	providerLogo = (ImageView)findViewById(R.id.logo);
       	
        // Set Detail
        pm = ProviderManager.getInstance();
        sm = SlotManager.getInstance();
        ui = UIManager.getInstance();
        
        
       	int currentSlotNumber = getIntent().getIntExtra("row",0);
       	
       	int wid = getIntent().getIntExtra("Widgetid",0);
       	
       	
       	// Get the Slot
       	thisProvider = sm.getSlot(currentSlotNumber);
       	
       	if (thisProvider==null) {
      		Toast t = Toast.makeText(this, "Could not find Provider for slot: " +currentSlotNumber+1, Toast.LENGTH_LONG);
      		t.show();
       		this.finish();
       		return;
       	}
       	
       	// Store in Provider Manager this Provider as the edit provider
       	pm.edit_provider_object = thisProvider;
   		pm.edit_provider_id = thisProvider.disk_isp;
   		pm.edit_provider_slot = sm.currentSlot;
 
   		
        // Title Bar
   		
   		
        actionBar = (ActionBar) findViewById(R.id.actionbar);
        if (actionBar!=null) {
        	// Landscape will be null
            actionBar.setTitle((pm.edit_provider_slot+1) + " of "+sm.NoSlots());
            actionBar.setHomeAction(new IntentAction(this, SummaryActivity.createIntent(this), R.drawable.ic_title_home_default));
            //actionBar.setHomeAction(new IntentBack(this,this, R.drawable.ic_title_home_default));
            
//            OnClickListener ri = new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// Check position
//					
//					// Next Slot
//					int ns = SlotManager.getInstance().nextSlot();
//					Intent i = DetailActivity.CreateDetailScreen(this, ns);
//					
//						
//					CreateDetailScreen
//					
//
//		          	//Intent resultIntent = new Intent();
//		        	//resultIntent.putExtra(SummaryActivity.GOTO_NEXT, 1);
//		        	//setResult(Activity.RESULT_OK, resultIntent);
//		        	//finish();
//		        	
//		        	
//				}
//            
//            };
            
            //actionBar.addAction(new ClickAction(this,ri,R.drawable.right));
            actionBar.addAction(new IntentAction(this,nextIntent(),R.drawable.right));
            
            actionBar.addAction(new IntentAction(this, ProviderSettings.createIntent(this), R.drawable.info));           
            
            
            OnClickListener li = new OnClickListener() {
                @Override
                public void onClick(View v) {
                	// Refresh Provider
                	RefreshProvider();
                }
              };
              

		    
            
            actionBar.addAction(new ClickAction(this,li,R.drawable.refresh));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
    }
    
    private Intent createShareIntent() {
  	  
//  	  Toast.makeText(this, "Button", Toast.LENGTH_SHORT).show();
  	  
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from the ActionBar widget.");
        return Intent.createChooser(intent, "Share");
    }

    // Must be called from UI Thread
    public void updateUI(Observable observable, Object data) {


		NotifyUpdate nu = (NotifyUpdate)data;
		
		if (nu.p==thisProvider) {
			if (nu.msg == ProviderManager.PM_EVENT_IGNORED) {
				updateProviderStatus(thisProvider.lastUpdated());
			}
			
			if (nu.msg == ProviderManager.PM_LOADING_START) {
				updateProviderStatus("In progress...");
			}
			
			if (nu.msg == ProviderManager.PM_EVENT_UPDATE) {
				updateProviderStatus(nu.p.loadmsg);
			}
			
			if (nu.msg == ProviderManager.PM_EVENT_COMPLETE) {
				updateProviderStatus(thisProvider.lastUpdated());
				this.inProgress(false);
				updateDetail();
			}
			
			if (nu.msg == ProviderManager.PM_EVENT_FAIL) {
				updateProviderStatus(thisProvider.getFailureCode());
				this.inProgress(false);
				showErrorMessage(thisProvider.loaderrormsg);
			}
			
			if (nu.msg == ProviderManager.PM_EVENT_ASKQUESTION) {
				updateProviderStatus("Selection in progress");
				if (thisProvider.questionchoices==null || thisProvider.questionchoices.length==0) {
					// Abort
					thisProvider.questionAnswered=true;
				} else {
					UIManager.getInstance().showProviderChoice(thisProvider,this, thisProvider.questionchoices);
				}
			}
		}
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
	
    protected void updateDetail() {
    	
    	if (thisProvider.notSetup()) {
    		return;
    	}
    	
	   	// Update Icon
	   	UpdateHeader();
	   	
	   	 
	   	// Do we have an Error
	   	if (!thisProvider.loadsuccess && !Utils.isBlank(thisProvider.loaderrormsg)) {
	   		showErrorMessage(thisProvider.loaderrormsg);
	   		return;
	   	}
	   	
	   	if (!thisProvider.hasData()) {
	   		hideDetailScreen();
	   		return;
	   	}
	   	
	   	showDetailScreen();
    	
    }
	   
    
}