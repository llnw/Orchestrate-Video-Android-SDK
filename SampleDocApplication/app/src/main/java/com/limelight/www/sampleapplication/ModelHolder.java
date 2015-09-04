package com.limelight.www.sampleapplication;

import java.util.ArrayList;
import android.net.Uri;

public class ModelHolder {

    ArrayList<String> mTitleList = new ArrayList<String>();
    ArrayList<Uri> mThumbnailUrlList = new ArrayList<Uri>();
    ArrayList<String> mIdList = new ArrayList<String>();

    public void setTitles(ArrayList<String> titleList) {
        mTitleList = titleList;
    }

    public ArrayList<String> getTitles() {
        return mTitleList;
    }

    public void setUrls(ArrayList<Uri> temp) {
        mThumbnailUrlList = temp;
    }
    public ArrayList<Uri> getUrls() {
        return mThumbnailUrlList;
    }
    public void setIds(ArrayList<String> idList) {
        mIdList = idList;
    }
    public ArrayList<String> getIds() {
        return mIdList;
    }
}
