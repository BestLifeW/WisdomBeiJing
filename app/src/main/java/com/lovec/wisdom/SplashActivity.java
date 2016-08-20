package com.lovec.wisdom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.lovec.wisdom.utils.PrefUtils;

public class SplashActivity extends Activity {

    private RelativeLayout rl_root;
    private ImageView imageView1;
    private static final String TAG = "SplashActivity";
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initView();
        animationView();
    }


    private void animationView() {
        // 旋转动画
        RotateAnimation animRotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animRotate.setDuration(1000);// 动画时间
        animRotate.setFillAfter(true);// 保持动画结束状态

        // 缩放动画
        ScaleAnimation animScale = new ScaleAnimation(0, 1, 0, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animScale.setDuration(1000);
        animScale.setFillAfter(true);// 保持动画结束状态

        // 渐变动画
        AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
        animAlpha.setDuration(2000);// 动画时间
        animAlpha.setFillAfter(true);// 保持动画结束状态

        // 动画集合
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(animRotate);
        set.addAnimation(animScale);
        set.addAnimation(animAlpha);

        imageView1.startAnimation(set);
        rl_root.startAnimation(animAlpha);

        //设置set监听动画
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            //动画结束
            @Override
            public void onAnimationEnd(Animation animation) {

                boolean is_frist_enter = PrefUtils.getBoolen(getApplicationContext(), "is_frist_enter", true);
                if (is_frist_enter) {
                    //跳到新手引导
                    Log.i(TAG, "onAnimationEnd: 第一次进入");

                    intent = new Intent(getApplicationContext(), GuideActivity.class);

                } else {
                    //跳到主页
                    Log.i(TAG, "onAnimationEnd: 不是第一次");
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /*
    * 初始化控件
    * */
    private void initView() {
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);
        imageView1 = (ImageView) findViewById(R.id.imageView1);
    }
}

