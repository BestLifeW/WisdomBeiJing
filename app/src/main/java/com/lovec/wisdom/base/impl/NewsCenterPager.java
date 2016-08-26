package com.lovec.wisdom.base.impl;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lovec.wisdom.MainActivity;
import com.lovec.wisdom.base.BaseMenuDetailPager;
import com.lovec.wisdom.base.BasePager;
import com.lovec.wisdom.base.impl.menu.InteractMenuDetailPager;
import com.lovec.wisdom.base.impl.menu.NewsMenuDetailPager;
import com.lovec.wisdom.base.impl.menu.PhotosMenuDetailPager;
import com.lovec.wisdom.base.impl.menu.TopicMenuDetailPager;
import com.lovec.wisdom.domain.NewsMenu;
import com.lovec.wisdom.fragment.LeftMenuFragment;
import com.lovec.wisdom.global.GlobalConstants;
import com.lovec.wisdom.utils.CacheUtils;

import java.util.ArrayList;

/**
 * 新闻中心
 *
 * @author Kevin
 * @date 2015-10-18
 */
public class NewsCenterPager extends BasePager {
    private static final String TAG = "NewsCenterPager";
    private NewsMenu mNewsData;//
    private ArrayList<BaseMenuDetailPager> mMenuDetailPagers;

    public NewsCenterPager(Activity activity) {
        super(activity);
    }

    @Override
    public void initData() {
        System.out.println("新闻中心初始化啦...");
        tvTitle.setText("新闻中心");

        //先判断有没有缓存，有就加载 ，没有就获取
        String cache = CacheUtils.getCache(GlobalConstants.CATEGORY_URL, mActivity);
        if (!TextUtils.isEmpty(cache)) {
            Log.i(TAG, "initData: 发现缓存");
            processData(cache);
        }
        getDataFromServer();
    }

    /*
    * 从服务器获取数据
    * */
    private void getDataFromServer() {
        HttpUtils utils = new HttpUtils();
        utils.send(HttpRequest.HttpMethod.GET, GlobalConstants.CATEGORY_URL, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                Log.i(TAG, "服务器返回结果: " + result);
                processData(result);
                CacheUtils.setCache(GlobalConstants.CATEGORY_URL, result, mActivity);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Toast.makeText(mActivity, "请求失败,错误码" + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    //解析网络返回的数据
    private void processData(String result) {
        Gson gson = new Gson();
        mNewsData = gson.fromJson(result, NewsMenu.class);
        Log.i(TAG, "processData: " + mNewsData.toString());


        MainActivity mainUI = (MainActivity) mActivity;
        LeftMenuFragment fragment = mainUI.getLeftMenuFragment();
        fragment.setMenuData(mNewsData.data);

        //初始化4个菜
        mMenuDetailPagers = new ArrayList<>();
        mMenuDetailPagers.add(new NewsMenuDetailPager(mActivity, mNewsData.data.get(0).children));
        mMenuDetailPagers.add(new TopicMenuDetailPager(mActivity));
        mMenuDetailPagers.add(new PhotosMenuDetailPager(mActivity, btnPhoto));
        mMenuDetailPagers.add(new InteractMenuDetailPager(mActivity));
        //新闻菜单详情页 设置为默认
        setCurrentDetailPager(0);
    }

    //设置菜单详情页
    public void setCurrentDetailPager(int position) {
        //重新给frameLayout 添加内容
        BaseMenuDetailPager pager = mMenuDetailPagers.get(position);//获取当前应该显示的界面
        View view = pager.mRootView;
        //清除之前所有布局
        flContent.removeAllViews();
        flContent.addView(view);
        pager.initData();
        tvTitle.setText(mNewsData.data.get(position).title);

        // 如果是组图页面, 需要显示切换按钮
        if (pager instanceof PhotosMenuDetailPager) {
            btnPhoto.setVisibility(View.VISIBLE);
        } else {
            // 隐藏切换按钮
            btnPhoto.setVisibility(View.GONE);
        }
    }
}

