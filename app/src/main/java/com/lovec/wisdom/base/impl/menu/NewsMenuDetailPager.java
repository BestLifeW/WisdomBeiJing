package com.lovec.wisdom.base.impl.menu;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lovec.wisdom.R;
import com.lovec.wisdom.base.BaseMenuDetailPager;
import com.lovec.wisdom.domain.NewsMenu;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;

/**
 * 菜单详情页-新闻
 *
 * @author Kevin
 * @date 2015-10-18
 */
public class NewsMenuDetailPager extends BaseMenuDetailPager {
    @ViewInject(R.id.vp_news_detail)
    private ViewPager mViewPager;
    @ViewInject(R.id.indicator)
    private TabPageIndicator mIndicator;
    private ArrayList<NewsMenu.NewsTabData> mTabDate;
    private ArrayList<TabDetailPager> mPagers;

    public NewsMenuDetailPager(Activity activity, ArrayList<NewsMenu.NewsTabData> children) {
        super(activity);
        mTabDate = children;
    }

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.pager_news_detail, null);
        ViewUtils.inject(this, view);
        return view;
    }

    class NewsMenuDetailAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mPagers.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TabDetailPager pager = mPagers.get(position);
            View view = pager.mRootView;
            container.addView(view);
            pager.initData();
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        //返回指示器标题
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabDate.get(position).title;
        }
    }

    @Override
    public void initData() {
        //初始化标签
        mPagers = new ArrayList<>();
        for (int i = 0; i < mTabDate.size(); i++) {
            TabDetailPager pagers = new TabDetailPager(mActivity, mTabDate.get(i));
            mPagers.add(pagers);
        }
        mViewPager.setAdapter(new NewsMenuDetailAdapter());
        mIndicator.setViewPager(mViewPager);
    }
}
