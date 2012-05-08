package com.southfreo.quota.control;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.southfreo.quota.control.ProviderManager.asyncUpdateProvider;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.parameter;
import com.southfreo.quota.utils.DateUtils;
import com.southfreo.quota.utils.GeoStore;
import com.southfreo.quota.utils.HttpHelper;
import com.southfreo.quota.utils.NetworkUtils;
import com.southfreo.quota.utils.Utils;
import com.southfreo.quota.utils.xmlUtils;
import com.southfreo.quota.xml.TagNode;
import com.southfreo.quota.xml.TextNode;
import com.southfreo.quota.xml.TreeBuilder;
import com.southfreo.quota.xml.TreeNode;
import com.southfreo.quota.utils.Base64;

public class ProviderParser {

	private final static String TAG="Quota-ProviderParser";
	public static String newline = System.getProperty("line.separator");
	
	public static void DebugLogStart(Provider p) {
		CacheManager cm = CacheManager.getInstance();
		cm.WriteDebugLog(p, "DebugLog Start:"+p.provider_definition.providerName+"\n\n", false);
	}

	public static void DebugLogLog(Provider p,String msg) {
		CacheManager cm = CacheManager.getInstance();
		cm.WriteDebugLog(p, msg+"\n", true);
	}
	
	public static void threemobile(Provider p) {
		// Manual Test
		CacheManager cm = CacheManager.getInstance();
		              
	   	String  username  = p.getParameterByID(1).CurrentValueAsInternalString();
	   	String  password  = p.getParameterByID(2).CurrentValueAsInternalString();
	   	String  pin	      = p.getParameterByID(3).CurrentValueAsInternalString();
	   	String  mobile	  = p.getParameterByID(4).CurrentValueAsInternalString();
	   	int     sd		  = (int)p.getParameterByID(6).numberval;
	   	double  data	  = p.getParameterByID(7).numberval;
	   	double  cap		  = p.getParameterByID(8).numberval;
	   	
	   	if (data==parameter.NUMBER_BLANK) {
	   		data=0;
	   	}
	   	if (cap==parameter.NUMBER_BLANK) {
	   		cap=0;
	   	}
	   	
	   	DefaultHttpClient client = HttpHelper.QuotaClient();
	   	
        try {
        	DebugLogStart(p);
           	String login = HttpHelper.sendHttpGet(client, "https://www.my.three.com.au/My3/jfn");
                   	
        	login = HttpHelper.sendHttpPost(client, "https://www.my.three.com.au/My3/jfn", String.format("cfunc=261&octx=d9&jfnRC=1&login=%s&password=%s",username,password));
        	// Check login
        	if (login.contains("The User ID you entered is not valid") || login.contains("The password you entered is not valid")) {
    	   	    p.loadsuccess=false;
         		p.loaderrormsg="Username or password incorrect";
        		p.failurereason = Provider.fail_authentication;
        		return;
        	}
			cm.WriteCachedusageData(p,login,cm.UsageFile(p),1);

        	// Send Pin
        	if (!Utils.isBlank(pin)) {
        		login = HttpHelper.sendHttpPost(client, "https://www.my.three.com.au/My3/jfn", String.format("cfunc=271&mfunc=261&octx=d9&jfnRC=2&pin=%s",pin));
            	if (login.contains("Pin incorrect")) {
        	   	    p.loadsuccess=false;
             		p.loaderrormsg="PIN is incorrect";
            		p.failurereason = Provider.fail_authentication;
            		return;
            	}
    			cm.WriteCachedusageData(p,login,cm.UsageFile(p),2);
        	}
        	
        	// Check for Extra URL
    		if (!(login.contains("<MOBILE_GROUP><IS_AJAX>Y") || login.contains("Credit remaining") || 
    			login.contains("Data remaining:"))) {
    			// Fire one more URL
    			int mn = login.indexOf("selectedLineNumber="+mobile);
    			if (mn!=-1) {
        			String mobpage = Utils.ExtractString(login, "showServiceDetails",mn-25, "'", "'");
 
        			if (!Utils.isBlank(mobpage)) {
        				mobpage = mobpage.replace("&amp;", "&");
        				String sUrl = "https://www.my.three.com.au/My3/"+mobpage;
            	     	DebugLogLog(p,"Mobile page "+sUrl);
            	        
            			login = HttpHelper.sendHttpGet(client, sUrl);
            			cm.WriteCachedusageData(p,login,cm.UsageFile(p),3);

        			} else {
            	   	    p.loadsuccess=false;
                		p.loaderrormsg="Could not locate mobile "+mobile;
                		p.failurereason = Provider.fail_connection;
                		return;
        			}
 
    			} else {
    				// Could not Locate Mobile
        	   	    p.loadsuccess=false;
             		p.loaderrormsg="Could not locate mobile "+mobile + "\n Check Username/Password/Pin are correct";
            		p.failurereason = Provider.fail_connection;
            		return;
    			}
    		}
	
    		// We now should have usage Data
			p.clearExtras();

			// Test Code
			//login = "<MOBILE_GROUP><IS_AJAX>N</IS_AJAX><INDICATOR>0</INDICATOR><TOKEN>http://www.avg.com.au/3mobile/index.cfm?token=2244ca0176ed974a12a31ffb783217d3</TOKEN><PLAN_TYPE>V</PLAN_TYPE><FINAL_DATE>13 Jan 11</FINAL_DATE><EFFECTIVE_DATE>09:00 pm 12 Feb 11</EFFECTIVE_DATE><GRAPH><MYUSAGE_GRAPH><CHILD_STATUS>true</CHILD_STATUS><DISPLAY_NAME>$29  Cap</DISPLAY_NAME><PARENT_DISCOUNT_STATUS>1</PARENT_DISCOUNT_STATUS><LENGTH>50</LENGTH><USED_LENGTH>55</USED_LENGTH><LOW_THRESHOLD_VALUE>29</LOW_THRESHOLD_VALUE><HIGH_THRESHOLD_VALUE>150</HIGH_THRESHOLD_VALUE><DISPLAY>CAP DISPLAY</DISPLAY></MYUSAGE_GRAPH><MYUSAGE_GRAPH><CHILD_STATUS>false</CHILD_STATUS><DISPLAY_NAME>Free 3 to 3</DISPLAY_NAME><PARENT_DISCOUNT_STATUS>1</PARENT_DISCOUNT_STATUS><LENGTH>0</LENGTH><USED_LENGTH>3</USED_LENGTH><LOW_THRESHOLD>0.0</LOW_THRESHOLD><LOW_THRESHOLD_VALUE>0</LOW_THRESHOLD_VALUE><HIGH_THRESHOLD_VALUE>200</HIGH_THRESHOLD_VALUE><PARENT_CATEGORY>FUP</PARENT_CATEGORY><DISPLAY>FREE_3_TO_3</DISPLAY></MYUSAGE_GRAPH></GRAPH><PLAN_SPEND>$29.00</PLAN_SPEND><FREE_SPEND>3.3</FREE_SPEND><GRAPH_DISPLAY><MYUSAGE_GRAPH_DISPLAY><GRAPH_DISPLAY>You&apos;ve used   $73.55 of your  $150 Cap and have  $76.45 left.   You can use up to  $76.45 of this on  international calls.</GRAPH_DISPLAY><DISPLAY>CAP DISPLAY</DISPLAY><DISCOUNT_STATUS>1</DISCOUNT_STATUS><PLAN_SPEND>$29.00</PLAN_SPEND><NON_PLAN_SPEND>$153.07</NON_PLAN_SPEND></MYUSAGE_GRAPH_DISPLAY><MYUSAGE_GRAPH_DISPLAY><GRAPH_DISPLAY>You&apos;ve used   3 minutes and 30 seconds  of your  included  200 minutes. You&apos;ve 196 minutes and 30 seconds  left.  </GRAPH_DISPLAY><DISPLAY>FREE_3_TO_3</DISPLAY><PLAN_SPEND>Free</PLAN_SPEND></MYUSAGE_GRAPH_DISPLAY></GRAPH_DISPLAY><IS_SHARED_UNITS_PRESENT>false</IS_SHARED_UNITS_PRESENT><IS_MY_UNITS_PRESENT>true</IS_MY_UNITS_PRESENT><BONUS_STATUS>true</BONUS_STATUS><ADD_UNIT_INFO_STATUS>true</ADD_UNIT_INFO_STATUS><ROLE>SSC</ROLE><BONUS_AND_REMAINING_UNITS><FREE_BONUS_UNIT><DISPLAY_NAME>Mobile Internet (On Net)</DISPLAY_NAME><REMAINING>124.137MB</REMAINING><USAGE>13.85</USAGE><EXPIRY_DATE>12 Feb 11</EXPIRY_DATE><IS_SHARED_UNITS_PRESENT>false</IS_SHARED_UNITS_PRESENT><IS_MY_UNITS_PRESENT>true</IS_MY_UNITS_PRESENT></FREE_BONUS_UNIT></BONUS_AND_REMAINING_UNITS><FAMILY_PACKS_AND_PASSES><PACKS_AND_PASSES>50% Off Mobile Internet Classic Member</PACKS_AND_PASSES></FAMILY_PACKS_AND_PASSES></MOBILE_GROUP";
			
    		if (login.contains("<MOBILE_GROUP")) {
    			// Postpay
    			String pt = Utils.RegEx(login, "<PLAN_TYPE>(.*?)</PLAN_TYPE>", 1);
    			if (pt.contains("B")) {
    				// Broadband
    				String pname  = Utils.RegEx(login, "<DISPLAY_NAME>(.*?)</DISPLAY_NAME>", 1);
      				String uamt   = Utils.RegEx(login, "<USAGE_AMOUNT>(.*?)</USAGE_AMOUNT>", 1);
      				String uquota = Utils.RegEx(login, "<HIGH_THRESHOLD>(.*?)</HIGH_THRESHOLD>", 1);
      			    p.setProgressPeak(sd, Utils.getDoubleFromString(uamt),Utils.getDoubleFromString(uquota));
      			    p.addExtra(0, "Plan", pname);
    			} else {
    				String asof  = Utils.RegEx(login, "(?si)<EFFECTIVE_DATE>(.*?)</EFFECTIVE_DATE>", 1);
    				String pname = Utils.RegEx(login, "(?si)<GRAPH>.*?MYUSAGE_GRAPH.*?DISPLAY_NAME>(.*?)</DISPLAY_NAME>", 1);
      			    p.addExtra(0, "Plan", pname);
      			    p.addExtra(0, "As of", asof);
    			
    				String usage1 = Utils.RegEx(login, "(?si)<GRAPH_DISPLAY>.*?MYUSAGE_GRAPH_DISPLAY.*?GRAPH_DISPLAY(.*?)</GRAPH_DISPLAY>", 1);
       				String uamt = "";
       				String ex1="";
       			
       				if (!Utils.isBlank(usage1)) {
    					uamt = Utils.RegEx(usage1,"You.*?used.*?(.*?)of",1);
    					double lim = Utils.getDoubleFromString(Utils.RegEx(usage1,"You.*?used.*?your.*?(.*?)Cap",1));
    					if (lim>0 && cap==0) {
    						cap = lim;
    					}
    					
       				}
       				
    				String usage2 = Utils.RegEx(login, "(?si)<GRAPH_DISPLAY>.*?MYUSAGE_GRAPH_DISPLAY.*?MYUSAGE_GRAPH_DISPLAY.*?GRAPH_DISPLAY(.*?)</GRAPH_DISPLAY>", 1);
       				if (!Utils.isBlank(usage2)) {
       					String eused = Utils.RegEx(usage2,"used.*?You&apos;ve(.*?)left",1);
       					p.addExtra(0, "Minutes Left", eused);
       				}
    			
       				// Process Data
       				//List<String> ua = Utils.RegExArray(login, "(?si)<BONUS_AND_REMAINING_UNITS>.*?MB.*?<USAGE>(.*?)</USAGE>", 1);
     				//List<String> ua = Utils.RegExArray("<MOBILE_GROUP><IS_AJAX>N</IS_AJAX><INDICATOR>0</INDICATOR><TOKEN>http://www.avg.com.au/3mobile/index.cfm?token=f73691aa652047aaf997561450b9f4a2</TOKEN><PLAN_TYPE>V</PLAN_TYPE><FINAL_DATE>01 Mar 11</FINAL_DATE><EFFECTIVE_DATE>09:00 pm 21 Mar 11</EFFECTIVE_DATE><GRAPH><MYUSAGE_GRAPH><CHILD_STATUS>false</CHILD_STATUS><DISPLAY_NAME>$49 Cap</DISPLAY_NAME><PARENT_DISCOUNT_STATUS>1</PARENT_DISCOUNT_STATUS><LENGTH>50</LENGTH><USED_LENGTH>13</USED_LENGTH><LOW_THRESHOLD>49.0</LOW_THRESHOLD><LOW_THRESHOLD_VALUE>49</LOW_THRESHOLD_VALUE><HIGH_THRESHOLD_VALUE>450</HIGH_THRESHOLD_VALUE><PARENT_CATEGORY>CAP DISPLAY</PARENT_CATEGORY><DISPLAY>CAP DISPLAY</DISPLAY></MYUSAGE_GRAPH></GRAPH><PLAN_SPEND>$84.90</PLAN_SPEND><GRAPH_DISPLAY><MYUSAGE_GRAPH_DISPLAY><GRAPH_DISPLAY>You&apos;ve used   $84.90 of your  $450 Cap.You&apos;ve $365.10 left.  </GRAPH_DISPLAY><DISPLAY>CAP DISPLAY</DISPLAY><DISCOUNT_STATUS>1</DISCOUNT_STATUS><PLAN_SPEND>$84.90</PLAN_SPEND><NON_PLAN_SPEND>$0.00</NON_PLAN_SPEND></MYUSAGE_GRAPH_DISPLAY></GRAPH_DISPLAY><IS_SHARED_UNITS_PRESENT>true</IS_SHARED_UNITS_PRESENT><IS_MY_UNITS_PRESENT>true</IS_MY_UNITS_PRESENT><BONUS_STATUS>true</BONUS_STATUS><ADD_UNIT_INFO_STATUS>false</ADD_UNIT_INFO_STATUS><ROLE>SSC</ROLE><BONUS_AND_REMAINING_UNITS><FREE_BONUS_UNIT><DISPLAY_NAME>Mobile Internet (On Net)</DISPLAY_NAME><REMAINING>0MB</REMAINING><USAGE>350</USAGE><EXPIRY_DATE>31 Mar 11</EXPIRY_DATE><IS_MY_UNITS_PRESENT>true</IS_MY_UNITS_PRESENT><IS_SHARED_UNITS_PRESENT>false</IS_SHARED_UNITS_PRESENT></FREE_BONUS_UNIT><FREE_BONUS_UNIT><BENEFIT_GROUP_MEMBER_INDICATOR>2</BENEFIT_GROUP_MEMBER_INDICATOR><DISPLAY_NAME>Mobile Bband On-Net Aggr</DISPLAY_NAME><ALLOWANCE>2224MB</ALLOWANCE><TOTAL_USAGE>&lt;span style=&apos;color: #008f37;text-decoration:underline;font-weight:lighter;cursor:pointer&apos; onclick=&apos;javascript:breakdownpopup(&quot;VG::35915287::15&quot;)&apos;&gt;382.729MB&lt;/span&gt;</TOTAL_USAGE><REMAINING>1841.271MB</REMAINING><SERVICES>2</SERVICES><USED_AMOUNT>145.946MB</USED_AMOUNT><USAGE>382.729</USAGE><EXPIRY_DATE>30 Dec 99</EXPIRY_DATE><IS_SHARED_UNITS_PRESENT>true</IS_SHARED_UNITS_PRESENT><IS_MY_UNITS_PRESENT>false</IS_MY_UNITS_PRESENT></FREE_BONUS_UNIT></BONUS_AND_REMAINING_UNITS><FAMILY_PACKS_AND_PASSES><PACKS_AND_PASSES>Mobile Internet Classic Member</PACKS_AND_PASSES><PACKS_AND_PASSES>yourMovies</PACKS_AND_PASSES></FAMILY_PACKS_AND_PASSES></MOBILE_GROUP>", "<USAGE>(.*?)</USAGE>", 1);
      				List<String> ua = Utils.RegExArray(login, "<USAGE>(.*?)</USAGE>", 1);
      					
      				double dused=0;
       				for (int i=0;i<ua.size();i++) {
       					String damt = ua.get(i);
       					dused += Utils.getDoubleFromString(damt);
       				}

       				p.setPostpayPeakOffPeak(sd, Utils.getDoubleFromString(uamt), cap, dused, data);

    			}
    			
    		} else {
    			// Prepay
    			String prepaytable  = Utils.ExtractString(login,"Credit remaining",0, "</strong>", "</tr>");
    			if (prepaytable!=null) {
    				String expiry  = Utils.ExtractString(prepaytable,"Expires",0, ":", "</p>");
       				String credit  = Utils.ExtractString(prepaytable,"<",0, "$", "<");
    				p.setPrepayMoney(expiry, "dd MMM yy", Utils.getDoubleFromString(credit), cap);
    			} else {
    				// Prepay Broadband
        			String prepaybroadband  = Utils.ExtractString(login,mobile,0, "</strong>", "</table>");
    				if (prepaybroadband!=null) {
    					String plan  = Utils.RemoveCrap(Utils.ExtractString(prepaybroadband,"<br",0, "/>", "</td>"));
    					String datar  = Utils.RemoveCrap(Utils.RegEx(login, "(?si)Data remaining:.*?\">(.*?)<", 1));
    	   				String expiry  = Utils.RemoveCrap(Utils.RegEx(login, "(?si)Expires:(.*?)</", 1));
    	   			    p.addExtra(0, "Plan", plan);		
    	   			 
    	   				p.setPrepayDataOnly3Mob(expiry, "dd MMM yy", Utils.getDoubleFromString(datar), data);
    	   				
    				}
    			}
    			
    		}
    		
        	
        	
        } catch (Exception e) {
        	DebugLogLog(p,"Exception "+e.toString());
	   	    p.loadsuccess=false;
      		p.loaderrormsg="Internal Error, please report problem";
    		p.failurereason = Provider.fail_connection;
    		return;
        }
	}
	
