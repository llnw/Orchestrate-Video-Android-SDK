package com.limelight.testvideosdk;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import com.limelight.videosdk.model.Channel;

public class ChannelFragment extends Fragment implements LoaderManager.LoaderCallbacks<ModelHolder>,OnItemClickListener,OnRefreshListener,OnScrollListener{

    private ModelAdapter mAdapter;
    private ListView mListView;
    private ChannelCallback mCallback;
    private static ArrayList<Channel> mChannels = null;
    private static String mErrorMsg;
    private TextView mTextView;
    private ProgressBar mProgress;
    private ProgressBar mProgressLoad;
    private SwipeRefreshLayout mSwipeLayout;
    private int mPreviousTotalCount = 0;

    public ChannelFragment(ChannelCallback channelCallback) {
        mCallback = channelCallback;
        mAdapter= null;
        mListView= null;
    }

    public interface ChannelCallback{
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
        setEmptyText("No Channels");
        mAdapter =  new ModelAdapter(getActivity());
        setListAdapter(mAdapter);
        setListShown(false);
        getActivity().getSupportLoaderManager().initLoader(11, null, this);
    }

    private void setListShown(boolean b) {
        mProgressLoad.setVisibility(View.GONE);
        if(b){
            //Hide progress
            mProgress.setVisibility(View.GONE);
            //hide text if list exist
            if(mChannels!= null && mChannels.size()>0){
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
        getActivity().getSupportLoaderManager().restartLoader(11, null, this);
    }

    private void loadMore(){
        mProgressLoad.setVisibility(View.VISIBLE);
        Bundle b = new Bundle();
        b.putBoolean("Refresh", true);
        getActivity().getSupportLoaderManager().restartLoader(11, b, this);
    }

    private static class ChannelLoader extends AsyncTaskLoader<ModelHolder> {

        ModelHolder mHolder;
        Context ctx;
        boolean refresh = false;
        public ChannelLoader(Context context, Bundle arg1) {
            super(context);
            ctx = context;
            if(arg1!= null){
                refresh = arg1.getBoolean("Refresh", false);
            }
            mHolder = new ModelHolder();
        }

        @Override
        public ModelHolder loadInBackground() {
            ArrayList<String> data = new ArrayList<String>();
            ArrayList<Uri> urls = new ArrayList<Uri>();
            ContentService contentService = new ContentService(ctx);
            try {
                contentService.setPagingParameters(100, Constants.SORT_BY_UPDATE_DATE, Constants.SORT_ORDER_ASC);
                mChannels = contentService.getAllChannel(refresh);
            } catch (Exception e) {
                mChannels= null;
                mErrorMsg = e.getMessage();
                return mHolder;
            }
            for(Channel channel : mChannels){
                data.add(channel.mTitle);
                urls.add(channel.mThumbnailUrl);
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
        ChannelLoader loader = new ChannelLoader(ChannelFragment.this.getActivity(),arg1);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ModelHolder> arg0, ModelHolder arg1) {
        if(mChannels == null){
            mSwipeLayout.setEnabled(true);
            setEmptyText(mErrorMsg);
        }
        else if(mChannels.size()==0){
            mSwipeLayout.setEnabled(true);
            setEmptyText("No Channel Found");
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
        mCallback.callback(mChannels.get(position).mChannelId);
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
        boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
        if (loadMore){
            mPreviousTotalCount  = totalItemCount;
            loadMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }
}
