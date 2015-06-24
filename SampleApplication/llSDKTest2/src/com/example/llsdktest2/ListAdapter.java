package com.example.llsdktest2;

import java.util.ArrayList;
import com.limelight.videosdk.utility.Downloader;
import com.limelight.videosdk.utility.Thumbnail;
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
import android.widget.ProgressBar;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {
    
    private ArrayList<String> mTitleList;
    private ArrayList<Uri> mUriList;    
    private ArrayList<String> mMediaIdList;    
    private FragmentActivity mFragmentActivity;
    private Bitmap mLoadingBitmap = null;
    private Downloader mDownloader = null;

    class ViewHolder {
        public TextView mTextView;
        public ImageView mThumbnailImage;
        public ProgressBar mProgress;
      }

    public ListAdapter(FragmentActivity mActivity) {
        mFragmentActivity = mActivity;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), android.R.drawable.ic_menu_gallery);
        mDownloader = new Downloader(mFragmentActivity);
    }

    public ListAdapter(FragmentActivity mActivity,int type) {
        mFragmentActivity = mActivity;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), android.R.drawable.ic_menu_gallery);
        mDownloader = new Downloader(mFragmentActivity);
    }
    
    public void setData(ArrayList<String> list, ArrayList<Uri> uri,ArrayList<String> mediaIds){
        mUriList = uri;
        mTitleList = list;
        mMediaIdList = mediaIds;
    }
    public ListAdapter(FragmentActivity mActivity, ArrayList<String> mList, ArrayList<Uri> mUri) {
        mTitleList = mList;
        mFragmentActivity = mActivity;
        mUriList = mUri;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), android.R.drawable.ic_menu_gallery);
        mDownloader = new Downloader(mFragmentActivity);
    }

    @Override
    public int getCount() {
        return mTitleList == null ? 0 : mTitleList.size();
    }

    @Override
    public Object getItem(int position) {
        return mTitleList == null ? null : mTitleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = (LayoutInflater) mFragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_media, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) rowView.findViewById(R.id.text);
            viewHolder.mThumbnailImage = (ImageView) rowView.findViewById(R.id.image);
            viewHolder.mProgress = (ProgressBar) rowView.findViewById(R.id.progressThumbnail);
            rowView.setTag(viewHolder);
        }
        
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.mTextView.setText(mTitleList.get(position));
        if(mUriList.get(position)!= null){
            mDownloader.startDownload(mUriList.get(position).toString(), null, null, mMediaIdList.get(position),new Downloader.DownLoadCallback(){
                @Override
                public void onSuccess(String path) {
                    Thumbnail t = new Thumbnail();
                    t.loadBitmap(mFragmentActivity, path, holder.mThumbnailImage, null);
                    holder.mProgress.setVisibility(View.GONE);
                }
                @Override
                public void onError(Throwable ex) {
                    holder.mThumbnailImage.setImageBitmap(mLoadingBitmap);
                    holder.mProgress.setVisibility(View.GONE);
                }
                @Override
                public void onProgress(int percentFinished) {
                    holder.mThumbnailImage.setImageBitmap(mLoadingBitmap);
                    holder.mProgress.setVisibility(View.VISIBLE);
                }
            });
        }

        return rowView;
    }

}
