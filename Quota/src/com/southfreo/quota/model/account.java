package com.southfreo.quota.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.southfreo.quota.utils.Utils;

public class account  implements Serializable  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	


	
	public String bal1name;
	public String bal1value;
	public String showChange;
	
	public String bal2name;
	public String bal2value;
	
	public double changePercent;
	public double changeNumber;
	
	public boolean hidesummary;
	
	public ArrayList<rssItem> rssData;
	public ArrayList<accountlines> transactions;

	// RSS Additions
	public String rssTitleKey;
	public String rssDescKey;
	public String rssTimeKey;
	public String rssLinkKey;
	public String rssImageKey;

	// Transaction Additions
	public String srcData;
	public String dateColumn;
	public String descriptionColumn;
	public String amountColumn;
	
	public String headings;
	public String descriptionformat;
	public String amountformat;
	public String dateformat;
	
	public boolean isRssFeed() {
		return rssData!=null; 
	}
	
	public boolean hasAccountData() {
		return transactions!=null; 
	}

	public void clearData() {
		if (rssData!=null) {
			rssData.clear();
		}
		if (transactions!=null) {
			transactions.clear();
		}
	}
	
	
}


