package com.study.localpictureutils.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.study.localpictureutils.LocalPictureActivity;
import com.study.localpictureutils.R;
import com.study.localpictureutils.adapter.PickerAlbumAdapter;
import com.study.localpictureutils.model.AlbumInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PictureListFragment extends Fragment implements ListView.OnItemClickListener {


    private ListView lv;
    private View view;
    private PickerAlbumAdapter adapter;
    private List<AlbumInfo> listPictures = new ArrayList<>();
    private LocalPictureActivity pictureActivity;
    private OnPictureListItemClickListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_picture_list, container, false);
        }
        pictureActivity = (LocalPictureActivity) getActivity();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lv = (ListView) view.findViewById(R.id.lv);
        initAdapter();
    }

    private void initAdapter() {
        Bundle bundle = getArguments();
        ArrayList<AlbumInfo> list = (ArrayList<AlbumInfo>) bundle.getSerializable("pictures");
        if (list != null) {
            listPictures = list;
        }
        adapter = new PickerAlbumAdapter(pictureActivity, listPictures);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> view, View view1, int position, long id) {
        listener.onPictureListItemClick(position);
    }

    public void setOnPictureListItemClickListener(OnPictureListItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnPictureListItemClickListener {
        void onPictureListItemClick(int position);
    }
}
