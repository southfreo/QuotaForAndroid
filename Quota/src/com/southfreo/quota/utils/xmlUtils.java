package com.southfreo.quota.utils;

import com.southfreo.quota.xml.TagNode;
import com.southfreo.quota.xml.TreeNode;

public class xmlUtils {

	public static String nodeValue(TreeNode node,String name) {
		if (node==null) return "";
        for (TreeNode anode = node.getChild(); anode != null; anode = anode.getSibling()) {
			  if (anode.toString().equalsIgnoreCase(name)) {
				  return anode.getChild().toString();
			  }
        }
        return "";
	}
	
	public static TreeNode subNode(TreeNode node,String name) {
        for (TreeNode anode = node.getChild(); anode != null; anode = anode.getSibling()) {
			  if (anode.toString().equalsIgnoreCase(name)) {
				  return anode;
			  }
        }
        return null;
	}
	
	public static String subNodeValue(TreeNode node,String name) {
		TreeNode t = subNode(node,name);
		if (t!=null) {
			if (t.getChild()!=null) {
				return t.getChild().toString();
			} else {
				return "";
			}
		} else {
			return "";
		}
	}
}
