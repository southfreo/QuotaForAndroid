/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.southfreo.quota.billing;

import com.southfreo.R;
import com.southfreo.quota.billing.BillingService.RequestPurchase;
import com.southfreo.quota.billing.BillingService.RestoreTransactions;
import com.southfreo.quota.billing.Consts.PurchaseState;
import com.southfreo.quota.billing.Consts.ResponseCode;
import com.southfreo.quota.control.UIManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A sample application that demonstrates in-app billing.
 */
public class Billing extends Activity implements OnClickListener {
    private static final String TAG = "Billing";

    /**
     * Used for storing the log text.
     */
    private static final String LOG_TEXT_KEY = "BILLING_LOG_TEXT";

    /**
     * The SharedPreferences key for recording whether we initialized the
     * database.  If false, then we perform a RestoreTransactions request
     * to get all the purchases for this user.
     */
    private static final String DB_INITIALIZED = "db_initialized";

    private BillingPurchaseObserver mDungeonsPurchaseObserver;
    private Handler mHandler;

    private BillingService mBillingService;
    private Button mBuyButton;
    private Button mCancelButton;
    private PurchaseDatabase mPurchaseDatabase;
    
    /**
     * The developer payload that is sent with subsequent
     * purchase requests.
     */
    private String mPayloadContents = null;

    private static final int DIALOG_CANNOT_CONNECT_ID = 1;
    private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 2;

    /**
     * Each product in the catalog is either MANAGED or UNMANAGED.  MANAGED
     * means that the product can be purchased only once per user (such as a new
     * level in a game). The purchase is remembered by Android Market and
     * can be restored if this application is uninstalled and then
     * re-installed. UNMANAGED is used for products that can be used up and
     * purchased multiple times (such as poker chips). It is up to the
     * application to keep track of UNMANAGED products for the user.
     */
    private enum Managed { MANAGED, UNMANAGED }