	public static void waitForAnswer(Provider p){
	     while(!p.questionAnswered){
	    	 try {
		         Thread.sleep(500);
	    	   } catch (Exception e) {}
	     }
	}
	
	
	
	public static void internodeUpdater(Provider p,asyncUpdateProvider task) {
		// Manual Test
		CacheManager cm = CacheManager.getInstance();
	
		String INODE_URL = "https://customer-webtools-api.internode.on.net/api/v1.5/";
		                    	
	   	String  username  = p.getParameterByID(1).CurrentValueAsInternalString();
	   	String  password  = p.getParameterByID(2).CurrentValueAsInternalString();
	   	parameter serviceparam = p.getParameterByID(4);

    	GeoStore client = new GeoStore(UIManager.getInstance().myapp);

        try {
        	DebugLogStart(p);
        	
        	DebugLogLog(p,"Accessing "+INODE_URL);
            
        	String serviceUsage = HttpHelper.HttpGetAuthenticated(client,INODE_URL,username,password);
     		//String serviceUsage="<?xml version='1.0' encoding='UTF-8'?><internode><api><services count='2'><service type='Personal_ADSL' href='/api/v1.5//usage/1917923'>1917923</service></services></api></internode>";
        	 
    		if (serviceUsage==null) {
    	   	    p.loadsuccess=false;
        		p.loaderrormsg="No usage returned";
        		p.failurereason = Provider.fail_connection;
        		return;
    		}
    		
            // Check Password
    		if (serviceUsage.contains("Authorization Required")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
    		}
            
    		// Get Service ID
   			//String service=Utils.ExtractString(serviceUsage, "<service type", 0, ">", "<");
  
    		List<String> slist = Utils.RegExArray(serviceUsage, "<service type.*?>(.*?)<", 1);
   			
    		String service=null;
   			
    		if (slist.size()==1) {
    			service = slist.get(0);			// Only 1 service
    		} else {
    			if (serviceparam.isBlank()) {
        			p.questionchoices = Utils.toStringArray(slist);
        		   	task.askQuestion();
        	    	waitForAnswer(p);
        	        service = slist.get(p.questionanswer);
        	        serviceparam.setTextValue(service);
        	        SlotManager.getInstance().saveSlot(p.slotnumber, false);
    			} else {
    				service = serviceparam.CurrentValueAsString();
    			}
    		}
   			
    		if (service!=null) {
    			
    			DebugLogLog(p,"Selected service id "+service);
    			
    		    String usageURL = String.format("https://customer-webtools-api.internode.on.net/api/v1.5/%s/usage",service);

       			DebugLogLog(p,"Getting usage via:"+usageURL);
       		 
    		    //String usageData="<internode><api><service type=\"Personal_ADSL\" request=\"usage\">1961855</service><traffic name=\"total\" rollover=\"2011-03-22\" plan-interval=\"Monthly\" quota=\"60000000000\" unit=\"bytes\">8633000712</traffic></api></internode>";
    		    String usageData = HttpHelper.HttpGetAuthenticated(client,usageURL,username,password);
    		    
        		if (usageData==null) {
          			DebugLogLog(p," -- null returned");
          			           		 
        	   	    p.loadsuccess=false;
            		p.loaderrormsg="Failed to retrieve usage";
            		p.failurereason = Provider.fail_connection;
            		return;
        		}
     			DebugLogLog(p," data returned ["+usageData+"]");
     			 
    			cm.WriteCachedusageData(p,usageData,cm.UsageFile(p),0);
    			        	   	
      			
        		String regExUsage=".*?rollover=\"(.*?)\".*?quota=\"(.*?)\".*?bytes\">(.*?)<";
          		Pattern upat = Pattern.compile(regExUsage);
        		Matcher mu = upat.matcher(usageData+newline+newline);
        		
        		if (mu.find())  {
            		//Log.i(TAG,mu.group());
          			cm.WriteCachedusageData(p,usageData,cm.UsageFile(p),1);
        			
          			
        			String rDate = mu.group(1);
        			double quota = Utils.getDoubleFromString(mu.group(2));
        			double used = Utils.getDoubleFromString(mu.group(3));
              
        			if (quota>0) {
        				quota /= 1000000;
        			}
        			if (used>0) {
        				used  /= 1000000;
        			}
        	        p.setProgressPeakStart(rDate,used,quota);
         	       	p.clearExtras();
         	       	
         	       	// Extra Info
         	       	try {
         			    String serviceURL = String.format("https://customer-webtools-api.internode.on.net/api/v1.5/%s/service",service);
            		    String serviceData = HttpHelper.HttpGetAuthenticated(client,serviceURL,username,password);
             			 
            		    if (serviceData!=null) {
                			cm.WriteCachedusageData(p,serviceData,cm.UsageFile(p),1);
                			p.addExtra(0, "Plan", Utils.RegEx(serviceData, "<plan>(.*?)</plan", 1));
               	      		p.addExtra(0, "Speed", Utils.RegEx(serviceData, "<speed>(.*?)</speed", 1));
               	      		p.addExtra(0, "Type", Utils.RegEx(serviceData, "<rating>(.*?)</rating", 1));
            		    }
            		    
            		    // History - Last 30 Days
         			    String historyURL = String.format("https://customer-webtools-api.internode.on.net/api/v1.5/%s/history?count=5",service);
            		    String historyData = HttpHelper.HttpGetAuthenticated(client,historyURL,username,password);
            		    
            		    if (serviceData!=null) {
                			cm.WriteCachedusageData(p,historyData,cm.UsageFile(p),2);
            		    }
            		    
         	       	} catch (Exception e) {
         				DebugLogLog(p,"Exception processing extra information "+e.toString() + "\nStack Trace " + Utils.getStackTrace(e));
         	       	}
         	       	
         	       	
             		p.loadsuccess=true;
            		p.loaderrormsg="";
            		return;

            		
        		} else {
          			cm.WriteCachedusageData(p,usageData,cm.UsageFile(p),1);

           	   	    p.loadsuccess=false;
            		p.loaderrormsg="Unexpected format returned, please report via Menu/Report problem";
            		p.failurereason = Provider.fail_connection;
            		return;
        		}
                
    		} else {
    			// No Service
    			cm.WriteCachedusageData(p, serviceUsage,cm.UsageFile(p),0);

       	   	    p.loadsuccess=false;
        		p.loaderrormsg="Could not locate service";
        		p.failurereason = Provider.fail_connection;
        		return;
    		}
    		
            
        } catch (Exception e) {
			DebugLogLog(p,"Exception processing provider "+e.toString() + "\nStack Trace " + Utils.getStackTrace(e));
			 
  	   	    p.loadsuccess=false;
    		p.loaderrormsg="Internal Error, please report problem via Menu/Report problem";
    		p.failurereason = Provider.fail_internalerror;
    		return;
        }
        
	}

