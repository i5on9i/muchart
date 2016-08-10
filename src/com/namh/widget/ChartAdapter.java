package com.namh.widget;

import java.util.ArrayList;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.muchart.ChartItem;
import com.muchart.R;
import com.namh.util.imageloader.ImageLoader;


public class ChartAdapter extends BaseAdapter{
	
	/**
	 * layout inflator
	 * 
	 * Instantiates a layout XML file into its corresponding View objects.
	 * @ref : http://developer.android.com/reference/android/view/LayoutInflater.html
	 */
	private LayoutInflater mInflater;
	private ArrayList<ChartItem> mData = new ArrayList<ChartItem>();
	
	
	private static final int TYPE_ITEM1 = 0;
	private static final int TYPE_ITEM2 = 1;
	private static final int TYPE_TITLE = 2;
	private static final int TYPE_MAX_COUNT = 3;
    private ImageLoader imageLoader; 


	
	//--------------------------------------------------------
	public ChartAdapter(Context context) {
		mInflater = (LayoutInflater)context.getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		imageLoader=new ImageLoader(context.getApplicationContext());
	}

	
	//--------------------------------------------------------
	public void addSeparatorItem(String string) {
		// TODO Auto-generated method stub
		
	}
	
	//--------------------------------------------------------
	public void addItem(ChartItem item) {
		mData.add(item);
		notifyDataSetChanged();
	}
	
	//--------------------------------------------------------
	public void addItemWithoutNoti(ChartItem item) {
		mData.add(item);

	}
	
	//--------------------------------------------------------
	public void clear() {
		mData.clear();
	}
	
	//--------------------------------------------------------
	@Override
	public int getCount() {
		return mData.size();
	}


	//--------------------------------------------------------
	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}


	//--------------------------------------------------------
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 *    getItemViewType()
	 *    getViewTypeCount()
	 * are needed for the different types of listitem.
	 */
	//--------------------------------------------------------
	@Override
    public int getItemViewType(int position) {

		return position%2;
    }

	//--------------------------------------------------------
    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

	//--------------------------------------------------------
	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		
		TextView textView = null;
		TextView textViewTitle = null;
		TextView textViewArtist = null;

		ViewHolder holder;

		if(convertView == null){
			holder = new ViewHolder();
			switch(getItemViewType(pos)){
				case TYPE_ITEM1:	// even
					convertView = mInflater.inflate(R.layout.item1, null);
					
					holder.rank = (TextView)convertView.findViewById(R.id.ranktext);
					holder.title= (TextView)convertView.findViewById(R.id.title);
					holder.artist = (TextView)convertView.findViewById(R.id.artist);
					holder.album = (TextView)convertView.findViewById(R.id.album);

					holder.thumb = (ImageView)convertView.findViewById(R.id.list_image);


				break;
				case TYPE_ITEM2:	// odd
					convertView = mInflater.inflate(R.layout.item1, null);
					
					holder.rank = (TextView)convertView.findViewById(R.id.ranktext);
					holder.title= (TextView)convertView.findViewById(R.id.title);
					holder.artist = (TextView)convertView.findViewById(R.id.artist);
					holder.album = (TextView)convertView.findViewById(R.id.album);

					holder.thumb = (ImageView)convertView.findViewById(R.id.list_image);

				break;
				case TYPE_TITLE:	// title
					convertView = mInflater.inflate(R.layout.item3, null);
					textView = (TextView)convertView.findViewById(R.id.title);
					
				break;
			}
			
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder)convertView.getTag();
		}
		

		ChartItem item = mData.get(pos);
		
		holder.rank.setText(item.getRank());
		holder.artist.setText(item.getArtist());
		holder.title.setText(item.getTitle());
		holder.album.setText(item.getAlbum());
		imageLoader.DisplayImage(holder.thumb,
				item.getThumbUrl());


		
		
		
		
		return convertView;
	}

	static class ViewHolder {
        TextView rank;
        TextView title;
        TextView artist;
        TextView album;
		ImageView thumb;
		
    }

	public ImageLoader getImageLoader(){
		return imageLoader;
	}
}
