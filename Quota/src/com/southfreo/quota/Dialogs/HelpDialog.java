package com.southfreo.quota.Dialogs;


	
	import com.southfreo.R;
import com.southfreo.quota.control.UIManager;

import android.app.Dialog;
	import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
	import android.view.View;
	import android.view.Window;
	import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
	
	/** Class Must extends with Dialog */
	
	/** Implement onClickListener to dismiss dialog when OK Button is pressed */
	
	public class HelpDialog extends Dialog implements OnClickListener {
	
	    Button okButton;
	
	    public HelpDialog(Context context) {
	
	        super(context);
	
	        /** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
	
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	
	        /** Design the dialog in main.xml file */
	
	        setContentView(R.layout.dialog_help);
	
	        okButton = (Button) findViewById(R.id.OkButton);
	        okButton.setOnClickListener(this);

	       // TextView appName = (TextView) findViewById(R.id.appName);
	       // String aTxt = String.format(context.getString(R.string.app_name_version), UIManager.getInstance().getVersion());
	        
	        //appName.setText(aTxt);
	        
	        TextView mTextSample = (TextView) findViewById(R.id.weblink);
	        mTextSample.setMovementMethod(LinkMovementMethod.getInstance());
	        String text = "<a href='http://www.southfreo.com/QuotaforAndroid/Home.html'>Visit Support site</a>";
	        mTextSample.setText(Html.fromHtml(text));
	        
	    }
	
	 
	
	    @Override
	
	    public void onClick(View v) {
	
	        /** When OK Button is clicked, dismiss the dialog */
	
	        if (v == okButton)
	
	            dismiss();
	
	    }
	
	 
	
	}



