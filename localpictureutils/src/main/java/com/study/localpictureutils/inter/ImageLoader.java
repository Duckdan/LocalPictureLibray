package com.study.localpictureutils.inter;

import android.app.Activity;
import android.widget.ImageView;

import java.io.Serializable;

/**
 * 图片加载的接口
 */
public interface ImageLoader extends Serializable {

    void displayImages(Activity activity, String path, ImageView imageView, int width, int height);

    void clearMemoryCache();
}
