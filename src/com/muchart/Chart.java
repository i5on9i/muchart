package com.muchart;




import com.muchart.ChartItem;



public interface Chart{

	
	
	/**
	 * Make a {@link #ChartItem()} through the parsing of RSS description
	 * 
	 * @param desc
	 * @return
	 */
	public ChartItem createChartItemFromDesc(String desc);

	/**
	 * return the url of rss feed.
	 * @return
	 */
	public String getFeedUrl();

	
}

