package com.southfreo.quota.widgets;



import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import com.southfreo.R;
import com.southfreo.quota.activities.DetailActivity;
import com.southfreo.quota.activities.SummaryActivity;
import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.control.ProviderManager.NotifyUpdate;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.cycle;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.Utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

public class summarywidget extends AppWidgetProvider implements Observer {

	private final static String TAG = "Quota-SummaryWidget_Large";
	
	private ProviderManager pm;
	
	private Context oContext;
	private AppWidgetManager oWidgetManager;
	private int[] oIDs;
	
    @Override
    public void onEnabled(Context context) {
        Log.i(TAG,"onEnabled()");
    	      	  
    	
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

	    Log.i(TAG,"Removing Widgets");
		try {
	        // Perform this loop procedure for each App Widget that belongs to this provider
	        for (int i=0; i<N; i++) {
	        	int appWidgetId = appWidgetIds[i];
	        	SlotManager.getInstance().preferences.edit().remove("widget_id"+appWidgetId).commit();
	        }
		} catch (Exception e) {
		    Log.i(TAG,"Problem removing widgets");
		}
    }
    
    

	public void update(Observable observable, Object data) {
		NotifyUpdate nu = (NotifyUpdate)data;
		
	    Log.i(TAG,"Observer-updatingWidget");
	    
		//ComponentName me=new ComponentName(oContext, summarywidget.class);
		//oWidgetManager.updateAppWidget(me, buildUpdate(oContext, oIDs));
	
	    // Should only update the Provider attached to this widget
	    Provider p = nu.p;
	    if (p!=null && p.widgetid>0) {
	    	int[] pids = new int[1];
	    	
	    	Log.i(TAG,"Pinging Widget:"+p.widgetid + " "+p.disk_planname);
	    	
	    	pids[0] = p.widgetid;
			//this.onUpdate(oContext, oWidgetManager, pids);
			this.onUpdate(oContext, oWidgetManager, oIDs);

	    } else {
	    	// Ping All
	    	if (nu.msg==ProviderManager.PM_WIDGET_UPDATE) {
		    	Log.i(TAG,"Pinging ALL Widgets");
				this.onUpdate(oContext, oWidgetManager, oIDs);
	    	} else {
		    	Log.i(TAG,"Widget - ignoring update message");
	    	}
	    	
	    }
		
		
	}

	
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	        Log.i(TAG,"onUpdate() ID:"+appWidgetIds.toString());
	       
			   // To prevent any ANR timeouts, we perform the update in a service
		    pm = ProviderManager.getInstance();
		   	pm.addObserver(this);

		   	
		   	
	        // Hang onto for Observer updates
	        oContext = context;
	        oWidgetManager = appWidgetManager;
	        oIDs = appWidgetIds;
		    
			final int N = appWidgetIds.length;

	        // Perform this loop procedure for each App Widget that belongs to this provider
	        for (int i=0; i<N; i++) {
	        	int appWidgetId = appWidgetIds[i];
	        	
				ComponentName me=new ComponentName(context, summarywidget.class);
				appWidgetManager.updateAppWidget(appWidgetId, buildUpdate(context, appWidgetId));

	        }
	        
			
			
