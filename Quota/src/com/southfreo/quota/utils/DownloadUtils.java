package com.southfreo.quota.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class DownloadUtils implements Runnable {

	public interface DownloadEvent
	{
	    // This is just a regular method so it can return something or
	    // take arguments if you like.
	    public void downloadComplete(boolean ok, String msg);
	}
	
	private final static String TAG = "DownloadUtils";
	ProgressDialog myProgress;
	static Handler myHandler;

	public boolean ok;
	public String errmsg;
	
   	private String dUrl;
   	private String dFileName;

   	public boolean inprogress;
	
   	private DownloadEvent ev;
   	
	Context c;
	
	  public DownloadUtils(Context c,DownloadEvent ev) {
	    	this.c = c;
	    	this.ev = ev;
	    	errmsg="";
	    	inprogress=false;
	    	ok=false;
	  }
	    
	    private Context getContext() {
	    	return c;
	    }
	    
	    public void downloadFile(String fileURL,String fileName) {
	        myProgress = ProgressDialog.show(getContext(), "Download file",
	                        "Downloading...", true, false);
	        inprogress=true;
	        myHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                        // process incoming messages here
                        switch (msg.what) {
                        case 0:
                                // update progress bar
                                myProgress.setMessage("" + (String) msg.obj);
                                break;
                        case 1:
                                myProgress.cancel();
                                Toast toast = Toast.makeText(getContext(),
                                                "Download successfully", Toast.LENGTH_SHORT);
                                toast.show();
                                ev.downloadComplete(true, "");
                                break;
                        case 2:
                        		ev.downloadComplete(false, "" + (String) msg.obj);
                                myProgress.cancel();
                                break;
                        }
                        super.handleMessage(msg);
                }

        };
        Thread workthread = new Thread(new DownloadUtils(fileURL, fileName));
        workthread.start();

	    }

	    
    public  DownloadUtils(String fileURL, String fileName) {  
    	dUrl = fileURL;
    	dFileName = fileName;
    }
    

	@Override
	public void run() {
		// TODO Auto-generated method stub
	       Message msg = new Message();

	       try {
               URL url = new URL(dUrl); 
               File file = new File(dFileName);

               /* Open a connection to that URL. */
               URLConnection ucon = url.openConnection();

               /*
                * Define InputStreams to read from the URLConnection.
                */
               InputStream is = ucon.getInputStream();
               BufferedInputStream bis = new BufferedInputStream(is);

               /*
                * Read bytes to the Buffer until there is nothing more to read(-1).
                */
               ByteArrayBuffer baf = new ByteArrayBuffer(50);
               int current = 0;
               while ((current = bis.read()) != -1) {
                       baf.append((byte) current);
               }

               /* Convert the Bytes read to a String. */
               FileOutputStream fos = new FileOutputStream(file);
               fos.write(baf.toByteArray());
               fos.close();
    	       msg.what = 1;

               ok=true;
               
       } catch (IOException e) {
       	   ok=false;
       	   msg.what=2;
       	   msg.obj="Could not load file "+e.toString();
       }
       myHandler.sendMessage(msg);

	}
    
    
    
}
