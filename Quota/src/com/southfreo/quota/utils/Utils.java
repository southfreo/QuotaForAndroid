package com.southfreo.quota.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.CharacterIterator;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.southfreo.R;
import com.southfreo.quota.smplmathparser.EvaluationTree;
import com.southfreo.quota.smplmathparser.MathParser;
import com.southfreo.quota.smplmathparser.MathParserException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.Uri;

import android.os.Build;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class Utils {
	
	
	public static final int	E_FORMAT_STRING=0;
	public static final int	E_FORMAT_NUMBER=1;				//
	public static final int	E_FORMAT_DATE=2;
	public static final int	E_FORMAT_DATA=3;
	public static final int	E_FORMAT_TIME=4;
	public static final int	E_FORMAT_DAYMONTH=5;
	public static final int	E_FORMAT_DATA_SIMPLE=6;
	public static final int	E_FORMAT_DATA_MB=7;
	public static final int	E_FORMAT_CURRENCY=8;
	public static final String INTERNAL_DATE_FORMAT ="dd/MM/yy";
	

	 private static final String TAG = "Utils";

	 public static boolean isBlank(String s) {
			return (s==null || s.length() ==0);	
		}
	 
	 public static String BlankString(String s) {
			if (isBlank(s)) {
			   return "";	
			} else {
				return s;	
			}
		}
	 
	 public static boolean FileExists(String name) {
		 File f = new File(name);
		 return f.exists();
	 }
	 
	 public static double getMBVal (String wval) {
			
			if (wval==null) return 0;

			double val=0; // Default 20GB
			
			if (wval!=null) {
				val = getDoubleFromString(wval);
				
				if (val>0) {
					// Convert to GB
					if (wval.contains("GB") || wval.contains("Gbyte")) {
						val *=1000;	
					}
					if (wval.contains("KB") || wval.contains("Kbyte") ) {
						val /=1000;	
					}
				}
			}
			return val;
		}
	 
	    public static void copyFile(File fromFile, File toFile) throws IOException {
	        BufferedReader reader = new BufferedReader(new FileReader(fromFile));
	        BufferedWriter writer = new BufferedWriter(new FileWriter(toFile));

	        //... Loop as long as there are input lines.
	        String line = null;
	        while ((line=reader.readLine()) != null) {
	            writer.write(line);
	            writer.newLine();   // Write system dependent end of line.
	        }

	        //... Close reader and writer.
	        reader.close();  // Close to unlock.
	        writer.close();  // Close to unlock and flush to disk.
	    }
	    
	  public static String DeviceInfo(Context c) {
		  
		  Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		  
		  DisplayMetrics met = new DisplayMetrics();
		  
		  display.getMetrics(met);
		  
		  String di = "Device   : "  + Build.DEVICE + "\n" 
			  	     +"Model    : " + Build.MODEL + "\n" 
		  			 +"Firmware : " + Build.VERSION.RELEASE + "\n" 
		  			 +"Display  : " + met.toString();
		  
		  return di;
	  }
	  public static int RandomInteger(int aStart, int aEnd, Random aRandom){
		    //get the range, casting to long to avoid overflow problems
		    long range = (long)aEnd - (long)aStart + 1;
		    // compute a fraction of the range, 0 <= frac < range
		    long fraction = (long)(range * aRandom.nextDouble());
		    
		    return (int)(fraction + aStart);    
		  }
	 
	 public static String ExtractString(String src,String search,int offset,String starttag,String endTag) {
		 int pos=src.indexOf(search, offset);
		 int pos2;
		 int pos3;
		 
		 if (pos!=-1) {
			 pos2 = src.indexOf(starttag,pos);
			 if (pos2!=-1) {
				 int pos3start = pos2+starttag.length()+1;
				 if (pos3start<src.length()) {
					 pos3 = src.indexOf(endTag,pos3start);
					 if (pos3!=-1) {
						 return src.substring(pos2+starttag.length(), pos3);
					 }
				 }
			 }
		 }
		 return null;
	 }
	 
	  
	 public static String RegEx(String src,String regEx,int pos) {
      		String rv=null;
      		
      		if (src==null) return rv;
      		
	   		Pattern upat = Pattern.compile(regEx);
    		Matcher mu = upat.matcher(src);
    		if (mu.find()) {
    			try {
    				return mu.group(pos);
    			} catch (Exception e) {
    				Log.e(TAG,"RegEx exception regEx:"+Utils.BlankString(regEx)+ " e:"+e.toString());
    			}
    		}
    		return rv;
	 }
	 
	
	  public static void removeDuplicate(ArrayList arlList)
	  {
	   HashSet h = new HashSet(arlList);
	   arlList.clear();
	   arlList.addAll(h);
	  }
	  
	@SuppressWarnings("unchecked")
	public static void removeDuplicateWithOrder(ArrayList arlList)
	 {
	 Set set = new HashSet();
	 List newList = new ArrayList();
	 for (Iterator iter = arlList.iterator();    iter.hasNext(); ) {
	 Object element = iter.next();
	   if (set.add(element))
	      newList.add(element);
	    }
	    arlList.clear();
	    arlList.addAll(newList);
	}
	 
	public static String[] toStringArray(List<String> list) {
		 if (list==null || list.size()==0) return null;
		 String[] strArray = new String[list.size()];
		 list.toArray(strArray);
		 return strArray;
	 }

	public static Integer[] toIntegerArray(List<Integer> list) {
		 if (list==null || list.size()==0) return null;
		 Integer[] strArray = new Integer[list.size()];
		 list.toArray(strArray);
		 return strArray;
	 }

	 public static List<String> RegExArray(String src,String regEx,int pos) {
   		String rv=null;
		List<String> matches = new ArrayList<String>();
   		
   		if (src==null) return matches;
   		
	   		Pattern upat = Pattern.compile(regEx);
 		    Matcher mu = upat.matcher(src);

 		    while (mu.find()) {
 				matches.add(mu.group(pos));
 		    }
 		    
 		return matches;
	 }
	 
	 public static String RegExBlank(String src,String regEx,int pos) {
		 String r = RegEx(src,regEx,pos);
		 return r==null?"":r;
	 }
	 
	  private static void addCharEntity(Integer aIdx, StringBuilder aBuilder){
		    String padding = "";
		    if( aIdx <= 9 ){
		       padding = "00";
		    }
		    else if( aIdx <= 99 ){
		      padding = "0";
		    }
		    else {
		      //no prefix
		    }
		    String number = padding + aIdx.toString();
		    aBuilder.append("&#" + number + ";");
	  }

	  public static double getHrsFromTime (String s)	
	  {
	  	double uhrs;
	  	
	  	if (s==null) return 0;
	  		
	  	uhrs=0;
	  	
	  	String ua[] = s.split(":");
	  	
	  	if (ua.length==3) {
	  		String hrs = ua[0];
	  		if (hrs!=null) {
	  			uhrs = Utils.getIntegerFromString(hrs);	
	  		}
	  		
	  		String min = ua[1];
	  		if (min!=null) {
	  			uhrs += Utils.getDoubleFromString(min)/60;	
	  		}
	  		
	  		String sec  = ua[2];
	  		if (sec!=null) {
	  			uhrs += Utils.getDoubleFromString(sec)/60/60;	
	  		}
	  	}
	  	return uhrs;
	  }
	  
	 public static String EscapeStringHTML(String aText){
		 return  java.net.URLEncoder.encode(aText);
	  }
	 
	 
	 public static String RemoveStringsFromString (String src, String replaceArray,String sep) {
			String newstr = src;
			
			if (src!=null) {
				if (replaceArray!=null) {
					String a[] = replaceArray.split(sep);
					for (int i=0;i<a.length;i++) {
						newstr = newstr.replace(a[i],"");
					}
				}
				return newstr;
			}
			return newstr;
		}

	 
	 public static String ReplaceStringsFromString (String src, String replaceArray,String sep) {
			String newstr = src;
			
			try {
				if (src!=null) {
					
					if (replaceArray!=null) {
						String a[] = replaceArray.split(sep);
						if (a!=null && a.length>=2) {
							int v=a.length;
							int i=0;
							int j=0;
							for (i=0;j<v/2;i=i+2) {
								newstr = newstr.replace(a[i], a[i+1]);
								j++;
							}
						}
					}
					
					return newstr;
				}
			}
			catch (Exception e) {
				return newstr;
			}
			
			return newstr;
		}
	 
	 public static double evaluteExpression(String exp) {
		 if (!Utils.isBlank(exp)) {
				MathParser parser = new MathParser();
                  try {
                      EvaluationTree tree = parser.parse(exp);
                      System.out.println(tree.evaluate());
                      double result=tree.evaluate();
                      return result;
                 } catch (MathParserException e) {
                      Log.e(TAG,"Could not evaluate :"+exp);
                  }
		 }
         return 0;
	 }
		 
	  public static String removeWhitespaceNewLine(String argStr)
	  {
		  if (Utils.isBlank(argStr)) return "";
		  
	      char last = argStr.charAt(0);
	      StringBuffer argBuf = new StringBuffer();

	      for (int cIdx = 0 ; cIdx < argStr.length(); cIdx++)
	      {
	          char ch = argStr.charAt(cIdx);
	          if ( (ch != '\n' || last != '\n') || (ch != '\t' || last != '\t'))
	          {
	              argBuf.append(ch);
	              last = ch;
	          }
	      }

	      return argBuf.toString().trim();
	  }
	  
	 public static String RemoveCrap(String s) {
		 
		  if (Utils.isBlank(s)) return "";

		  String ns = removeWhitespaceNewLine(s);
		 
			ns = ns.replace("&nbsp;" ," ");
			ns = ns.replace("\t" ,"");
			ns = ns.replace("\n" ,"");
			ns = ns.replace("&amp;" ,"&");
			ns = ns.replace("<b>" ,"");
			ns = ns.replace("</b>" ,"");
			ns = ns.replace("<u>" ,"");
			ns = ns.replace("</u>" ,"");
			ns = ns.replace("&lt;" ,"");
			ns = ns.replace("&gt;" ,"");
			ns = ns.replace("<br/>" ,",");
			ns = ns.replace("&quot;" ,"'");

			return ns;
	 }
	 
	 public static String RemoveHTML(String s) {
		 if (!isBlank(s)) {
			 String ns = s.replaceAll("<script>.*?</script>", " ");
			 ns = ns.replaceAll("<(.|\n)*?>", " ");
			 return ns;
		 }
		 return null;
	 }
	 
	 
	 public static Uri ParseUrl(String s) {
		 try {
			 return Uri.parse(s);
		 } catch (Exception e) {
			 return Uri.parse("http://www.southfreo.com");
		 }
	 }
	 
	 public static String currencyvalue (float f,int digits) {
		 
		 // Change this to allow International currencies
		 NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
		 nf.setMaximumFractionDigits(digits);
		 
		 return nf.format(f);
		}

	 public static String currencyvalue (double f,int digits) {
		 return currencyvalue((float)f,digits);
	 }
	 
	 public static String UnEscapeString (String src) {
			
			String ns = src;
			
			if (ns!=null) {

				ns = ns.replace("&amp;" ,"&");
				ns = ns.replace("&#38;" ,"&");
				
				
				ns = ns.replace("&apos;" ,"'");
				ns = ns.replace("&quot;" ,"\"");
				ns = ns.replace("&#27;" ,"'");
				ns = ns.replace("&#39;" ,"'");
				ns = ns.replace("&#92;" ,"'");
				ns = ns.replace("&#96;" ,"'");
				
				ns = ns.replace("&#169;" ,"©");
				ns = ns.replace("&copy;" ,"©");

				ns = ns.replace("&#153;" ,"ª");
				
				ns = ns.replace("&#162;" ,"¢");
				ns = ns.replace("&cent;" ,"¢");
				
				ns = ns.replace("&#163;" ,"£");
				ns = ns.replace("&pound;","£");

				ns = ns.replace("&#9733;","");
				
				ns = ns.replace("&#174;" ,"¨");
				ns = ns.replace("&reg;" ,"¨");

				ns = ns.replace("&#165;" ,"´");
				ns = ns.replace("&yen;" ,"´");

				ns = ns.replace("&#060;" ,"<");
				ns = ns.replace("&#062;" ,">");
				ns = ns.replace("&#064;" ,"");
				
				ns = ns.replace("&gt;" ,">");
				ns = ns.replace("&gt;" ,">");
				ns = ns.replace("&gt;" ,">");
				ns = ns.replace("&gt;" ,">");
				ns = ns.replace("&lt;" ,"<");

				ns = ns.replace("&euro;","Û");
				
				ns = ns.replace("&mdash;","-");
				
				ns = ns.replace("&#160;"," ");
				ns = ns.replace("&nbsp;"," ");
			
				ns = ns.replace("&#8211;","-");
				ns = ns.replace("&#8212;","-");
				ns = ns.replace("&#8216;","'");
				ns = ns.replace("&#8217;","'");
				ns = ns.replace("&#8220;","\"");
				ns = ns.replace("&#8221;","\"");
				ns = ns.replace("&#8224;","+");
				ns = ns.replace("&#8225;","+");
				ns = ns.replace("&#8226;","o");
				
				ns = ns.replace("&#8232;"," ");
				ns = ns.replace("&#8233;"," ");
				ns = ns.replace("&#8232;"," ");
				ns = ns.replace("&#8230;","...");
				ns = ns.replace("&#8240;","%");
				ns = ns.replace("&#8243;","\"");
				ns = ns.replace("&#8364;","Û");
				ns = ns.replace("&#8482;","tm");
				ns = ns.replace("&#980;","");
				return ns;
			}
			return "";
		}

	 
	 
	 public static String getValFormat (double val,String format,boolean dec,boolean pre) {
			double nv=val;

			if (pre) {
				if (!dec) {
					return String.format("%s%g",format,nv);
				} else {
					return String.format("%s%.2f",format,nv);
				}
			} else {
				if (!dec) {
					return String.format("%.0f%s",nv,format);
				} else {
					return String.format("%.2f%s",nv,format);
				}
			}
			
		}
	 
	 
	 public static String DecimalTimetoHrsMins(double Hrs) {

			//
			// Convert Decimal Time Hrs.mins to 
			//
			double rem;
			rem=Hrs;
			int hrs = (int)rem;
			rem -= hrs;
			rem *=60;
			int mins = (int)(rem);
			rem -= mins;
			rem *=60;
			int secs = (int)(rem);
			return String.format("%02d:%02d:%02d",hrs,mins,secs);
			
		}
	 
	 
	 public static String DataFormat(double vn,double div,boolean dec) {
			
			double avn;
			
			avn=java.lang.Math.abs(vn);
			
			String rv="";

			if (avn>=(div*div*div*div)) { 
				rv=getValFormat(vn/div/div/div/div," TB",true,false);
			} else if (avn>=(div*div*div)) { 
				// Change Override GB to always show dev
				rv=getValFormat(vn/div/div/div," GB",true,false);
			} else if (avn>=(div*div)) {
				rv=getValFormat(vn/div/div," MB",dec,false);
			} else if (avn>=div) {
				rv=getValFormat(vn/div," KB",dec,false);
			} else if (avn<div) {
				rv=getValFormat(vn," B",dec,false);
			}
			
			return rv;	
		}
	 
	 public static String getErrd (int No) {
			
			String suff="th";
			
			if (No==1 || No==21 || No==31) {
				suff="st";	
			}
			if (No==2 || No==22) {
				suff="nd";	
			}
			if (No==3 || No==23) {
				suff="rd";	
			}
			return suff;
		}

	 public static Double getDoubleFromString(String s) {
		 try {
			 NumberFormat nf = NumberFormat.getNumberInstance();
			 
			 if (s==null) return 0.00;
			 String val=s.replace("$", "").trim();
			 if (val.contains("-")) {
				 return nf.parse(val.replace("-", "")).doubleValue()*-1;
			 } else {
				 return nf.parse(val).doubleValue();
			 }
			 
		 } catch (Exception e) {
			 return 0.0;
		 }
	 }
	 
	 public static boolean isNumber(String s) {
		 for (int i = 0; i < s.length(); i++) {
	            //If we find a non-digit character we return false.
	            if (!(Character.isDigit(s.charAt(i)) || (s.charAt(i) =='.')))
	                return false;
	        }
	        return true; 
	 }
	 
	 public static int getIntegerFromString(String s) {
		 try {
			 if (s==null) return 0;
			 return getDoubleFromString(s).intValue();
		 } catch (Exception e) {
			 return 0;
		 }
	 }
	 
	  public static boolean deleteDirectory(File path) {
		    if( path.exists() ) {
		      File[] files = path.listFiles();
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {
		           deleteDirectory(files[i]);
		         }
		         else {
		           files[i].delete();
		         }
		      }
		    }
		    return( path.delete() );
		  }


	 public static boolean getBoolFromString(String s) {
		 try {
			 if (s==null) return false;
			 return Boolean.parseBoolean(s);
		 } catch (Exception e) {
			 return false;
		 }
	 }
	 
	 public static String FormatValueC (int  type, double val) {
			return  formatValue(null, val, null, type,"%s",true);
		}
	 
	 public static InputStream StringToInputStream(String s) {
		 try {
			 return new ByteArrayInputStream(s.getBytes("UTF-8"));
		 } catch (Exception e) {
			 return null;
		 }
	 }
	 
	 public static String readFileAsString(File filePath) {
		 try {
			    StringBuffer fileData = new StringBuffer(1000);
		        BufferedReader reader = new BufferedReader(
		                new FileReader(filePath));
		        char[] buf = new char[1024];
		        int numRead=0;
		        while((numRead=reader.read(buf)) != -1){
		            fileData.append(buf, 0, numRead);
		        }
		        reader.close();
		        return fileData.toString();
		 } catch (Exception e) {
			 return null;
		 }
	 }
	 
	 public static double DecMBtoBytes (double mb) {
		    double bytes;
			
			bytes=0;
			if (mb < 1000) {
				bytes = mb * 1048576; 	
			} else {
			    bytes = mb * 1073741.82;
			}
			return bytes;
		}
	 
	 
	@SuppressWarnings("unchecked")
	public static ArrayList<ArrayList> TableFromString(String src,String tableRegEx,String rowRegEx, String colRegEx) {
			
			
			ArrayList<ArrayList> rowArray = new ArrayList<ArrayList>();
			
			// Get The Table - Assume in () - Parameter?
			try {
				
				String tabledata=Utils.RegEx(src, tableRegEx, 1);
				
				// Enumerate All Rows
				List<String> rows = Utils.RegExArray(tabledata, rowRegEx, 1);

				for (int i=0;i< rows.size();i++) {
					String row = rows.get(i);
					if (!Utils.isBlank(row)) {

						List<String> cols = Utils.RegExArray(row, colRegEx, 1);
						
						ArrayList<String> foundCol = new ArrayList<String>();
						
						for (int j=0;j< cols.size();j++) {
							String col = cols.get(j);
							col = col.replaceAll("<(.|\n)*?>", " ");
							col = RemoveCrap(col);
							foundCol.add(col);
						}

						if (foundCol.size()>0) {
							rowArray.add(foundCol);
						}
						
					}
				}

				return rowArray;
			}
			catch (Exception e) {
				Log.e(TAG,"Problem Creating Table from HTML "+e.toString());
				// Died trying to Create Table
				return null;
			}
		}
	 
	  public static String getStackTrace(Throwable aThrowable) {
		    final Writer result = new StringWriter();
		    final PrintWriter printWriter = new PrintWriter(result);
		    aThrowable.printStackTrace(printWriter);
		    return result.toString();
		  }
	  
	 public static String formatValue (String v, double vn,Date vd,int type,String format,boolean dec) {
			
			try {
				switch (type) {
					case E_FORMAT_STRING:{
						if (isBlank(format)) {
							return v==null?"":v;
						} else {
							return String.format(format.replace("%@", "%s"),v==null?"":v);
						}

					}
					case E_FORMAT_NUMBER:{
						if (isBlank(format)) {
						   return String.format("%.0f",vn);
						} else {
							if (format.equalsIgnoreCase("CURRENCY")) {
							   return currencyvalue((float)vn, 2);	
							} else if (format.equalsIgnoreCase("CURRENCYZERO")) {
								return currencyvalue((float)vn, 0);	
							} else {
								return String.format(format,vn);
								}
						}
					}
					case E_FORMAT_DATE:{
						if (Utils.isBlank(format)) {
							return DateUtils.DateFormat(vd, Utils.INTERNAL_DATE_FORMAT);
						} else {
							return DateUtils.DateFormat(vd,format);
//							return DateUtils.DateShort(vd);
						}
					}
					case E_FORMAT_DATA_MB: {
						// Always show 2 Decimal Places
						return DataFormat(vn*1000*1000, 1000, dec);
						
					}
						
					case E_FORMAT_DATA: {
					    return DataFormat(vn,1024,dec);
						
					}
					case E_FORMAT_DATA_SIMPLE:{
						return DataFormat(vn,1000,dec);
					}
						
					case E_FORMAT_TIME: {
						return DecimalTimetoHrsMins(vn);
					}
						
					case E_FORMAT_CURRENCY: {
						if (dec) {
							return currencyvalue((float)vn, 2);
						} else {
							return currencyvalue((float)vn, 0);
						}
					}
						
						
					case E_FORMAT_DAYMONTH: {
					    return String.format("%d%s",(int)vn,getErrd((int)vn));	
					}
					default:
						break;
				}
			}
			catch (Exception e) {
				Log.e(TAG,"formatValue Exception!!!: %s"+e);
				return "?";
			}
			
			return "Unknown format";
		}
	 
    
}
