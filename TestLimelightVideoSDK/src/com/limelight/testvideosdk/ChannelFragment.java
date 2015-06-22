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
import com.limelight.videosdk.model.Channel;
import com.limelight.testvideosdk.MediaFragment.PlaylistCallback;
/**
 * This class represents the channel fragment page which lists all the available channels.
 *
 */
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
    
/**
 * Constructor.
 * @param channelCallback object to preserve ChannelCallback set by activity.
 */
    public ChannelFragment(ChannelCallback channelCallback) {
        mCallback = channelCallback;
        mAdapter= null;
        mListView= null;
    }

    /**
     * This is callback to communicate with the activity.
     *
     */
    public interface ChannelCallback{
        void callback(String id); 
        void playChannel(String channelId);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
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

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No Channels");
        mAdapter =  new ModelAdapter(getActivity(),Constants.TYPE_CHANNEL,new PlaylistCallback() {
            
            @Override
            public void removeFromPlaylist(int position) {
                //do nothing. cant remove
            }
            
            @Override
            public void addToPlaylist(int position) {
                mCallback.playChannel(mChannels.get(position).mChannelId);
            }
        });
        setListAdapter(mAdapter);
        setListShown(false);
        getActivity().getSupportLoaderManager().initLoader(11, null, this);
    }

    /**
     * Method to show or hide the list control
     */
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
/**
 * Method to set the list adapter
 * @param adapter the ModelAdapter
 */
    private void setListAdapter(ModelAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    private void setEmptyText(String string) {
        mTextView.setText(string);
    }

    /**
     * Method to restart the async task loader
     */
    private void restartLoader(){
        mPreviousTotalCount = 0;
        setListShown(false);
        getActivity().getSupportLoaderManager().restartLoader(11, null, this);
    }

    /**
     * Method to load next set of data by fetching the next page data.
     */
    private void loadMore(){
        mProgressLoad.setVisibility(View.VISIBLE);
        Bundle b = new Bundle();
        b.putBoolean("Refresh", true);
        getActivity().getSupportLoaderManager().restartLoader(11, b, this);
    }
/**
 * This class is to fetch all the data for channel asynchronously.
 *
 */
    private static class ChannelLoader extends AsyncTaskLoader<ModelHolder> {

        private ModelHolder mHolder;
        private boolean refresh = false;
        private static ContentService mContentService = null;
        private Context mContext;

        public ChannelLoader(Context context, Bundle arg1) {
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

        /**
         * (non-Javadoc)
         * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
         * Communicating with the SDK and fetching the channel data.
         */
        @Override
        public ModelHolder loadInBackground() {
            ArrayList<String> titles = new ArrayList<String>();
            ArrayList<Uri> urls = new ArrayList<Uri>();
            ArrayList<String> channelIds = new ArrayList<String>();
            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                String orgId = preferences.getString(mContext.getResources().getString(R.string.OrgIDEditPrefKey), null);
                String accessKey = preferences.getString(mContext.getResources().getString(R.string.AccKeyEditPrefKey), null);
                String secret = preferences.getString(mContext.getResources().getString(R.string.SecKeyEditPrefKey), null);
                if((mContentService.getOrgId().equalsIgnoreCase(orgId) == false) ||
                        (mContentService.getAccessKey().equalsIgnoreCase(accessKey) == false) ||
                        (mContentService.getSecret().equalsIgnoreCase(secret) == false)){
                    mContentService = new ContentService(mContext,orgId,accessKey,secret);
                }
                mContentService.setPagingParameters(50, Constants.SORT_BY_UPDATE_DATE, Constants.SORT_ORDER_DESC);
                mChannels = mContentService.getAllChannel(refresh);
            } catch (Exception e) {
                mChannels= null;
                mErrorMsg = e.getMessage();
                return mHolder;
            }
            for(Channel channel : mChannels){
                titles.add(channel.mTitle);
                urls.add(channel.mThumbnailUrl);
                channelIds.add(channel.mChannelId);
            }
            mHolder.setTitles(titles);
            mHolder.setUrls(urls);
            mHolder.setIds(channelIds);
            return mHolder;
        }

        @Override
        public void deliverResult(ModelHolder data) {
            super.deliverResult(data);
        }
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<ModelHolder> onCreateLoader(int arg0, Bundle arg1) {
        ChannelLoader loader = new ChannelLoader(ChannelFragment.this.getActivity(),arg1);
        loader.forceLoad();
        return loader;
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     * Setting the data to Channel Fragment.
     */
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
        mAdapter.setData(arg1.getTitles(),arg1.getUrls(),arg1.getIds());
        mAdapter.notifyDataSetChanged();
        setListShown(true);
        mSwipeLayout.setRefreshing(false);
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<ModelHolder> arg0) {
        mAdapter.setData(null,null,null);
        mListView.setAdapter(null);
    }

    /**
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        if(mChannels!= null && !mChannels.isEmpty()){
            mCallback.callback(mChannels.get(position).mChannelId);
        }
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh()
     */
    @Override
    public void onRefresh() {
        restartLoader();
        mSwipeLayout.setRefreshing(true);
    }

    /**
     * (non-Javadoc)
     * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
     * Along with scrolling allowing to do refresh the list and fetch the next page data.
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        if(mSwipeLayout != null){
            if (firstVisibleItem == 0)
            {
                //Only if the first cell is visible then only allowing to do a refresh
                View v = mListView.getChildAt(0);
                int offset = (v == null) ? 0 : v.getTop();
                if (offset == 0) {
                    mSwipeLayout.setEnabled(true);
                }else{
                    mSwipeLayout.setEnabled(false);
                }
            }
            else
            {
                mSwipeLayout.setEnabled(false);
            }
        }
        if (totalItemCount == 0 || mAdapter == null)
            return;
        if (mPreviousTotalCount == totalItemCount)
            return;
        boolean loadMore = (firstVisibleItem + visibleItemCount >= totalItemCount);
        if (loadMore){
            //Fetching next page data
            mPreviousTotalCount  = totalItemCount;
            loadMore();
        }
    }

    /**
     * (non-Javadoc)
     * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onDestroyView()
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mChannels != null){
            mChannels.clear();
            mChannels = null;
        }
    }
}
