package com.southfreo.quota.model;

import java.io.Serializable;


public class extradata implements Serializable {

	private static final long serialVersionUID = 1;

	public final static int extratype_string=0;
	
	public final static int extratype_html=1;		//html
	public final static int extratype_calls=2;		//calls
	public final static int extratype_graph=3;		//graph
	public final static int extratype_textarray=4;	//textarray
	public final static int extratype_gps=5;		//textarray
		
	
	public int section;
	public int type;								//0 - Normal, 1 - Web, 2 - Pop, 3 Graph
	public String name;
	public String value;

	public String nameFormat;
	public String valueFormat;
	public String src;

	public int order;
	public boolean showwhenempty;

	public void setExtraType(String ut) {
		type=extratype_string;
		if (ut.toUpperCase().equalsIgnoreCase("TEXT")) {
			type=extratype_string;
		} else if (ut.toUpperCase().equalsIgnoreCase("HTML")) {
			type=extratype_html;
		} else if (ut.toUpperCase().equalsIgnoreCase("FILE")) {
			type=extratype_calls;
		} else if (ut.toUpperCase().equalsIgnoreCase("GRAPH")) {
			type=extratype_graph;
		} else if (ut.toUpperCase().equalsIgnoreCase("TEXTARRAY")) {
			type=extratype_textarray;
		} else if (ut.toUpperCase().equalsIgnoreCase("GPS")) {
			type=extratype_gps;
		}
		
	}
	
}
