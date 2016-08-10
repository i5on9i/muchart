package com.namh.util.imageloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.muchart.R;
import com.namh.util.cache.FileCache;

import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
	
	protected static final int BUFFER_SIZE = 8 * 1024; // 8 Kb
    
    private MemoryCache mMemoryCache=new MemoryCache();
    private FileCache mFileCache;
    private Map<ImageView, String> mImageViews
		= Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService mExecutorService;
    Handler mHandler=new Handler();//mHandler to display images in UI thread
    
    public ImageLoader(Context context){
        mFileCache=new FileCache(context);
        mExecutorService=Executors.newFixedThreadPool(5);
    }
    
    final int stub_id=R.drawable.noimage;
    public void DisplayImage(ImageView imageView, String url)
    {
    	if(url ==""){
    		//TODO
    		// do something
    	}
    		
        mImageViews.put(imageView, url);
        Bitmap bitmap = mMemoryCache.get(url);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else
        {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }

	boolean imageViewReused(PhotoToLoad photoToLoad){
		String tag=mImageViews.get(photoToLoad.imageView);
		
		if(tag==null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        mExecutorService.submit(new PhotosLoader(p));	// thread.execute()
    }
    
    
	private Bitmap decodeFile(InputStream in){
		//decode with inSampleSize
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize=getProperScale(in);

		return BitmapFactory.decodeStream(in, null, opt);
	}
	
	private int getProperScale(InputStream in){
		//decode image size
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(in,null,opt);
        
        //Find the correct scale value. It should be the power of 2.
        final int REQUIRED_SIZE=70;
        int width_tmp=opt.outWidth, height_tmp=opt.outHeight;
        int scale=1;
        while(true){
            if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                break;
            width_tmp/=2;
            height_tmp/=2;
            scale*=2;
        }

		return scale;

			
	}
    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1=new FileInputStream(f);
            BitmapFactory.decodeStream(stream1,null,o);
            stream1.close();
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            FileInputStream stream2=new FileInputStream(f);
            Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



	
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    private class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
		
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            try{
				/**
				 * We use {@link #ListView.getView()}, so that
				 * the {@link #imageViewReused()} is not proper.
				 */
//                if(imageViewReused(photoToLoad))
//                    return;

				Bitmap thumbnail = null;
				try {
					String url = photoToLoad.url;
					thumbnail = getBitmap(url);
					mMemoryCache.put(url, thumbnail);


				} catch (Exception e) {
					Log.e("Error DownloadImageTask", e.getMessage());
					e.printStackTrace();
				}

				
				BitmapDisplayer bd=new BitmapDisplayer(thumbnail, photoToLoad);
                mHandler.post(bd);	// i don't know how this works




				
//                Bitmap bmp=getBitmap(photoToLoad.url);
//                mMemoryCache.put(photoToLoad.url, bmp);
//                if(imageViewReused(photoToLoad))
//                    return;
//                BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
//                mHandler.post(bd);
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
		
		private Bitmap getBitmap(String url) 
		{
			
			//from SD cache
			File imageFile = mFileCache.getFile(url);
			Bitmap thumbnail = null;

			// Try to load image from disc cache
			if (imageFile.exists()) {
				thumbnail = decodeFile(imageFile);
				if(thumbnail != null)
					return thumbnail;
			}
			
			// Missing Cache, Get the image From web
			try {
				
				URL imageUrl = new URL(url);
				
				HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
				conn.setConnectTimeout(30000);
				conn.setReadTimeout(30000);
				conn.setInstanceFollowRedirects(true);
				
				// caching as a file.
				InputStream is=new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
				try {
					// TODO
					// http://stackoverflow.com/questions/1239026/how-to-create-a-file-in-android
					imageFile.createNewFile();
					if(imageFile.exists())
					{
						OutputStream os = new BufferedOutputStream(new FileOutputStream(imageFile), BUFFER_SIZE);
						try {
							// save a image file to the disk
							FileUtils.copyStream(is, os);
						} finally {
							os.close();
						}
					}
				
				}finally{
					is.close();
				}

				thumbnail = BitmapFactory.decodeStream(is);

				return thumbnail;

			} catch (Throwable ex){
			   ex.printStackTrace();
			   if(ex instanceof OutOfMemoryError)
				   mMemoryCache.clear();
			   return null;
			}
		}
    }
    
    
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
			bitmap=b;
			photoToLoad=p;
		}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

}
