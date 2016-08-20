package com.lovec.wisdom.fragment;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lovec.wisdom.MainActivity;
import com.lovec.wisdom.R;
import com.lovec.wisdom.base.impl.NewsCenterPager;
import com.lovec.wisdom.domain.NewsMenu;

import java.util.ArrayList;

/**
 * Created by lovec on 2016/8/20.
 */
public class LeftMenuFragment extends BaseFragment {

    @ViewInject(R.id.lv_list)
    private ListView lvlist;
    private ArrayList<NewsMenu.NewsMenuData> mNewsMenuDatas;
    private int mCurrentPos;
    private leftMenuAdapter adapter;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_left_menu, null);
        //ListView viewById = (ListView) view.findViewById(R.id.lv_list);
        ViewUtils.inject(this, view);


        return view;
    }

    @Override
    public void initDate() {

    }

    public void setMenuData(ArrayList<NewsMenu.NewsMenuData> data) {
        //更新界面

        mCurrentPos = 0;//选中的位置归零
        mNewsMenuDatas = data;
        adapter = new leftMenuAdapter();
        lvlist.setAdapter(adapter);

        lvlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentPos = position;
                adapter.notifyDataSetChanged();
                //收起侧边栏
                toggle();
                setCurrentDetailPager(position);
            }
        });
    }

    /*
    * 设置当前菜单详情页
    * */
    private void setCurrentDetailPager(int position) {
        MainActivity mainUi = (MainActivity) mActivity;
        ContentFragment fragment = mainUi.getContentFragment();
        //获取新闻中心
        NewsCenterPager newsCenterPager = fragment.getNewsCenterPager();
        newsCenterPager.setCurrentDetailPager(position);

    }

    /*
    * 打开或者关闭侧边栏
    * */
    private void toggle() {
        MainActivity mainUi = (MainActivity) mActivity;
        SlidingMenu slidingMenu = mainUi.getSlidingMenu();
        slidingMenu.toggle();
    }

    class leftMenuAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mNewsMenuDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mNewsMenuDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(mActivity, R.layout.list_item_left_menu, null);

            TextView tvMenu = (TextView) view.findViewById(R.id.tv_menu);

            NewsMenu.NewsMenuData item = (NewsMenu.NewsMenuData) getItem(position);
            tvMenu.setText(item.title);

            if (position == mCurrentPos) {
                tvMenu.setEnabled(true);
            } else {
                tvMenu.setEnabled(false);
            }

            return view;
        }
    }
}
