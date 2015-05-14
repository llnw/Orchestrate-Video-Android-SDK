package com.limelight.testvideosdk;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.limelight.videosdk.Constants;
import com.limelight.videosdk.ContentService;
import com.limelight.videosdk.IPlayerControl;
import com.limelight.videosdk.IPlaylistCallback;
import com.limelight.videosdk.PlayerSupportFragment;
import com.limelight.videosdk.ContentService.EncodingsCallback;
import com.limelight.videosdk.model.Encoding;
import com.limelight.videosdk.model.Media;
import com.limelight.testvideosdk.MediaFragment.PlaylistCallback;

public class PlayersFragment extends Fragment implements OnItemClickListener{

    private static final int READ_REQUEST_CODE = 50;
    private IPlayerControl mControl;
    private SearchView mEdit;
    private TextView mLocalfileText = null;
    private String mMediaInfo;
    private PlayerSupportFragment mPlayer;
    private ProgressDialog mProgress = null;
    private ProgressDialog mProgressDialog = null;
    private CheckBox mDeliveryCheck = null;
    private CheckBox mAutoPlayCheck = null;
    private PlaylistAdapter mPlayListAdapter;
    private ArrayList<Media> mPlayList = new ArrayList<Media>();
    private RelativeLayout mPlayListNameLayout;
    private ListView mPlayListView;
    private RelativeLayout mEditLayout;
    private RelativeLayout mPlayLayout;
    private int mCurrentPlayPosition = -1;
    private boolean mIsPlaylistPlaying = false;
    private boolean isChannelPlaylist;
    private PlaylistAdapter mChannelPlayListAdapter;
    private ListView mChannelPlayListView;
    private IPlaylistCallback mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container,false);
        mEdit = (SearchView) rootView.findViewById(R.id.edit);
        mLocalfileText = (TextView) rootView.findViewById(R.id.localFileName);////findViewById
        Button choose = (Button) rootView.findViewById(R.id.choose);
        choose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        mPlayListView = (ListView) rootView.findViewById(R.id.playlist);
        mChannelPlayListView = (ListView) rootView.findViewById(R.id.channelplaylist);
        mChannelPlayListAdapter = new PlaylistAdapter(getActivity(), Constants.TYPE_CHANNEL,null);
        mChannelPlayListAdapter.setData(mPlayList);
        mChannelPlayListView.setAdapter(mChannelPlayListAdapter);
        mChannelPlayListView.setOnItemClickListener(this);
        mPlayListAdapter = new PlaylistAdapter(getActivity(), Constants.TYPE_MEDIA,new PlaylistCallback() {
            @Override
            public void removeFromPlaylist(int position) {
                PlayersFragment.this.removeFromPlaylist(mPlayList.get(position));
                if(mCurrentPlayPosition == position){
                    if(mControl != null){
                        mControl.stop();
                    }
                    if(mPlayList.size() == 0){
                        mCurrentPlayPosition = -1;
                    }
                    else{
                        if(mAutoPlayCheck.isChecked() && (mCurrentPlayPosition +1) < mPlayList.size()){
                            mPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
                            mPlayListAdapter.notifyDataSetChanged();
                            mPlayListView.setSelection(mCurrentPlayPosition);
                            if(mControl != null){
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                                String orgId = preferences.getString(getActivity().getResources().getString(R.string.OrgIDEditPrefKey), null);
                                String accessKey = preferences.getString(getActivity().getResources().getString(R.string.AccKeyEditPrefKey), null);
                                String secret = preferences.getString(getActivity().getResources().getString(R.string.SecKeyEditPrefKey), null);
                                ContentService contentService = new ContentService(getActivity(),orgId,accessKey,secret);
                                mControl.play(mPlayList.get(mCurrentPlayPosition).mMediaID,contentService);
                                showProgress(true, getResources().getString(R.string.progressDlgEncodingMessage));
                            }
                        }
                        else{
                            mPlayListAdapter.setCurrentPlayingPosition(-1);
                            mPlayListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void addToPlaylist(int position) {
            }
        });
        mPlayListAdapter.setData(mPlayList);
        mPlayListView.setAdapter(mPlayListAdapter);
        mPlayListView.setOnItemClickListener(this);
        mPlayListNameLayout = (RelativeLayout)rootView.findViewById(R.id.playListNameLayout);
        mEditLayout = (RelativeLayout)rootView.findViewById(R.id.editLayout);
        mPlayLayout = (RelativeLayout)rootView.findViewById(R.id.playLayout);
        mProgress = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        mProgress.setCanceledOnTouchOutside(false);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMessage("Downloading. Please Wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);

        Button play = (Button) rootView.findViewById(R.id.play);
        play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String orgId = preferences.getString(getActivity().getResources().getString(R.string.OrgIDEditPrefKey), null);
                String accessKey = preferences.getString(getActivity().getResources().getString(R.string.AccKeyEditPrefKey), null);
                String secret = preferences.getString(getActivity().getResources().getString(R.string.SecKeyEditPrefKey), null);
                ContentService contentService = new ContentService(getActivity(),orgId,accessKey,secret);
                play(contentService);
            }
        });
        mDeliveryCheck = (CheckBox)rootView.findViewById(R.id.deliveryCheck);
        mAutoPlayCheck = (CheckBox)rootView.findViewById(R.id.is_autoPlay);
        mAutoPlayCheck.setChecked(true);
        mAutoPlayCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChannelPlaylist){
                    mControl.setAutoPlay(mAutoPlayCheck.isChecked());
                }
            }
        });
        mDeliveryCheck.setChecked(true);
        mEdit.setFocusable(false);
        showKeyboard(false);
        mPlayer = new PlayerSupportFragment();
        getChildFragmentManager().beginTransaction().add(R.id.videoLayout, mPlayer).addToBackStack(null).commitAllowingStateLoss();
        return rootView;
    }

    private void getAllEncodings(String mediaId, final ContentService contentService) {
        if(contentService != null){
            contentService.getAllEncodingsForMediaId(mediaId, new EncodingsCallback() {
                @Override
                public void onError(Throwable throwable) {
                    hide();
                    showProgress(false,null);
                    showMessage(throwable.getMessage());
                }

                @Override
                public void onSuccess(ArrayList<Encoding> encodingList) {
                    showEncodingDialog(encodingList, contentService);
                }
            });
        }
        else{
            showMessage("No Media Library !");
            showProgress(false, null);
        }
    }

    public void play(ContentService svc) {
        //clear playlist start
        if(mIsPlaylistPlaying || isChannelPlaylist){
            mIsPlaylistPlaying = false;
            isChannelPlaylist = false;
            mCurrentPlayPosition = -1;
            for(int i = 0 ; i < mPlayList.size() ;){
                mPlayList.remove(i);
            }
            mPlayListNameLayout.setVisibility(View.GONE);
            mChannelPlayListView.setVisibility(View.GONE);
            mPlayListView.setVisibility(View.GONE);
            mPlayLayout.setVisibility(View.VISIBLE);
            mEditLayout.setVisibility(View.VISIBLE);
        }
        //clear playlist end
        setLocalfileName("");
        showKeyboard(false);
        mEdit.setFocusable(false);
        CharSequence editPath = mEdit.getQuery();
        mMediaInfo = editPath.toString().trim();
        if (mControl != null) {
            mControl.stop();
            hide();
            hideController();
            if(URLUtil.isValidUrl(mMediaInfo)) {
                showProgress(true, "Loading...");
                //mControl.setVideoPath(mMediaInfo);// set the path for player
                mControl.play(mMediaInfo,svc);
                mEdit.setFocusable(false);
                showKeyboard(false);
            }
            else{
                String scheme = Uri.parse(mMediaInfo).getScheme();
                if("RTSP".equalsIgnoreCase(scheme)){
                    mControl.play(mMediaInfo, null);//used with delivery
                }else{
                    if(mDeliveryCheck.isChecked())
                        //This content service object is passed from Media or ALL media tab.
                        mControl.play(mMediaInfo, svc);//used with delivery
                    else{
                        getAllEncodings(mMediaInfo, svc);
                    }
                }
            }
        } else
            Log.e(getActivity().getLocalClassName(), "Control is null");
    }

    public void setEditText(String media){
        mEdit.setQuery(media, false);
    }
    
    public void setLocalfileName(String localFileName){
        if(mLocalfileText != null)
            mLocalfileText.setText(localFileName);
    }

    public void showKeyboard(boolean show) {   
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(show){
                inputManager.showSoftInput(view, InputMethodManager.HIDE_NOT_ALWAYS);
            }else{
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                view.clearFocus();
            }
        }
    }

    public void showProgress(final boolean show, final String strMsg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgress != null)
                    if(show){
                        mProgress.setMessage(strMsg);
                        mProgress.show();
                    }
                    else{
                        mProgress.hide();
                    }
                
            }
        });
    }
    
    public void showProgressDialog(final int percent){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mProgressDialog != null){
                    if(!mProgressDialog.isShowing() && percent==0){
                        mProgressDialog.show();
                    }
                    if(percent != 100){
                        mProgressDialog.setProgress(percent);
                    }else{
                        mProgressDialog.dismiss();
                    }
                }
            }
        });
    }

    public void showMessage(final String msg){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = new Toast(getActivity());
                toast.setGravity(Gravity.TOP, 0, 300);
                toast.setDuration(Toast.LENGTH_LONG);
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.toast, null);
                TextView tv = (TextView)view.findViewById(R.id.text);
                tv.setText(msg);
                toast.setView(view);
                toast.show();
            }
        });
    }

    public void performFileSearch() {
        mEdit.setFocusable(false);
        showKeyboard(false);
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            Intent i = Intent.createChooser(intent, "Pick Video File");
            getActivity().startActivityForResult(i, READ_REQUEST_CODE);
        } else {
            Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            Intent i = Intent.createChooser(intent, "Pick Video File");
            getActivity().startActivityForResult(i, READ_REQUEST_CODE);
        }
    }

    public void setControl(IPlayerControl control) {
        mControl = control;
    }

    public void show() {
        getChildFragmentManager().beginTransaction().show(mPlayer).addToBackStack(null).commitAllowingStateLoss();
    }

    public void hide() {
        getChildFragmentManager().beginTransaction().hide(mPlayer).addToBackStack(null).commitAllowingStateLoss();
    }

    public void hideController(){
        mPlayer.hideMediaController();
    }

    public int getPlayerState(){
        return mPlayer.getCurrentPlayState();
    }

    public void showEncodingDialog(final ArrayList<Encoding> encodings, final ContentService svc){
        final ArrayList<String> list = new ArrayList<String>();
        for(Encoding enc: encodings){
            list.add(enc.primaryUse.name()+" "+enc.mAudioBitRate+" X"+ enc.mVideoBitRate+"kbps"+" "+enc.mHeight+"X"+enc.mWidth);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showKeyboard(false);
                mProgress.hide();
                if(list.size()==0){
                    showMessage("No Valid Encodings Found !");
                    return;
                }
                ArrayAdapter<String> primaryUseAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice,list);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.choose_encoding)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(mControl != null)
                            mControl.stop();
                    }
                })
                .setSingleChoiceItems(primaryUseAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            mProgress.setMessage(getResources().getString(R.string.progressDlgMediaMessage));
                            mProgress.show();
                            Encoding enc = encodings.get(which);
                            if(mControl != null)
                                mControl.play(enc.mEncodingUrl.toString(), svc);
                            dialog.dismiss();
                    }
                });
                AlertDialog encodingDialog = builder.create();
                encodingDialog.show();
            }
        });
    }

    public void addToPlaylist(Media media){
        if(isChannelPlaylist){
            isChannelPlaylist = false;
            for(int i = 0 ; i < mPlayList.size() ;){
                mPlayList.remove(i);
            }
            mCurrentPlayPosition = -1;
            mChannelPlayListView.setVisibility(View.GONE);
        }
        mPlayListNameLayout.setVisibility(View.VISIBLE);
        mPlayListView.setVisibility(View.VISIBLE);
        mPlayLayout.setVisibility(View.GONE);
        mEditLayout.setVisibility(View.GONE);
        mPlayList.add(media);
        mPlayListAdapter.setData(mPlayList);
        mPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
        mPlayListView.setSelection(mCurrentPlayPosition);
        mPlayListAdapter.notifyDataSetChanged();
    }

    public void removeFromPlaylist(Media media){
        mPlayList.remove(media);
        if(mPlayList.size()==0){
            mPlayListNameLayout.setVisibility(View.GONE);
            mPlayListView.setVisibility(View.GONE);
            mPlayLayout.setVisibility(View.VISIBLE);
            mEditLayout.setVisibility(View.VISIBLE);
        }else{
            mPlayListNameLayout.setVisibility(View.VISIBLE);
            mPlayListView.setVisibility(View.VISIBLE);
            mPlayLayout.setVisibility(View.GONE);
            mEditLayout.setVisibility(View.GONE);
        }
        mPlayListAdapter.setData(mPlayList);
        mPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
        mPlayListView.setSelection(mCurrentPlayPosition);
        mPlayListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View item, int position, long arg3) {
        mIsPlaylistPlaying = true;
        switch(arg0.getId()){
        case R.id.playlist:
        mCurrentPlayPosition = position;
        mPlayListAdapter.setCurrentPlayingPosition(position);
        mPlayListAdapter.notifyDataSetChanged();
        mPlayListView.setSelection(mCurrentPlayPosition);
        if(mControl != null){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String orgId = preferences.getString(getActivity().getResources().getString(R.string.OrgIDEditPrefKey), null);
            String accessKey = preferences.getString(getActivity().getResources().getString(R.string.AccKeyEditPrefKey), null);
            String secret = preferences.getString(getActivity().getResources().getString(R.string.SecKeyEditPrefKey), null);
            ContentService contentService = new ContentService(getActivity(),orgId,accessKey,secret);
            mControl.play(mPlayList.get(position).mMediaID,contentService);
            showProgress(true, getResources().getString(R.string.progressDlgEncodingMessage));
        }
        break;
        case R.id.channelplaylist:
            mCurrentPlayPosition = position;
            mChannelPlayListAdapter.setCurrentPlayingPosition(position);
            mChannelPlayListAdapter.notifyDataSetChanged();
            mChannelPlayListView.setSelection(mCurrentPlayPosition);
            if(mControl != null){
                mControl.playInPlaylist(position);
                showProgress(true, getResources().getString(R.string.progressDlgEncodingMessage));
            }
            break;
        }
    }

    public void playCompleted() {
        if(isChannelPlaylist){
            if(mIsPlaylistPlaying){
                if(mControl != null){
                    mCurrentPlayPosition = mControl.getPlaylistPosition();
                    mChannelPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
                    mChannelPlayListAdapter.notifyDataSetChanged();
                    mChannelPlayListView.setSelection(mCurrentPlayPosition);
                }
            }
        }else{
            if(mIsPlaylistPlaying){
                if(mControl != null){
                    if(mAutoPlayCheck.isChecked() && (mCurrentPlayPosition +1) < mPlayList.size()){
                        mCurrentPlayPosition++;
                        mPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
                        mPlayListAdapter.notifyDataSetChanged();
                        mPlayListView.setSelection(mCurrentPlayPosition);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        String orgId = preferences.getString(getActivity().getResources().getString(R.string.OrgIDEditPrefKey), null);
                        String accessKey = preferences.getString(getActivity().getResources().getString(R.string.AccKeyEditPrefKey), null);
                        String secret = preferences.getString(getActivity().getResources().getString(R.string.SecKeyEditPrefKey), null);
                        ContentService contentService = new ContentService(getActivity(),orgId,accessKey,secret);
                        mControl.play(mPlayList.get(mCurrentPlayPosition).mMediaID,contentService);
                        showProgress(true, getResources().getString(R.string.progressDlgEncodingMessage));
                    }
                }
            }
        }
}

    public void playChannel(String channelId) {
        //initial state for channel playlist
        mIsPlaylistPlaying = true;
        isChannelPlaylist = true;
        mCurrentPlayPosition = 0;
        for(int i = 0 ; i < mPlayList.size() ;){
            mPlayList.remove(i);
        }
        mChannelPlayListAdapter.setData(mPlayList);
        mChannelPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
        mChannelPlayListView.setSelection(mCurrentPlayPosition);
        mChannelPlayListAdapter.notifyDataSetChanged();

        mCallback = new IPlaylistCallback() {
            @Override
            public void getChannelPlaylist(final ArrayList<Media> playlist) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPlayListNameLayout.setVisibility(View.VISIBLE);
                        mChannelPlayListView.setVisibility(View.VISIBLE);
                        mPlayListView.setVisibility(View.GONE);
                        mPlayLayout.setVisibility(View.GONE);
                        mEditLayout.setVisibility(View.GONE);
                        mPlayList.addAll(playlist);
                        mChannelPlayListAdapter.setData(mPlayList);
                        mChannelPlayListAdapter.setCurrentPlayingPosition(mCurrentPlayPosition);
                        mChannelPlayListView.setSelection(mCurrentPlayPosition);
                        mChannelPlayListAdapter.notifyDataSetChanged();
                    }
                });
            }
        };

        if(mControl!= null){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String orgId = preferences.getString(getActivity().getResources().getString(R.string.OrgIDEditPrefKey), null);
            String accessKey = preferences.getString(getActivity().getResources().getString(R.string.AccKeyEditPrefKey), null);
            String secret = preferences.getString(getActivity().getResources().getString(R.string.SecKeyEditPrefKey), null);
            ContentService contentService = new ContentService(getActivity(),orgId,accessKey,secret);
            contentService.setPagingParameters(100, Constants.SORT_BY_UPDATE_DATE, Constants.SORT_ORDER_DESC);
            mControl.setAutoPlay(mAutoPlayCheck.isChecked());
            mControl.playChannel(channelId, contentService,mCallback);
        }
    }
}