			super.onUpdate(context, appWidgetManager, appWidgetIds);
	   }
	    

	    protected RemoteViews buildUpdate(Context ctxt, int idWidget) {
	        Log.i(TAG,"buildUpdate() ID:"+idWidget);

	    	
	        int black_color = 0xff000000;
	        int white_color = 0xffffffff;
	        int red_color = 0xffff0000;
	        
			Intent i=new Intent(ctxt, summarywidget.class);
			
			// Get Provider for ID
			//("widget_id"+idWidget
					
			int sn = SlotManager.getInstance().preferences.getInt("widget_id"+idWidget, -1);
      		int dp = SlotManager.getInstance().preferences.getInt("widget_id_display"+idWidget, 0);
      		int back = SlotManager.getInstance().preferences.getInt("widget_id_background"+idWidget, 0);
      	  
	    	RemoteViews updateViews;
	    	
	    	if (back==1) {
		    	updateViews=new RemoteViews(ctxt.getPackageName(),R.layout.widget_summary_21_white);
	    	} else if (back==2){
		    	updateViews=new RemoteViews(ctxt.getPackageName(),R.layout.widget_summary_21_transparent);
	    	} else {
		    	updateViews=new RemoteViews(ctxt.getPackageName(),R.layout.widget_summary_21_black);
	    	} 
	    	
      		
			if (sn!=-1) {
			
			// Get Slot
			Provider p = SlotManager.getInstance().getSlot(sn);
			
			if (p==null) {
		        Log.i(TAG,"widget  ID:"+idWidget+ " invalid slot - removing..."+sn);
	        	SlotManager.getInstance().preferences.edit().remove("widget_id"+idWidget).commit();

		        return null;
			}
			
	 		 if (!p.loadsuccess) {
				  updateViews.setTextViewText(R.id.headersublabel, p.getFailureCode());
	 		 }
	 		 
	 		 p.widgetid=idWidget;
	 		 
			// Common Code ??
			 if (!p.notSetup() && p.hasData()) {
	   			  updateViews.setViewVisibility(R.id.summarypanel, View.VISIBLE);

				  // Header
				  updateViews.setTextViewText(R.id.headerlabel, p.disk_planname);
				  
				  if (p.loadstatus==Provider.provider_status_idle) {
					  updateViews.setTextViewText(R.id.headersublabel, p.lastUpdatedWidget());
				  } else {
					  updateViews.setTextViewText(R.id.headersublabel, p.loadmsg);
				  }
				  
			   		if (back==1) {
 			   			updateViews.setTextColor(R.id.headerlabel, black_color);
 			   			updateViews.setTextColor(R.id.headersublabel, black_color);
			   		}
				  
			   		if (p.provider_definition.loadedFrom==1) {
						  updateViews.setImageViewBitmap(R.id.logo, UIManager.getInstance().getBitMapforIcon(p.provider_definition.icon) );
			   		} else {
						  updateViews.setImageViewResource(R.id.logo, UIManager.getInstance().getIconIdentifier(p.provider_definition.icon) );
			   		}
				 
	   			 if (p.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE && dp==0) {
	   				 // Turn off text
	   				
	   				updateViews.setViewVisibility(R.id.txtpanel, View.GONE);
	   				
	   				 // Turn on Progress
	   				updateViews.setViewVisibility(R.id.progresspanel, View.VISIBLE);

	   				// Get Progress Bars
	   			   	if (p.progress !=null && p.progress.size()>0) {
	   	            	kbProgressBar pb1 = p.progress.get(0);
	   		        	 if (p.p1OverIdeal()) {
	   		   				updateViews.setViewVisibility(R.id.txtp1, View.VISIBLE);
	   		        	 } else {
		   		   			updateViews.setViewVisibility(R.id.txtp1, View.GONE);
	   		        	 }
	   	            	updateViews.setProgressBar(R.id.bar_1, 100, (int)pb1.percentage, false);
	   			   	}
	   			   	
	  			   	if (p.progress !=null && p.progress.size()==2) {
	   	            	kbProgressBar pb2 = p.progress.get(1);
	   		        	 if (p.p2OverIdeal()) {
		   		   				updateViews.setViewVisibility(R.id.txtp2, View.VISIBLE);
		   		        	 } else {
			   		   			updateViews.setViewVisibility(R.id.txtp2, View.GONE);
		   		        	 }
	   		        	 
	   		        	updateViews.setProgressBar(R.id.bar_2, 100, (int)pb2.percentage, false);
	   	        	}
	  			   	
//	  			   	if (changeColor) {
//	  			   		updateViews.setTextColor(R.id.headerlabel, 0xffff8c00);
//	  			   	} else {
//	  			   		updateViews.setTextColor(R.id.headerlabel, 0xFFb1b6c3);
//	  			   	}
	   				
	   			 } else if (p.provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT || dp!=0){
		   			updateViews.setViewVisibility(R.id.progresspanel,View.GONE);
	   				updateViews.setViewVisibility(R.id.txtpanel, View.VISIBLE);
	   				
	   				if (p.provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT) {
		   				updateViews.setTextViewText(R.id.txtSummary, p.AccountSummaryText());
	   				} else {
	   					// ISP %
   						kbProgressBar pb1 = p.pbar1();
   						kbProgressBar pb2 = p.pbar2();
   						String dtext="";
   						boolean changeColor;

   						if (pb2==null) {
   	  						changeColor = pb1.overIdeal();
   						} else {
  	  						changeColor = pb1.overIdeal() || pb2.overIdeal();
   						}
   						
	   					if (dp==1) {
	   						if (pb2==null) {
	   							// Only pb1
	   							dtext = pb1.percentage_msg;
	   						} else {
	   							dtext = pb1.percentage_msg+"\n"+pb2.percentage_msg;
	   						}
	   					} else if (dp==2) {
	   						// Used
	   						if (pb2==null) {
	   							// Only pb1
	   							dtext = Utils.BlankString(pb1.UsedTxt);
	   						} else {
	   							dtext = Utils.BlankString(pb1.UsedTxt)+"\n"+Utils.BlankString(pb2.UsedTxt);
	   						}
	   						
	   					} else if (dp==3) {
	   					    // Remaining
	   						if (pb2==null) {
	   							// Only pb1
	   							dtext = Utils.BlankString(pb1.RemainTxt);
	   						} else { 
	   							dtext = Utils.BlankString(pb1.RemainTxt)+"\n"+Utils.BlankString(pb2.RemainTxt);
	   						}
	   						
	   					} else if (dp==4) {
	   					       // Days %
	   						   cycle c = p.current_cycle;
	   						   if (c!=null){
		   						   dtext = Utils.BlankString((int)c.dayspercentage+"% cycle");
	   						   } else {
	   							   dtext = "N/A";
	   						   }
	   					} else if (dp==5) {
	   					       // Days Remain
	   						   cycle c = p.current_cycle;
	   						   if (c!=null){
		   						   dtext = Utils.BlankString(java.lang.Math.abs(c.daysremaining)+" days");
	   						   } else {
	   							   dtext = "N/A";
	   						   }
	   					} else if (dp==6) {
	   					       // Peak Used & Days % (2 & 4)
	   						   cycle c = p.current_cycle;
	   						   if (c!=null){
		   						   dtext = Utils.BlankString(pb1.UsedTxt) + "\n" + Utils.BlankString((int)c.dayspercentage+"% cycle");
	   						   } else {
	   							   dtext = "N/A";
	   						   }
	   						} 
	   					
		   				updateViews.setTextViewText(R.id.txtSummary, dtext);
		  			   	if (changeColor) {
	  			   			updateViews.setTextColor(R.id.txtSummary, red_color);
	  			   		} else {
		  			   		if (back==1) {
		  			   			updateViews.setTextColor(R.id.txtSummary, black_color);
		  			   		} else {
		  			   			updateViews.setTextColor(R.id.txtSummary, white_color);
		  			   		}
	  			   		}
		   			}
	   			 }
			 } else {
	   				updateViews.setViewVisibility(R.id.summarypanel, View.GONE);
			 }
			
	
			// Progress
			
			
			//i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			
			i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, idWidget);
			
			i.setAction(Long.toString(DateUtils.DateMills()));
			
			PendingIntent pi=PendingIntent.getBroadcast(ctxt, 0	, i,PendingIntent.FLAG_UPDATE_CURRENT);
	    
			// Attach to SumamryView
		    //Intent intent = new Intent(ctxt, SummaryActivity.class);
		    
			Intent intent = DetailActivity.CreateDetailScreen(ctxt, sn);
			intent.setAction(Long.toString(DateUtils.DateMills()));
			
			intent.putExtra("Widgetid", idWidget);
			
	        //PendingIntent pendingIntent = PendingIntent.getActivity(ctxt, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	        PendingIntent pendingIntent = PendingIntent.getActivity(ctxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	        
	        
	        // Get the layout for the App Widget and attach an on-click listener to the button
	        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

			}
			
			
			
			return updateViews;
	    }
	    
	    
	 
}
