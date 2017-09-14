package com.study.localpictureutils;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.study.localpictureutils.fragment.PictureListFragment;
import com.study.localpictureutils.fragment.PictureSelectFragment;
import com.study.localpictureutils.model.AlbumInfo;
import com.study.localpictureutils.model.ChageEvent;
import com.study.localpictureutils.model.PhotoInfo;
import com.study.localpictureutils.utils.PermissionCheckUtils;
import com.study.localpictureutils.utils.PickerConfig;
import com.study.localpictureutils.utils.PictureManager;
import com.study.localpictureutils.utils.ScreenUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LocalPictureActivity extends AppCompatActivity implements PictureListFragment.OnPictureListItemClickListener {

    private ImageView ivBack;
    private Toolbar toolbar;
    private PictureListFragment fragment1;
    private PictureSelectFragment fragment2;
    private FrameLayout flContent;
    private FragmentManager manager;
    private int index = -1;
    private ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();
    private ArrayList<PhotoInfo> photoInfoList = new ArrayList<>();
    private final int PREVIEW_REQUEST_CODE = 300;
    private TextView tvNum;
    private TextView tvPreview;
    private TextView tvPreviewSend;
    private List<PhotoInfo> tempList = new ArrayList<>();
    private HashMap<Integer, PhotoInfo> filterMap = new HashMap<>();
    public static final int LOCAL_MULIT_PICTURE = 200;
    private List<PhotoInfo> listItmes;

    public ArrayList<PhotoInfo> getPhotoInfoList() {
        return photoInfoList;
    }

    public TextView getTvNum() {
        return tvNum;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_picture);
        ScreenUtil.init(this);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvNum = (TextView) findViewById(R.id.tv_num);
        flContent = (FrameLayout) findViewById(R.id.fl_content);
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        tvPreviewSend = (TextView) findViewById(R.id.tv_preview_send);

        tvPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LocalPictureActivity.this, PreviewActivity.class);
                //将当前的所有图片传过去
                intent.putExtra("all_pictures", photoInfoList);
                intent.putExtra("select_num", photoInfoList.size());
                startActivityForResult(intent, PREVIEW_REQUEST_CODE);
            }
        });

        tvPreviewSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (photoInfoList.size() == 0) {
                    Toast.makeText(LocalPictureActivity.this, "至少选择一张图片！", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent data = new Intent();
                data.putExtra("photos", photoInfoList);
                setResult(LOCAL_MULIT_PICTURE, data);
                finish();

            }
        });
        int permissionCode = PermissionCheckUtils.checkActivityPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        if (permissionCode == 0) {
            initData();
        } else {
            PermissionCheckUtils.setOnOnWantToOpenPermissionListener(new PermissionCheckUtils.OnWantToOpenPermissionListener() {
                @Override
                public void onWantToOpenPermission() {
                    // 帮跳转到该应用的设置界面，让用户手动授权
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
        }

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                photoInfoList.clear();
                albumInfoList.clear();
            }
        });

        manager = getSupportFragmentManager();
        EventBus.getDefault().register(this);
    }


    /**
     * 利用eventBud接收通知，改变预览按钮的状态
     * EventBus3.0之后的版本，在接收通知的函数上需要添加 @Subscribe的注解
     *
     * @param event
     */
    @Subscribe
    public void onEventMainThread(ChageEvent event) {
        if (event.type == 0) {
            tvPreview.setEnabled(false);
        } else {
            tvPreview.setEnabled(true);
        }
    }

    private void initData() {

        Observable.
                create(new ObservableOnSubscribe<List<AlbumInfo>>() {

                    @Override
                    public void subscribe(@NonNull ObservableEmitter<List<AlbumInfo>> e) throws Exception {
                        PictureManager.getAllMediaThumbnails(LocalPictureActivity.this);
                        List<AlbumInfo> list = PictureManager.getAllMediaPhotos(LocalPictureActivity.this);
                        e.onNext(list);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<AlbumInfo>>() {
                    @Override
                    public void accept(@NonNull List<AlbumInfo> list) throws Exception {
                        index = 0;
                        if (list != null) {
                            albumInfoList = (ArrayList<AlbumInfo>) list;
                        }
                        if (fragment1 == null) {
                            fragment1 = new PictureListFragment();
                            fragment1.setOnPictureListItemClickListener(LocalPictureActivity.this);
                            Bundle args = new Bundle();
                            args.putSerializable("pictures", (Serializable) list);
                            fragment1.setArguments(args);
                            manager.beginTransaction().add(R.id.fl_content, fragment1).commit();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        PickerConfig.checkImageLoaderConfig(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @android.support.annotation.NonNull String[] permissions,
                                           @android.support.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            initData();
        } else {
            Toast.makeText(this, "您已拒绝授权，无法选择图片", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPictureListItemClick(int position) {
        index = 1;
        listItmes = albumInfoList.get(position).getList();
        FragmentTransaction transaction = manager.beginTransaction();
        hideFragment(transaction);
        if (fragment2 == null) {
            fragment2 = new PictureSelectFragment();
            Bundle args = new Bundle();
            args.putSerializable("photos", (Serializable) listItmes);
            fragment2.setArguments(args);
            transaction.add(R.id.fl_content, fragment2);
        } else {
            fragment2.setNewData(listItmes);
            transaction.show(fragment2);
        }

        transaction.commit();
    }

    public void hideFragment(FragmentTransaction transaction) {
        if (fragment1 != null) {
            transaction.hide(fragment1);
        }

        if (fragment2 != null) {
            transaction.hide(fragment2);
        }
    }

    @Override
    public void onBackPressed() {
        if (index == 1) {
            FragmentTransaction transaction = manager.beginTransaction();
            hideFragment(transaction);
            transaction.show(fragment1).commit();
            index = 0;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREVIEW_REQUEST_CODE) {
            ArrayList<PhotoInfo> photoInfoResultList = (ArrayList<PhotoInfo>) data.getSerializableExtra("all_pictures");
            filterMap.clear();
            for (int i = 0; i < photoInfoResultList.size(); i++) {
                PhotoInfo photoInfo = photoInfoList.get(i);
                PhotoInfo photoInfoResult = photoInfoResultList.get(i);
                if (photoInfo.getImageId() == photoInfoResult.getImageId()) {
                    boolean choose = photoInfoResult.isChoose();
                    photoInfo.setChoose(choose);
                    if (choose) {
                        filterMap.put(photoInfo.getImageId(), photoInfo);
                    }
                }
            }
            tempList.clear();
            tempList.addAll(photoInfoList);
            for (int i = 0; i < photoInfoList.size(); i++) {
                PhotoInfo tempInfo = photoInfoList.get(i);
                if (!tempInfo.isChoose()) {
                    tempList.remove(tempInfo);
                } else {
                    filterMap.remove(tempInfo.getImageId());
                }
            }

            Set<Integer> keySet = filterMap.keySet();
            for (Integer id : keySet) {
                tempList.add(filterMap.get(id));
            }
            photoInfoList.clear();
            photoInfoList.addAll(tempList);

            if (photoInfoList.size() > 0) {
                tvNum.setText("已选" + photoInfoList.size() + "张");
            } else {
                tvNum.setText("");
                tvPreview.setEnabled(false);
            }

            if (index != 0) {
                fragment2.refreshAdapter();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
