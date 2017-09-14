package com.study.localpictureutils.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.study.localpictureutils.model.AlbumInfo;
import com.study.localpictureutils.model.PhotoInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/8/25.
 */

public class PictureManager {
    private final static String TAG = "YMH";
    private static final String FILE_PREFIX = "file://";
    private static List<AlbumInfo> albumInfolist = new ArrayList<AlbumInfo>();

    /**
     * 通过内容解析者获取手机本地的图片缩略图信息
     *
     * @param context
     * @return
     */
    @Nullable
    private static Cursor getPictureThumbnailsCursor(Context context) {
        final String[] projection = new String[]{
                MediaStore.Images.Thumbnails._ID,
                MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.DATA};
        final Uri images = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(images, projection, null,
                    null, MediaStore.Images.Thumbnails._ID + " DESC");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getAllMediaThumbnails exception");
        }
        return cursor;
    }

    /**
     * 通过内容解析者获取媒体库中所有图片
     *
     * @param context
     * @return
     */
    private static Cursor getMediaPhotosCursor(final Context context) {
        final Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(images, null, null,
                    null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getAllMediaPhotos exception");
        }
        return cursor;
    }

    /**
     * 判断图片路径是否有效
     *
     * @param filePath
     * @return
     */
    private static boolean isValidImageFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            return true;
        }

        return false;
    }

    /**
     * 将查询到的媒体信息库中的缩略图信息以键值对的形式保存到内存里
     * @param context
     */
    public static void getAllMediaThumbnails(Context context) {
        ThumbnailsUtil.clear();
        Cursor cursorThumb = null;
        try {
            cursorThumb = getPictureThumbnailsCursor(context);
            if (cursorThumb != null && cursorThumb.moveToFirst()) {
                int imageID;
                String imagePath;
                do {
                    imageID = cursorThumb.getInt(cursorThumb.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID));
                    imagePath = cursorThumb.getString(cursorThumb.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                    ThumbnailsUtil.put(imageID, FILE_PREFIX + imagePath);
                } while (cursorThumb.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursorThumb != null) {
                    cursorThumb.close();
                }
            } catch (Exception e) {
            }
        }
    }


    public static List<AlbumInfo> getAllMediaPhotos(Context context) {
        if (albumInfolist == null) {
            albumInfolist = new ArrayList<AlbumInfo>();
        } else {
            albumInfolist.clear();
        }

        Cursor cursorPhotos = null;
        try {
            cursorPhotos = getMediaPhotosCursor(context);
            HashMap<String, AlbumInfo> hash = new HashMap<String, AlbumInfo>();
            AlbumInfo albumInfo = null;
            PhotoInfo photoInfo = null;

            if (cursorPhotos != null && cursorPhotos.moveToFirst()) {
                do {
                    int index = 0;
                    int _id = cursorPhotos.getInt(cursorPhotos.getColumnIndex(MediaStore.Images.Media._ID));
                    String path = cursorPhotos.getString(cursorPhotos.getColumnIndex(MediaStore.Images.Media.DATA));
                    String album = cursorPhotos.getString(cursorPhotos.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    long size = cursorPhotos.getLong(cursorPhotos.getColumnIndex(MediaStore.Images.Media.SIZE));

                    if (!isValidImageFile(path)) {
                        Log.d("PICKER", "it is not a vaild path:" + path);
                        continue;
                    }

                    List<PhotoInfo> photoList = new ArrayList<PhotoInfo>();
                    photoInfo = new PhotoInfo();
                    if (hash.containsKey(album)) {
                        albumInfo = hash.remove(album);
                        if (albumInfolist.contains(albumInfo))
                            index = albumInfolist.indexOf(albumInfo);
                        photoInfo.setImageId(_id);
                        photoInfo.setFilePath(FILE_PREFIX + path);
                        photoInfo.setAbsolutePath(path);
                        photoInfo.setSize(size);
                        albumInfo.getList().add(photoInfo);
                        albumInfolist.set(index, albumInfo);
                        hash.put(album, albumInfo);
                    } else {
                        albumInfo = new AlbumInfo();
                        photoList.clear();
                        photoInfo.setImageId(_id);
                        photoInfo.setFilePath(FILE_PREFIX + path);
                        photoInfo.setAbsolutePath(path);
                        photoInfo.setSize(size);
                        photoList.add(photoInfo);
                        albumInfo.setImageId(_id);
                        albumInfo.setFilePath(FILE_PREFIX + path);
                        albumInfo.setAbsolutePath(path);
                        albumInfo.setAlbumName(album);
                        albumInfo.setList(photoList);
                        albumInfolist.add(albumInfo);
                        hash.put(album, albumInfo);
                    }
                } while (cursorPhotos.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursorPhotos != null) {
                    cursorPhotos.close();
                }
            } catch (Exception e) {
            }
        }

        return albumInfolist;
    }


}
