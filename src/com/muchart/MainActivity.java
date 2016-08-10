package com.muchart;




import java.util.ArrayList;

import java.util.List;






import com.namh.widget.ChartAdapter;
import com.namh.widget.MusicChartAdapter;


import com.namh.util.AndroidSaxFeedParser;
import com.namh.util.RssItem;
import com.namh.widget.SlidingView;
import com.namh.widget.flowindicator.TitleFlowIndicator;
import com.namh.widget.flowindicator.ViewFlow;

import android.app.Activity;


import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;

import android.view.View;





import android.widget.AdapterView.OnItemClickListener;


import android.widget.ListView;
import android.widget.ProgressBar;




//--------------------------------------------------------
public class MainActivity extends Activity {

	
	private ListView listView; 
	static final int fakeItemCount = 10;
	private static final int MUSICCHARTCOUNT = 4;
	private static final int FADING_EDGE_LEGTH = 10;
	private OnItemClickListener mItemClickListener;

	
	
	
	//--------------------------------------------------------
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		// Inflate Views
        setContentView(R.layout.title_layout);
   
		ViewFlow viewFlow = (ViewFlow) findViewById(R.id.viewflow);
		MusicChartAdapter adapter = new MusicChartAdapter(this);

		final int STARTPOS = 0;
		viewFlow.setAdapter(adapter, STARTPOS);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.viewflowindic);
		indicator.setTitleProvider(adapter);
		viewFlow.setFlowIndicator(indicator);
		
	

		ArrayList<Chart> cl = new ArrayList<Chart>();
		cl.add(new BillboardChart("http://www.billboard.com/rss/charts/hot-100"));
		cl.add(new AChart("http://populmap.appspot.com/muchart/rss/billboard-weekly-more"));
		cl.add(new AChart("http://populmap.appspot.com/muchart/rss/melon-weekly-more"));
		cl.add(new AChart("http://populmap.appspot.com/muchart/rss/melon-indie-weekly-more"));


		int i = 0;
		ArrayList<ChartAdapter> adapterlist = adapter.getChartAdapter();
		for(ChartAdapter c : adapterlist){
			
			LoadChartData load = new LoadChartData(c, cl.get(i));
			load.execute("");
			i++;
			
		}
		


//        
//        
//		
//		/**
//		 * Start to load the data
//		 */
//		int length = v.size();
//		for(int i = 0 ; i<length ; i++){
//			View vv = v.get(i);
//			Pair<String, String> pp = chartlist.get(i);
//			
//			//ProgressBar pb = (ProgressBar)vv.findViewById(R.id.progressbar);
//			
//			
//			listView = (ListView) vv.findViewById(R.id.listview);
//			listView.setTag(pp.first);
//			listView.setOnItemClickListener(mItemClickListener);
//			LoadChartData load = new LoadChartData( 
//					listView,
//					pb,
//					pp.first,	// name of chart
//					pp.second	// url for chart
//			);
//			
//			
//			load.execute("");
//		}
//		
		
        // Explanation
