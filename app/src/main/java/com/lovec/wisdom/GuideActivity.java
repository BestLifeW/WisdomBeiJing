package com.lovec.wisdom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lovec.wisdom.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {

    private ViewPager vp_guide;
    private int[] imagesID = {R.drawable.guide_1, R.drawable.guide_2, R.drawable.guide_3};
    private List<ImageView> mImageViewsList;
    private LinearLayout ll_container;
    private ImageView iv_red_point;
    private static final String TAG = "GuideActivity";
    private int mPointDis;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        initView();
        initData();
        vp_guide.setAdapter(new GuideAdapter());
        vp_guide.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //滑动过程中的回调事件
                int leftMargin = (int) (mPointDis * positionOffset) + position
                        * mPointDis;// 计算小红点当前的左边距
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) iv_red_point
                        .getLayoutParams();
                params.leftMargin = leftMargin;// 修改左边距

                // 重新设置布局参数
                iv_red_point.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected: 当前位置是" + position);
                if (position == mImageViewsList.size() - 1) {
                    Log.i(TAG, "onPageSelected: 显示");
                    btn_start.setVisibility(View.VISIBLE);
                } else {
                    Log.i(TAG, "onPageSelected: 隐藏");
                    btn_start.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        iv_red_point.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                iv_red_point.getViewTreeObserver()
                        .removeGlobalOnLayoutListener(this);
                mPointDis = ll_container.getChildAt(1).getLeft() - ll_container.getChildAt(0).getLeft();
                System.out.println("圆点距离:" + mPointDis);
            }
        });


        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.setBoolen(getApplicationContext(), "is_frist_enter", false);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*
    * 初始化数据
    * */
    private void initData() {
        ImageView imageView;
        ImageView ivRedPoint;
        mImageViewsList = new ArrayList<ImageView>();
        for (int i = 0; i < imagesID.length; i++) {
            imageView = new ImageView(getApplicationContext());
            imageView.setBackgroundResource(imagesID[i]);//填充背景
            mImageViewsList.add(imageView);

            //初始化小圆点
            ivRedPoint = new ImageView(getApplicationContext());
            ivRedPoint.setImageResource(R.drawable.shape_point_gray);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.leftMargin = 10;
            }
            ll_container.addView(ivRedPoint, params);
        }

    }

    /*
    * 初始化
    * */
    private void initView() {
        vp_guide = (ViewPager) findViewById(R.id.vp_guide);
        ll_container = (LinearLayout) findViewById(R.id.ll_container);
        iv_red_point = (ImageView) findViewById(R.id.iv_red_point);
        btn_start = (Button) findViewById(R.id.btn_start);
    }

    class GuideAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mImageViewsList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView view = mImageViewsList.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
