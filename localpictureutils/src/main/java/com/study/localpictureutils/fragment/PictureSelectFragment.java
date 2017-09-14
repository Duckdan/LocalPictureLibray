package com.study.localpictureutils.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.study.localpictureutils.LocalPictureActivity;
import com.study.localpictureutils.PreviewActivity;
import com.study.localpictureutils.R;
import com.study.localpictureutils.adapter.PickerPhotoAdapter;
import com.study.localpictureutils.model.ChageEvent;
import com.study.localpictureutils.model.PhotoInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictureSelectFragment extends Fragment {


    private View view;
    private GridView gd;
    private ArrayList<PhotoInfo> photoInfoList = new ArrayList<>();
    private ArrayList<PhotoInfo> previewList = new ArrayList<>();
    private ArrayList<PhotoInfo> selectedList;
    private TextView tvNum;
    private LocalPictureActivity lpa;
    private final int PREVIEW_REQUEST_CODE = 300;
    private List<PhotoInfo> tempList;
    private HashMap<Integer, PhotoInfo> filterMap = new HashMap<>();
    private PickerPhotoAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_picture_select, container, false);
            lpa = (LocalPictureActivity) getActivity();
            selectedList = lpa.getPhotoInfoList();
            tempList = new ArrayList<>();
            tvNum = lpa.getTvNum();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gd = (GridView) view.findViewById(R.id.picture_select_gridview);
        initAdapter();
    }

    private void initAdapter() {
        Bundle bundle = getArguments();
        ArrayList<PhotoInfo> photos = (ArrayList<PhotoInfo>) bundle.getSerializable("photos");
        if (photos != null) {
            photoInfoList = photos;
        }
        adapter = new PickerPhotoAdapter(getActivity(), photoInfoList, gd, true, selectedList, 9, tvNum);
        gd.setAdapter(adapter);
        initGdItemClickListener();
    }

    public void setNewData(List<PhotoInfo> list) {
        if (list == null) {
            return;
        }
        photoInfoList = (ArrayList<PhotoInfo>) list;
        adapter = new PickerPhotoAdapter(getActivity(), photoInfoList, gd, true, selectedList, 9, tvNum);
        gd.setAdapter(adapter);
    }


    private void initGdItemClickListener() {
        gd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View view1, int position, long id) {
                Intent intent = new Intent(lpa, PreviewActivity.class);
                //将当前的所有图片传过去
                intent.putExtra("all_pictures", photoInfoList);
                intent.putExtra("select_num", selectedList.size());
                intent.putExtra("click_position", position);
                startActivityForResult(intent, PREVIEW_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
            tempList.addAll(selectedList);
            for (int i = 0; i < selectedList.size(); i++) {
                PhotoInfo tempInfo = selectedList.get(i);
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
            selectedList.clear();
            selectedList.addAll(tempList);

            PickerPhotoAdapter adapter = new PickerPhotoAdapter(getActivity(), photoInfoList, gd, true, selectedList, 9, tvNum);
            gd.setAdapter(adapter);

            if (selectedList.size() == 0) {
                EventBus.getDefault().post(new ChageEvent(0));
            }
        }
    }

    public void refreshAdapter() {
        adapter.setHasSelect(selectedList.size());
        adapter.notifyDataSetChanged();
    }
}
