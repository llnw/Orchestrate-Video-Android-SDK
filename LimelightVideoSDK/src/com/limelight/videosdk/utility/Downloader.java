package com.limelight.videosdk.utility;

import android.app.Activity;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

/**
 * This class facilitates downloading of widevine offline content and thumbnails associated with media and channels.
 * @author kanchan
 */
public class Downloader {

    private final Set<DownloadTask> mDownloadTasks = new HashSet<DownloadTask>();
    private Activity mActivity;

    /**
     * Callback interface for download.<br>
     * OnSuccess return the path of downloaded file<br>
     * OnError return the error message<br>
     * OnProgress return the progress percentage.<br>
     * @author kanchan
     *
     */
    public interface DownLoadCallback {
        void onSuccess(String path);
        void onError(Throwable ex);
        void onProgress(int percentFinished);
    }

    public Downloader(Activity activity){
        mActivity = activity;
    }

    /**
     * This method downloads the content. 
     * Content may be widevine offline content or thumbnail associated with 
     * channel or media. It has an inner async task class which downloads content 
     * asynchronously.
     * @param url
     * @param mimetype
     * @param saveDirLocation
     * @param callback
     */
    public void startDownload(String url,String mimetype,String saveDirLocation,DownLoadCallback callback){
        if(saveDirLocation == null){
//          if(Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
//                  && ! (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)))
//              saveDirLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
//          else
              saveDirLocation = mActivity.getFilesDir().getPath();
      }

        File dir = new File(saveDirLocation);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                callback.onError(new Throwable("Could Not Create Directory: " + saveDirLocation));
                return;
            }
        }

        if (dir.isFile()) {
            callback.onError(new Throwable("Directory Path Must Be Directory"));
            return;
        }

        if(!Connection.isConnected(mActivity)){
            callback.onError(new Throwable("Device Not Connected"));
            return;
        }

        if(url != null && url.length() > 0 && URLUtil.isValidUrl(url)){
            DownloadTask task = new DownloadTask(mimetype,saveDirLocation,callback);
            mDownloadTasks.add(task);
            task.execute(url);
        }
    }

    public boolean cancelDownload(String url){
        for(DownloadTask task : mDownloadTasks){
            if(url.equals(task.mUrl)){
                task.cancel(true);
                return true;
            }
        }
        return false;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private final DownLoadCallback mCallback;
        private String mSaveDirLocation;
        private String mMimetype;
        private Exception mError;
        private File mTempFile = null;
        private File mFile = null;
        private String mUrl = null;

        DownloadTask(String mimetype, String saveDirLocation, DownLoadCallback callback) {
            this.mSaveDirLocation = saveDirLocation;
            this.mMimetype = mimetype;
            this.mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallback.onProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mCallback.onProgress(progress[0]);
        }

        @Override
        protected String doInBackground(String... urls) {
            publishProgress(0);
            final String url = urls[0];
            mUrl = url;

            String filename = getFilenameFromUrl(url,mMimetype);
            String eventualFileLocation = String.format("%s/%s", mSaveDirLocation, filename);
            mFile = new File(eventualFileLocation);

            if (mFile.exists()) {
                publishProgress(100);
                return eventualFileLocation;
            }
            mTempFile = new File(eventualFileLocation+".tmp");

            InputStream input = null;
            OutputStream output;

            try {
                URL urlOfFile = new URL(url);
                URLConnection connection = urlOfFile.openConnection();
                connection.connect();

                int fileLength = connection.getContentLength();
                int tickSize = 2 * fileLength / 100;
                int nextProgress = tickSize;

                input = new BufferedInputStream(urlOfFile.openStream());

                output = new FileOutputStream(mTempFile);

                byte data[] = new byte[1024 * 1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                   if(isCancelled()){
                       output.close();
                       data = null;
                       mError = new Exception("Cancelled");
                   }
                    total += count;
                    if (total >= nextProgress) {
                        nextProgress = (int) ((total / tickSize + 1) * tickSize);
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    output.write(data, 0, count);
                }
                output.close();
                data = null;
            }
            catch (IOException ex) {
                mError = ex;
            }
            finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ignored) { }
            }
            return eventualFileLocation;
        }


        @Override
        protected void onPostExecute(String pathToFile) {
            mDownloadTasks.remove(this);
            if (mError == null) {
                mCallback.onProgress(100);
                if(mTempFile!= null && mTempFile.exists()){
                    if(mTempFile.renameTo(mFile))
                        mCallback.onSuccess(pathToFile);
                    else
                        mCallback.onError(new Throwable("Failed To Write !"));
                }else{
                    mCallback.onSuccess(pathToFile);
                }
            } else {
                mCallback.onError(mError);
                if(mTempFile!= null && mTempFile.exists())
                    mTempFile.delete();
            }
        }

        /**
         * Method to get filename from URL.If mimetype is supplied then use it 
         * for file extension or get the file extension from URL.
         * @param url
         * @param mimetype
         * @return file name
         */
        private String getFilenameFromUrl(String url,String mimetype) {
            if(mimetype == null)
                mimetype = MimeTypeMap.getFileExtensionFromUrl(url);
            return URLUtil.guessFileName(url, null, mimetype);
        }
    }
}
