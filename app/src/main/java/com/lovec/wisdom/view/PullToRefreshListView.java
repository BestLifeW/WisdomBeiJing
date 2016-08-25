package com.lovec.wisdom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lovec.wisdom.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lovec on 2016/8/23.
 */
public class PullToRefreshListView extends ListView implements AbsListView.OnScrollListener {


    private static final int STATE_PULL_TO_REFRESH = 1;
    private static final int STATE_RELEASE_TO_REFRESH = 2;
    private static final int STATE_REFRESHING = 3;

    private int mCurrenState = STATE_PULL_TO_REFRESH;//当前的刷新状态

    private View mHeaderView;
    private int measuredHeight;
    private int startY;
    private TextView tvTitle;
    private TextView tvTime;
    private ImageView ivArrow;
    private ProgressBar pbProgress;
    private RotateAnimation animUp;
    private RotateAnimation animDown;
    private View mFooterView;
    private int mFooterViewHeight;
    private boolean isLoadMore;// 标记是否正在加载更多

    public PullToRefreshListView(Context context) {
        super(context);
        initHeaderView();
        initFooterView();
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderView();
        initFooterView();
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initHeaderView();
        initFooterView();
    }

    /*
    *
    * 初始化头布局
    * */
    private void initHeaderView() {
        mHeaderView = View.inflate(getContext(), R.layout.pull_to_refresh_header, null);
        this.addHeaderView(mHeaderView);
        //隐藏头布局

        tvTitle = (TextView) mHeaderView.findViewById(R.id.tv_title);
        tvTime = (TextView) mHeaderView.findViewById(R.id.tv_time);
        ivArrow = (ImageView) mHeaderView.findViewById(R.id.iv_arrow);
        pbProgress = (ProgressBar) mHeaderView.findViewById(R.id.pb_loading);


        mHeaderView.measure(0, 0);
        measuredHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -measuredHeight, 0, 0);
        initAnim();
        setCurrentTime();
    }

    /*
    * 初始化脚布局
    * */
    private void initFooterView() {
        mFooterView = View.inflate(getContext(), R.layout.pull_to_refresh_footer, null);
        this.addFooterView(mFooterView);

        mFooterView.measure(0, 0);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);

        this.setOnScrollListener(this);//设置滑动接听
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (int) ev.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (startY == -1) {
                    startY = (int) ev.getY();
                }
                if (mCurrenState == STATE_REFRESHING) {
                    break;
                }
                int endY = (int) ev.getY();
                int dy = endY - startY;

                int firstVisiblePosition = getFirstVisiblePosition();
                if (dy > 0 && firstVisiblePosition == 0) {
                    int padding = dy - measuredHeight;
                    mHeaderView.setPadding(0, padding, 0, 0);

                    if (padding > 0 && mCurrenState != STATE_RELEASE_TO_REFRESH) {
                        mCurrenState = STATE_RELEASE_TO_REFRESH;
                        refreshState();
                    } else if (padding < 0 && mCurrenState != STATE_PULL_TO_REFRESH) {
                        mCurrenState = STATE_PULL_TO_REFRESH;
                        refreshState();
                    }
                    return true;
                }

                break;

            case MotionEvent.ACTION_UP:

                startY = -1;
                if (mCurrenState == STATE_RELEASE_TO_REFRESH) {
                    mCurrenState = STATE_REFRESHING;
                    refreshState();
                    //完整展示头布局k
                    mHeaderView.setPadding(0, 0, 0, 0);

                    //4进行回调
                    if (mListener != null) {
                        mListener.onRefresh();
                    }

                } else if (mCurrenState == STATE_PULL_TO_REFRESH) {
                    mHeaderView.setPadding(0, -measuredHeight, 0, 0);
                }
                break;

            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    /*
    * 根据当前状态刷新界面
    * */
    private void refreshState() {
        switch (mCurrenState) {
            case STATE_PULL_TO_REFRESH:
                tvTitle.setText("下拉刷新");
                ivArrow.startAnimation(animDown);
                pbProgress.setVisibility(INVISIBLE);
                ivArrow.setVisibility(VISIBLE);
                break;
            case STATE_RELEASE_TO_REFRESH:
                tvTitle.setText("松开刷新");
                ivArrow.startAnimation(animUp);
                pbProgress.setVisibility(INVISIBLE);
                ivArrow.setVisibility(VISIBLE);
                break;
            case STATE_REFRESHING:
                tvTitle.setText("正在刷新...");
                ivArrow.clearAnimation();//清除箭头动画,否则无法隐藏
                pbProgress.setVisibility(VISIBLE);
                ivArrow.setVisibility(INVISIBLE);
                break;
            default:
                break;

        }
    }

    /**
     * 初始化箭头动画
     */
    private void initAnim() {
        animUp = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animUp.setDuration(200);
        animUp.setFillAfter(true);

        animDown = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animDown.setDuration(200);
        animDown.setFillAfter(true);
    }

    public void onRefreshComplete(boolean success) {

        if (!isLoadMore) {

            mHeaderView.setPadding(0, -measuredHeight, 0, 0);
            mCurrenState = STATE_PULL_TO_REFRESH;
            tvTitle.setText("下拉刷新");
            ivArrow.startAnimation(animDown);
            pbProgress.setVisibility(INVISIBLE);
            ivArrow.setVisibility(VISIBLE);

            if (success) {
                setCurrentTime();
            }
        } else {
            mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
            isLoadMore = true;
        }

    }

    /*
    *
    * 设置刷新时间
    * */
    private void setCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(new Date());
        tvTime.setText(time);
    }

    /*
    * 2.暴露接口，设置监听
    *
    * */
    //3，定义成员变量，接收进啊听对象
    private OnRefreshListener mListener;


    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }


    //滑动状态变化
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {// 空闲状态
            int lastVisiblePosition = getLastVisiblePosition();

            if (lastVisiblePosition == getCount() - 1 && !isLoadMore) {// 当前显示的是最后一个item并且没有正在加载更多
                // 到底了
                System.out.println("加载更多...");

                isLoadMore = true;

                mFooterView.setPadding(0, 0, 0, 0);// 显示加载更多的布局

                setSelection(getCount() - 1);// 将listview显示在最后一个item上,
                // 从而加载更多会直接展示出来, 无需手动滑动

                //通知主界面加载下一页数据
                if (mListener != null) {
                    mListener.onLoadMore();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /*
    * 1.下拉刷新的回调接口
    *
    * */
    public interface OnRefreshListener {
        void onRefresh();

        void onLoadMore();
    }
}

