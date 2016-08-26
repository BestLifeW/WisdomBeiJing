package com.lovec.wisdom.base.impl.menu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lovec.wisdom.NewsDetailActivity;
import com.lovec.wisdom.R;
import com.lovec.wisdom.base.BaseMenuDetailPager;
import com.lovec.wisdom.domain.NewsMenu;
import com.lovec.wisdom.domain.NewsTabBean;
import com.lovec.wisdom.global.GlobalConstants;
import com.lovec.wisdom.utils.CacheUtils;
import com.lovec.wisdom.utils.PrefUtils;
import com.lovec.wisdom.view.PullToRefreshListView;
import com.lovec.wisdom.view.TopNewsViewPager;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

/**
 * 页签页面对象
 * Created by lovec on 2016/8/23.
 */
public class TabDetailPager extends BaseMenuDetailPager {

    private NewsMenu.NewsTabData mTabData;// 单个页签的网络数据
    // private TextView view;

    @ViewInject(R.id.vp_top_news)
    private TopNewsViewPager mViewPager;

    @ViewInject(R.id.indicator)
    private CirclePageIndicator mIndicator;

    @ViewInject(R.id.tv_title)
    private TextView tvTitle;

    @ViewInject(R.id.lv_list)
    private PullToRefreshListView lvList;

    private String mUrl;
    private Handler mHandler;
    private ArrayList<NewsTabBean.TopNews> mTopNews;
    private ArrayList<NewsTabBean.NewsData> mNewsList;

    private NewsAdapter mNewsAdapter;
    private String mMoreUrl;

    public TabDetailPager(Activity activity, NewsMenu.NewsTabData newsTabData) {
        super(activity);
        mTabData = newsTabData;

        mUrl = GlobalConstants.SERVER_URL + mTabData.url;
    }

    @Override
    public View initView() {
        // 要给帧布局填充布局对象
        // view = new TextView(mActivity);
        //
        // // view.setText(mTabData.title);//此处空指针
        //
        // view.setTextColor(Color.RED);
        // view.setTextSize(22);
        // view.setGravity(Gravity.CENTER);

        View view = View.inflate(mActivity, R.layout.pager_tab_detail, null);
        ViewUtils.inject(this, view);

        // 给listview添加头布局
        View mHeaderView = View.inflate(mActivity, R.layout.list_item_header,
                null);
        ViewUtils.inject(this, mHeaderView);// 此处必须将头布局也注入
        lvList.addHeaderView(mHeaderView);

        //5，前端界面 设置回调
        lvList.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromServer();
            }

