package com.southfreo.quota.model;

public class datasource {
	
	public final static int datasource_string=0;	
	public final static int datasource_xml=1;
	public final static int datasource_url=2;
	public final static int datasource_key=3;
	public final static int datasource_internal=4;
	
	
	public int myid;
	public int type;
	public Object object;
	public String text;

	public datasource(urlinfo url) {
		type = datasource.datasource_url;
		object = url;
	}
	
	public datasource(datakey key) {
		type = datasource.datasource_key;
		object = key;
	}
	
}
