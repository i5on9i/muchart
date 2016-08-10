package com.muchart;



import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.muchart.ChartItem;


/**
 * AChart parses the feed from the muchart_server.
 * 
 * @author namh
 *
 */
public class AChart implements Chart{
	private static final String TAG = "AChart";

	private String mFeedUrl;
	private Pattern mPattern = Pattern.compile(
			"(.+?)\\s+?by\\s+?(.+?)\\s+?ranks\\s+?(.+?)\\s+?" +
			"<p>\\s*?album\\s*?:(.*?)</p>" +
			"\\s*?.*?img\\s+?src\\s*?=\\s*?'(.*?)'",
			Pattern.DOTALL);


	// the below order is associated with the mPattern group
	static final int TITLE = 0;
	static final int ARTIST = 1;
	static final int RANK = 2;
	static final int ALBUM = 3;
	static final int THUMB = 4;

	
	
	public AChart(String feedUrl)
	{
		mFeedUrl = feedUrl;
	}

	
	@Override
	public ChartItem createChartItemFromDesc(String desc)
	{
		/**
    	 * for test
    	 */
//    	desc = "Locked Out Of Heaven by Bruno Mars ranks #1";
//    			" <p>album: hey</p> " +
//    			"<img src=''/>";

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

		Log.d(TAG, "rg groupCount = " + count);


		ChartItem ci = new ChartItem(matchedList.get(RANK),
							matchedList.get(TITLE),
							matchedList.get(ARTIST),
							matchedList.get(ALBUM),
							matchedList.get(THUMB)
						);
		
		return ci;
	}
	
	@Override
	public String getFeedUrl() {
		return mFeedUrl;
	}

	
	
}