            @Override
            public void onLoadMore() {
                if (mMoreUrl != null) {
                    //有下一页
                    getMoreDataFromServer();
                } else {
                    //没有下一页
                    Toast.makeText(mActivity, "没有更多数据了", Toast.LENGTH_LONG).show();
                    lvList.onRefreshComplete(true);
                }
            }
        });

        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                position = position - (lvList.getHeaderViewsCount());
                System.out.print("第" + position + "被点击了");

                NewsTabBean.NewsData news = mNewsList.get(position);
                //read_ids 字段

                String readIds = PrefUtils.getString(mActivity, "read_ids", "");
                if (!readIds.contains(news.id + "")) {
                    readIds = readIds + news.id + ",";
                    PrefUtils.setString(mActivity, "read_ids", readIds);
                }
                TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tvTitle.setTextColor(Color.GRAY);

                Intent intent = new Intent(mActivity, NewsDetailActivity.class);
                intent.putExtra("url", news.url);
                mActivity.startActivity(intent);

            }
        });
        return view;
    }

    /*
    * 加载下一页数据
    * */
    private void getMoreDataFromServer() {
        HttpUtils utils = new HttpUtils();
        utils.send(HttpRequest.HttpMethod.GET, mMoreUrl, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                processData(result, true);


                //收起下拉刷新控件
                lvList.onRefreshComplete(true);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                // 请求失败
                error.printStackTrace();
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                lvList.onRefreshComplete(false);
            }
        });
    }

    @Override
    public void initData() {
        // view.setText(mTabData.title);
        String cache = CacheUtils.getCache(mUrl, mActivity);
        if (!TextUtils.isEmpty(cache)) {
            processData(cache, false);
        }

        getDataFromServer();
    }

    private void getDataFromServer() {
        HttpUtils utils = new HttpUtils();
        utils.send(HttpRequest.HttpMethod.GET, mUrl, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                processData(result, false);

                CacheUtils.setCache(mUrl, result, mActivity);

                //收起下拉刷新控件
                lvList.onRefreshComplete(true);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                // 请求失败
                error.printStackTrace();
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
                lvList.onRefreshComplete(false);
            }
        });
    }

    protected void processData(String result, boolean isMore) {
        Gson gson = new Gson();
        NewsTabBean newsTabBean = gson.fromJson(result, NewsTabBean.class);
        String moreUrl = newsTabBean.data.more;
        if (!TextUtils.isEmpty(moreUrl)) {
            mMoreUrl = GlobalConstants.SERVER_URL + moreUrl;
        } else {
            mMoreUrl = null;
        }

        if (!isMore) {// 头条新闻填充数据
            mTopNews = newsTabBean.data.topnews;
            if (mTopNews != null) {
                mViewPager.setAdapter(new TopNewsAdapter());
                mIndicator.setViewPager(mViewPager);
                mIndicator.setSnap(true);// 快照方式展示

                // 事件要设置给Indicator
                mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                    @Override
                    public void onPageSelected(int position) {
                        // 更新头条新闻标题
                        NewsTabBean.TopNews topNews = mTopNews.get(position);
                        tvTitle.setText(topNews.title);
                    }

                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                                               int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

                // 更新第一个头条新闻标题
                tvTitle.setText(mTopNews.get(0).title);
                mIndicator.onPageSelected(0);// 默认让第一个选中(解决页面销毁后重新初始化时,Indicator仍然保留上次圆点位置的bug)
            }

            // 列表新闻
            mNewsList = newsTabBean.data.news;
            if (mNewsList != null) {
                mNewsAdapter = new NewsAdapter();
                lvList.setAdapter(mNewsAdapter);

            }
            if (mHandler == null) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int currentItem = mViewPager.getCurrentItem();
                        currentItem++;
                        if (currentItem > mTopNews.size() - 1) {
                            currentItem = 0;//到了最后一个页面后，跳到0位置
                        }
                        mViewPager.setCurrentItem(currentItem);
                        mHandler.sendEmptyMessageDelayed(0, 3000);
                    }
                };
                mHandler.sendEmptyMessageDelayed(0, 3000);
            }
        } else {
            //加载更多数据
            ArrayList<NewsTabBean.NewsData> moreNews = newsTabBean.data.news;
            mNewsList.addAll(moreNews);//追加在原来的集合中
            //刷新Listview
            mNewsAdapter.notifyDataSetChanged();

        }

    }


    // 头条新闻数据适配器
    class TopNewsAdapter extends PagerAdapter {

        private BitmapUtils mBitmapUtils;

        public TopNewsAdapter() {
            mBitmapUtils = new BitmapUtils(mActivity);
            mBitmapUtils
                    .configDefaultLoadingImage(R.drawable.topnews_item_default);// 设置加载中的默认图片
        }

        @Override
        public int getCount() {
            return mTopNews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView view = new ImageView(mActivity);
            // view.setImageResource(R.drawable.topnews_item_default);
            view.setScaleType(ImageView.ScaleType.FIT_XY);// 设置图片缩放方式, 宽高填充父控件

            String imageUrl = mTopNews.get(position).topimage;// 图片下载链接

            // 下载图片-将图片设置给imageview-避免内存溢出-缓存
            // BitmapUtils-XUtils
            mBitmapUtils.display(view, imageUrl);

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    class NewsAdapter extends BaseAdapter {

        private BitmapUtils mBitmapUtils;

        public NewsAdapter() {
            mBitmapUtils = new BitmapUtils(mActivity);
            mBitmapUtils.configDefaultLoadingImage(R.drawable.news_pic_default);
        }

        @Override
        public int getCount() {
            return mNewsList.size();
        }

        @Override
        public NewsTabBean.NewsData getItem(int position) {
            return mNewsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mActivity, R.layout.list_item_news,
                        null);
                holder = new ViewHolder();
                holder.ivIcon = (ImageView) convertView
                        .findViewById(R.id.iv_icon);
                holder.tvTitle = (TextView) convertView
                        .findViewById(R.id.tv_title);
                holder.tvDate = (TextView) convertView
                        .findViewById(R.id.tv_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NewsTabBean.NewsData news = getItem(position);
            holder.tvTitle.setText(news.title);
            holder.tvDate.setText(news.pubdate);
            String readIds = PrefUtils.getString(mActivity, "read_ids", "");
            if (readIds.contains(news.id + "")) {
                holder.tvTitle.setTextColor(Color.GRAY);
            } else {
                holder.tvTitle.setTextColor(Color.BLACK);
            }
            mBitmapUtils.display(holder.ivIcon, news.listimage);

            return convertView;
        }

    }

    static class ViewHolder {
        public ImageView ivIcon;
        public TextView tvTitle;
        public TextView tvDate;
    }

}