    /**
     * A {@link PurchaseObserver} is used to get callbacks when Android Market sends
     * messages to this application so that we can update the UI.
     */
    private class BillingPurchaseObserver extends PurchaseObserver {
        public BillingPurchaseObserver(Handler handler) {
            super(Billing.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported) {
            if (Consts.DEBUG) {
                Log.i(TAG, "supported: " + supported);
            }
            if (supported) {
                restoreDatabase();
            } else {
                showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
            }
        }

        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
                int quantity, long purchaseTime, String developerPayload) {
            if (Consts.DEBUG) {
                Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
            }
            if (purchaseState == PurchaseState.PURCHASED) {
            	// &&  mPurchaseDatabase.up001()
            	UIManager.getInstance().isUnlocked=true;
            	MsgBoxSuccess(Billing.this,"Quota","Thankyou, your purchase was successful.");
            } else {
            	MsgBoxError(Billing.this,"Quota","Your order was cancelled");
            }
            
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request,
                ResponseCode responseCode) {
            if (Consts.DEBUG) {
                Log.d(TAG, request.mProductId + ": " + responseCode);
            }
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Consts.DEBUG) {
                    Log.i(TAG, "purchase was successfully sent to server");
                }
            } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
                if (Consts.DEBUG) {
                    Log.i(TAG, "user canceled purchase");
                }
            } else {
                if (Consts.DEBUG) {
                    Log.i(TAG, "purchase failed");
                }
            }
        }

        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request,
                ResponseCode responseCode) {
            if (responseCode == ResponseCode.RESULT_OK) {
                if (Consts.DEBUG) {
                    Log.d(TAG, "completed RestoreTransactions request");
                }
                // Update the shared preferences so that we don't perform
                // a RestoreTransactions again.
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean(DB_INITIALIZED, true);
                edit.commit();
            } else {
                if (Consts.DEBUG) {
                    Log.d(TAG, "RestoreTransactions error: " + responseCode);
                }
            }
        }
    }

    private static class CatalogEntry {
        public String sku;
        public int nameId;
        public Managed managed;

        public CatalogEntry(String sku, int nameId, Managed managed) {
            this.sku = sku;
            this.nameId = nameId;
            this.managed = managed;
        }
    }

    /** An array of product list entries for the products that can be purchased. */
    private static final CatalogEntry[] CATALOG = new CatalogEntry[] {
    	new CatalogEntry(PurchaseDatabase.uk(),  R.string.quota_upgrade, Managed.MANAGED),
    };

    private String mItemName;
    private String mSku;
  
    private final static int QUOTA_UPGRADE=0;
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_screen);

        mHandler = new Handler();
        mDungeonsPurchaseObserver = new BillingPurchaseObserver(mHandler);
        mBillingService = new BillingService();
        mBillingService.setContext(this);

        mPurchaseDatabase = new PurchaseDatabase(this);
        setupWidgets();

        // The only product Available
        mItemName = getString(CATALOG[QUOTA_UPGRADE].nameId);
        mSku = CATALOG[QUOTA_UPGRADE].sku;

        // Check if billing is supported.
        ResponseHandler.register(mDungeonsPurchaseObserver);
        if (!mBillingService.checkBillingSupported()) {
            showDialog(DIALOG_CANNOT_CONNECT_ID);
        }
    }

    /**
     * Called when this activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        ResponseHandler.register(mDungeonsPurchaseObserver);
    }

    /**
     * Called when this activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        ResponseHandler.unregister(mDungeonsPurchaseObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPurchaseDatabase.close();
        mBillingService.unbind();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_CANNOT_CONNECT_ID:
            return createDialog(R.string.cannot_connect_title,
                    R.string.cannot_connect_message);
        case DIALOG_BILLING_NOT_SUPPORTED_ID:
            return createDialog(R.string.billing_not_supported_title,
                    R.string.billing_not_supported_message);
        default:
            return null;
        }
    }

    private Dialog createDialog(int titleId, int messageId) {
        String helpUrl = replaceLanguageAndRegion(getString(R.string.help_url));
        if (Consts.DEBUG) {
            Log.i(TAG, helpUrl);
        }
        final Uri helpUri = Uri.parse(helpUrl);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleId)
            .setIcon(android.R.drawable.stat_sys_warning)
            .setMessage(messageId)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(R.string.learn_more, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, helpUri);
                    startActivity(intent);
                }
            });
        return builder.create();
    }

	 private void MsgBoxError(Context c,String Title,String msg) {
	   		AlertDialog alertDialog = new AlertDialog.Builder(c).create();
		alertDialog.setTitle(Title);
		alertDialog.setMessage(msg);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
		      // here you can add functions
			  dialog.dismiss();
		   }
		});
		alertDialog.show();
	 }
	 
	 private void MsgBoxSuccess(Context c,String Title,String msg) {
	   		AlertDialog alertDialog = new AlertDialog.Builder(c).create();
 		alertDialog.setTitle(Title);
 		alertDialog.setMessage(msg);
 		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
 		   public void onClick(DialogInterface dialog, int which) {
 		      // here you can add functions
 			  dialog.dismiss();
 			  finish();
 		   }
 		});
 		alertDialog.show();
	 }
	 
    /**
     * Replaces the language and/or country of the device into the given string.
     * The pattern "%lang%" will be replaced by the device's language code and
     * the pattern "%region%" will be replaced with the device's country code.
     *
     * @param str the string to replace the language/country within
     * @return a string containing the local language and region codes
     */
    private String replaceLanguageAndRegion(String str) {
        // Substitute language and or region if present in string
        if (str.contains("%lang%") || str.contains("%region%")) {
            Locale locale = Locale.getDefault();
            str = str.replace("%lang%", locale.getLanguage().toLowerCase());
            str = str.replace("%region%", locale.getCountry().toLowerCase());
        }
        return str;
    }

    
    private void setupWidgets() {

        mBuyButton = (Button) findViewById(R.id.buy_button);
        mBuyButton.setEnabled(true);
        mBuyButton.setOnClickListener(this);
        
        mCancelButton = (Button)findViewById(R.id.btnCancel);
        mCancelButton.setEnabled(true);
        mCancelButton.setOnClickListener(this);
    }

     /**
     * If the database has not been initialized, we send a
     * RESTORE_TRANSACTIONS request to Android Market to get the list of purchased items
     * for this user. This happens if the application has just been installed
     * or the user wiped data. We do not want to do this on every startup, rather, we want to do
     * only when the database needs to be initialized.
     */
    private void restoreDatabase() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean initialized = prefs.getBoolean(DB_INITIALIZED, false);
        if (!initialized) {
            mBillingService.restoreTransactions();
            Toast.makeText(this, R.string.restoring_transactions, Toast.LENGTH_LONG).show();
        }
    }


     /**
     * Called when a button is pressed.
     */
    public void onClick(View v) {
        if (v == mBuyButton) {
            if (Consts.DEBUG) {
                Log.d(TAG, "buying: " + mItemName + " sku: " + mSku);
            }
            if (!mBillingService.requestPurchase(mSku, mPayloadContents)) {
                showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
            }
        } else if (v==mCancelButton) {
        	finish();
        }
    }

 
 
}
