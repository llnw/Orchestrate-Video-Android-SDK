package com.limelight.videosdk.utility;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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
import java.util.LinkedHashSet;
import java.util.Set;

import com.limelight.videosdk.Constants;

/**
 * This class facilitates downloading of widevine offline content and thumbnails associated with media and channels.
 * @author kanchan
 */
public class Downloader {

    private final Set<DownloadTask> mDownloadTasks = new LinkedHashSet<DownloadTask>();
    private final Activity mActivity;

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
        void onError(Throwable throwable);
        void onProgress(int percentFinished);
    }

    public Downloader(final Activity activity){
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
    public void startDownload(final String url,final String mimetype,String saveDirLocation,final String mediaId,final DownLoadCallback callback){

        if(Build.VERSION.SDK_INT < 18
                && mimetype!= null
                && mimetype.equalsIgnoreCase("video/wvm")
                && saveDirLocation == null
                && Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                && ! (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY))){
            saveDirLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        }
        if(saveDirLocation == null){
            saveDirLocation = mActivity.getFilesDir().getPath();
        }
        if(mediaId != null && !mediaId.trim().isEmpty()){
            saveDirLocation = saveDirLocation+"/"+mediaId;
        }
        final File dir = new File(saveDirLocation);

        if (!dir.exists() && !dir.mkdirs()) {
            callback.onError(new Throwable("Could Not Create Directory: " + saveDirLocation));
            return;
        }

        if (dir.isFile()) {
            callback.onError(new Throwable("Directory Path Must Be Directory"));
            return;
        }

        if(!Connection.isConnected(mActivity)){
            callback.onError(new Throwable(Constants.CONNECTION_ERROR));
            return;
        }

        if(url != null && url.length() > 0 && URLUtil.isValidUrl(url)){
            final DownloadTask task = new DownloadTask(mimetype,saveDirLocation,callback);
            mDownloadTasks.add(task);
            task.execute(url);
        }
        else{
            callback.onError(new Throwable("URL Invalid"));
        }
    }

    /**
     * This method cancels a downloading operation.
     * @param url
     * @return
     */
    public boolean cancelDownload(final String url){
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
        private final String mSaveDirLocation;
        private final String mMimetype;
        private Exception mError;
        private File mTempFile;
        private File mFile;
        private String mUrl;

        DownloadTask(final String mimetype, final String saveDirLocation, final DownLoadCallback callback) {
            this.mSaveDirLocation = saveDirLocation;
            this.mMimetype = mimetype;
            this.mCallback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(mCallback !=null)
                mCallback.onProgress(0);
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            super.onProgressUpdate(progress);
            if(mCallback !=null)
                mCallback.onProgress(progress[0]);
        }

        @Override
        protected String doInBackground(final String... urls) {
            publishProgress(0);
            final String url = urls[0];
            mUrl = url;

            final String filename = getFilenameFromUrl(url,mMimetype);
            final String fileLocation = String.format("%s/%s", mSaveDirLocation, filename);
            mFile = new File(fileLocation);

            if (mFile.exists()) {
                publishProgress(100);
                return fileLocation;
            }
            mTempFile = new File(fileLocation+".tmp");

            InputStream input = null;
            OutputStream output;

            try {
                final URL urlOfFile = new URL(url);
                final URLConnection connection = urlOfFile.openConnection();
                connection.setConnectTimeout(Constants.PREPARING_TIMEOUT);
                connection.connect();

                final int fileLength = connection.getContentLength();
                final int tickSize = 2 * fileLength / 100;
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
                       mTempFile.delete();
                       mError = new Exception("Cancelled");
                       break;
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
                } catch (IOException ignored) {
                    //ignored
                }
            }
            return fileLocation;
        }


        @Override
        protected void onPostExecute(final String pathToFile) {
            mDownloadTasks.remove(this);
            if (mError == null) {
                if(mCallback !=null)
                    mCallback.onProgress(100);
                if(mTempFile!= null && mTempFile.exists()){
                    if(mTempFile.renameTo(mFile)){
                        if(mCallback !=null)
                            mCallback.onSuccess(pathToFile);
                    }
                    else{
                        if(mCallback !=null)
                            mCallback.onError(new Throwable("Failed To Write !"));
                    }
                }else{
                    if(mCallback !=null)
                        mCallback.onSuccess(pathToFile);
                }
            } else {
                if(mCallback !=null)
                    mCallback.onError(mError);
                if(mTempFile!= null && mTempFile.exists()){
                    mTempFile.delete();
                }
            }
        }

        /**
         * Method to get filename from URL.If mimetype is supplied then use it 
         * for file extension or get the file extension from URL.
         * @param url
         * @param mimetype
         * @return file name
         */
        private String getFilenameFromUrl(final String url,String mimetype) {
            if(mimetype == null){
                mimetype = MimeTypeMap.getFileExtensionFromUrl(url);
            }
            return URLUtil.guessFileName(url, null, mimetype);
        }
    }
}
