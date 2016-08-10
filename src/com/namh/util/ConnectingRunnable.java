package com.namh.util;

import java.io.IOException;

import android.util.Xml;

public class ConnectingRunnable implements Runnable{
	private AndroidSaxFeedParser parser;
	
	ConnectingRunnable(AndroidSaxFeedParser parser){
		this.parser = parser;
	}
	
	@Override
	public void run() {
//		android.os.Debug.waitForDebugger();
//		parser.setInputStream();
//		try {
//			is = feedUrl.openConnection().getInputStream();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		// Parsing
//		Xml.parse(this.is, Xml.Encoding.UTF_8, root.getContentHandler());

	}
	

}
