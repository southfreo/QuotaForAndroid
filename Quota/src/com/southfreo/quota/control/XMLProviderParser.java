package com.southfreo.quota.control;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

import com.southfreo.quota.control.ProviderManager.asyncUpdateProvider;
import com.southfreo.quota.model.Provider;
import com.southfreo.quota.model.account;
import com.southfreo.quota.model.accountlines;
import com.southfreo.quota.model.condition;
import com.southfreo.quota.model.cycle;
import com.southfreo.quota.model.datakey;
import com.southfreo.quota.model.datasource;
import com.southfreo.quota.model.extradata;
import com.southfreo.quota.model.kbProgressBar;
import com.southfreo.quota.model.parameter;
import com.southfreo.quota.model.urlinfo;
import com.southfreo.quota.model.rssItem;
import com.southfreo.quota.smplmathparser.EvaluationTree;
import com.southfreo.quota.smplmathparser.MathParser;
import com.southfreo.quota.smplmathparser.MathParserException;
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
import com.southfreo.quota.utils.customRedirects;


public class XMLProviderParser {
	private final static String TAG="Quota-XMLProviderParser";


	public static String DynamicString(Provider isp,String string) {
		return DynamicStringBV(isp,string,"");
	}
	
	
	public static String DynamicStringBV(Provider isp,String string,String dvalue) {

		if (Utils.isBlank(string)) return "";
		
		String newString = string;
		
		//
		// Parameter Substitution
		//
		if (newString.contains("--DS_")) {
			
			for (int i=0;i<isp.disk_parameters.params.size();i++) {
				parameter p = isp.disk_parameters.params.get(i);
				String key="--DS_PARAM"+p.pid+"--";
				String keyI="--DS_PARAM_I_"+p.pid+"--";
				newString = newString.replace(key, p.CurrentValueWithEscape());
				newString = newString.replace(keyI, p.CurrentValueAsInternalString());
			}
		}

		
		//
		// DataKey Substitution
		//
		if (newString.contains("--DK_")) {
			List<String> keys= Utils.RegExArray(newString, "--DK_.*?--", 0);
			
			if (keys.size()>0) {
				
				for (int i=0;i<keys.size();i++) {
					
					String key=keys.get(i);
					datakey fk = isp.getDataKeyForName(key);
					if (fk!=null) {
						
						// Add Logic for Array,RegEx part matching
						if (key.contains("[")) {
							// Array
							int col = Utils.getIntegerFromString(Utils.RegEx(key, "[(.*?)]", 1));
							if (fk.strArray!=null) {
								if (col<fk.strArray.size()) {
									try {
										newString = newString.replace(key, (String)fk.strArray.get(col));
									} catch (Exception e) {
										Log.e(TAG,"Incorrect array specified in Dynamic Key");
									}
								}
							}
						} else if (key.contains("_I_")) {
							newString = newString.replace(key,fk.InternalText());
						} else if (key.contains("{")) {
						     String regEx = Utils.RegEx(key, "\\{(.*?)\\}", 1);
						     if (regEx!=null) {
						    	 String val = Utils.RegEx(fk.OutputText(), regEx, 1);
						    	 if (val!=null) {
						    		 newString = newString.replace(key,val);
						    	 }
						     }
						} else {
							newString = newString.replace(key,fk.OutputText());
						}
					} else {
						// Blank out
						if (Utils.isBlank(dvalue)) {
							newString = newString.replace(key, "");
						} else {
							newString = newString.replace(key, dvalue);
						}
					}				
				}

				
			} else {
				Log.e(TAG,"Internal error localtion DataKey");
			}
		}
		
		return newString;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void ProcessURL(Provider p,urlinfo u,DefaultHttpClient client) {
		
		u.returndata = null;
		boolean postflag;
		
		// DynamicString
		u.urlstring = DynamicString(p,u.urlstring);
		u.postdata  = DynamicString(p,u.postdata);
		u.username  = DynamicString(p,u.username);
		u.password  = DynamicString(p,u.password);
		u.headers   = DynamicString(p,u.headers);
		
		HttpRequestBase request;
		postflag=false;
		
		DebugLogLog(p,"\n\nProcessing URL id:"+u.myid + " "+ u.msg);

		// Proxy Code
		// client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("192.168.0.22",8888));

		if (Utils.isBlank(u.postdata)) {
			DebugLogLog(p,"Processing GET URL id:"+u.myid);
			request = new HttpGet(u.urlstring);
		} else {
			DebugLogLog(p,"Processing POST URL id:"+u.myid);
			HttpPost post = new HttpPost(u.urlstring);
			
			if (!u.headers.contains("Content-Type")) {
				post.setHeader("Content-Type", "application/x-www-form-urlencoded");
			}
			
			StringEntity tmp=null;
			try {
				tmp = new StringEntity(u.postdata,"utf-8");
				post.setEntity(tmp);

				// Alternate Method
//				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
//				String[] farray = u.postdata.split("&");
//				for (int i=0;i<farray.length;i++) {
//					String pv = farray[i];
//					String[] parray = pv.split("=");
//					try {
//						if (parray.length==1) {
//							formparams.add(new BasicNameValuePair(parray[0], ""));
//						} else if (parray.length==2) {
//							formparams.add(new BasicNameValuePair(parray[0], parray[1]));
//						}
//					} catch (Exception e) {}
//				}
//				UrlEncodedFormEntity fentity = new UrlEncodedFormEntity(formparams, "UTF-8");
//				fentity.setChunked(true);
//				post.setEntity(fentity);
				
				
			} catch (Exception e) {
				Log.e(TAG,"Could not create StringEntity for post");
			}
			
			request = post;
			postflag=true;
		}
		

		// Add Headers
		if (!Utils.isBlank(u.headers)) {
			DebugLogLog(p," - adding headers");
			String[] harray = u.headers.split("\\|\\|");
			int v=harray.length;
			int i=0;
			int j=0;
			for (i=0;j<v/2;i=i+2) {
				String name = harray[i];
				String val  = harray[i+1];
				request.addHeader(name, val);
				Log.i(TAG,"Name=["+name+"] val ["+val+"]");
				j++;
			}
		}
		
		// Add Password
		if (!Utils.isBlank(u.username) && !Utils.isBlank(u.password)) {
			DebugLogLog(p," - adding authentication");
			HttpHelper.addHttpAuthentication(request, u.username, u.password);
		}
		
		try {
			HttpResponse response=null;
			response = client.execute(request);
			if (response!=null) {
				u.returndata = EntityUtils.toString(response.getEntity());  
				DebugLogLog(p," - RESPONSE OK size:"+u.returndata.length());
			} else {
				DebugLogLog(p," - RESPONSE NULL");
			}
		} catch (Exception e) {
			DebugLogLog(p," - RESPONSE EXCEPTION"+e.getMessage());
			Log.e(TAG,"Error retrieving URL: "+e.toString());
		}
		
	}
	
	
	public static void waitForAnswer(Provider p){
	     while(!p.questionAnswered){
	    	 try {
		         Thread.sleep(500);
	    	   } catch (Exception e) {}
	     }
	}
	
	
	//
	
	public static boolean ProcessAction(Provider isp,condition c,datakey pk, datasource ds,asyncUpdateProvider task) {
		
		boolean abort=false;

		DebugLogLog(isp,"ProcessAction :"+c.action);

		if (c.action.equalsIgnoreCase("SKIP_URL")) {
			// Skip URL
			Log.i(TAG,"Skipping URL"+isp.currentURL+1);
			isp.currentURL++;
		} 
		
		//
		// Retry 
		//
		else if (c.action.equalsIgnoreCase("RETRY")) {
			// Abort Load
			double wait=Utils.getDoubleFromString(c.ap2)*1000;
			if (wait<1000) {
				wait=1000;
			}
			
			int tries=Utils.getIntegerFromString(c.ap3);
			urlinfo url = (urlinfo)ds.object;
			if (url.attempt<tries) {
				Log.i(TAG,String.format("Retry URL Attempt: [%d] Wait %.2f Tries %d",url.attempt,wait,tries));	
				try {
					Thread.sleep((long)wait);
				} catch (Exception e) {}
				url.attempt++;
				if (c.when.equalsIgnoreCase("AFTER")) {
					isp.currentURL--;
				}
			}
		} 
		//
		// Retry Alternate
		//
		else if (c.action.equalsIgnoreCase("RETRY_ALTERNATE")) {
			// Abort Load
			double wait=Utils.getDoubleFromString(c.ap2);
			int tries=Utils.getIntegerFromString(c.ap3);
			urlinfo url = (urlinfo)ds.object;
			// Create new URL from Alternate string
			url.urlstring = DynamicString(isp,url.urlalternate);
			url.postdata = DynamicString(isp,url.postalternate);
			
			if (url.attempt<tries) {
				Log.i(TAG,String.format("RetryAlternate URL [%s] Attempt: [%d]",url.urlalternate,url.attempt));	
				try {
					Thread.sleep((long)wait);
				} catch (Exception e) {}
				url.attempt++;
				if (c.when.equalsIgnoreCase("AFTER")) {
					isp.currentURL--;
				}
			}
		}

		
		//
		// Write Key to Cache
		//
		else if (c.action.equalsIgnoreCase("WRITE_CACHE")) {

			//AP1 = CacheName
			//AP2 = Data
			
			if (!Utils.isBlank(c.ap1) && !Utils.isBlank(c.ap2)) {
				String data = DynamicString(isp,c.ap2);
				//[isp writeStringFileToCache:c.ap1:data];
			}
		} 
		
		
		//
		// Popup Picker
		//
		else if (c.action.equalsIgnoreCase("POPUP_PICKER")) {
			// Show Picker
			datakey dk = isp.getDataKeyForName(c.ap2);
			if (dk!=null && dk.strArray!=null) {
				isp.questionchoices = Utils.toStringArray((List<String>) dk.strArray);
    		   	task.askQuestion();
    	    	waitForAnswer(isp);
    	    	
    	    	String v = (String) dk.strArray.get(isp.questionanswer);
    	    	parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.ap3));
    	    	if (p!=null) {
    	    		p.setTextValue(v);
    	    		SlotManager.getInstance().saveSlot(isp.slotnumber, false);
    	    	}
    
			}
			
		} 
		
		
		//
		// Fail 
		//
		else if (c.action.equalsIgnoreCase("FAIL")) {
			// Abort Load
			isp.failurereason = Provider.fail_nousagereturned;
			isp.loaderrormsg = c.ap1;
			isp.loadsuccess=false;
			abort=true;
		} 
		
