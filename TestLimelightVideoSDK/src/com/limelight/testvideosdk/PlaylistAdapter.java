package com.limelight.testvideosdk;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.limelight.testvideosdk.MediaFragment.PlaylistCallback;
import com.limelight.videosdk.Constants;
import com.limelight.videosdk.model.Media;
import com.limelight.videosdk.utility.Downloader;
import com.limelight.videosdk.utility.Thumbnail;

public class PlaylistAdapter extends BaseAdapter {
    ArrayList<Media> mPlayList;
    FragmentActivity mFragmentActivity;
    Bitmap mLoadingBitmap = null;
    Downloader mDownloader = null;
    PlaylistCallback mCallback;
    private int mPosition = -1;
    private int mModelType = -1;

    public PlaylistAdapter(FragmentActivity mActivity) {
        mFragmentActivity = mActivity;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), android.R.drawable.ic_menu_gallery);
        mDownloader = new Downloader(mFragmentActivity);
    }

    public PlaylistAdapter(FragmentActivity mActivity,int type,PlaylistCallback playlistCallback) {
        mFragmentActivity = mActivity;
        mLoadingBitmap = BitmapFactory.decodeResource(mFragmentActivity.getResources(), android.R.drawable.ic_menu_gallery);
        mDownloader = new Downloader(mFragmentActivity);
        mCallback = playlistCallback;
        mModelType = type;
    }

    public void setData(ArrayList<Media> list){
       mPlayList = list;
    }

    public void setCurrentPlayingPosition(int position){
        mPosition = position;
     }

    @Override
    public int getCount() {
        return mPlayList == null ? 0 : mPlayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlayList == null ? null : mPlayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        public TextView mTextView;
        public ImageView mThumbnailImage;
        public ProgressBar mProgress;
        public ImageView mRemovePlaylist;
      }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = (LayoutInflater) mFragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.playlist_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) rowView.findViewById(R.id.text);
            viewHolder.mThumbnailImage = (ImageView) rowView.findViewById(R.id.image);
            viewHolder.mProgress = (ProgressBar) rowView.findViewById(R.id.progressThumbnail);
            viewHolder.mRemovePlaylist = (ImageView) rowView.findViewById(R.id.delete);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.mTextView.setText(mPlayList.get(position).mTitle);
        if(mPosition == position){
            holder.mTextView.setTextColor(Color.BLUE);
        }else{
            holder.mTextView.setTextColor(Color.BLACK);
        }
        if(mPlayList.get(position)!= null && mPlayList.get(position).mThumbnail!= null && mPlayList.get(position).mThumbnail.mUrl!= null){
            mDownloader.startDownload(mPlayList.get(position).mThumbnail.mUrl.toString(), null, null, mPlayList.get(position).mMediaID,new Downloader.DownLoadCallback(){
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
        if(mModelType == Constants.TYPE_MEDIA){
            holder.mRemovePlaylist.setVisibility(View.VISIBLE);
            holder.mRemovePlaylist.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.removeFromPlaylist(position);
                }
            });
        }
        if(mModelType == Constants.TYPE_CHANNEL){
            holder.mRemovePlaylist.setVisibility(View.GONE);
        }
        return rowView;
    }
}
