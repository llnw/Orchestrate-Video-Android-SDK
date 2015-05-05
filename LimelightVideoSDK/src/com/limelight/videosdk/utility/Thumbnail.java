package com.limelight.videosdk.utility;

import java.lang.ref.WeakReference;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * This class helps in loading thumbnails associated with the channels and media.
 * It decodes and loads the images in background as decoding bitmaps in UI thread causes OutOfMenory.
 * @author kanchan
 */
public class Thumbnail {

    private Bitmap mLoadingBitmap;

    /**
     * To load a bitmap into image view in background.
     * @param ctx
     * @param path path of the bitmap/image
     * @param imageView
     * @param loadingBitmap placeholder or loading bitmap
     */
    public void loadBitmap(final Context ctx,final String path, final ImageView imageView,final Bitmap loadingBitmap) {
        if(ctx != null && imageView != null && cancelPotentialWork(path, imageView)){
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            if(loadingBitmap != null){
                mLoadingBitmap = loadingBitmap;
            }
            if(mLoadingBitmap == null){
                mLoadingBitmap = BitmapFactory.decodeResource(ctx.getResources(),android.R.drawable.ic_menu_gallery);
            }
            final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(path);
        }
    }

    private boolean cancelPotentialWork(final String path, final ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final String bitmapPath = bitmapWorkerTask.mPath;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapPath == null || !(bitmapPath.equals(path))) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private BitmapWorkerTask getBitmapWorkerTask(final ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> mWorker;

        public AsyncDrawable(final BitmapWorkerTask bitmapWorkerTask) {
            super(mLoadingBitmap);
            mWorker = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return mWorker.get();
        }
    }

    /**
     * This method scales an image as per the requested height and width.
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return Scaled Bitmap
     */
    public Bitmap scaleImage(final String path, final int reqWidth, final int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }

    private int calculateInSampleSize(final BitmapFactory.Options options, final int reqWidth, final int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> mImageReference;
        private String mPath;
        public BitmapWorkerTask(final ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            mImageReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(final String... params) {
            mPath = params[0];
            if(mPath == null){
                return null;
            }
            final ImageView image = mImageReference.get();
            if(image == null){
                return scaleImage(mPath, 100, 100);
            }
            else{
                return scaleImage(mPath, image.getWidth(), image.getHeight());
            }
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()||bitmap == null) {
                bitmap = mLoadingBitmap;
            }
            if (mImageReference != null && bitmap != null) {
                final ImageView imageView = mImageReference.get();
                final BitmapWorkerTask bitmapWorkerTask =getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
