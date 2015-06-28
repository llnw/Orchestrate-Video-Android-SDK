package com.example.ll.llsdktest;



        import java.util.ArrayList;
        import com.limelight.videosdk.Constants;
        import com.limelight.videosdk.ContentService;
        import com.limelight.videosdk.model.Media;
        import com.limelight.videosdk.model.Media.MediaThumbnail;
        import android.content.Context;
        import android.net.Uri;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.LoaderManager;
        import android.support.v4.content.AsyncTaskLoader;
        import android.support.v4.content.Loader;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AbsListView;
        import android.widget.AdapterView;
        import android.widget.ListView;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.widget.AbsListView.OnScrollListener;
        import android.widget.AdapterView.OnItemClickListener;

class MediaOfChannel extends Fragment implements LoaderManager.LoaderCallbacks<ModelHolder>,OnItemClickListener,OnScrollListener{
    private static String organizationID = "17fbbde5ce1e43e087bcbfffbf0680ca";
    private static String accessKey = "sB8TWEftj8ZhYXol7pVWFuutJ4M=";
    private static String secretKey = "lR46aeyMmlgTOod17Kl9XqNgaYE=";
    private ListView mListView;
    private TextView mTextView;
    private ProgressBar mProgress;
    private ProgressBar mProgressLoad;
    private static String mChannelId = null;
    private static String mErrorMsg;
    private MediaOfChannelCallback mActivityCallback;
    private int mPreviousTotalCount = 0;
    private static ContentService mContentService = null;
    private static ArrayList<Media> mMedias = null;
    private ListAdapter mAdapter;
    private static final int TYPE_MEDIA = 3;
    public MediaOfChannel(MediaOfChannelCallback callback,String id) {
        mActivityCallback = callback;
        mChannelId = id;
        mAdapter= null;
        mListView= null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media, container,false);

        mListView = (ListView) view.findViewById(android.R.id.list);
        mTextView = (TextView) view.findViewById(android.R.id.empty);
        mProgress = (ProgressBar) view.findViewById(R.id.progress);
        mProgressLoad = (ProgressBar) view.findViewById(R.id.progress_load);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("No Channel Selected");
        mAdapter = new ListAdapter(getActivity(),TYPE_MEDIA);
        mListView.setAdapter(mAdapter);
        setListShown(false);
        if(mChannelId == null){
            setListShown(true);
        }
        else{
            getActivity().getSupportLoaderManager().initLoader(13, null, this);
        }
    }
    public interface MediaOfChannelCallback{
        void playMediaId(String mediaId, ContentService contentservice);
    }

    private void setEmptyText(String string) {
        mTextView.setText(string);
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

    private void loadMore(){
        mProgressLoad.setVisibility(View.VISIBLE);
        Bundle b = new Bundle();
        b.putBoolean("Refresh", true);
        if(mChannelId != null){
            getActivity().getSupportLoaderManager().restartLoader(13, b, this);
        }
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {

    }
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        if((mContentService.getOrgId().equalsIgnoreCase(organizationID) == false) ||
                (mContentService.getAccessKey().equalsIgnoreCase(accessKey) == false) ||
                (mContentService.getSecret().equalsIgnoreCase(secretKey) == false)){
            mContentService = new ContentService(getActivity(),organizationID,accessKey,secretKey);
        }
        if(mMedias!= null && !mMedias.isEmpty()){
            mActivityCallback.playMediaId(mMedias.get(position).mMediaID, mContentService);
        }
    }
    @Override
    public Loader<ModelHolder> onCreateLoader(int arg0, Bundle arg1) {

        MediaOfChannelLoader loader = new MediaOfChannelLoader(MediaOfChannel.this.getActivity(),arg1);
        loader.forceLoad();
        return loader;
    }
    @Override
    public void onLoadFinished(Loader<ModelHolder> arg0, ModelHolder arg1) {

        if(mMedias == null){
            setEmptyText(mErrorMsg);
            setListShown(true);
        }
        else if(mMedias.size()==0){
            setEmptyText("No Media Found In This Channel");
            setListShown(true);
        }
        mAdapter.setData(arg1.getTitles(),arg1.getUrls(),arg1.getIds());
        mAdapter.notifyDataSetChanged();
        setListShown(true);
    }
    @Override
    public void onLoaderReset(Loader<ModelHolder> arg0) {
        mAdapter.setData(null, null, null);
        mListView.setAdapter(null);
    }
    /**
     * This class is to fetch all the media of channel  asynchronously.
     *
     */
    private static class MediaOfChannelLoader extends AsyncTaskLoader<ModelHolder> {

        private ModelHolder mHolder;
        private boolean refresh = false;
        private Context mContext;

        public MediaOfChannelLoader(Context context, Bundle arg1) {
            super(context);
            mContext = context;
            if(arg1!= null){
                refresh = arg1.getBoolean("Refresh", false);
            }
            mHolder = new ModelHolder();
            if(mContentService == null){
                mContentService = new ContentService(mContext,organizationID,accessKey,secretKey);
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
                if(mChannelId != null){
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
}
