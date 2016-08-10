package com.namh.util.cache;

import java.io.File;
import java.net.URLEncoder;




import android.content.Context;

public class FileCache {
    
    private File cacheDir;
	private FileNameGenerator fileNameGenerator;
	
    public FileCache(Context context){

		fileNameGenerator = new Md5FileNameGenerator();

		//Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"LazyList");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();

    }
    
    public File getFile(String url){

		String filename = fileNameGenerator.generate(url);
		File f = new File(cacheDir, filename);
		return f;
	}
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files != null)
        {
	        for(File f:files){
    	        f.delete();
	        }
        }// end of if
    }

}