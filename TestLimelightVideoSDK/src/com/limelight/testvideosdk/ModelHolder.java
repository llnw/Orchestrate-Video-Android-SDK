package com.limelight.testvideosdk;

import java.util.ArrayList;

import android.net.Uri;

public class ModelHolder {

    ArrayList<String> mListStrings = new ArrayList<String>();
    ArrayList<Uri> mListThumbUrls = new ArrayList<Uri>();
    public void setData(ArrayList<String> temp) {
        mListStrings = temp;
    }

    public ArrayList<String> getData() {
        return mListStrings;
    }
    public void setUrls(ArrayList<Uri> temp) {
        mListThumbUrls = temp;
    }

    public ArrayList<Uri> getUrls() {
        return mListThumbUrls;
    }
}