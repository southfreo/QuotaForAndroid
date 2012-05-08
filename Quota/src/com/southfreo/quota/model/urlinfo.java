package com.southfreo.quota.model;

public class urlinfo {
	//NSURL	 *url;			
	public String urlstring;
	public String urlalternate;
	public String postalternate;
	
	public int coding;
	
	public String postdata;	
	
	// Used if type XML
	//CXMLDocument *xmlDocument;			// Contains XML Format;
	public String returndata;				    // Contains String format

	public String msg;
	public String type;
	
	public int		 attempt;
	public boolean	 isusage;
	
	public boolean passwordcheck;
	
	public String username;
	public String password;
	
	public String headers;
	
	
	// Extras Added when converting to XML
	public int  myid;
	public String  xmlConditionid;
	public int timeout;
	
	// Extra Array Logic
	public boolean isArray;
	public String arrayKey;
	
	//NSArray urlArray;
	
}
