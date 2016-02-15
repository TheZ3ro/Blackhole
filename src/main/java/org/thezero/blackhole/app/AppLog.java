package org.thezero.blackhole.app;

import android.util.Log;

public class AppLog{
    private static final String APP_TAG = "Blackhole";

    public static int i(String message){
        return Log.i(APP_TAG,message);
    }

    public static int d(String message){
        return Log.d(APP_TAG,message);
    }
}