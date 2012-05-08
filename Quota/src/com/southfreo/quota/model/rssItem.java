package com.southfreo.quota.model;

import java.io.Serializable;
import java.util.Date;

public class rssItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String title;
	public String description;
	public String link;
	public String time;
	public String imageURL;
	public Date internalTime;
}

