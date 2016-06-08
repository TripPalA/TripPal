package com.trippal.trippal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by samskim on 6/7/16.
 */
//reference: http://android--code.blogspot.com/2015/08/android-imageview-set-image-from-url.html
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

    private final String LOG_TAG = DownloadImageTask.class.getSimpleName();

    ImageView imageView;

    public DownloadImageTask(ImageView bmImage) {
        this.imageView = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urlOfImage = urls[0];
        Bitmap logo = null;
        try{
            InputStream is = new URL(urlOfImage).openStream();
            logo = BitmapFactory.decodeStream(is);
        }catch(Exception e){ // Catch the download exception
            e.printStackTrace();
        }
        return logo;
    }

    protected void onPostExecute(Bitmap result) {
        Log.v(LOG_TAG, "IMAGEDOWNLOAD: " + result.toString() );
        imageView.setImageBitmap(result);
    }

}
