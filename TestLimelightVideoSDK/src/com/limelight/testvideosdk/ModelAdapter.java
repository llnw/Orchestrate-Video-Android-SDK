package com.limelight.testvideosdk;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.limelight.videosdk.utility.Downloader;
import com.limelight.videosdk.utility.Thumbnail;

public class ModelAdapter extends BaseAdapter {
    ArrayList<String> mLisArrayList;
    ArrayList<Uri> mUriList;
    FragmentActivity mFragmentActivity;
    Bitmap mLoadingBitmap = null;
    Downloader mDownloader = null;
    public ModelAdapter(FragmentActivity mActivity) {
        mFragmentActivity = mActivity;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), R.drawable.ic_media_video_poster);
        mDownloader = new Downloader(mFragmentActivity);
    }
    
    public void setData(ArrayList<String> list, ArrayList<Uri> uri){
        mUriList = uri;
        mLisArrayList = list;
    }
    public ModelAdapter(FragmentActivity mActivity, ArrayList<String> mList, ArrayList<Uri> mUri) {
        mLisArrayList = mList;
        mFragmentActivity = mActivity;
        mUriList = mUri;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), R.drawable.ic_media_video_poster);
        mDownloader = new Downloader(mFragmentActivity);
    }

    @Override
    public int getCount() {
        return mLisArrayList == null ? 0 : mLisArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLisArrayList == null ? null : mLisArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        public TextView mTextView;
        public ImageView image;
      }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = (LayoutInflater) mFragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_model, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) rowView.findViewById(R.id.text);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.image);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.mTextView.setText(mLisArrayList.get(position));
        if(mUriList.get(position)!= null){
            mDownloader.startDownload(mUriList.get(position).toString(), null, null, new Downloader.DownLoadCallback(){
                @Override
                public void onSuccess(String path) {
                    Thumbnail t = new Thumbnail();
                    t.loadBitmap(mFragmentActivity, path, holder.image, mLoadingBitmap);
                }
                @Override
                public void onError(Throwable ex) {
                }
                @Override
                public void onProgress(int percentFinished) {
                }
            });
        }
        return rowView;
    }
}
