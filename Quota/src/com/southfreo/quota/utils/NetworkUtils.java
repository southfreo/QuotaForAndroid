package com.southfreo.quota.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;

import com.southfreo.quota.control.CacheManager;
import com.southfreo.quota.control.SlotManager;
import com.southfreo.quota.model.cycle;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.util.Log;

public class NetworkUtils {
	
	private final static String TAG = "Quota-NetworkUtils";
	
	
	// Mobile Stats
	private static final String mobileRxFile_1 = "/sys/class/net/rmnet0/statistics/rx_bytes";
	private static final String mobileTxFile_1 = "/sys/class/net/rmnet0/statistics/tx_bytes";

	private static final String mobileRxFile_2 = "/sys/class/net/ppp0/statistics/rx_bytes";
	private static final String mobileTxFile_2 = "/sys/class/net/ppp0/statistics/tx_bytes";

	private static final String mobileRxFile_3 = "/sys/class/net/pdp0/statistics/rx_bytes";
	private static final String mobileTxFile_3 = "/sys/class/net/pdp0/statistics/tx_bytes";

	// Wifi Stats
	private static final String wifiRxFile_1 = "/sys/class/net/eth0/statistics/rx_bytes";
	private static final String wifiTxFile_1 = "/sys/class/net/eth0/statistics/tx_bytes";

	private static final String wifiRxFile_2 = "/sys/class/net/tiwlan0/statistics/rx_bytes";
	private static final String wifiTxFile_2 = "/sys/class/net/tiwlan0/statistics/tx_bytes";
	
	private static final String wifiRxFile_3 = "/sys/class/net/wlan0/statistics/rx_bytes";
	private static final String wifiTxFile_3 = "/sys/class/net/wlan0/statistics/tx_bytes";

	private static final String wifiRxFile_4 = "/sys/class/net/athwlan0/statistics/rx_bytes";
	private static final String wifiTxFile_4 = "/sys/class/net/athwlan0/statistics/tx_bytes";

	private static final String wifiRxFile_5 = "/sys/class/net/eth1/statistics/rx_bytes";
	private static final String wifiTxFile_5 = "/sys/class/net/eth1/statistics/tx_bytes";
	
	
	public static boolean isUp(String sfile) {
		File f = new File(sfile);
		return f.canRead();
	}

	private static RandomAccessFile getFile(String filename) throws IOException {
		File f = new File(filename);
		return new RandomAccessFile(f, "r");
	}


