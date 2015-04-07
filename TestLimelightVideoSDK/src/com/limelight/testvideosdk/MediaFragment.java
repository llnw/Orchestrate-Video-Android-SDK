package com.limelight.testvideosdk;

import java.util.ArrayList;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.limelight.videosdk.Constants;
import com.limelight.videosdk.ContentService;
import com.limelight.videosdk.model.Media;
import com.limelight.videosdk.model.Media.MediaThumbnail;

public class MediaFragment extends Fragment implements LoaderManager.LoaderCallbacks<ModelHolder>,OnItemClickListener,OnRefreshListener,OnScrollListener{

    private ModelAdapter mAdapter;
    private ListView mListView;
    private static ArrayList<Media> mMedias = null;
    private static String mErrorMsg;
    private TextView mTextView;
    private ProgressBar mProgress;
    private ProgressBar mProgressLoad;
    private MediaCallback mCallback;
    private SwipeRefreshLayout mSwipeLayout;
    private int mPreviousTotalCount = 0;

    public MediaFragment(MediaCallback callback) {
        mCallback = callback;
        mAdapter= null;
        mListView= null;
    }

    public interface MediaCallback{
        void callback(String id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model, container,false);
        mListView = (ListView) view.findViewById(android.R.id.list);
        mTextView = (TextView) view.findViewById(android.R.id.empty);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mProgressLoad = (ProgressBar) view.findViewById(R.id.progress_load);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeColors(Color.BLUE,Color.GREEN,Color.RED);
        mSwipeLayout.setDistanceToTriggerSync(250);
        mSwipeLayout.setEnabled(false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No Media");
        mAdapter = new ModelAdapter(getActivity());
        setListAdapter(mAdapter);
        setListShown(false);
        getActivity().getSupportLoaderManager().initLoader(12, null, this);
    }

    private void setListShown(boolean b) {
        mProgressLoad.setVisibility(View.GONE);
        if(b){
            //Hide progress
            mProgress.setVisibility(View.GONE);
            //hide text if list exist
            if(mMedias!= null && mMedias.size()>0){
                mListView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
            }else{
                mListView.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
            }
        }else{
            //show progress hide text hide list
            mListView.setVisibility(View.GONE);
            mTextView.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
        }
    }

    private void setListAdapter(ModelAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    private void setEmptyText(String string) {
        mTextView.setText(string);
    }

    private void restartLoader(){
        mPreviousTotalCount = 0;
        setListShown(false);
        getActivity().getSupportLoaderManager().restartLoader(12, null, this);
    }

    private void loadMore(){
        mProgressLoad.setVisibility(View.VISIBLE);
        Bundle b = new Bundle();
        b.putBoolean("Refresh", true);
        getActivity().getSupportLoaderManager().restartLoader(12, b, this);
    }

    private static class MediaLoader extends AsyncTaskLoader<ModelHolder> {

        private ModelHolder mHolder;
        private boolean refresh = false;
        private static ContentService mContentService = null;
        private Context mContext;

        public MediaLoader(Context context, Bundle arg1) {
            super(context);
            mContext = context;
            if(arg1!= null){
                refresh = arg1.getBoolean("Refresh", false);
            }
            mHolder = new ModelHolder();
            if(mContentService == null){
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String orgId = preferences.getString(mContext.getResources().getString(R.string.OrgIDEditPrefKey), null);
                String accessKey = preferences.getString(mContext.getResources().getString(R.string.AccKeyEditPrefKey), null);
                String secret = preferences.getString(mContext.getResources().getString(R.string.SecKeyEditPrefKey), null);
                mContentService = new ContentService(mContext,orgId,accessKey,secret);
            }
        }

        @Override
        public ModelHolder loadInBackground() {
            ArrayList<String> data = new ArrayList<String>();
            ArrayList<Uri> urls = new ArrayList<Uri>();

            try {
                mContentService.setPagingParameters(100, Constants.SORT_BY_UPDATE_DATE, Constants.SORT_ORDER_ASC);
//                mMedias = contentService.searchMedia(ctx, refresh, "and", "4", null, null, null, null, null);
                mMedias = mContentService.getAllMedia(refresh);
            } catch (Exception e) {
                mMedias = null;
                mErrorMsg = e.getMessage();
                return mHolder;
            }
            for(int i = 0; i<mMedias.size() ;i++){
                data.add(mMedias.get(i).mTitle);
                MediaThumbnail t = mMedias.get(i).mThumbnail;
                if(t!= null)
                    urls.add((t.mUrl));
            }
            mHolder.setData(data);
            mHolder.setUrls(urls);
            return mHolder;
        }

        @Override
        public void deliverResult(ModelHolder data) {
            super.deliverResult(data);
        }
    }
    
    @Override
    public Loader<ModelHolder> onCreateLoader(int arg0, Bundle arg1) {
        MediaLoader loader = new MediaLoader(MediaFragment.this.getActivity(),arg1);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ModelHolder> arg0, ModelHolder arg1) {
        if(mMedias == null){
            setEmptyText(mErrorMsg);
            mSwipeLayout.setEnabled(true);
        }
        else if(mMedias.size()==0){
            setEmptyText("No Media Found");
            mSwipeLayout.setEnabled(true);
        }
        mAdapter.setData(arg1.getData(),arg1.getUrls());
        mAdapter.notifyDataSetChanged();
        setListShown(true);
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ModelHolder> arg0) {
        mListView.setAdapter(null);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        mCallback.callback(mMedias.get(position).mMediaID);
    }

    @Override
    public void onRefresh() {
        restartLoader();
        mSwipeLayout.setRefreshing(true);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if(mSwipeLayout != null){
        if (firstVisibleItem == 0)
            mSwipeLayout.setEnabled(true);
        else
            mSwipeLayout.setEnabled(false);
        }
        if (totalItemCount == 0 || mAdapter == null)
            return;
        if (mPreviousTotalCount == totalItemCount)
            return;
        boolean loadMore = firstVisibleItem + visibleItemCount > totalItemCount;
        if (loadMore){
            mPreviousTotalCount  = totalItemCount;
            loadMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
