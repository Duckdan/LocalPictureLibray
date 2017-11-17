package com.study.localpicturelibray;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.study.localpictureutils.LocalPictureActivity;
import com.study.localpictureutils.PictureDialog;
import com.study.localpictureutils.model.PhotoInfo;
import com.study.localpictureutils.utils.PhotoUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView ivShow;
    private static PictureDialog pictureDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivShow = (ImageView) findViewById(R.id.iv_show);
    }

    /**
     * 弹出底部对话框
     *
     * @param view
     */
    public void showGallery(View view) {
        if (pictureDialog == null) {
            pictureDialog = new PictureDialog(this, 0, this);
            pictureDialog.setOnResultUriListener(new PictureDialog.OnResultUriListener() {
                @Override
                public void resultUri(Uri uri, Bitmap bitmap) {
                    Bitmap bitmapD = BitmapFactory.decodeFile(uri.getPath());
                    Log.d("picture::", uri.getPath() + "===" + bitmap);
                    ivShow.setImageBitmap(bitmap);
                }
            });
        }
        pictureDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PictureDialog.CAMERA_PERMISSION_CODE) {
            if (permissions.length <= 0) {
                return;
            }

            boolean flag = true;
            for (int i = 0; i < grantResults.length; i++) {
                flag = flag & (grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
            if (!flag) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                return;
            }
            if (PictureDialog.clickIndex == 1) {
                pictureDialog.getPhotoUtils().takePicture(this);
            } else if (PictureDialog.clickIndex == 2) {
                pictureDialog.jumpToPicture();
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PictureDialog.LOCAL_MULTI_CODE:
                if (resultCode == LocalPictureActivity.LOCAL_MULIT_PICTURE) {
                    ArrayList<PhotoInfo> photoLists = (ArrayList<PhotoInfo>) data.getSerializableExtra("photos");
                    Log.d("picture::", photoLists.size() + "====");
                }
                break;
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                if (pictureDialog != null) {
                    pictureDialog.getPhotoUtils().onActivityResult(this, requestCode, resultCode, data);
                }
                break;
        }
    }
}
