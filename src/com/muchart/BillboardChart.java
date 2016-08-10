package com.muchart;



import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.muchart.ChartItem;



public class BillboardChart implements Chart{

	private String mFeedUrl;
	private Pattern mPattern = Pattern.compile(
									"(.+?)\\s+?by\\s+?(.+?)\\s+?ranks\\s+?(.*?\\d+)",
									Pattern.DOTALL);
	
	// the below order is associated with the mPattern group
	static final int TITLE = 0;
	static final int ARTIST = 1;
	static final int RANK = 2;

	
	public BillboardChart(String feedUrl)
	{
		mFeedUrl = feedUrl;
	}
	
	@Override
	public ChartItem createChartItemFromDesc(String desc)
	{
		/**
		 * example of the description
		 *
		 *	"Locked Out Of Heaven by Bruno Mars ranks #1" +
		 *	" <p>album: hey</p> " +
		 *	"<img src='http://www.billboardk.com/imgserver/artist/49796/49796.jpg'/>";
		 */
		int count = 0;
		ArrayList<String> matchedList = new ArrayList<String>();
		
		Matcher m = mPattern.matcher(desc);
		while (m.find()) { // Find each match in turn; String can't do this.
			
			count = m.groupCount();
			for(int j = 1 ; j<= count ; j++){
				matchedList.add(m.group(j));
			}
		}


		ChartItem ci = new ChartItem(matchedList.get(RANK),
							matchedList.get(TITLE),
							matchedList.get(ARTIST)
						);
		
		return ci;

	}

	
	@Override
	public String getFeedUrl() {
		return mFeedUrl;
	}
	
	
}

