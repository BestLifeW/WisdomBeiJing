package com.lovec.wisdom.utils;

import android.content.Context;

/**
 * Created by lovec on 2016/8/20.
 */
public class CacheUtils {


    public static void setCache(String url, String json, Context context) {
        PrefUtils.setString(context, url, json);
    }

    public static String getCache(String url, Context context) {
        String string = PrefUtils.getString(context, url, null);
        return string;
    }
}
