package com.lovec.wisdom;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class NewsDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NewsDetailActivity";
    @ViewInject(R.id.ll_control)
    private LinearLayout llControl;
    @ViewInject(R.id.btn_back)
    private ImageButton btnBack;
    @ViewInject(R.id.btn_textsize)
    private ImageButton btnTextSize;
    @ViewInject(R.id.btn_share)
    private ImageButton btnShare;
    @ViewInject(R.id.btn_menu)
    private ImageButton btnMenu;
    @ViewInject(R.id.wv_news_detail)
    private WebView mWebView;
    @ViewInject(R.id.pb_loading)
    private ProgressBar pbLoading;
    private String mUrl;
    private String mUrl1;
    private String mURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_news_detail);
        ShareSDK.initSDK(this);
        ViewUtils.inject(this);

        llControl.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        btnMenu.setVisibility(View.GONE);


        btnBack.setOnClickListener(this);
        btnTextSize.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        mURL = getIntent().getStringExtra("url");
        mWebView.loadUrl(mURL);
        pbLoading.setVisibility(View.VISIBLE);
        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(true);//显示缩放按钮
        settings.setUseWideViewPort(true);//双击缩放
        settings.setJavaScriptEnabled(true);//打开js支持
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i(TAG, "onPageStarted: 网页开始加载了");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.i(TAG, "onPageFinished: 网页加载结束了");

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);//在跳转链接时，强制本本页跳转对象;
                return super.shouldOverrideUrlLoading(view, url);

            }
        });


        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                Log.i(TAG, "onProgressChanged: 加载" + newProgress + "了");
                if (newProgress == 100) {
                    pbLoading.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_textsize:
                showChooseDialog();
                break;
            case R.id.btn_share:
                showShare();
                break;
            default:
                break;
        }
    }

    private int mTempWhich;
    private int mCurrenWhick = 2;//当前选中的字体大小

    /*
    * 选择字体大小对话框
    * */
    private void showChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NewsDetailActivity.this);
        builder.setTitle("选择字体大小");
        String[] item = {"超大号", "大号", "正常", "小号", "超小号"};
        builder.setSingleChoiceItems(item, mCurrenWhick, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTempWhich = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WebSettings settings = mWebView.getSettings();
                switch (mTempWhich) {
                    case 0://超大字体
                        settings.setTextZoom(22);
                        break;
                    case 1: //大号
                        settings.setTextZoom(18);
                        break;
                    case 2: //正常
                        settings.setTextZoom(16);
                        break;
                    case 3: //小号
                        settings.setTextZoom(12);
                        break;
                    case 4://超小号
                        settings.setTextZoom(10);
                        break;
                    default:
                        break;

                }
                mCurrenWhick = mTempWhich;
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    //分享
    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
//关闭sso授权
        oks.disableSSOWhenAuthorize();

// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle("标题");
// titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl("http://sharesdk.cn");
// text是分享文本，所有平台都需要这个字段
        oks.setText("我是分享文本");
// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
//oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
// url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
// comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
// site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
// siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

// 启动分享GUI
        oks.show(this);
    }
}
