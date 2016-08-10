package com.muchart;

public class ChartItem{
	
	private final String mRank;
	private final String mTitle;
	private final String mArtist;
	private final String mAlbum;
	private final String mThumbUrl;

	public ChartItem(String rank, String title, String artist){
		this.mRank = rank;
		this.mTitle = title;
		this.mArtist = artist;
		this.mAlbum = "";
		this.mThumbUrl = "";
	}
	
	public ChartItem(String rank, String title, String artist, String album, String thumburl){
		this.mRank = rank;
		this.mTitle = title;
		this.mArtist = artist;
		this.mAlbum = album;
		this.mThumbUrl = thumburl;
	}

	
	public String getRank(){
		return mRank;
	}
	public String getTitle(){
		return mTitle;
	}
	public String getArtist(){
		return mArtist;
	}
	public String getAlbum(){
		return mAlbum;
	}

	public String getThumbUrl(){
		return mThumbUrl;
	}
	
	public String toString(){
		return mRank + ": " + mTitle + ", " + mArtist;
	}
	
	
}
