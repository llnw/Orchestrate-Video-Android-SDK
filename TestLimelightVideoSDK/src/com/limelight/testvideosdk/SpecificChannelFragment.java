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
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.limelight.testvideosdk.MediaFragment.PlaylistCallback;
import com.limelight.videosdk.Constants;
import com.limelight.videosdk.ContentService;
import com.limelight.videosdk.model.Media;
import com.limelight.videosdk.model.Media.MediaThumbnail;

/**
 * This class represents the specific channel fragment page, which lists all the media in a specific channel.
 *
 */
public class SpecificChannelFragment extends Fragment implements LoaderManager.LoaderCallbacks<ModelHolder>,OnItemClickListener,OnRefreshListener,OnScrollListener{

    private ModelAdapter mAdapter;
    private ListView mListView;
    private static String mChannelId = null;
    private SpecificChannelCallback mCallback;
    private static ArrayList<Media> mMedias = null;
    private static String mErrorMsg;
    private TextView mTextView;
    private ProgressBar mProgress;
    private ProgressBar mProgressLoad;
    private SwipeRefreshLayout mSwipeLayout;
    private int mPreviousTotalCount = 0;
    private static ContentService mContentService = null;
    private Button mAddAllPlaylist;

    public SpecificChannelFragment(SpecificChannelCallback callback,String id) {
        mCallback = callback;
        mChannelId = id;
        mAdapter= null;
        mListView= null;
    }

    /**
     * This is callback to communicate with the activity.
     *
     */
    public interface SpecificChannelCallback{
        void callback(String id, ContentService svc);
        void addToPlaylist(Media media);
        void removeFromPlaylist(Media media);
    }
    
/**
 * (non-Javadoc)
 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
 */
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container,false);
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
        mAddAllPlaylist = (Button)view.findViewById(R.id.add_all_playlist);
