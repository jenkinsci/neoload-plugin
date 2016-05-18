package org.jenkinsci.plugins.neoload.integration.supporting;

import java.util.Comparator;

public class ComparatorListByLength implements Comparator<String> {
	
	private final boolean order;
	
	/**
	 * 
	 * @param order <b>true</b> : lesser to higher; <b>false</b> : higher to lesser
	 */
	public ComparatorListByLength(final boolean order) {
		this.order = order;
	}

	@Override
	public int compare(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return 0;
		}
		else {
			int value = 0;
			if (str1 == null) {
				value = -1;
			}
			else {
				if (str2 == null) {
					value = 1;
				}
				else {
					value = str1.length() - str2.length();
				}
			}
			if (order) {
				return value;
			}
			else {
				return -value;
			}
		}
	}

}
