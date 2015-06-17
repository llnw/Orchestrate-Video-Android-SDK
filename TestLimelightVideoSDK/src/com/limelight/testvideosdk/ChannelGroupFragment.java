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
import com.limelight.videosdk.model.ChannelGroup;

public class ChannelGroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<ModelHolder>,OnItemClickListener,OnRefreshListener,OnScrollListener{

    private ModelAdapter mAdapter;
    private ListView mListView;
    private ChannelGroupCallback mCallback;
    private static ArrayList<ChannelGroup> mGroups = null;
    private static String mErrorMsg;
    private TextView mTextView;
    private ProgressBar mProgress;
    private ProgressBar mProgressLoad;
    private SwipeRefreshLayout mSwipeLayout;
    private int mPreviousTotalCount = 0;

    public ChannelGroupFragment(ChannelGroupCallback channelGroupCallback) {
        mCallback = channelGroupCallback;
        mAdapter= null;
        mListView= null;
    }

    public interface ChannelGroupCallback{
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
        setEmptyText("No Channel Groups");
        mAdapter =  new ModelAdapter(getActivity());
        setListAdapter(mAdapter);
        setListShown(false);
        getActivity().getSupportLoaderManager().initLoader(10, null, this);
    }

    private void setListShown(boolean b) {
        mProgressLoad.setVisibility(View.GONE);
        if(b){
            //Hide progress
            mProgress.setVisibility(View.GONE);
            //hide text if list exist
            if(mGroups!= null && mGroups.size()>0){
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
        getActivity().getSupportLoaderManager().restartLoader(10, null, this);
    }

    private void loadMore(){
        mProgressLoad.setVisibility(View.VISIBLE);
        Bundle b = new Bundle();
        b.putBoolean("Refresh", true);
        getActivity().getSupportLoaderManager().restartLoader(10, b, this);
    }

    private static class ChannelGroupLoader extends AsyncTaskLoader<ModelHolder> {

        private ModelHolder mHolder;
        private boolean refresh = false;
        private static ContentService mContentService = null;
        private Context mContext;

        public ChannelGroupLoader(Context context, Bundle arg1) {
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
            ArrayList<String> titleList = new ArrayList<String>();
            ArrayList<Uri> urls = new ArrayList<Uri>();
            ArrayList<String> groupId = new ArrayList<String>();
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
                mGroups = mContentService.getAllChannelGroup(refresh);
            } catch (Exception e) {
                mGroups = null;
                mErrorMsg = e.getMessage();
                return mHolder;
            }
            for(ChannelGroup group : mGroups){
                titleList.add(group.mTitle);
                urls.add(group.mThumbnailUrl);
                groupId.add(group.mChannelGroupId);
            }
            mHolder.setTitles(titleList);
            mHolder.setUrls(urls);
            mHolder.setIds(groupId);
            return mHolder;
        }

        @Override
        public void deliverResult(ModelHolder data) {
            super.deliverResult(data);
        }
    }

    @Override
    public Loader<ModelHolder> onCreateLoader(int arg0, Bundle arg1) {
        ChannelGroupLoader channelGroupLoader = new ChannelGroupLoader(ChannelGroupFragment.this.getActivity(),arg1);
        channelGroupLoader.forceLoad();
        return channelGroupLoader;
    }

    @Override
    public void onLoadFinished(Loader<ModelHolder> arg0, ModelHolder arg1) {
        if(mGroups == null){
            mSwipeLayout.setEnabled(true);
            setEmptyText(mErrorMsg);
        }
        else if(mGroups.size()==0){
            mSwipeLayout.setEnabled(true);
            setEmptyText("No Channel Group Found");
        }
        mAdapter.setData(arg1.getTitles(),arg1.getUrls(),arg1.getIds());
        mAdapter.notifyDataSetChanged();
        setListShown(true);
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<ModelHolder> arg0) {
        mAdapter.setData(null,null,null);
        mListView.setAdapter(null);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        if(mGroups!= null && !mGroups.isEmpty()){
            mCallback.callback(mGroups.get(position).mChannelGroupId);
        }
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
            {
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
            mPreviousTotalCount  = totalItemCount;
            loadMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mGroups != null){
            mGroups.clear();
            mGroups = null;
        }
    }
}
