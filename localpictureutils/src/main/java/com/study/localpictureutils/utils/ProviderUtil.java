package com.study.localpictureutils.utils;

import android.content.Context;


public class ProviderUtil {

    public static String getFileProviderName(Context context){
        return context.getPackageName()+".provider";
    }
}
