package com.study.localpictureutils;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.study.localpictureutils.utils.PermissionCheckUtils;
import com.study.localpictureutils.utils.PhotoUtils;

/**
 * Created by Administrator on 2017/9/11.
 */

public class PictureDialog {

    private final Context context;
    private int themeResId = R.style.dialog;
    private final Activity activity;
    public final static int LOCAL_MULTI_CODE = 100;
    public final static int CAMERA_PERMISSION_CODE = 300;
    private PhotoUtils photoUtils;
    private OnResultUriListener listener;
    private Dialog dialog;
    public static int clickIndex = -1;

    /**
     * @param context    上下文实例
     * @param themeResId 对话框的弹出风格，传0是默认从底部弹出对话框
     * @param activity   对话框弹出的宿主Acitivity实例
     */
    public PictureDialog(Context context, int themeResId, Activity activity) {
        this.context = context;
        if (themeResId != 0) {
            this.themeResId = themeResId;
        }
        this.activity = activity;
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri, Bitmap bitmap) {
                if (listener != null) {
                    listener.resultUri(uri, bitmap);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
        PermissionCheckUtils.setOnOnWantToOpenPermissionListener(new PermissionCheckUtils.OnWantToOpenPermissionListener() {
            @Override
            public void onWantToOpenPermission() {
                Toast.makeText(PictureDialog.this.context, "请授予应用读取存储空间和使用相机的权限", Toast.LENGTH_SHORT).show();
            }
        });

        initView();
    }

    private void initView() {
        boolean isExists = PermissionCheckUtils.checkExternalStorageIsExists();
        if (!isExists) {
            Toast.makeText(context, "当前设备的内存开不存在，无法进行图片相关操作！", Toast.LENGTH_SHORT).show();
            return;
        }
        View inflate = View.inflate(context, R.layout.dialog_layout, null);
        dialog = new Dialog(activity, themeResId);

        dialog.setContentView(inflate);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = window.getWindowManager().getDefaultDisplay().getWidth();
        window.setAttributes(layoutParams);
        dialog.findViewById(R.id.tv_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoUtils.selectPicture(activity);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = PermissionCheckUtils.checkActivityPermissions(activity, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
                clickIndex = 1;
                if (size == 0) {
                    photoUtils.takePicture(activity);
                }
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int size = PermissionCheckUtils.checkActivityPermissions(activity, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERMISSION_CODE);
                clickIndex = 2;
                if (size == 0) {
                    jumpToPicture();
                }
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.tv_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    public void jumpToPicture() {
        Intent intent = new Intent(activity, LocalPictureActivity.class);
        activity.startActivityForResult(intent, LOCAL_MULTI_CODE);
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }


    public PhotoUtils getPhotoUtils() {
        return photoUtils;
    }

    public void setOnResultUriListener(OnResultUriListener listener) {
        this.listener = listener;
    }

    public interface OnResultUriListener {
        /**
         * @param uri    当前图片在设备上的uri
         * @param bitmap uri对应的bitmap对象
         */
        void resultUri(Uri uri, Bitmap bitmap);
    }
}