	private static boolean VirginLogin(DefaultHttpClient http, Provider p) {
		CacheManager cm = CacheManager.getInstance();
		
		String sLoginUrl="https://www.virginmobile.com.au/selfcare/MyAccount/LogoutLoginPre.jsp";
		
	    try {
			HttpPost post = new HttpPost(sLoginUrl);

		   	parameter username_param = p.getParameterByID(1);
	    	parameter password_param = p.getParameterByID(3);

	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("username", username_param.CurrentValueAsInternalString()));
	        nameValuePairs.add(new BasicNameValuePair("password", password_param.CurrentValueAsInternalString()));
	        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = http.execute(post);
	        String landing=null;
	        
	          if (response != null) {
	        	  landing = EntityUtils.toString(response.getEntity());
				  cm.WriteCachedusageData(p, landing,cm.UsageFile(p),1);

	        	  if (landing.contains("Welcome to the Members' Area")) {
	        		  return true;
	        	  }
	            }
	          
	    } catch (Exception e) {
	    	return false;
	    }
	    
	    return false;
	}
	
	
	public static void virginPostpay(Provider p) {
		DefaultHttpClient http = HttpHelper.QuotaClient();
		CacheManager cm = CacheManager.getInstance();
		
	   	String mobile = p.getParameterByID(1).CurrentValueAsInternalString();

	   	
	   	double DataLimit = 0;
	   	parameter datap = p.getParameterByID(8);
	   	if (!datap.isBlank()) {
	   		DataLimit = datap.numberval;
	   	}
	   	
	   	double CreditLimit = 0;
	   	if (!p.getParameterByID(7).isBlank()) {
	   		CreditLimit = p.getParameterByID(7).numberval;
	   	}
		
	   	// Start Day
	   	int StartDay = 1;
	   	if (!p.getParameterByID(6).isBlank()) {
	   		StartDay = (int)p.getParameterByID(6).numberval;
	   	}
	   	
	   	
		if (!VirginLogin(http,p)) {
	   		p.loadsuccess=false;
    		p.loaderrormsg="Incorrect user details";
    		p.failurereason = Provider.fail_authentication;
    		return;
		} else {
			String sUrl = "https://www.virginmobile.com.au/selfcare/dispatch/PostPayUnbilledUsage";
			String billusage = HttpHelper.sendHttpGet(http,sUrl);
			//String billusage = "<tr><td width=\"15\">&nbsp;</td><td align=\"left\" height=\"30\" style=\"border-bottom: 1px solid #cccccc; border-right: 1px solid #cccccc; padding-left: 15px;\"><b>0435318943</b></td><td align=\"left\" style=\"border-bottom: 1px solid #cccccc; border-right: 1px solid #cccccc; padding-left: 15px;\">Smart Cap 29</td><td align=\"left\" style=\"border-bottom: 1px solid #cccccc; padding-left: 15px;\">$4.99</td><td width=\"15\">&nbsp;</td></tr>";
			if (billusage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="Did not retreive Billing data, check site via Provider URL";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			} else {

				// Plan - (?si).*?0408883484.*?">(.*?)<
				// Used - (?si).*?0408883484.*?">.*?">(.*?)<
				
				String plan=Utils.RegEx(billusage, "(?si).*?"+mobile+".*?\">(.*?)<",1);
				String bal=Utils.RegEx(billusage,"(?si).*?"+mobile+".*?\">.*?\">(.*?)<",1);

				double balamt = Utils.getDoubleFromString(bal==null?"":bal);
				double dataused=0;
				
				// Usage - <span.*?>.*?USAGE:.?([+\-]?(?:[0-9]*\.[0-9]+|[0-9]+\.)).?MB

				String sUrl2 = "https://www.virginmobile.com.au/selfcare/dispatch/DataUsageRequest";
				String datausage = HttpHelper.sendHttpGet(http,sUrl2);
				if (datausage!=null) {
					String data=Utils.RegEx(datausage,"<span.*?>.*?USAGE:.?([+\\-]?(?:[0-9]*\\.[0-9]+|[0-9]+\\.)).?MB",1);
					dataused = Utils.getDoubleFromString(data==null?"":data);
				}
				  
				
				cm.WriteCachedusageData(p, datausage,cm.UsageFile(p),1);
				
				p.setPostpayPeakOffPeak(StartDay, balamt, CreditLimit, dataused, DataLimit);
				p.clearExtras();
				p.addExtra(0, "Plan", plan);
			
			}
			
		
		
		}
		
		
		
	}
	
	
	
	
	public static void virginPrepay(Provider p) {

		DefaultHttpClient http = HttpHelper.QuotaClient();
		CacheManager cm = CacheManager.getInstance();
		
	   	String mobile = p.getParameterByID(1).CurrentValueAsInternalString();

	   	double DataLimit = 0;
	   	parameter datap = p.getParameterByID(7);
	   	if (!datap.isBlank()) {
	   		DataLimit = datap.numberval;
	   	}
	   	
	   	double CreditLimit = 0;
	   	if (!p.getParameterByID(8).isBlank()) {
	   		CreditLimit = p.getParameterByID(8).numberval;
	   	}
		
		if (!VirginLogin(http,p)) {
	   		p.loadsuccess=false;
    		p.loaderrormsg="Incorrect user details";
    		p.failurereason = Provider.fail_authentication;
    		return;
		} else {
			// Get Usage
			String sUrl = "https://www.virginmobile.com.au/selfcare/dispatch/AccountHistory";
			String usage = HttpHelper.sendHttpGet(http,sUrl);
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="Did not retreive usage, check site via Provider URL";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			} else {
				cm.WriteCachedusageData(p, usage,cm.UsageFile(p),1);

				try {
					String bal=Utils.RegEx(usage, ".*?displayBalanceBreakDown.*?"+mobile+"','\\$(.*?)'",1);
					String data=Utils.RegEx(usage, "(?si).*?displayBalanceBreakDown.*?"+mobile+"'.*?Data Plan.*?\"middle\">(.*?)<",1);
					String expires=Utils.RegEx(usage, "(?si).*?displayBalanceBreakDown.*?"+mobile+".*?width=\"20\".*?width=\"20\".*?<td.*?cccc\">(.*?)<",1);

					double dbal = Utils.getDoubleFromString(bal==null?"":bal);
					double ddata = Utils.getMBVal(data==null?"":data);
					
					p.setPrepayData(expires, "EEEE dd MMMM yyyy", dbal, CreditLimit, ddata, DataLimit);
					p.clearExtras();
					
				} catch (Exception e) {
			   		p.loadsuccess=false;
		    		p.loaderrormsg="Problem parsing prepay data, please use Menu/Report problem";
		    		p.failurereason = Provider.fail_internalerror;
		    		return;
				}
				
				
				
			}
		}
		
	}
	
	public static void dodoISP(Provider p) {
		DefaultHttpClient http = HttpHelper.QuotaClient();
		CacheManager cm = CacheManager.getInstance();
		
      	parameter userp = p.getParameterByID(1);
    	parameter passw = p.getParameterByID(2);

    	String user = Utils.EscapeStringHTML(userp.CurrentValueAsInternalString());
     	String pwd = Utils.EscapeStringHTML(passw.CurrentValueAsInternalString());
 
		try {

			String url;
			
			url = String.format("https://secure.dodo.com.au/externalwebservices/MembersPageUsage.asmx/ProvideUsage?un=%s&pw=%s",user,pwd);
			
			String usage = HttpHelper.sendHttpGet(http,url);
			//String usage = "<?xml version=\"1.0\" encoding=\"utf-8\"?> <PageUsageResponse xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://secure.dodo.com.au/externalservices\"> <AccountInformation> <PlanName>Dodo 1.5Mbps Gold (bundled with HomePhone)</PlanName> <PeriodStarts>6/7/2010</PeriodStarts> <PeriodEnds>5/8/2010</PeriodEnds> </AccountInformation> <Usage> <UsagePeriod> <Text>Peak</Text> <StartsAt>12:59</StartsAt> <EndsAt>01:00</EndsAt> <Allowance>10000</Allowance> <AmountDownloaded>3720.59</AmountDownloaded> <AmountUploaded>825.1</AmountUploaded> <TotalUsage>4545.69</TotalUsage> <PercentageUsed>45.46</PercentageUsed> <HasPurchasedTopUpBlocks>false</HasPurchasedTopUpBlocks> <TopUpBlocks /> </UsagePeriod> <UsagePeriod> <Text>Off-Peak</Text> <StartsAt>00:59</StartsAt> <EndsAt>13:00</EndsAt> <Allowance>100000</Allowance> <AmountDownloaded>2794.14</AmountDownloaded> <AmountUploaded>755.88</AmountUploaded> <TotalUsage>3550.02</TotalUsage> <PercentageUsed>3.55</PercentageUsed> <HasPurchasedTopUpBlocks>false</HasPurchasedTopUpBlocks> <TopUpBlocks /> </UsagePeriod> </Usage> <ExcessUsage /> </PageUsageResponse>";
			
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="No usage returned";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			

			if (usage.contains("<PlanName />")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}
			
			usage = usage.trim();
			cm.WriteCachedusageData(p, usage,cm.UsageFile(p),0);

			// Parse Data
			String planName = Utils.RegEx(usage, "<PlanName>(.*?)</", 1);
			int sd          = Utils.getIntegerFromString(Utils.RegEx(usage, "<PeriodStarts>(.*?)/", 1));
			
			// Peak
			double peakAllowance =  Utils.getDoubleFromString(Utils.RegEx(usage, "(?s)Peak.*?Allowance>(.*?)<", 1));
			double peakUsed      =  Utils.getDoubleFromString(Utils.RegEx(usage, "(?s)Peak.*?TotalUsage>(.*?)<", 1));
			
			if (peakAllowance==0) {
				peakAllowance =  Utils.getDoubleFromString(Utils.RegEx(usage, "(?s)Usage.*?Allowance>(.*?)<", 1));
				peakUsed      =  Utils.getDoubleFromString(Utils.RegEx(usage, "(?s)Usage.*?TotalUsage>(.*?)<", 1));
			}
			
			double offpeakAllowance =  Utils.getDoubleFromString(Utils.RegEx(usage, "(?s)Off-Peak.*?Allowance>(.*?)<", 1));
			double offpeakUsed      =  Utils.getDoubleFromString(Utils.RegEx(usage, "(?s)Off-Peak.*?TotalUsage>(.*?)<", 1));
			
			p.setProgressPeakOffPeak(sd, peakUsed, peakAllowance, offpeakUsed, offpeakAllowance);
			
			p.clearExtras();
			p.addExtra(0, "Plan", planName);
			
			Log.i(TAG,"Dodo complete");
			
		} catch (Exception e) {
	  		p.loadsuccess=false;
    		p.loaderrormsg="Parser error "+e.toString();
    		p.failurereason = Provider.fail_internalerror;
		}
		
	}
	
	private static double OctValue(String s) {
		if (s==null) return 0;
		double dv = Utils.getDoubleFromString(s);
		if (dv>1) {
			dv /= 1000000;
		}
		return dv;
	}
	
	public static void amNet(Provider p) {
		DefaultHttpClient http = HttpHelper.QuotaClient();
		CacheManager cm = CacheManager.getInstance();
		
      	parameter userp = p.getParameterByID(1);
    	parameter passw = p.getParameterByID(2);

    	String user = Utils.EscapeStringHTML(userp.CurrentValueAsInternalString());
     	String pwd = Utils.EscapeStringHTML(passw.CurrentValueAsInternalString());
 
		try {

			String url;
			
			url = String.format("https://memberutils.amnet.com.au/usage.asmx/GetCurrentPeakUsage?username=%s&password=%s",user,pwd);
			
			String usage = HttpHelper.sendHttpGet(http,url);
			//String usage = "<?xml version=\"1.0\" encoding=\"utf-8\"?> <CurrentPeakUsage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://au.com.amnet.memberutils/\"> <Period> <Summary xmlns=\"http://amcom.com.au/UsageWebServices\"> <Customer>19802</Customer> <Start>2010-07-11T00:00:00</Start> <Finish>2010-08-11T00:00:00</Finish> <PeakOtherOctetsIn>4085645270</PeakOtherOctetsIn> <PeakOtherOctetsOut>495056779</PeakOtherOctetsOut> <PeakPeerOctetsIn>1418872756</PeakPeerOctetsIn> <PeakPeerOctetsOut>172745200</PeakPeerOctetsOut> <OffpeakOtherOctetsIn>7238516445</OffpeakOtherOctetsIn> <OffpeakOtherOctetsOut>3582795978</OffpeakOtherOctetsOut> <OffpeakPeerOctetsIn>3405905005</OffpeakPeerOctetsIn> <OffpeakPeerOctetsOut>1052158289</OffpeakPeerOctetsOut> </Summary> <RateLimits xmlns=\"http://amcom.com.au/UsageWebServices\" /> </Period> <AllowanceList> <RateGroupName xmlns=\"http://amcom.com.au/UsageWebServices\">Broadband 2+ Non-Enabled 20G/40G - $89.00</RateGroupName> <Allowances xmlns=\"http://amcom.com.au/UsageWebServices\"> <Allowance> <Name>Off-Peak Basic</Name> <OctetsIn>40000000000</OctetsIn> <OctetsOut xsi:nil=\"true\" /> <Period>Offpeak</Period> <Class>Other</Class> </Allowance> <Allowance> <Name>Off-Peak Peering</Name> <OctetsIn>80000000000</OctetsIn> <OctetsOut xsi:nil=\"true\" /> <Period>Offpeak</Period> <Class>Peer</Class> </Allowance> <Allowance> <Name>Peak Basic</Name> <OctetsIn>20000000000</OctetsIn> <OctetsOut xsi:nil=\"true\" /> <Period>Peak</Period> <Class>Other</Class> </Allowance> <Allowance> <Name>Peak Peering</Name> <OctetsIn>40000000000</OctetsIn> <OctetsOut xsi:nil=\"true\" /> <Period>Peak</Period> <Class>Peer</Class> </Allowance> </Allowances> </AllowanceList> </CurrentPeakUsage>";
			//String usage = "<?xml version=\"1.0\" encoding=\"utf-8\"?> <CurrentPeakUsage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://au.com.amnet.memberutils/\"> <Period> <Summary xmlns=\"http://amcom.com.au/UsageWebServices\"> <Customer>2200</Customer> <Start>2010-01-19T00:00:00</Start> <Finish>2010-02-19T00:00:00</Finish> <PeakOtherOctetsIn>27141534054</PeakOtherOctetsIn> <PeakOtherOctetsOut>23661674193</PeakOtherOctetsOut> <PeakPeerOctetsIn>12577175371</PeakPeerOctetsIn> <PeakPeerOctetsOut>6023904132</PeakPeerOctetsOut> <OffpeakOtherOctetsIn>11412633209</OffpeakOtherOctetsIn> <OffpeakOtherOctetsOut>14483685555</OffpeakOtherOctetsOut> <OffpeakPeerOctetsIn>13533782922</OffpeakPeerOctetsIn> <OffpeakPeerOctetsOut>4109243573</OffpeakPeerOctetsOut> </Summary> <RateLimits xmlns=\"http://amcom.com.au/UsageWebServices\"> <RateLimit> <TrafficPeriod>Both</TrafficPeriod> <TrafficClass>Other</TrafficClass> </RateLimit> </RateLimits> </Period> <AllowanceList> <RateGroupName xmlns=\"http://amcom.com.au/UsageWebServices\">Amnet ADSL 2 40GB - $69.00 - XMAS 2006 Special</RateGroupName> <Allowances xmlns=\"http://amcom.com.au/UsageWebServices\"> <Allowance> <Name>Peering</Name> <OctetsIn>40000000000</OctetsIn> <OctetsOut xsi:nil=\"true\" /> <Period>Both</Period> <Class>Peer</Class> </Allowance> <Allowance> <Name>Basic</Name> <OctetsIn>40000000000</OctetsIn> <OctetsOut xsi:nil=\"true\" /> <Period>Both</Period> <Class>Other</Class> </Allowance> </Allowances> </AllowanceList> </CurrentPeakUsage>";
			
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="No usage returned";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			
			

			if (!usage.contains("<Customer>")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}
			
			usage = usage.trim();
			cm.WriteCachedusageData(p, usage,cm.UsageFile(p),0);

			// Parse Data
			String planName = Utils.RegEx(usage, "<RateGroupName.*?>(.*?)</", 1);
			
			int sd          = Utils.getIntegerFromString(Utils.RegEx(usage, "<Start>....-..-(..)", 1));
			
			// Peak - Total
			String ptotal = Utils.RegEx(usage, "(?si)<Name>Peak Basic</Name>.*?OctetsIn>(.*?)</", 1);
			if (ptotal==null) {
				ptotal = Utils.RegEx(usage, "(?si)Basic</Name>.*?OctetsIn>(.*?)</", 1);
			}
			String peakin = Utils.RegEx(usage, "PeakOtherOctetsIn>(.*?)</", 1);
			String peakout = Utils.RegEx(usage, "PeakOtherOctetsOut>(.*?)</", 1);
			
			// OffPeak Total
			String optotal = Utils.RegEx(usage, "(?si)<Name>Off-Peak Basic</Name>.*?OctetsIn>(.*?)</", 1);
			if (optotal==null) {
				optotal = Utils.RegEx(usage, "(?si)Peering</Name>.*?OctetsIn>(.*?)</", 1);
			}
			String offpeakin = Utils.RegEx(usage, "OffpeakOtherOctetsIn>(.*?)</", 1);
			String offpeakout = Utils.RegEx(usage, "OffpeakOtherOctetsOut>(.*?)</", 1);

			
			p.setProgressPeakOffPeak(sd, OctValue(peakin), OctValue(ptotal), OctValue(offpeakin), OctValue(optotal));
			
			p.clearExtras();
			p.addExtra(0, "Plan", planName);
			p.addExtra(0, "Peak Uploads", Utils.FormatValueC(Utils.E_FORMAT_DATA_SIMPLE,Utils.getDoubleFromString(peakout)));
			p.addExtra(0, "Off-Peak Uploads", Utils.FormatValueC(Utils.E_FORMAT_DATA_SIMPLE,Utils.getDoubleFromString(offpeakout)));
			
			Log.i(TAG,"AmNet complete");
			
		} catch (Exception e) {
	  		p.loadsuccess=false;
    		p.loaderrormsg="Parser error "+e.toString();
    		p.failurereason = Provider.fail_internalerror;
		}
		
	}
	
	
	public static void aaNet(Provider p) {
		
		DefaultHttpClient http = HttpHelper.OpenClient();
		CacheManager cm = CacheManager.getInstance();
		
      	parameter userp = p.getParameterByID(1);
    	parameter passw = p.getParameterByID(2);

    	String user = Utils.EscapeStringHTML(userp.CurrentValueAsInternalString());
     	String pwd = Utils.EscapeStringHTML(passw.CurrentValueAsInternalString());
 
		try {

			String url;
			
			url = String.format("https://www.aanet.com.au/aau.php?%s,%s",user,pwd);
			
			String usage = HttpHelper.sendHttpGet(http,url);
			//String usage = "0,4343852671,2011-05-02 19:01:30,2010-05-10,12288,1500/256,1500-M1,203.171.84.30,8145.38,4.00,0,2006-08-11,mark ,smith";
			
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="No usage returned";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			

			if (usage.contains("Login Failed")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}
			
			usage = usage.trim();
			cm.WriteCachedusageData(p, usage,cm.UsageFile(p),0);

			// Fields are
			//  0 == Upload Bytes
			//  1 == Download Bytes
			//  2 == Last Update
			//  3 == Next Anniversary
			//  4 == Included Data
			//  5 == Speed
			//  6 == Plan Name
			//  7 == IP
			//  8 == Remaining Quota
			//  9 == Excess Usage Charges
			//  10 == Current Excess Charge
			//  11 == Signup Date
			//  12 == Name
			
			String au[] = usage.split(",");
			if (au.length<7) {
		  		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect data returned";
	    		p.failurereason = Provider.fail_connection;
			} else {
				
				double peakused  = Utils.getDoubleFromString(au[1])/1024/1024;
				double peaklimit = (  (int)(Utils.getDoubleFromString(au[4])/1019))*1000;
				
      	        p.setProgressPeak(au[3],peakused,peaklimit);
     	       	p.clearExtras();

     	       	p.addExtra(0, "Plan", au[6]);
    	       	p.addExtra(0, "Connected", au[2]);
      	       	p.addExtra(0, "Uploaded", Utils.FormatValueC(Utils.E_FORMAT_DATA_SIMPLE,Utils.getDoubleFromString(au[0])/1024/1024));
     	       	p.addExtra(0, "Speed", au[5]);
    	       	p.addExtra(0, "IP Address", au[7]);
     	       	p.addExtra(0, "Excess Charges", au[9]);
 				
			}
			
		} catch (Exception e) {
	  		p.loadsuccess=false;
    		p.loaderrormsg="Parser error "+e.toString();
    		p.failurereason = Provider.fail_internalerror;
		}
	}
	
	
	public static void westNet(Provider p) {
		// Manual Test
		DefaultHttpClient http = HttpHelper.OpenClient();
		CacheManager cm = CacheManager.getInstance();
		
      	parameter userp = p.getParameterByID(1);
    	parameter passw = p.getParameterByID(2);

       	int sd = (int)p.getParameterByID(6).numberval;

    	String user = Utils.EscapeStringHTML(userp.CurrentValueAsInternalString());
     	String pwd = Utils.EscapeStringHTML(passw.CurrentValueAsInternalString());
 
		try {

			String url;
			
			url = String.format("https://secure1.wn.com.au/webservices/customer/ADSLUsage/adslxmlusage.asmx/getUsage?username=%s&password=%s",user,pwd);
			
			String usage = HttpHelper.sendHttpGet(http,url);
							 
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="No usage returned";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			

			if (!usage.contains("<Usage")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}
			
			

			// Save Usage Data
			cm.WriteCachedusageData(p, usage,cm.UsageFile(p),0);
			
			String usagenocommans = usage.replace(",", "");
			
			// Parse Data
			double peaklimit = Utils.getDoubleFromString(Utils.RegEx(usagenocommans, "usagelimit>(.*?)/", 1));
			double offpeaklimit = Utils.getDoubleFromString(Utils.RegEx(usagenocommans, "usagelimit>.*?/(.*?)<", 1));
			double peakUsed = Utils.getDoubleFromString(Utils.RegEx(usagenocommans, "peakused>(.*?)<", 1));
			double offpeakUsed = Utils.getDoubleFromString(Utils.RegEx(usagenocommans, "offpeakused>(.*?)<", 1));
	        p.setProgressPeakOffPeak(sd, peakUsed, peaklimit, offpeakUsed, offpeaklimit);
			
			
		} catch (Exception e) {
    		p.loadsuccess=false;
    		p.loaderrormsg="Parser error "+e.toString();
    		p.failurereason = Provider.fail_internalerror;
		}
		
	}
	
	public static void Netspace(Provider p) {
		// Manual Test
		DefaultHttpClient http = HttpHelper.OpenClient();
		CacheManager cm = CacheManager.getInstance();
		
      	parameter userp = p.getParameterByID(1);
    	parameter passw = p.getParameterByID(2);
    	parameter uploads = p.getParameterByID(10);
 
    	String user = userp.CurrentValueAsInternalString();
     	String pwd = passw.CurrentValueAsInternalString();
 
     	boolean includeup = uploads.numberval==0;
     	
		try {
			
	      	String usage = HttpHelper.HttpGetAuthenticated(http,"https://usage.netspace.net.au/usage-meter/adslusage?version=4&granularity=MONTH",user,pwd);
	        
	      	//String usage = "<USAGE> <DATABLOCKS></DATABLOCKS> <END_DATE>23-10-2010</END_DATE> <GRANULARITY>MONTH</GRANULARITY> <PLAN> <DESCRIPTION>Naked Home-1</DESCRIPTION> <LIMIT> <MEGABYTES>50000</MEGABYTES> <NAME>Peak</NAME> </LIMIT> <LIMIT> <MEGABYTES>60000</MEGABYTES> <NAME>Off Peak</NAME> </LIMIT> </PLAN> <START_DATE>24-09-2010</START_DATE> <TRAFFIC> <DATA> <DOWNLOADS>24532</DOWNLOADS> <TIMESTAMP>2010-09-24 00:00:00</TIMESTAMP> <TYPE>Peak</TYPE> <UPLOADS>7438</UPLOADS> </DATA> <DATA> <DOWNLOADS>13811</DOWNLOADS> <TIMESTAMP>2010-09-24 00:00:00</TIMESTAMP> <TYPE>Off Peak</TYPE> <UPLOADS>25121</UPLOADS> </DATA> <DESCRIPTION>Normal Billable Traffic</DESCRIPTION> </TRAFFIC> <TRAFFIC> <DATA> <DOWNLOADS>3</DOWNLOADS> <TIMESTAMP>2010-09-24 00:00:00</TIMESTAMP> <TYPE>Peak</TYPE> <UPLOADS>0</UPLOADS> </DATA> <DATA> <DOWNLOADS>4</DOWNLOADS> <TIMESTAMP>2010-09-24 00:00:00</TIMESTAMP> <TYPE>Off Peak</TYPE> <UPLOADS>0</UPLOADS> </DATA> <DESCRIPTION>Free Traffic</DESCRIPTION> </TRAFFIC> <VERSION>4</VERSION> </USAGE>";
	      	
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="No usage returned";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			

			if (usage.contains("401 Authorization Required")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}
			
			// PL (?si)LIMIT.*?MEGABYTES>(.*?)</.*?<NAME>Peak
			// OL (?si)Peak.*?LIMIT.*?MEGABYTES>(.*?)</
			// SD <START_DATE>(.*?)-
			// PD (?si)<TRAFFIC>.*?<DATA>.*?<DOWNLOADS>(.*?)</
			// PU (?si)<TRAFFIC>.*?<DATA>.*?<UPLOADS>(.*?)</
			// OP U (?si)<TRAFFIC>.*?<DATA>.*?<DATA>.*?<UPLOADS>(.*?)</
			// OP D (?si)<TRAFFIC>.*?<DATA>.*?<DATA>.*?<DOWNLOADS>(.*?)</

			// Save Usage Data
			cm.WriteCachedusageData(p, usage,cm.UsageFile(p),0);
			
			
			// Parse Data
			int sd = Utils.getIntegerFromString(Utils.RegEx(usage, "<START_DATE>(.*?)-", 1));
			double peaklimit = Utils.getDoubleFromString(Utils.RegEx(usage, "(?si)LIMIT.*?MEGABYTES>(.*?)</.*?<NAME>Peak", 1));
			double offpeaklimit = Utils.getDoubleFromString(Utils.RegEx(usage, "(?si)Peak.*?LIMIT.*?MEGABYTES>(.*?)</", 1));
			
			double peakDown = Utils.getDoubleFromString(Utils.RegEx(usage, "(?si)<TRAFFIC>.*?<DATA>.*?<DOWNLOADS>(.*?)</", 1));
			double peakUp = Utils.getDoubleFromString(Utils.RegEx(usage, "(?si)<TRAFFIC>.*?<DATA>.*?<UPLOADS>(.*?)</", 1));
			
			double offpeakDown = Utils.getDoubleFromString(Utils.RegEx(usage, "(?si)<TRAFFIC>.*?<DATA>.*?<DATA>.*?<UPLOADS>(.*?)</", 1));
			double offpeakUp = Utils.getDoubleFromString(Utils.RegEx(usage, "(?si)<TRAFFIC>.*?<DATA>.*?<DATA>.*?<UPLOADS>(.*?)</", 1));
			
			if (includeup) {
		        p.setProgressPeakOffPeak(sd, peakDown+peakUp, peaklimit, offpeakDown+offpeakUp, offpeaklimit);
			} else {
		        p.setProgressPeakOffPeak(sd, peakDown, peaklimit, offpeakDown, offpeaklimit);
			}
			
			
		} catch (Exception e) {
    		p.loadsuccess=false;
    		p.loaderrormsg="Parser error "+e.toString();
    		p.failurereason = Provider.fail_internalerror;
		}
		
	}
	
	
	public static void iiNetUpdate(Provider p) {
		// Manual Test
		DefaultHttpClient http = HttpHelper.QuotaClient();
		CacheManager cm = CacheManager.getInstance();
		
      	parameter userp = p.getParameterByID(1);
    	parameter passw = p.getParameterByID(2);

    	String user = Utils.EscapeStringHTML(userp.CurrentValueAsInternalString());
     	String pwd = Utils.EscapeStringHTML(passw.CurrentValueAsInternalString());
 
		try {

			String url;
			
			if (p.provider_definition.disk_isp==0) {
				// iiNet
				url = String.format("https://toolbox.iinet.net.au/cgi-bin/new/volume_usage_xml.cgi?username=%s&action=login&password=%s",user,pwd);
			} else {
				// Westnet
				url = String.format("https://myaccount2.westnet.com.au/cgi-bin/new/volume_usage_xml.cgi?username=%s&action=login&password=%s",user,pwd);
			}
			
			String usage = HttpHelper.sendHttpGet(http,url);
							 
			if (usage==null) {
		   		p.loadsuccess=false;
	    		p.loaderrormsg="No usage returned";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			

			if (usage.contains("Authentication failure")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Incorrect username/password";
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}

			if (usage.contains("Sorry, due to the large number of customers attempting to access Toolbox at this time, volume usage is unavailable.") || usage.contains("Maintenance")) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Toolbox is busy or under Maintenance, Try later";
	    		p.failurereason = Provider.fail_connection;
	    		return;
			}
			
			// Save Usage Data
			cm.WriteCachedusageData(p, usage,cm.UsageFile(p),0);
			
			
			InputStream ix = Utils.StringToInputStream(usage);
		    TreeBuilder builder = new TreeBuilder();
	        TagNode root = (TagNode) builder.parseXML( ix );

	        String plan=null;
	        String offperiod=null;
	        String ip=null;
	        Date connected=null;

	        int aniversary=1;			// Get from Params

	        double pv=0;
	        double pm=0;
	        boolean ps=false;
	        
	        double opv=0;
	        double opm=0;
	        double uploads=0;
	        boolean ops=false;
	        
	        double free=0;
	        
	        for (TreeNode node = root.getChild(); node != null; node = node.getSibling()) {
				  
	        	  if (node.toString().equalsIgnoreCase("account_info")) {
					  plan = xmlUtils.nodeValue(node,"plan");
				  }
	        	  if (node.toString().equalsIgnoreCase("volume_usage")) {
	        		  offperiod = xmlUtils.nodeValue(node,"offpeak_start") + "-" + xmlUtils.nodeValue(node,"offpeak_end");
	        		  aniversary = Utils.getIntegerFromString(xmlUtils.nodeValue(xmlUtils.subNode(node, "quota_reset"),"anniversary"));
	        		  
	            	  TreeNode traffic= xmlUtils.subNode(node, "expected_traffic_types");
			          for (TreeNode tnode = traffic.getChild(); tnode != null; tnode = tnode.getSibling()) {
			        	  if (tnode.toString().equalsIgnoreCase("type")) {
				        	  TagNode t=(TagNode)tnode;
				        	  
				        	  double  usedMb=0;
				        	  double  maxMb=0;
				        	  boolean shaped=false;
				        	  
				        	  String ctraf= t.getAttribute("classification");
				        	  usedMb = Utils.getDoubleFromString(t.getAttribute("used"));
				        	  if (usedMb>0) {
				        		  usedMb /= 1000000;
				        	  }
				        	  
				        	  maxMb = Utils.getDoubleFromString(xmlUtils.nodeValue(tnode,"quota_allocation"));
				        	  shaped = Utils.getBoolFromString(xmlUtils.nodeValue(tnode,"is_shaped"));
				        	  
				        	  if (ctraf.equalsIgnoreCase("peak") || ctraf.equalsIgnoreCase("anytime")) {
				        		  // Setup Peak Value
				        		  pv = usedMb;
				        		  pm = maxMb;
				        		  ps = shaped;
				        	  } else if (ctraf.equalsIgnoreCase("freezone")) {
				        		  free = usedMb * 1000000;
				        	  } else if (ctraf.equalsIgnoreCase("uploads")) {
				        		  uploads = usedMb * 1000000; 
				        	  }
				        	  else {
				        		  // Off Peak
				        		  opv = usedMb;
				        		  opm = maxMb;
				        		  ops = shaped;
				        	  }
				        	  
				        	  Log.i(TAG,"Class:"+ctraf);
			        	  }
			          } // Traffic
	        	  }
	        	  if (node.toString().equalsIgnoreCase("connections")) {
	            	  TagNode ipNode= (TagNode)xmlUtils.subNode(node, "ip");
	            	  if (ipNode!=null) {
		            	  String cd=ipNode.getAttribute("on_since");
		            	  connected = DateUtils.DateFromStringTZ(cd, "yyyy-MM-dd HH:mm:ss", "UTC");
		        		  ip=xmlUtils.nodeValue(node,"ip");
	            	  }
	        	  }
	          }
	          
	        // Setup Progress
	        p.setProgressPeakOffPeak(aniversary, pv, pm, opv, opm);
	        
 	       	p.clearExtras();
 	       
	        // Finish Parsing
	        if (connected!=null) {
		       	  p.addExtra(0,"Connected since",DateUtils.DateLong(connected));
	        }
	        if (ip!=null) {
		        p.addExtra(0,"IP Address",ip);
	        }
	        
	        p.addExtra(0, "Freezone", Utils.FormatValueC(Utils.E_FORMAT_DATA_SIMPLE,free));
	        if (uploads>0) {
		        p.addExtra(0, "Uploads", Utils.FormatValueC(Utils.E_FORMAT_DATA_SIMPLE,uploads));
	        }
	        
	        if (offperiod!=null) {
		        p.addExtra(0,"Off Peak Period",offperiod);
	        }
	        
			p.addExtra(0,"Peak shaped",ps?"Yes":"No");
			p.addExtra(0,"OffPeak shaped",ops?"Yes":"No");

	        if (plan!=null) {
		        p.addExtra(0,"Plan",plan);
            }
     		p.loadsuccess=true;
    		p.loaderrormsg="";
    		return;
          
		} catch (Exception e) {
     		p.loadsuccess=false;
    		p.loaderrormsg="Parser error "+e.toString();
    		p.failurereason = Provider.fail_internalerror;
		}
    	
	}
	
}
