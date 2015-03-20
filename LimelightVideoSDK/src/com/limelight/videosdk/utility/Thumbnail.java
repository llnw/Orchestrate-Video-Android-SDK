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

    static Bitmap sLoadingBitmap = null;

    /**
     * To load a bitmap into image view in background.
     * @param ctx
     * @param path path of the bitmap/image
     * @param imageView
     * @param loadingBitmap placeholder or loading bitmap
     */
    public void loadBitmap(Context ctx,String path, ImageView imageView,Bitmap loadingBitmap) {
        if(ctx != null && imageView != null){
            if (cancelPotentialWork(path, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                if(loadingBitmap != null)
                    sLoadingBitmap = loadingBitmap;
                if(sLoadingBitmap == null)
                    sLoadingBitmap = BitmapFactory.decodeResource(ctx.getResources(),android.R.drawable.ic_menu_gallery);
                final AsyncDrawable asyncDrawable = new AsyncDrawable(task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(path);
            }
        }
    }

    private boolean cancelPotentialWork(String path, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final String bitmapPath = bitmapWorkerTask.path;
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

    private BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
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
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(BitmapWorkerTask bitmapWorkerTask) {
            super(sLoadingBitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public Bitmap scaleImage(String path, int reqWidth, int reqHeight) {

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

    int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
        private final WeakReference<ImageView> imageViewReference;
        private String path = null;
        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            path = params[0];
            if(path == null)
                return null;
            ImageView i = imageViewReference.get();
            if(i == null)
                return scaleImage(path, 100, 100);
            else
                return scaleImage(path, i.getWidth(), i.getHeight());
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if(bitmap == null){
                bitmap = sLoadingBitmap;
            }
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
