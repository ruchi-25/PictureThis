package com.aviary.android.picturethis;

import android.app.Application;

import com.aviary.android.feather.sdk.IAviaryClientCredentials;

public class MyApplication extends Application implements IAviaryClientCredentials {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public String getBillingKey() {
        return "";
    }

    @Override
    public String getClientID() {
        return "ae701f9183534ae695659b5d82e177e0";
    }

    @Override
    public String getClientSecret() {
        return "270fae29-b315-489a-a581-02781fdc0403";
    }
}