//        mAddAllPlaylist.setVisibility(View.VISIBLE);
        mAddAllPlaylist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMedias != null && mMedias.size() >0)
                {
                    for(int i= 0;i<mMedias.size();i++)
                        mCallback.addToPlaylist(mMedias.get(i));
                    Toast.makeText(getActivity(), "Added To Playlist", 5).show();
                }
            }
        });
        return view;
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No Channel Selected");
        mAdapter = new ModelAdapter(getActivity(),Constants.TYPE_MEDIA,new PlaylistCallback() {
            @Override
            public void addToPlaylist(int position) {
                Toast.makeText(getActivity(), "Added To Playlist", Toast.LENGTH_SHORT).show();
                mCallback.addToPlaylist(mMedias.get(position));
            }

            @Override
            public void removeFromPlaylist(int position) {
                mCallback.removeFromPlaylist(mMedias.get(position));
            }
        });
        setListAdapter(mAdapter);
        setListShown(false);
        if(mChannelId == null){
            setListShown(true);
            mSwipeLayout.setEnabled(true);
        }
        else{
            getActivity().getSupportLoaderManager().initLoader(13, null, this);
        }
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
            if(mMedias!= null && mMedias.size()>0){
                mListView.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
                mAddAllPlaylist.setVisibility(View.VISIBLE);
            }else{
                mListView.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
                mAddAllPlaylist.setVisibility(View.GONE);
            }
        }else{
            //show progress hide text hide list
            mListView.setVisibility(View.GONE);
            mTextView.setVisibility(View.GONE);
            mProgress.setVisibility(View.VISIBLE);
            mAddAllPlaylist.setVisibility(View.GONE);
        }
    }

    /**
     * Method to set the list adapter
     * @param adapter the ModelAdapter
     */
    private void setListAdapter(ModelAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    /**
     * Method to set the text view text.
     * @param string Holds the string data to set to text view.
     */
    private void setEmptyText(String string) {
        mTextView.setText(string);
    }

    /**
     * Method to restart the async task loader
     */
    public void restartLoader(String channelId){
        mPreviousTotalCount = 0;
        mChannelId = channelId;
        setListShown(false);
        mSwipeLayout.setRefreshing(true);
        if(mChannelId == null){
            mSwipeLayout.setEnabled(true);
            mSwipeLayout.setRefreshing(false);
            setEmptyText("No Channel Selected");
            setListShown(true);
        }
        else{
            getActivity().getSupportLoaderManager().restartLoader(13, null, this);
        }
    }

    /**
     * Method to load next set of data by fetching the next page data.
     */
    private void loadMore(){
        mProgressLoad.setVisibility(View.VISIBLE);
        Bundle b = new Bundle();
        b.putBoolean("Refresh", true);
        if(mChannelId != null){
            getActivity().getSupportLoaderManager().restartLoader(13, b, this);
        }
    }

    /**
     * This class is to fetch all the data for channel media asynchronously.
     *
     */
    private static class SpecificMediaLoader extends AsyncTaskLoader<ModelHolder> {

        private ModelHolder mHolder;
        private boolean refresh = false;
        private Context mContext;

        public SpecificMediaLoader(Context context, Bundle arg1) {
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
         * Communicating with the SDK and fetching the media data for a specific channel.
         */
        @Override
        public ModelHolder loadInBackground() {
            ArrayList<String> tilteList = new ArrayList<String>();
            ArrayList<Uri> urls = new ArrayList<Uri>();
            ArrayList<String> mediaIds = new ArrayList<String>();

            try {
                if(mChannelId == null){
                    //mMedias = contentService.getAllMedia(ctx);
                }
                else{
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
                    mMedias = mContentService.getAllMediaOfChannel(mChannelId,refresh);
                }
            } catch (Exception e) {
                mMedias = null;
                mErrorMsg = e.getMessage();
                return mHolder;
            }
            for(int i = 0; i<mMedias.size() ;i++){
                tilteList.add(mMedias.get(i).mTitle);
                MediaThumbnail t = mMedias.get(i).mThumbnail;
                if(t!= null)
                    urls.add((t.mUrl));
                mediaIds.add(mMedias.get(i).mMediaID);
            }
            mHolder.setTitles(tilteList);
            mHolder.setUrls(urls);
            mHolder.setIds(mediaIds);
            return mHolder;
        }

        @Override
        public void deliverResult(ModelHolder data) {
            super.deliverResult(data);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<ModelHolder> onCreateLoader(int arg0, Bundle arg1) {
        SpecificMediaLoader loader = new SpecificMediaLoader(SpecificChannelFragment.this.getActivity(),arg1);
        loader.forceLoad();
        return loader;
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     * Setting the data to specific channel Fragment.
     */
    @Override
    public void onLoadFinished(Loader<ModelHolder> arg0, ModelHolder arg1) {
        if(mMedias == null){
            mSwipeLayout.setEnabled(true);
            setEmptyText(mErrorMsg);
            mSwipeLayout.setRefreshing(false);
            setListShown(true);
        }
        else if(mMedias.size()==0){
            mSwipeLayout.setEnabled(true);
            setEmptyText("No Media Found In This Channel");
            mSwipeLayout.setRefreshing(false);
            setListShown(true);
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
        mAdapter.setData(null, null, null);
        mListView.setAdapter(null);
    }

    /**
     * (non-Javadoc)
     * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
     */
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
      //Always checking whether there is any change in settings, there could be situation where settings changed and fragment page not refreshed.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String orgId = preferences.getString(getActivity().getResources().getString(R.string.OrgIDEditPrefKey), null);
        String accessKey = preferences.getString(getActivity().getResources().getString(R.string.AccKeyEditPrefKey), null);
        String secret = preferences.getString(getActivity().getResources().getString(R.string.SecKeyEditPrefKey), null);
        if((mContentService.getOrgId().equalsIgnoreCase(orgId) == false) ||
                (mContentService.getAccessKey().equalsIgnoreCase(accessKey) == false) ||
                (mContentService.getSecret().equalsIgnoreCase(secret) == false)){
            mContentService = new ContentService(getActivity(),orgId,accessKey,secret);
        }
        if(mMedias!= null && !mMedias.isEmpty()){
            mCallback.callback(mMedias.get(position).mMediaID, mContentService);
        }
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener#onRefresh()
     */
    @Override
    public void onRefresh() {
        restartLoader(mChannelId);
    }

    /**
     * (non-Javadoc)
     * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mMedias != null){
            mMedias.clear();
            mMedias = null;
        }
    }
}