//		boolean firstboot = getSharedPreferences("BOOT_PREF", MODE_PRIVATE)
//							.getBoolean("firstboot", true);
//		if(firstboot){
//			// show the explanation view
//			View v1 = v.get(0);
//			ViewStub stub = (ViewStub)v1.findViewById(R.id.stub_howto);
//			stub.setVisibility(View.VISIBLE);
//			RelativeLayout rlayout = (RelativeLayout)v1.findViewById(R.id.howto_import);
//			ImageView iview = (ImageView)rlayout.findViewById(R.id.imageView1);
//
//
//			iview.setOnClickListener(new OnClickListener(){
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					Log.d("test", "clicked");
//					
//				}
//				
//			});
//			
//		
//			getSharedPreferences("BOOT_PREF", MODE_PRIVATE)
//				.edit()
//				.putBoolean("firstboot", false)
//				.commit();
//		}
    }
	

	//--------------------------------------------------------
	private SlidingView findViewById(String string) {
		// TODO Auto-generated method stub
		return null;
	}


	

	
	
	//--------------------------------------------------------
	private class LoadChartData extends AsyncTask<String, Void, List<RssItem>> {
		
		private Context mContext;
		private ListView mList;
		private ChartAdapter mAdapter;
		private String mTitle;
		private String mFeedUrl;
		private Chart mChart;
		
		private ProgressBar mProgress;
		private int mProgressStatus = 0;
		private Handler mHandler = new Handler();
		
		//--------------------------------------------------------
		public LoadChartData(ListView l, ProgressBar pb,
							String title, String feedurl){
			this.mList = l;
			this.mProgress = pb;
			this.mTitle = title;
			this.mFeedUrl = feedurl;
			return;
		}
		
		//--------------------------------------------------------
		public LoadChartData(Context context,
							String feedurl){
			this.mContext = context;
			this.mFeedUrl = feedurl;
			return;
		}
		
		//--------------------------------------------------------
		public LoadChartData(ChartAdapter adapter, String feedurl){
			this.mAdapter = adapter;
			this.mFeedUrl = feedurl;
			return;
		}
		
		//--------------------------------------------------------
		public LoadChartData(ChartAdapter adapter, Chart chart){
			this.mAdapter = adapter;
			this.mChart = chart;
			this.mFeedUrl = chart.getFeedUrl();
			return;
		}
				
		
		//--------------------------------------------------------
        @Override
        protected List<RssItem> doInBackground(String... params) {

        	return loadFeed2();
        }      
        
        
        //--------------------------------------------------------
        /**
         * @param its type must be same as the return type of 
         * 			doInBackground()
         */
        @Override
        protected void onPostExecute(List<RssItem> messages) {
        	if(messages == null){
        		Log.e("onPostExecute", "messages is null");

        	}else{
	        	mAdapter.clear();
	        	
	        	// remove the progress bar
				/**
				 *	At the moment the "progressbar" is not shown,
				 *	for example, the user scrolls down the empty listview,
				 *	"pb" is null.
				 */
	        	if(mProgress != null)
	        		mProgress.setVisibility(View.GONE);


				/**
					Add items in order to make adapter
				*/
				
				int length = messages.size();
	            for (int i=0 ; i < length; i++) {
	            	String desc = messages.get(i).getDescription();

					ChartItem ci = mChart.createChartItemFromDesc(
										messages.get(i).getDescription());
	        		
	                mAdapter.addItemWithoutNoti(ci);
	            }
				
	            mAdapter.notifyDataSetChanged();
	            
        	}
        }

        //--------------------------------------------------------
        @Override
        protected void onPreExecute() {
			/**
			 * to show UI first, before the data is loaded
			 */

            
        }

        //--------------------------------------------------------
        @Override
        protected void onProgressUpdate(Void... values) {
        }
        
        //--------------------------------------------------------
  		private List<RssItem> loadFeed2(){
  			List<RssItem> messages = null;
  	    	try{
  	    		Log.i("AndroidNews", "ParserType AndroidSAX");

  		    	long start = System.currentTimeMillis();
  		    	Log.i("AndroidNews", "Parser start=" + start);
  		    	
  		    	AndroidSaxFeedParser saxParser = null;
  		    	// retrieve the rss feed and parse it.
  		    	saxParser = new AndroidSaxFeedParser(mFeedUrl);
  		    	saxParser.run();
  		    	messages = saxParser.getMessages();
				
  		    	long duration = System.currentTimeMillis() - start;
  		    	Log.i("AndroidNews", "Parser duration=" + duration);

  		    	
  	    	} catch (Throwable t){
  	    		Log.e("loadFeed2()",t.getMessage(),t);
				/**
				 *	TODO
				 *	UnknownHostException
				 *	FileNotFoundException
				 *	
				 *	handling of them is needed?
				 *
				 */
  	    	}
  	    	return messages;
  	    }

	}
	
	
}
