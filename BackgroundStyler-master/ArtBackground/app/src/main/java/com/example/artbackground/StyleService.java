package com.example.artbackground;

import android.app.Service;
import android.app.Activity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.example.artbackground.R;

import java.io.File;
import java.io.InputStream;

import static android.R.attr.data;
import static android.R.attr.duration;


public class StyleService extends Service
{
    private HttpEntity resEntity;
    Activity act;

    private String lastUploaded = null;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        registerContentObserver();

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void registerContentObserver()
    {
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler())
                {
                    @Override
                    public void onChange(boolean selfChange)
                    {
                        Log.d("ScratchService", "External Media has been added");
                        onMediaChanged();
                        super.onChange(selfChange);
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
                new ContentObserver(new Handler())
                {
                    @Override
                    public void onChange(boolean selfChange)
                    {
                        Log.d("ScratchService", "Internal Media has been added");
                        onMediaChanged();
                        super.onChange(selfChange);
                    }
                }
        );

    }

    private void onMediaChanged()
    {
        // find out what changed (if at all)
        final String cameraFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
        File latest = getLatestFilefromDir(cameraFolder);
        if (!latest.toString().equals(lastUploaded))
        {
            // picture was added
            doFileUpload(latest);
        }
    }

    private File getLatestFilefromDir(String dirPath)
    {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0)
        {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++)
        {
            if (lastModifiedFile.lastModified() < files[i].lastModified())
            {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    private void doFileUpload(File file)
    {
        new UploadTask().execute(file);
    }

    private class UploadTask extends AsyncTask<File, Void, Void>
    {
        @Override
        protected Void doInBackground(File... file)
        {
            String urlString = "http://54.159.184.84:5000/upload";
            try
            {
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urlString);
                FileBody bin1 = new FileBody(file[0]);
                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("uploadedfile1", bin1);
                reqEntity.addPart("user", new StringBody("User"));
                post.setEntity(reqEntity);
                HttpResponse response = client.execute(post);
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) response.getEntity().getContent());
                // TODO do something with bitmap

            } catch (Exception ex)
            {
                Log.e("Debug", "error: " + ex.getMessage(), ex);
            }

            return null;
        }
    }
}