	public static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    FileInputStream f = new FileInputStream(filePath);
	    f.read(buffer);
	    return new String(buffer);
	}
	
	
	public static void emailFile(Context context, String emailTo, String emailCC,
		    String subject, String emailText, String sendFile)
		{
		    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		 
		    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailTo});
		    emailIntent.putExtra(android.content.Intent.EXTRA_CC, new String[]{emailCC});

		    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,emailText);
		    emailIntent.setType("plain/text");
		    if (Utils.FileExists(sendFile)) {
		    	String sdfile = CacheManager.getInstance().copyToSDCard(sendFile,"Quota-UsageFile.txt");
		    	if (sdfile!=null) {
				    emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + sdfile));
		    	}
		    }
		    emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); 
		    
		    context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}
	
	
	 public static long getMobileRx() {
		 long r=-1;
		 r = readLong(mobileRxFile_1);
		 if (r==-1) {
			 r = readLong(mobileRxFile_2);
			 if (r==-1) {
				 r = readLong(mobileRxFile_3);
			 }
		 }
		 return r;
	 }

	 public static long getMobileTx() {
		 long r=-1;
		 r = readLong(mobileTxFile_1);
		 if (r==-1) {
			 r = readLong(mobileTxFile_2);
			 if (r==-1) {
				 r = readLong(mobileTxFile_3);
			 }
		 }
		 return r;
	 }
	 
	 public static long getWifiRx() {
		 long r=-1;
		 r = readLong(wifiRxFile_1);
		 if (r==-1) {
			 r = readLong(wifiRxFile_2);
			 if (r==-1) {
				 r = readLong(wifiRxFile_3);
				 if (r==-1) {
					 r = readLong(wifiRxFile_4);
					 if (r==-1) {
						 r = readLong(wifiRxFile_5);
					 }
				 }
			 }
		 }
		 return r;
	 }
	 public static long getWifiTx() {
		 long r=-1;
		 r = readLong(wifiTxFile_1);
		 if (r==-1) {
			 r = readLong(wifiTxFile_2);
			 if (r==-1) {
				 r = readLong(wifiTxFile_3);
				 if (r==-1) {
					 r = readLong(wifiTxFile_4);
					 if (r==-1) {
						 r = readLong(wifiTxFile_5);
					 }
				 }
			 }
		 }
		 return r;
	 }

	private static long readLong(String file) {
		RandomAccessFile raf=null;
		
		if (!isUp(file)) {
			return -1;
		}
		try {
			raf = getFile(file);
			return Long.valueOf(raf.readLine());
		} catch (Exception e) {
			return -1;
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	
    public static boolean isOnline(Context c) {
    	ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
    	if (cm.getActiveNetworkInfo()!=null) {
    	   	return cm.getActiveNetworkInfo().isConnectedOrConnecting();
    	}
    	return false;
    }
    
    public static boolean isOnlineWifi(Context c) {
    	ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
    	State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
    	return (wifi == State.CONNECTED || wifi == State.CONNECTING);
    }

    
    public final static String wifiRxignore = "wifiRx_ignore";
    public final static String wifiTxignore = "wifiTx_ignore";
    public final static String mobRxignore  = "mobRx_ignore";
    public final static String mobTxignore  = "mobTx_ignore";
    
    public final static String wifiRxcurrent = "wifiRx_current";
    public final static String wifiTxcurrent = "wifiTx_current";
    public final static String wifiTotcurrent = "wifiTot_current";
       
    public final static String mobRxcurrent  = "mobRx_current";
    public final static String mobTxcurrent  = "mobRx_current";
    public final static String mobTotcurrent = "mobTot_current";
   
    public final static String lastWifi = "last_wifi";
    public final static String lastMobile = "last_mob";
    public final static String lastReboot = "lastReboot";
       
    @SuppressWarnings("unchecked")
	public static void putLong(Hashtable h,String k,long l) {
    	h.put(k, new Long(l));
    }
    
    
    @SuppressWarnings("unchecked")
	public static long getLong(Hashtable h,String k) {
    	long r = 0;
    	if (h!=null) {
    		Long l = (Long)h.get(k);
    		if (l!=null) {
    			r=l;
    		}
    	}
    	return r;
    }
    
    @SuppressWarnings("unchecked")
	public static void ignoreCurrentTotals(Hashtable h) {
      	
    	long mobRx  = getMobileRx();
		if (mobRx!=-1) {
			putLong(h,mobRxignore, mobRx);
		}
		
		long mobTx  = getMobileTx();
		if (mobTx!=-1) {
			putLong(h,mobTxignore, mobTx);
		}
		
		long wifiRx  = getWifiRx();
		if (wifiRx!=-1) {
			putLong(h,wifiRxignore, wifiRx);
		}

		long wifiTx  = getWifiTx();
		if (wifiTx!=-1) {
			putLong(h,wifiTxignore, wifiTx);
		}
		
		putLong(h,wifiRxcurrent, 0);
		putLong(h,wifiTxcurrent, 0);
		putLong(h,mobRxcurrent, 0);
		putLong(h,mobTxcurrent, 0);

		h.put(lastReboot, rebootTime());
		
    }
    
    @SuppressWarnings("unchecked")
	public static Hashtable resetNetworkCounters() {
		Log.i(TAG,"Reset NetStats");

		Hashtable h = new Hashtable();
		
		ignoreCurrentTotals(h);
		
    	h.put("lastreset", new Date());

  		
		return h;
		
    }
    
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
    
    
    public static Long rebootMilli() {
    	return android.os.SystemClock.elapsedRealtime();
    }
    
    public static Date rebootTime() {
    	return new Date(new Date().getTime() - android.os.SystemClock.elapsedRealtime());
    }
    
    @SuppressWarnings("unchecked")
	public static Hashtable updateNetworkCounters(int startDay) {
		Log.i(TAG,"UpdateNetworkCounters()");
		

		Hashtable h = CacheManager.getInstance().getStatsDict();
		
		// Auto-Reset
		Date d = (Date)h.get("lastreset");
		if (d!=null) {
			cycle c = new cycle();
			c.initWithAnniversaryDay(startDay);
			if (!DateUtils.inBetween(d, c.startDate, c.endDate)) {
				h = resetNetworkCounters();
			}
		}
		
		// Check-Reboot
		Date lr = (Date)h.get(lastReboot);
		Date nr = rebootTime();

		// We have been rebooted
		if (lr==null) {
			ignoreCurrentTotals(h);
		} else if (java.lang.Math.abs(lr.getTime() - nr.getTime())>2000) {
			ignoreCurrentTotals(h);
		}
		
		
		// Get Current Totals
		long mobRx  = getMobileRx();
		long mobTx  = getMobileTx();
		long mobTot = mobTx+mobTx;
		
		Log.i(TAG,"Mobile Usage: "+mobTot);
		
		long wifiRx  = getWifiRx();
		long wifiTx  = getWifiTx();
		long wifiTot = wifiRx+wifiTx;
		
		Log.i(TAG,"Wifi Usage : "+wifiTot);
	
		// Ignore Values
		long wifirx_ignore = getLong(h,wifiRxignore);
		long wifitx_ignore = getLong(h,wifiTxignore);
		long mobrx_ignore = getLong(h,mobRxignore);
		long mobtx_ignore = getLong(h,mobTxignore);
		
		
		// Current Values
		long wifirx_current = getLong(h,wifiRxcurrent);
		long wifitx_current = getLong(h,wifiTxcurrent);
		long wifitot_current = getLong(h,wifiTotcurrent);
		
		
		long mobrx_current = getLong(h,mobRxcurrent);
		long mobtx_current = getLong(h,mobTxcurrent);
		long mobtot_current = getLong(h,mobTotcurrent);

		
		// Wifi New Data
		long new_txwifi = wifiTx - wifitx_ignore - wifitx_current;
		long new_rxwifi = wifiRx - wifirx_ignore - wifirx_current;
		long new_totwifi = new_txwifi + new_rxwifi;
		
		
		if (new_txwifi>0) {
			wifitx_current += new_txwifi; 
			putLong(h,wifiTxcurrent,wifitx_current);
		}
		
		if (new_rxwifi>0) {
			wifirx_current += new_rxwifi; 
			putLong(h,wifiRxcurrent,wifirx_current);
		}
		
		if (new_totwifi>0) {
			wifitot_current += new_totwifi;
			putLong(h,wifiTotcurrent,wifitot_current);
			String lw = Utils.FormatValueC(Utils.E_FORMAT_DATA,new_totwifi);
			h.put(lastWifi, lw + " at "+DateUtils.DateTime(new Date()));
		}
		
		
		Log.i(TAG,"Wifi new TX"+new_txwifi);
		Log.i(TAG,"Wifi new RX"+new_rxwifi);
		Log.i(TAG,"Wifi new TOT"+new_totwifi);
	
		// Mobile New Data
		long new_txmob = mobTx - mobtx_ignore - mobtx_current;
		long new_rxmob = mobRx - mobrx_ignore - mobrx_current;
		long new_totmob = new_txmob + new_rxmob;
			
		if (new_txmob>0) {
			mobtx_current += new_txmob; 
			putLong(h,mobTxcurrent,mobtx_current);
		}
		
		if (new_rxmob>0) {
			mobrx_current += new_rxmob; 
			putLong(h,mobRxcurrent,mobrx_current);
		}

		if (new_totmob>0) {
			mobtot_current += new_totmob;
			putLong(h,mobTotcurrent,mobtot_current);
			String lw = Utils.FormatValueC(Utils.E_FORMAT_DATA,new_totmob);
			h.put(lastMobile, lw + " at "+DateUtils.DateTime(new Date()));
		}

		Log.i(TAG,"Mobile new TX"+new_txmob);
		Log.i(TAG,"Mobile new RX"+new_rxmob);
		Log.i(TAG,"Mobile new TOT"+new_totmob);
	
		CacheManager.getInstance().writeStatsDict(h);
		
		return h;
		
	}
	
}