		//
		// Set a Key Value 
		//	
		else if (c.action.equalsIgnoreCase("SETKEY")) {
			// Evaluate the Matchs expression
			
			if (c.ap1==null || c.ap2==null) {
				Log.e(TAG,"setkey error");
			} else {
				// Create Key
				datakey ispk = new datakey();
				
				ispk.myid				= 9999;
				ispk.name				= c.ap1;
				ispk.type				= "STRING";
				
				ispk.setTextValue(DynamicString(isp,c.ap2));
				isp.datakeys.add(ispk);
			}
		}
		//
		// Set a Parameter Value 
		//	
		else if (c.action.equalsIgnoreCase("SETPARAMETER")) {
			// Evaluate the Matchs expression
			
			
			if (Utils.isBlank(c.ap1) || Utils.isBlank(c.ap2)) {
				Log.e(TAG,"setparameter error");
			} else {
				parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.ap1));
				if (p!=null) {
					String newval = DynamicString(isp,c.ap2);
					p.setTextValue(newval);	
				}
			}
		}
		
		
		//
		// Expression 
		//	
		else if (c.action.equalsIgnoreCase("EXPR")) {
			// Evaluate the Matchs expression

			if (Utils.isBlank(c.ap1)) {
				Log.e(TAG,"Nothing to evalutae in condition");
			} else {
				String ns = c.ap1.replace("--DS_CDATA--",getDataSourceSourceDataString(ds));
				ns = DynamicString(isp,ns);
				double res=Utils.evaluteExpression(ns);
				
				// Add Key
				datakey ispk =  isp.getDataKeyForName(pk.name);
				ispk.setTextValue(String.format("%.4f",res));
				isp.datakeys.add(ispk);
			}

			
		}
		//
		// Template
		//
		else if (c.action.equalsIgnoreCase("XXXXX")) {
			//
		} else {
			//[[VarManager sharedVarManager]xmlLogger:[NSString stringWithFormat:@"XML Error: condition action incorrect %@",c.action]];
		}
		return abort;
		
		
	}
	
	public static boolean ProcessConditionOperator (Provider isp, condition c , datakey dk ,datasource ds,asyncUpdateProvider task) {

		boolean abort=false;

		
		DebugLogLog(isp,"ProcessConditionOperator :"+c.xmlid);

		// Process the Operator
		if (c.operator.equalsIgnoreCase("IS_KEY_EMPTY")) {

			datakey ldk=null;
			
			if (!Utils.isBlank(c.p1)) {
				ldk = isp.getDataKeyForName(c.p1);
			} else {
				ldk = isp.getDataKeyForName(dk.name);
			}
			
			if (ldk==null || ldk.isEmpty()) {
				abort=ProcessAction(isp,c,ldk,ds,task);
			}
		} 
		
		else if (c.operator.equalsIgnoreCase("IS_KEY_NOT_EMPTY")) {
			// Check Key Data
			datakey ldk=null;
			if (!Utils.isBlank(c.p1)) {
				ldk = isp.getDataKeyForName(c.p1);
			} else {
				ldk = isp.getDataKeyForName(dk.name);
			}
			
			if (ldk!=null && !ldk.isEmpty()) {
				abort=ProcessAction(isp,c,ldk,ds,task);
			}
		
		}

		else if (c.operator.equalsIgnoreCase("IS_PARAM_EMPTY")) {
			// Check Param Data
			parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.p1));
			
			if (p.isBlank()) {
				abort=ProcessAction(isp,c,dk,ds,task);
			}
		}
		
		else if (c.operator.equalsIgnoreCase("IS_PARAM_NOT_EMPTY")) {
			// Check Param Data
			parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.p1));
			
			if (!p.isBlank()) {
				abort=ProcessAction(isp,c,dk,ds,task);
			}
		}
		
		
		else if (c.operator.equalsIgnoreCase("IF_PARAM_EQUALS")) {
			// Get the Option
			parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.p1));
			String ct = c.p2;
			if (p==null || c.p1==null || ct==null) {
				Log.e(TAG,"param_equals : invalid parameters");
			} else {
				// do?
				//KBLOG(@"Comparing P2:[%@] with VAL:[%@]",ct,[p CurrentValueAsString]);
				if (ct.equalsIgnoreCase(p.CurrentValueAsString())) {
					abort=ProcessAction(isp,c,dk,ds,task);
				}
			}
		}
		
		// RegEx Match
		else if (c.operator.equalsIgnoreCase("IF_PARAM_MATCHES")) {
			// Get the Option
			parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.p1));
			String ct = c.p2;
			if (p==null || c.p1==null || ct==null) {
				Log.e(TAG,"param_matches : invalid parameters");
			} else {
				// do?
				String pv = p.CurrentValueAsInternalString();
				if (pv.contains(ct)) {
					abort=ProcessAction(isp,c,dk,ds,task);
				}
			}
		}
		
		// Parameter Checked
		else if (c.operator.equalsIgnoreCase("IF_PARAM_CHECKED")) {
			// Get the Option
			parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.p1));
			String ct = c.p2;
			if (p==null || c.p1==null || ct==null || c.p2==null) {
				Log.e(TAG,"param_checked : invalid parameters");
			} else {
				// Check if Selected
				try {
					if (p.isSelectedVal(Utils.getIntegerFromString(c.p2)-1)) {
						abort=ProcessAction(isp,c,dk,ds,task);
					}
						}
				catch (Exception  e) {
					Log.e(TAG,"param_checked : invalid parameters");
				}
			}
		}
		//
		// Parameter NOT Checked - Duplicate Code
		//
		else if (c.operator.equalsIgnoreCase("IF_PARAM_NOT_CHECKED")) {
			// Get the Option
			parameter p = isp.getParameterByID(Utils.getIntegerFromString(c.p1));
			String ct = c.p2;
			if (p==null || c.p1==null || ct==null || c.p2==null) {
				Log.e(TAG,"param_notchecked : invalid parameters");
			} else {
				// Check if Selected
				try {
					if (!p.isSelectedVal(Utils.getIntegerFromString(c.p2)-1)) {
						abort=ProcessAction(isp,c,dk,ds,task);
					}
						}
				catch (Exception  e) {
					Log.e(TAG,"param_checked : invalid parameters");
				}
			}
		}
		
		else if (c.operator.equalsIgnoreCase("TRUE")) {
		    // Just Perform Action
			abort=ProcessAction(isp,c,dk,ds,task);
		} 
		
		
		else {
			Log.e(TAG,"condition operator incorrect");
		}
		
		return abort;
	}

	
	
	//
	
	
	
	
	private static boolean ProcessConditionForKey(Provider isp,datakey dk, String when, datasource ds,asyncUpdateProvider task) {
		boolean abort=false;
		
		if (isp.provider_definition.conditions==null) {
			return abort;	
		}
		
		
		String matchconditions=dk.condition;
		
		if (!Utils.isBlank(matchconditions)) {
			String ma[] =  matchconditions.split(",");
			for (int i=0;i<ma.length;i++) {
				String aid=ma[i];
				condition c=  isp.provider_definition.getConditionForID(aid);
				if (c!=null) {
					if (c.when.equalsIgnoreCase(when)) {
						abort=  ProcessConditionOperator(isp,c,dk,ds,task);
						if (abort) {
							// Don't process any more conditions
							return true;
						}
					}
				}
			}
		} 
		return abort;

	}
	
	
	private static String getDataSourceSourceDataString (datasource ds) {
		// Check Type and Cast
		if (ds.type==datasource.datasource_url) {
			urlinfo u = (urlinfo)ds.object;
			return u.returndata;
		} else if (ds.type==datasource.datasource_key) {
			datakey dk = (datakey)ds.object;
			return dk._text;
		} else if (ds.type==datasource.datasource_string) {
			//
			return ds.text;
		}	
		return null;
	}
	
	private static void srcExtractAddKey (Provider isp, datakey pk, datasource d_s) {

		DebugLogLog(isp,String.format("ExtractKey ID:[%d] NAME:[%s]",pk.myid,pk.name));

		
		// Add this key to isp
		datakey ispk = new datakey();
		
		ispk.myid				= pk.myid;
		ispk.name				= pk.name;
		
		if (pk.extract.toUpperCase().contains("EXPRESSION")) {
			ispk.find				= DynamicStringBV(isp,pk.find,"0");
		} else {
			ispk.find				= DynamicString(isp,pk.find);
		}

		ispk.start				= DynamicString(isp,pk.start);
		ispk.end				= DynamicString(isp,pk.end);
		ispk.format				= pk.format;
		ispk.outputformat		= DynamicString(isp,pk.outputformat);
		ispk.outputtype			= pk.outputtype;
		ispk.type				= pk.type;
		ispk.pos				= pk.pos;
		ispk.removechars		= pk.removechars;
		ispk.replacechars		= pk.replacechars;
		ispk.trimspace			= pk.trimspace;
		ispk.removehtml			= pk.removehtml;
		ispk.prefix				= DynamicString(isp,pk.prefix);
		ispk.postfix			= DynamicString(isp,pk.postfix);
		ispk.escape				= pk.escape;
		ispk.javascript			= pk.javascript;
		
		
		// Get Source
		String src=getDataSourceSourceDataString(d_s);
		
		// Store value and translate based on type
			String et=null;
			// 
			//   REGEX Matching
			//
			if (pk.extract.equalsIgnoreCase("REGEX")) {
	
				String regEx = Utils.RegEx(src, ispk.find, pk.pos);
				
				if(regEx!=null) {
					ispk.setTextValue(regEx);
					isp.datakeys.add(ispk);
				} else {
					Log.e(TAG,"No RegEx match: "+ispk.find);
				}
		    } else if (pk.extract.equalsIgnoreCase("REGEX-ARRAY")) {
		    	
				List<String> slist = Utils.RegExArray(src, ispk.find, ispk.pos);
			   	
				if (slist!=null && slist.size()>0) {
					ArrayList<String>mlist = new ArrayList<String>();
					for (int i=0;i< slist.size();i++) {
						String mv = slist.get(i);
						ispk.setTextValue(mv);
						if (!Utils.isBlank(ispk.outputformat)) {
							mlist.add(ispk.outputformat);
						} else {
							mlist.add(ispk._text);
						}
					}
					
					Utils.removeDuplicateWithOrder((ArrayList) mlist);
					ispk.strArray = (ArrayList<?>) mlist;
					isp.datakeys.add(ispk);
				}
		    	

		} 
		
		// 
		//   REGEX-ARRAY - Leave Duplicates
		//
		
		else if (pk.extract.equalsIgnoreCase("REGEX-ARRAY-RAW")) {
			List<String> slist = Utils.RegExArray(src, ispk.find, ispk.pos);
		   	
			if (slist!=null && slist.size()>0) {
				ArrayList<String>mlist = new ArrayList<String>();
				for (int i=0;i< slist.size();i++) {
					String mv = slist.get(i);
					ispk.setTextValue(mv);
					if (!Utils.isBlank(ispk.outputformat)) {
						mlist.add(ispk.outputformat);
					} else {
						mlist.add(ispk._text);
					}
				}
				ispk.strArray = (ArrayList<?>) mlist;
				isp.datakeys.add(ispk);
			}
		}
			
		
		
		// 
		//   utc-captions 
		//
		else if (pk.extract.equalsIgnoreCase("UTC_CONVERT")) {
//			if (!isBlank(ispk.find)) {
//				// Get Data Key
//				datakey *ldk = [isp getDataKeyForName:[NSString stringWithFormat:@"--%@--",ispk.find]];
//				if (ldk!=nil && ldk.strArray!=nil) {
//					int nurls=[ldk.strArray count];
//					NSMutableArray *matchArray = [[NSMutableArray alloc] init];
//
//					for (int i=0;i<nurls;i++) {
//					    // Get the Name e.g IDR00004.T.201001170348.png
//						NSString *fn = [ldk.strArray objectAtIndex:i];
//						NSString *t = ExtractText(fn, ispk.start, 0, @".", @".");
//						NSDate *nowtime = DateFromStringTZ(t,@"yyyyMMddHHmm",@"UTC");
//						NSString *tim;
//						if (nowtime!=nil) {
//							tim = DateLong(nowtime);
//						} else {
//						    tim=@"";	
//						}
//						
//						[ispk setTextValue: tim];
//						[matchArray addObject:ispk._text];
//					}
//					ispk.strArray = matchArray;
//					[isp.datakeys addObject:ispk];
//					[matchArray release];
//				}
//			} else {
//				[self xmlError:[NSString stringWithFormat:@"UTC-CAPTIONS no find specified",ispk.find]];
//			}
		}
		
		// 
		//   Image-Stitching 
		//
		else if (pk.extract.equalsIgnoreCase("STITCH_IMAGES")) {
//			if (!isBlank(ispk.find)) {
//				// Get Data Key
//				datakey *ldk = [isp getDataKeyForName:[NSString stringWithFormat:@"--%@--",ispk.find]];
//				if (ldk!=nil && ldk.strArray!=nil) {
//					int nurls=[ldk.strArray count];
//					NSMutableArray *matchArray = [[NSMutableArray alloc] init];
//					
//					for (int i=0;i<nurls;i++) {
//						
//						// Get Local File from Cache - group (pos) contains background images
//						NSString *backimage = [isp getUrlFileNamePath:getIntegerFromString(ispk.start) :i];
//						
//						// Array of Background Images
//						NSArray *oimages = [ispk.end componentsSeparatedByString:@","];
//						
//						NSMutableArray *sa = [[NSMutableArray alloc]init];
//						for (int j=0;j<[oimages count];j++) {
//						    // Get Background Name
//							NSString *oname = [oimages objectAtIndex:j];
//							NSString *overlayimage = [isp getUrlFileNamePath:getIntegerFromString(oname) :0];
//							[sa addObject:overlayimage];
//						}
//						
//						// Create StitchName
//						NSString *sname = [isp getUrlFileNamePath:999:i];
//						
//						KBLOG(@"Stitching Back:[%@]\nOverlay:[%@]",backimage,sa);
//						
//						StitchImages(backimage, sa, sname);
//						
//						NSString *urlname = [NSString stringWithFormat:@"file://%@",sname];
//						NSMutableString *um = [NSMutableString stringWithString:urlname];
//						replaceValue(um,@" ",@"%20");
//						
//						// Special Paths
//
//							
//						// Create an Array of Stiched Images..
//						[ispk setTextValue: um];
//						[matchArray addObject:ispk._text];
//						
//						
//					}
//					ispk.strArray = matchArray;
//					[isp.datakeys addObject:ispk];
//					[matchArray release];
//				}
//			} else {
//				[self xmlError:[NSString stringWithFormat:@"UTC-CAPTIONS no find specified",ispk.find]];
//			}
		}
		
		
		
		// 
		//   ARRAYSPLIT 
		//
		else if (pk.extract.equalsIgnoreCase("SPLIT-ARRAY")) {
//			
//			if (!isBlank(ispk.find)) {
//				NSArray *sa = [ispk.find componentsSeparatedByString:ispk.start];
//				if(sa!=nil) {
//					ispk.strArray = sa;
//					[isp.datakeys addObject:ispk];
//				}
//			} else {
//				[self xmlError:[NSString stringWithFormat:@"SPLIT-ARRAY NO Data %@",ispk.find]];
//			}
		}
		
		// 
		//   COUNT THE SOURCE KEY ARRAY
		//
		else if (pk.extract.equalsIgnoreCase("COUNT")) {
			
			datakey sk = (datakey)d_s.object;
			
			int c=0;
			if (sk!=null && sk.strArray!=null) {
				c = sk.strArray.size();
			}
			ispk.setTextValue(c+"");
			isp.datakeys.add(ispk);
		
		} else if (pk.extract.equalsIgnoreCase("start_cycle")) {
			cycle c = new cycle();
			c.initWithAnniversaryDay(Utils.getIntegerFromString(ispk.start));
			if (c.startDate!=null) {
				String fmt = DateUtils.DateFormat(c.startDate, ispk.outputformat);
				if (!Utils.isBlank(fmt)) {
					ispk.setTextValue(fmt);
					isp.datakeys.add(ispk);
				}
			}
		}
		
		
		// 
		//   SUM_DATA Matching
		//
		else if (pk.extract.equalsIgnoreCase("SUM_DATA")) {
			// Process the HTML Table
			datakey sk = (datakey)d_s.object;
			
			int matchcol = Utils.getIntegerFromString(ispk.start);
			if (sk!=null) {
				ArrayList list = sk.strArray;
				if (list!=null && list.size()>0) {
					for (int i=0;i<list.size();i++) {
						ArrayList line = (ArrayList) list.get(i);
						int acol=matchcol-1;
						if (line.size()>acol) {
							String colval = (String) line.get(acol);
							// Might be a RegEx needed here
							String dm = Utils.RegEx(colval, ispk.find, 0);
							if (!Utils.isBlank(dm)) {
								String av = (String) line.get(ispk.pos-1);
								if (ispk.type.equalsIgnoreCase("NUMBER")) {
									ispk.setTextValueSum(av);
								} else {
									ispk.setTextValue(av);
								}
							}
						}
					}
				}
				isp.datakeys.add(ispk);
			}
			
		}
		
		// 
		//   HTML TABLE Matching
		//
		else if (pk.extract.equalsIgnoreCase("HTMLTABLE")){
		    ArrayList<?> tbl =  Utils.TableFromString(src,ispk.find,ispk.start,ispk.end);
		    if (tbl!=null && tbl.size()>0) {
				ispk.strArray = tbl;
				isp.datakeys.add(ispk);
		    }
		}
		
			// 
			//   SIMPLE Matching
			//
			else if (pk.extract.equalsIgnoreCase("SIMPLE")){
				String set=Utils.ExtractString(src, ispk.find, 0, ispk.start, ispk.end);
				if (set!=null) {
					ispk.setTextValue(set);
					isp.datakeys.add(ispk);
				} else {
					Log.e(TAG,"Did not simple match "+ispk.find);
				}
			} 

		// 
		//   Dynamic String
		//
			else if (pk.extract.equalsIgnoreCase("DSTRING")){
				et=DynamicString(isp,ispk.find);
				if (et!=null) {
					ispk.setTextValue(et);
					isp.datakeys.add(ispk);
				} 
			}
		// 
		//   Expression
		//
			else if (pk.extract.equalsIgnoreCase("EXPRESSION")) {
				
				et=DynamicString(isp,ispk.find);
				if (!Utils.isBlank(et)) {
					double res=Utils.evaluteExpression(et);
    				ispk.setTextValue(String.format("%.4f",res));
					isp.datakeys.add(ispk);
				}

			}
			
		// 
		//   Extended Matching
		//
			else if (pk.extract.equalsIgnoreCase("JAVASCRIPTFUNC")) {
				
			}
			else if (pk.extract.equalsIgnoreCase("JAVASCRIPTEXTEND")) {
			}	
		
		
		// 
		//   Latest  Date
		//
			else if (pk.extract.equalsIgnoreCase("LATEST_DATE")) {
				
				// Loop through all Array Items
				datakey dk = (datakey)d_s.object;
				
				if (dk==null || dk.strArray==null) {
					Log.e(TAG,"LatestDate, No arrayavailable");
				} else {
					Date cd = DateUtils.getDateAfterDays(-800);
					Date today = new Date();
					String ld=null;
					for (int i=0;i<dk.strArray.size();i++) {
						// Row
						ArrayList row = (ArrayList) dk.strArray.get(i);
						// Col
						String v = (String)row.get(ispk.pos-1);
						Date pd = DateUtils.DateFromString(v, ispk.find);
						
						if (pd!=null) {
							if (pd.compareTo(cd)>0 && (pd.compareTo(today)<0)) {
								ld=v;
							}
						}
					}
					
					// Store?
					if (ld==null) {
						Log.e(TAG,"Latest Date- did not locate date");
					} else {
        				ispk.setTextValue(ld);
    					isp.datakeys.add(ispk);
					}
					
				}
			}
			
			// 
			//   XPATH Matching
			//
			else if (pk.extract.equalsIgnoreCase("XPATH")) {
				
			}	
			// 
			//   JSON Extraction
			//
			else if (pk.extract.equalsIgnoreCase("JSON")){
			}	
			
			else if (pk.extract.equalsIgnoreCase("COPY")){
				//KBLOG(@"Copying [%@]",src);
				ispk.setTextValue(src);
				isp.datakeys.add(ispk);
			}
			else if (pk.extract.equalsIgnoreCase("CONTROL")){
			     // Don't Do Anything...
				//KBLOG(@"Control Key");
			}
		
		if (!Utils.isBlank(pk.defaultval) &&  !isp.datakeys.contains(ispk))  {
			String dd = DynamicString(isp,pk.defaultval);
	
			ispk.setTextValue(dd);
			isp.datakeys.add(ispk);

		}
		
		
		// Subkey Searching....
		if (pk.subkey>0 && !isp.datakeys.contains(ispk)) {
			// Get the SubKey
			DebugLogLog(isp,"Processing SubKey extract : "+pk.subkey);

			datakey sdk = isp.provider_definition.getDataKeyForID(pk.subkey);
			
			if (sdk==null) {
			   Log.i(TAG,"Could not locate subKey "+pk.subkey);
			} else {
				// Recursive Call
				srcExtractAddKey(isp,sdk,d_s);
				
				// New 12_4 - Add Subkey processing to subkey
				datakey ck = isp.getDataKeyForName(sdk.name);
				if (ck!=null) {
					datasource dsk = new datasource(ck);
					ProcessKey(isp,"KEY",sdk.myid,dsk,false,null);
				}
			}
		}

		
	}
	
	
	private static boolean ProcessKey (Provider isp, String src, int myid, datasource ds, boolean before,asyncUpdateProvider task) {
		boolean abort=false;
		
			for (int i=0;i<isp.provider_definition.datakeys.size();i++) {
				datakey dk = isp.provider_definition.datakeys.get(i);
				//
				// Does the current Key match the defined SRC and SRCID?
				//
				if (dk.srcid==myid && (dk.src.equalsIgnoreCase(src))) {
					Log.i(TAG,"Processing Key"+dk.myid);
					if (before) {
						abort = ProcessConditionForKey(isp,dk,"BEFORE",ds,task);
						if (abort) {
							return true;	
						} else {
							return false;
						}
					}
					
					// Data Assignment
					datakey chk = isp.getDataKeyForName(dk.name);
					
					// Retry Logic
					if (chk!=null) {
						if (ds.type==datasource.datasource_url) {
							urlinfo u = (urlinfo)ds.object;
							if (u.attempt>0) {
								Log.i(TAG,String.format("Removing Key %s due to retry..",dk.name));
								isp.datakeys.remove(chk);
								chk=isp.getDataKeyForName(dk.name);
							}
						}
					}
					
					if (chk==null) {
						// Extract Data for Key
						srcExtractAddKey(isp,dk,ds);
						
						datakey ck = isp.getDataKeyForName(dk.name);
						if (ck!=null) {
							datasource dsk = new datasource(ck);
							ProcessKey(isp,"KEY",dk.myid,dsk,false,task);
						}
						
					} else {
						Log.e("Key already setup %s",dk.name);	
					}
					
					int cu=isp.currentURL;
					abort=ProcessConditionForKey(isp,dk,"AFTER",ds,task);
					if (cu!=isp.currentURL) {
						Log.e(TAG,"Current URL Changed, Aborting Key Processing for URL");	
						break;
					}
					if (abort) {
						return true;	
					}
					
				}
				
			}
		
		return abort;
	}
	
	
	private static void xmlProgressBar(Provider isp,kbProgressBar pb)  {
		
		// Setup Progress Bar

		if (pb==null) return;
			
		try {
			// Create Local Progress Bar with Template from Provider
			kbProgressBar lpb = new kbProgressBar();
			
			lpb.pid  = pb.pid;
			lpb.type = pb.type;
			
			lpb.name = DynamicString(isp,pb.name);

			lpb.value_value = Utils.getDoubleFromString(DynamicString(isp,pb.srcValue));
			lpb.value_max   = Utils.getDoubleFromString(DynamicString(isp,pb.srcMaxValue));

			// Copy type and format from source key (Or Parameter)
			datakey dk = isp.getDataKeyForName(pb.srcValue);
			
			if (dk!=null) {
				lpb.outtype   = dk.outputtype;
				lpb.outformat = dk.outputformat;
			} else {
				lpb.outtype     = pb.outtype;
				lpb.outformat   = pb.outformat;
			}
			
			lpb.cycle_usage = isp.current_cycle;

			lpb.used		= pb.used;

			lpb.UpdateValues();
			
			isp.progress.add(lpb);
			
		} catch (Exception e) {
			DebugLogLog(isp,"xmlProgressBar Exception "+e.getMessage() + "\nStack Trace " + Utils.getStackTrace(e));
		}

		
	}
	
	public static void xmlCycle(Provider ispconnection) {
		
		
		try {
			cycle pc = ispconnection.provider_definition.current_cycle;
			
			// Decide How to Create this Based on Type
			if (pc.typecycle==1) {
				//
				// Start/End Date Supplied, Include enddate passed as flag
				//
				String start_date =	DynamicString(ispconnection,pc.srcStartDate);
				String end_date =	DynamicString(ispconnection,pc.srcEndDate);
				ispconnection.current_cycle =  new cycle();
				ispconnection.current_cycle.initWithStringDates(start_date, end_date, Utils.INTERNAL_DATE_FORMAT, pc.includeend);
				
			} else if (pc.typecycle==2) {
			    
				
				int startday = Utils.getIntegerFromString(DynamicString(ispconnection,pc.srcStartDay));
				
				if (startday!=0) {
					ispconnection.current_cycle = new cycle();
					ispconnection.current_cycle.initWithAnniversaryDay(startday);
				} else {
					// End Date Only Calculate Start Date
					String end_date =	DynamicString(ispconnection,pc.srcEndDate);
					if (end_date!=null) {
						ispconnection.current_cycle = new cycle();
						ispconnection.current_cycle.initWithEndDate(end_date, Utils.INTERNAL_DATE_FORMAT);
					}
				}
			} 		
		} catch (Exception e) {
			DebugLogLog(ispconnection,"xmlCycle Exception "+e.getMessage() + "\nStack Trace " + Utils.getStackTrace(e));
			 
		}

	
	}
	
	public static String getCol(ArrayList arr,int col) {
	    if (arr.size()>col) {
			return (String) arr.get(col);
		} else {
		    return null;	
		}
	}
	
	public static String replaceColumnGroups(ArrayList cols,String coltemplate,String options) {
		// Example
		//"(--COL1--DR) ($--COL4--)"
		
		String s="";
		
		String retstring=coltemplate;
		
		try {
			List<String> matchArray = Utils.RegExArray(retstring, "\\(.*?\\)", 0);
			if (matchArray!=null && matchArray.size()>0)   {
				int i=0;
				for (i=0;i<matchArray.size();i++) {
					String kn = matchArray.get(i);

					String ts =  Utils.RegEx(kn, "\\((.*?)\\)",1);
					String col = Utils.RegEx(kn,"--COL(.*?)--",1);
					int cn = Utils.getIntegerFromString(col); 

					String val = getCol(cols,cn-1);	// 6
					
					String nv;
					
					if ( options.compareToIgnoreCase("noblank")==0  && Utils.isBlank(val) ) {
						nv="";
					} else {
						nv=ts.replace(String.format("--COL%d--", cn), val);
					}
					s = s + nv;
				}
				return s;
			}
		}
		catch (Exception e) {
			return "";
		}
		return "";
	}
	
	
	public static String getCols(ArrayList arr,String[] cols,String extra) {
		StringBuffer s = new StringBuffer();
		
		try {
			if (cols!=null) {
				for (int i=0;i<cols.length;i++) {
					int col=Utils.getIntegerFromString(cols[i])-1;
					String val = getCol(arr,col);
					if (!Utils.isBlank(val)) {
						s.append(String.format("%s%s",val,extra));
					}
				}
			}
			return s.toString();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	
	public static String getColFormat(ArrayList arr,int col,String format) {
		String val;
		val=null;
		val = getCol(arr, col);
		if (val!=null) {
		   // Format
			val =  String.format(format.replace("%@","%s"),val);;
		}
		return val;
	}
	
	
	public static void xmlExtras (Provider ispconnection) {
		// Process all Extras
		int i=0;

		ispconnection.clearExtras();
		if (ispconnection.provider_definition.extras==null) return;
		
		for (i=0;i<ispconnection.provider_definition.extras.size();i++) {
			
			extradata ed = ispconnection.provider_definition.extras.get(i);
			
			// Set Extra for current connection
			String val =	DynamicString(ispconnection,ed.value);
			
			if (ed.type != extradata.extratype_textarray) {
				
				if (Utils.isBlank(val)) {
					if (ed.showwhenempty) {
						ispconnection.addExtra(ed.order, ed.name, "N/A");
					} 
				} else {
					ispconnection.addExtra(ed.order, ed.name, val);
				}
			} else {
				
				try {
					// Display Text Array
					datakey lk = ispconnection.getDataKeyForName(ed.src);
					if (lk!=null && lk.strArray!=null) {
						// Process the Table
						int namecol = Utils.getIntegerFromString(ed.name)-1;
						
						for (int i2=0;i2<lk.strArray.size();i2++) {
						    // Get The Name Column
							ArrayList line = (ArrayList) lk.strArray.get(i2);
							
							String namval = getColFormat(line, namecol,ed.nameFormat);
							String valval=null;
							// Get The Value Columns - Assume 2 for now
							
							String valcolar[] = ed.value.split(",");
							
							if (valcolar!=null && valcolar.length==2) {
								String v1 = getCol(line, Utils.getIntegerFromString(valcolar[0])-1);
								String v2 = getCol(line, Utils.getIntegerFromString(valcolar[1])-1);
								
								v1 = DateUtils.ConvertSecsToHrsMins(v1);
								
								valval = String.format(ed.valueFormat.replace("%@", "%s"),v1==null?"":v1,v2==null?"":v2);
							}
							
							if (namval!=null && valval!=null) {
								ispconnection.addExtra(0, namval, Utils.BlankString(valval));
							}
						}	
						}
				} catch (Exception e) {
					ispconnection.addExtra(0, "Error", "Error adding extra");
				}
			}		
		}
		
	}
	
	public static void xmlBalance (Provider ispconnection) {
	
		account  pa = ispconnection.provider_definition.accountdata;
		
		double old_changenumber=0;
		double old_changepercent=0;
		
		account la=null;
		
		if (ispconnection.accountdata!=null) {
			la = ispconnection.accountdata;
			old_changenumber=la.changeNumber;
			old_changepercent=la.changePercent;
		}

		la  = new account();
		ispconnection.accountdata = la;
		
		la.changeNumber  = old_changenumber;
		la.changePercent = old_changepercent;
		la.bal1name		= DynamicString(ispconnection,pa.bal1name);
		la.bal2name		= DynamicString(ispconnection,pa.bal2name);
		la.bal1value	= DynamicString(ispconnection,pa.bal1value);
		la.bal2value	= DynamicString(ispconnection,pa.bal2value);
		
		// Recording Balances??
//		if (!utils.isBlank(pa.bal1value)) {
//			double v = Utils.getDoubleFromString(Utils.BlankString(la.bal1value));
//			double pv = ispconnection.previousValue;
//			if (v!=pv) {
//				// Calculate new Change Number and Percentage
//				la.changeNumber = v-ispconnection.previousValue;
//				if (ispconnection.previousValue==0) {
//					la.changePercent = 100;
//				} else {
//					la.changePercent = (la.changeNumber/ispconnection.previousValue)*100;
//				}
//				
//				ispconnection.previousValue=v;
//				ispconnection.needssave=true;
//			} else {
//				// No Change
//				//la.changeNumber=0;
//				//la.changePercent=0;
//			}
//		}
		
		// Rss Processing
		if (!Utils.isBlank(pa.rssTitleKey)) {
			//
			// RSS Lines
			//
			la.rssTitleKey = pa.rssTitleKey;
			
			la.rssData = new ArrayList();
			la.hidesummary = pa.hidesummary;
			
			
			//accountrss
			
			datakey titles = null;
			datakey descr  = null;
			datakey links  = null;
			datakey dates  = null;
			datakey images = null;
			
			if (!Utils.isBlank(pa.rssTitleKey)) {
				titles=ispconnection.getDataKeyForName(pa.rssTitleKey);
				if (titles!=null && titles.strArray!=null) {
				   // Try to get Other Arrays
					if (!Utils.isBlank(pa.rssDescKey)) {
						descr = ispconnection.getDataKeyForName(pa.rssDescKey);
					}
					if (!Utils.isBlank(pa.rssLinkKey)) {
						links = ispconnection.getDataKeyForName(pa.rssLinkKey);
					}
					if (!Utils.isBlank(pa.rssTimeKey)) {
						dates = ispconnection.getDataKeyForName(pa.rssTimeKey);
					}
					if (!Utils.isBlank(pa.rssImageKey)) {
						images = ispconnection.getDataKeyForName(pa.rssImageKey);
					}
					
					// Process All Titles
					boolean datesok=true;
					
					for (int i=0;i<titles.strArray.size();i++) {
						rssItem rss = new rssItem();
						
						String ts = (String)titles.strArray.get(i);
						
						rss.title = Utils.RemoveHTML(Utils.UnEscapeString(ts));
						
						// Try to fill in others
						try {
							if (descr.strArray==null || (descr.strArray.size()==0)) {
								rss.description = "...";
							} else {
								rss.description = Utils.RemoveHTML(Utils.UnEscapeString((String)descr.strArray.get(i)));
							}
						}
						catch (Exception e) {
						    rss.description = "";
						}
						
						try {
							rss.link = (String)links.strArray.get(i);
						}
						catch (Exception e) {
						    rss.link = "";
						}
						
						try {
							rss.time = "";
							if (dates!=null) {
								String tstring=(String)dates.strArray.get(i);
								if (tstring!=null) {
								   // May not work internationally
									Date d = DateUtils.DateFromString(tstring, dates.format);
									if (d!=null) {
										rss.internalTime=d;
										rss.time=DateUtils.agoDate(d);	
									}
								}
							}
						}
						catch (Exception e) {
							datesok=false;
						    rss.time = "";
						}
						
						// Try Link
						try {
							String url;
							rss.imageURL = "";
							url="";
							if (images!=null && images.strArray!=null && (images.strArray.size()>0) ) {
								if (i<images.strArray.size()) {
									//url = GetDataVal([images.strArray objectAtIndex:i],pa.rssImageKey);
								}
								if (!Utils.isBlank(url)) {
									rss.imageURL=url;	
								}
							}
						}
						catch (Exception e) {
						    rss.imageURL = "";
						}
						
						
						
						la.rssData.add(rss);
					}
					
//					if (datesok && pa.sortrss) {
//						@try {
//							if ([la.rssitems count]>1) {
//								[la.rssitems sortUsingSelector:@selector(ItemDateCompare:)];
//							}
//						}
//						@catch (NSException * e) {
//							// Ignore..
//						}
//					}
				}
				
			}
			
			
			
		} else {
			try {
				// Transaction Support
				datakey dk = ispconnection.getDataKeyForName(pa.srcData);
				
				if (dk!=null) {
					// Get Columns
					int dateCol = Utils.getIntegerFromString(pa.dateColumn)-1;
					
					String[] amountCol = pa.amountColumn.split(",");
					String[] descripAr = pa.descriptionColumn.split(",");
					
					
					if (dk!=null && dk.strArray!=null) {
						la.transactions = new ArrayList<accountlines>();
						
						for (int i=0;i<dk.strArray.size();i++) {
							ArrayList row = (ArrayList)dk.strArray.get(i);
							
							String dateVal = getCol(row,dateCol);
							
							String descVal="";
							if (!pa.descriptionColumn.contains("--COL")) {
								descVal = getCols(row,descripAr,"");
							} else {
								descVal = replaceColumnGroups(row, pa.descriptionColumn, pa.descriptionformat);	
							}
							
							String amountVal="";
							if ( !pa.amountColumn.contains("--COL")) {
								amountVal = getCols(row,amountCol, "");
							} else {
								amountVal = replaceColumnGroups(row, pa.amountColumn, pa.amountformat);
							}
							
							// Create Rows
							accountlines  acl = new accountlines();
							
							// Format Date?
							if (!Utils.isBlank(dateVal) && !Utils.isBlank(pa.dateformat)) {
								try {
									String[] dateformats = pa.amountColumn.split("\\|");
									Date d = DateUtils.DateFromString(dateVal, dateformats[0]);
									dateVal = DateUtils.DateFormat(d, dateformats[1]);
								}
								catch (Exception e) {
									// We tried
								}
							}
							acl.date = Utils.BlankString(dateVal);
							acl.description = Utils.BlankString(descVal);
							acl.amount = Utils.BlankString(amountVal);
							
							la.transactions.add(acl);
						}	
				}

				}
			} catch (Exception e) {
				DebugLogLog(ispconnection,"Exception Account Transactions "+e.toString() + "\nStack Trace " + Utils.getStackTrace(e));
			}
			
		}
		
		
		
		
	}
	
	
	public static void UIProcess(Provider p) {
		if (p.provider_definition.disk_display_type==Provider.kDisplayType_ISPMOBILE) {
			DebugLogLog(p,"UIProcess: ISPMOBILE");

			xmlCycle(p);
			p.clearProgress();
			
			xmlProgressBar(p,p.provider_definition.pbar1());
			xmlProgressBar(p,p.provider_definition.pbar2());
			
			xmlExtras(p);
	 		p.loadsuccess=true;
    		p.loaderrormsg="";
		} else if (p.provider_definition.disk_display_type==Provider.kDisplayType_ACCOUNT) {
			// Process Account
			DebugLogLog(p,"UIProcess: ACCOUNT");
			xmlBalance(p);
			xmlExtras(p);
	 		p.loadsuccess=true;
    		p.loaderrormsg="";
			
		} else {
       		p.loadsuccess=false;
    		p.loaderrormsg="This provider type is not yet supported on this platform";
    		p.failurereason = Provider.fail_internalerror;
		}
	}
	
	public static void DebugLogStart(Provider p) {
		CacheManager cm = CacheManager.getInstance();
		cm.WriteDebugLog(p, "DebugLog Start:"+p.provider_definition.providerName+"\n\n", false);
	}

	public static void DebugLogLog(Provider p,String msg) {
		CacheManager cm = CacheManager.getInstance();
		cm.WriteDebugLog(p, msg+"\n", true);
	}

	public static void DumpDataKeys(Provider p)  {
		
		DebugLogLog(p,"\n\n DataKeys\n\n");

		if (p.datakeys==null) return;
		
		StringBuilder buf = new StringBuilder();
		
		for (int i=0;i<p.datakeys.size();i++) {
			datakey d = p.datakeys.get(i);
			buf.append(d.toString());
		}
		DebugLogLog(p,buf.toString());
	}
	
	
	public static void ProcessProvider(Provider p,asyncUpdateProvider task) {
		
		CacheManager cm = CacheManager.getInstance();
	
		try {
			
			boolean ok = ProviderManager.getInstance().reloadProvider(p);
			
			if (!ok) {
	     		p.loadsuccess=false;
	    		p.loaderrormsg="Problem loading provider:"+p.provider_definition.disk_isp +"reason:" +ProviderManager.getInstance().xmlLoadMsg;
	    		p.failurereason = Provider.fail_authentication;
	    		return;
			}
			
		    p.provider_definition = ProviderManager.getInstance().getProviderDefinition(p.disk_isp);

			DefaultHttpClient http=null;

			if (p.provider_definition.providerName.toLowerCase().contains("telstra") 
					|| p.provider_definition.providerName.toLowerCase().contains("tpg")
					|| p.provider_definition.providerName.toLowerCase().contains("exetel")
					|| p.provider_definition.providerName.toLowerCase().contains("skywards")
					|| p.provider_definition.providerName.toLowerCase().contains("bigpond")
					|| p.provider_definition.providerName.toLowerCase().contains("crazy")
					|| p.provider_definition.providerName.toLowerCase().contains("everyday")
					|| p.provider_definition.providerName.toLowerCase().contains("comcen")
					|| p.provider_definition.providerName.toLowerCase().contains("live connected")
					|| p.provider_definition.loadedFrom==1
					|| (p.provider_definition.disk_isp>1620)
					|| p.disk_isp==31 || p.disk_isp==51 || p.disk_isp==56		// NZ && Vodafone
				) {
				if (p.provider_definition.providerName.toLowerCase().contains("telstra")) {
					http = HttpHelper.IE8HttpOpenClient();
				} else {
					http = HttpHelper.OpenClient();
				}
			} else {
				http = HttpHelper.QuotaClient();
			}

			http.setRedirectHandler(new customRedirects());

			DebugLogStart(p);
			
			// Clear Cookies
			http.getCookieStore().clear();
			
			// Clear Download state
			p.clearURLs(true);
			p.clearDataKeys();
			
			p.currentURL=0;
			int cu=p.currentURL;
			
			boolean firstURL=true;
			
			// Process Them
			while (cu<p.urls.size()) {
				
				
				// Preprocess the URL
				urlinfo u = p.urls.get(cu);
				ProcessKey(p,"URL",u.myid,new datasource(u),true,task);
				while (p.currentURL>cu && p.currentURL<p.urls.size()) {
					// We Skipped in Preprocess, Check this new URL for preprocessing
					DebugLogLog(p,"Skip, reprocess"+p.currentURL);
					cu=p.currentURL;
					u = p.urls.get(cu);
					ProcessKey(p,"URL",u.myid,new datasource(u),true,task);
				}
				
				// Check if We've finished
				if (p.currentURL >= p.urls.size()) {
				   	// Setup flags?
					break;
				} else {
					
					// Excute the URL
					p.loadmsg = u.msg;
					task.updateMsg();
					
					ProcessURL(p,u,http);//
					
					if (u.returndata!=null) {
						
						try {

							//parameter uname=p.getParameterByID(1);
							parameter pwd = p.getParameterByID(2);
							//String unamerep="";
							String passwordrep="";
							
							//if (uname!=null) {
							//	unamerep = Utils.BlankString(uname.CurrentValueAsInternalString());
							//}
							
							if (pwd!=null) {
								passwordrep = Utils.BlankString(pwd.CurrentValueAsInternalString());
							}
							
							// Create new Buffer
							//u.returndata.contains(unamerep) || 
							if (u.returndata.contains(passwordrep)) {
								if (passwordrep.length()>1) {
									String nd=u.returndata;
									nd = u.returndata.replace(passwordrep, "**!PASSWORD!**");
									u.returndata=nd;
								}
							}
							
						} catch (Exception e) {
							// Could not fix password issue
						}
						
						cm.WriteCachedusageData(p, u.returndata,cm.UsageFile(p),firstURL?0:cu);
						
						firstURL=false;
						// Post Process
						boolean abort=ProcessKey(p,"URL",u.myid,new datasource(u),false,task);
						
						if (abort) {
							DebugLogLog(p,"Aborting");
							DumpDataKeys(p);
				    		return;
						} else {
							// Next URL
							p.currentURL++;	
							cu=p.currentURL;
						}
					} else {
						// Abort
			      		p.loadsuccess=false;
			    		p.loaderrormsg="No data returned, check website and internet connection";
			    		p.failurereason = Provider.fail_connection;
			    		return;
			 		}
				}
			}
			DumpDataKeys(p);
			// UI Process
            UIProcess(p);
            
        } catch (Exception e) {
 			DebugLogLog(p,"Exception processing provider "+e.toString() + "\nStack Trace " + Utils.getStackTrace(e));
 			DumpDataKeys(p);
 			
       		p.loadsuccess=false;
    		p.loaderrormsg="Internal error :-\n"+e;
    		p.failurereason = Provider.fail_internalerror;
    		return;
        }
        
		
	}
	
	
}
