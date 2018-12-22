package com.steps.targe;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Arka Bhowmik on 7/31/2016.
 */
public class Targe extends Application  {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    SharedPreferences mPrefs;
    @Override
    public void onCreate() {
        super.onCreate();

        Context mContext = this.getApplicationContext();
        //0 = mode private. only this app can read these preferences
        mPrefs = mContext.getSharedPreferences("myAppPrefs", 0);


        // the rest of your app initialization code goes here
    }
    public boolean getFirstRun() {
        return mPrefs.getBoolean("firstRun", true);
    }

    public void setRunned() {
        SharedPreferences.Editor edit = mPrefs.edit();
        edit.putBoolean("firstRun", false);
        edit.apply();
    }
}
