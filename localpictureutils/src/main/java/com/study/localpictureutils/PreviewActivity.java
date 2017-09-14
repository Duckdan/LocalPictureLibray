package com.study.localpictureutils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.study.localpictureutils.model.PhotoInfo;
import com.study.localpictureutils.utils.ScreenUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;


public class PreviewActivity extends AppCompatActivity {

    private ViewPager vp;
    private CheckBox ibOriginalImage;
    private TextView tvImageTip;
    private TextView tvPreViewSend;
    private ImageButton ibSelect;
    private ArrayList<PhotoInfo> photoList;
    private int selectNum;
    private int width;
    private int height;
    private int clickPosition;
    private TextView tvNum;
    private PictureAdapter pictureAdapter;
    private int SELECT_RESULT_CODE = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        ScreenUtil.init(this);
        width = ScreenUtil.getDisplayWidth();
        height = ScreenUtil.getDisplayHeight();
        initView();
        initData();
        initListener();
    }




    private void initView() {
        vp = (ViewPager) findViewById(R.id.vp);
        ImageView ivBack = (ImageView) findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ibOriginalImage = (CheckBox) findViewById(R.id.ib_original_image);
        tvImageTip = (TextView) findViewById(R.id.tv_image_tip);
        tvPreViewSend = (TextView) findViewById(R.id.tv_preview_send);
        ibSelect = (ImageButton) findViewById(R.id.ib_select);
        tvNum = (TextView) findViewById(R.id.tv_num);
    }

    private void initData() {
        Intent intent = getIntent();
        photoList = (ArrayList<PhotoInfo>) intent.getSerializableExtra("all_pictures");
        selectNum = intent.getIntExtra("select_num", -1);
        clickPosition = intent.getIntExtra("click_position", 0);
        pictureAdapter = new PictureAdapter();
        vp.setAdapter(pictureAdapter);
        vp.setCurrentItem(clickPosition, false);
        tvNum.setText((clickPosition + 1) + "/" + photoList.size());
        tvPreViewSend.setText("发送（" + selectNum + "）");
        if (photoList.get(clickPosition).isChoose()) {
            ibSelect.setImageResource(R.drawable.picker_image_selected);
        } else {
            ibSelect.setImageResource(R.drawable.picker_preview_unselected);
        }
    }

    private void initListener() {
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvNum.setText((position + 1) + "/" + photoList.size());
                PhotoInfo info = photoList.get(position);

                if (info.isChoose()) {
                    ibSelect.setImageResource(R.drawable.picker_image_selected);
                } else {
                    ibSelect.setImageResource(R.drawable.picker_preview_unselected);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ibOriginalImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                int item = vp.getCurrentItem();
                PhotoInfo info = photoList.get(item);
                String fileSize = Formatter.formatFileSize(PreviewActivity.this, info.getSize());
                if (b) {
                    tvImageTip.setText("发送原图（" + fileSize + "）");
                } else {
                    tvImageTip.setText("发送原图");
                }
            }
        });

        ibSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int item = vp.getCurrentItem();
                PhotoInfo info = photoList.get(item);
                if (info.isChoose()) {
                    selectNum--;
                    info.setChoose(false);
                    ibSelect.setImageResource(R.drawable.picker_preview_unselected);
                } else {
                    if (selectNum < 9) {
                        selectNum++;
                        info.setChoose(true);
                        ibSelect.setImageResource(R.drawable.picker_image_selected);
                    } else {
                        Toast.makeText(PreviewActivity.this, "最多选择九张图片！", Toast.LENGTH_SHORT).show();
                    }
                }
                tvPreViewSend.setText("发送（" + selectNum + "）");
            }
        });
    }


    private class PictureAdapter extends PagerAdapter {
        private float ratio = 0;

        @Override
        public int getCount() {

            return photoList != null ? photoList.size() : 0;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            PhotoInfo info = photoList.get(position);

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(info.getAbsolutePath(), opts);
            int bitmapWidth = opts.outWidth;
            int bitmapHeight = opts.outHeight;
            if (width < bitmapWidth || height < bitmapHeight) {
                ratio = Math.min(width * 1.0f / bitmapWidth, height * 1.0f / bitmapHeight);
            }
            if (ratio > 0) {
                opts.inSampleSize = (int) ratio;
            }
            opts.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(info.getAbsolutePath(), opts);
            ImageView imageView = new ImageView(PreviewActivity.this);
            imageView.setImageBitmap(bitmap);
            ViewPager.LayoutParams params = new ViewPager.LayoutParams();
            params.gravity = Gravity.CENTER;
            container.addView(imageView, params);
            return imageView;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {

            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            container.removeView((View) object);
        }
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("all_pictures", photoList);
        setResult(SELECT_RESULT_CODE, data);
        super.onBackPressed();
    }


}
