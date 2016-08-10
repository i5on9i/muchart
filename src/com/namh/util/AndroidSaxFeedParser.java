package com.namh.util;



import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;


import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

public class AndroidSaxFeedParser implements Runnable{
	
	static final String CHANNEL = "channel";
	static final String PUB_DATE = "pubDate";
	static final  String DESCRIPTION = "description";
	static final  String LINK = "link";
	static final  String TITLE = "title";
	static final  String ITEM = "item"; 
	
	static final String RSS = "rss";
	private final URL feedUrl;
	
	private List<RssItem> messages;
	
	//--------------------------------------------------------
	public AndroidSaxFeedParser(String feedUrl) {
		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	//--------------------------------------------------------
	private InputStream getInputStream(){
		try {
			return feedUrl.openConnection().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	//--------------------------------------------------------
	public void parse() {
		final RssItem currentItem = new RssItem();
		final List<RssItem> messages = new ArrayList<RssItem>();
		
		RootElement root = new RootElement(RSS);

		Element channel = root.getChild(CHANNEL);
		Element item = channel.getChild(ITEM);
		
		item.setEndElementListener(new EndElementListener(){
			public void end() {
				messages.add(currentItem.copy());
			}
		});
		item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentItem.setTitle(body);
			}
		});
		item.getChild(LINK).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentItem.setLink(body);
			}
		});
		item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentItem.setDescription(body);
			}
		});
		item.getChild(PUB_DATE).setEndTextElementListener(new EndTextElementListener(){
			public void end(String body) {
				currentItem.setDate(body);
			}
		});
		
		// Parsing
		try {
			
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.messages = messages;
	}

	//--------------------------------------------------------
	@Override
	public void run() {
		this.parse();
	}
	
	
	//--------------------------------------------------------
	public List<RssItem> getMessages(){
		return this.messages;
	}

	
}
