/*
 * Copyright (C) 2011 Patrik �kerfeldt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.namh.widget;

import java.util.ArrayList;

import com.muchart.ChartItem;
import com.muchart.R;
import com.namh.widget.flowindicator.TitleProvider;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Toast;

import android.widget.ListView;


public class MusicChartAdapter extends BaseAdapter implements TitleProvider {

	private LayoutInflater mInflater;
	private Context mContext;

	private static final int fakeItemCount = 10;
	
	private ArrayList<ChartAdapter> mCharts;
	private static final String[] mNames = {
									"Billboard Hot-100",
									"Billboard K-POP Hot-100",
									"Melon weekly chart",
									"Melon weekly indi-chart"};
	private OnItemClickListener mItemClickListener;


	public MusicChartAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		// make empty listitems
		makeEmptyChartAdapters();
		
		makeHandlerForListItem();
		

	}
	
	@Override
	public int getCount() {
		return mNames.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListView lview;
		
		if(convertView == null) {
			convertView = mInflater.inflate(R.layout.flow_item, null);
			lview = ((ListView) convertView.findViewById(R.id.listview));

			// Fading Edge attribute
			lview.setVerticalFadingEdgeEnabled(true);	// on flow_item.xml, this was not worked.
			lview.setFadingEdgeLength(5);
			lview.setCacheColorHint(Color.DKGRAY);

			// add listener
			lview.setOnItemClickListener(mItemClickListener);
			
			
			convertView.setTag(lview);
		}
		else
		{
			lview = (ListView)convertView.getTag();			
		}
		
		lview.setAdapter(mCharts.get(position));
		
		return convertView;
	}

	/* (non-Javadoc)
	 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
	 */
	@Override
	public String getTitle(int position) {
		return mNames[position];
	}
	
	public void setAdapter(ArrayList<ChartAdapter> c){
		mCharts = c;
		
	}

	private void makeEmptyChartAdapters(){
		mCharts = new ArrayList<ChartAdapter>();
		
		
		int length = mNames.length;
		for(int i = 0; i < length ; i++)
		{
			ChartAdapter ca = new ChartAdapter(mContext);

			// add empty items
			for (int j = 0; j < fakeItemCount; j++) {
				ca.addItemWithoutNoti(new ChartItem("", "", "", "", ""));
			}
			ca.notifyDataSetChanged();
			mCharts.add(ca);
		}
	}


	// to interface
	public ArrayList<ChartAdapter> getChartAdapter(){

		return mCharts;
	}


	private void makeHandlerForListItem(){
		mItemClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long l_position) {

				// TODO TEST
				mCharts.get(0).getImageLoader().clearCache();
				mCharts.get(0).notifyDataSetChanged();
				// end of TEST
				
				ChartItem citem = (ChartItem)parent.getAdapter().getItem(position);
				String item = citem.toString();
				
				if(position == SlidingView.TITLE_POSITION || item == "")
				{// with empty list item
					return;
				}
				Toast.makeText(mContext.getApplicationContext(), item, Toast.LENGTH_SHORT).show();
				
				// Invoke the Youtube to search
				final String youtube = "com.google.android.youtube";
				Intent intent = new Intent(Intent.ACTION_SEARCH);
				intent.setPackage(youtube);
				intent.putExtra("query",
						item.substring(item.indexOf(": ")));	// strip the rank out
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				
				try{
					mContext.startActivity(intent);
				}catch(ActivityNotFoundException e){
					Toast.makeText(mContext.getApplicationContext(), youtube + " does not exist", Toast.LENGTH_LONG).show();
				}
			}

			
		};
	}


}
