package com.southfreo.quota.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.southfreo.quota.control.ProviderManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.control.UIManager;
import com.southfreo.quota.model.Provider;
import com.southfreo.R;
import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseProviderActivity extends Activity {
    
	// Scrolling flag
	public static String CHOSEN_PROVIDER_CODE = "PROVIDER_CHOSEN_CODE";
	
	
    private boolean scrolling = false;

    
    private ArrayList<Provider> selectedProviders;
    private String currentType;
    private Provider selectedProvider;
    
    private WheelView whlptypes;
    private WheelView whlproviders;
    private Button butOK;
    private Button butCancel;
    private ProviderManager pm;
    private UIManager ui;
    private boolean addScreen;
    
    TextView pname;
    TextView desc;
    TextView version;
    
    
    @Override 
    protected void onResume(){
        super.onResume();
        UIManager.getInstance().myapp.pauseService();
    }
    
    @Override 
    protected void onPause() { 
        super.onPause();
        UIManager.getInstance().myapp.unpauseService();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chooseprovider_layout);
             
        // Screen Controls
        pname = (TextView) findViewById(R.id.providername);
        desc = (TextView) findViewById(R.id.providerdescription);
        version = (TextView) findViewById(R.id.providerversion);
        
        // Who called this intent?
        addScreen = getIntent().getBooleanExtra("addscreen",false);
        
        // Get instance of PM
        pm = ProviderManager.getInstance();
        ui = UIManager.getInstance();
   
        // Button Control
        butOK = (Button) findViewById(R.id.okbutt);
        butCancel = (Button) findViewById(R.id.cancelbutt);
        
        this.butCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              setResult(RESULT_CANCELED);
              finish();
            }
          });
        

        this.butOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ProviderManager pm=ProviderManager.getInstance();
            	
            	Provider chkp = pm.getProviderDefinition(selectedProvider.disk_isp);
            	
            	if (chkp!=null && chkp.secure && !SlotManager.getInstance().hasPin()) {
            	   UIManager.getInstance().MsgBoxInfo(ChooseProviderActivity.this, "PIN Required", "This provider cannot be used without a PIN being setup");
            	   return;
            	}
            	
                // From Add Screen
                if (addScreen) {
                    SlotManager sm=SlotManager.getInstance();
                    
              		Provider p = sm.addSlot();
              		pm.ResetProvider(p, selectedProvider.disk_isp);
              		pm.edit_provider_object=p;
              		pm.edit_provider_id=selectedProvider.disk_isp;
              		pm.edit_provider_slot=sm.currentSlot;
                  	
              		Intent i = ProviderSettings.createIntent(ChooseProviderActivity.this);
              		i.putExtra("addprovider",true);
               		startActivityForResult(i,0);
                } else {
                  	Intent resultIntent = new Intent();
                    resultIntent.putExtra(CHOSEN_PROVIDER_CODE, selectedProvider.disk_isp);
                	setResult(Activity.RESULT_OK, resultIntent);
                }
                finish();
                
            }
          });
        

        
 
        // Types Wheel
        whlptypes = (WheelView) findViewById(R.id.providertype);
        whlptypes.setVisibleItems(4);
        typesAdapter ta = new typesAdapter(this);
        ta.setTextSize(12);
        whlptypes.setViewAdapter(ta);
        
        // Providers Wheel
        whlproviders = (WheelView) findViewById(R.id.provider);
        whlproviders.setVisibleItems(4);
        whlproviders.setViewAdapter(new providerAdapter(this));

        
        
        whlptypes.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
			    if (!scrolling) {
			        updateProviders(newValue,false,0);
			    }
			}
		});
        
        whlptypes.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                updateProviders(whlptypes.getCurrentItem(),false,0);
            }
        });
        
        whlproviders.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
			    if (!scrolling) {
			        // Get Current Provider and update Labels
			    	updateProviderDetail(newValue);
			    }
			}
		});
        
 
        
        

        // Setup Initial positions
        
        pm.createTypesAndSort();
        
        // Find the type for selected provider
        if (pm.edit_provider_id!=-1) {
        	// Find the correctType matching the selected provider
        	Provider p=pm.getProviderDefinition(pm.edit_provider_id);
        	int typeindex=0;
        	for (int i=0;i<pm.typesArray.length;i++) {
        		if (p.providertype.equalsIgnoreCase(pm.typesArray[i])) {
        			typeindex=i;
        		}
        	}
            whlptypes.setCurrentItem(typeindex);
            updateProviders(typeindex,true,pm.edit_provider_id);
        } else {
        	// Just default to the first
            whlptypes.setCurrentItem(0);
            updateProviders(0,false,0);
        }
        
    }
    
    
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (   keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
          	Intent resultIntent = new Intent();
        	resultIntent.putExtra(CHOSEN_PROVIDER_CODE, selectedProvider.disk_isp);
        	setResult(Activity.RESULT_OK, resultIntent);
        	finish();
        	return true;
        }
        return false;
       // return super.onKeyDown(keyCode, event);
    }
    

    private void updateProviderDetail(int index) {
    	selectedProvider = selectedProviders.get(index);
        whlproviders.setCurrentItem(index); 
    	
    	// Update Details
    	
    	pname.setText(selectedProvider.providerName);
    	desc.setText(selectedProvider.xmlDescription);
    	version.setText(selectedProvider.ExtraInfo());
    	
    }

    private void updateProviders(int typeindex,boolean selectprovider,int provindex) {
        
        // Get Current Item for Type
        providerAdapter pa = new providerAdapter(this);
         
        currentType = pm.typesArray[typeindex];
        // Get Providers associates with this type
        selectedProviders = pm.types.get(currentType);
        // Sort them
        Collections.sort(selectedProviders);
        
        // Find the correct index for the provider
        int correctindex=selectedProviders.size()/2;	// Choose Middle of Wheel
        if (selectprovider) {
        	for (int i=0;i<selectedProviders.size();i++) {
        		Provider p=selectedProviders.get(i);
        		if (p.disk_isp==provindex) {
        			correctindex=i;
        			break;
        		}
        	}
        } 
        
        whlproviders.setViewAdapter(pa);
        updateProviderDetail(correctindex);
    }
    
    /**
     * Adapter for Provider Types
     */
    
    private class providerAdapter extends AbstractWheelTextAdapter {
         
        /**
         * Constructor
         */
        protected providerAdapter(Context context) {
            super(context, R.layout.provider_layout, NO_RESOURCE);
            
            setItemTextResource(R.id.provider_name);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            ImageView img = (ImageView) view.findViewById(R.id.provider_icon);
            
            Provider p = selectedProviders.get(index);
            
            ui.setIconForProvider(img, p,p.loadedFrom);
            
            //img.setImageResource(pm.getIconIdentifier(p.icon));
            
            return view;
        }
        
        @Override
        public int getItemsCount() {
        	if (selectedProviders!=null) {
                return selectedProviders.size();
            }
        	return 0;
        }
        
        @Override
        protected CharSequence getItemText(int index) {
            return selectedProviders.get(index).providerName;
        }
    }
    
    private class typesAdapter extends AbstractWheelTextAdapter {
        // Types names
        
        /**
         * Constructor
         */
        protected typesAdapter(Context context) {
            super(context);
        }
        
        @Override
        public int getItemsCount() {
            return pm.typesArray.length;
        }
        
        @Override
        protected CharSequence getItemText(int index) {
            return pm.typesArray[index];
        }
    }
    
    
}
    
