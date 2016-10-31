package com.jihui88.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * @author wes
 *  功能描述：欢迎界面
 */
public class WelcomeActivity extends Activity implements  Runnable{

    //true第一次使用 false第二次
    private boolean isFirstUse;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        /**
         * 启动一个延迟线程
         */
        new Thread(this).start();
    }

    public void run() {
        try {
            /**
             * 延迟两秒时间
             */
            Thread.sleep(5000);

            //读取SharedPreferences中需要的数据
            SharedPreferences preferences = getSharedPreferences("isFirstUse", Context.MODE_WORLD_READABLE);

            isFirstUse = preferences.getBoolean("isFirstUse", true);

            /**
             *如果用户不是第一次(true)使用则直接调转到显示主界面
             */
            if (!isFirstUse) {
                startActivity(new Intent(this, MainActivity.class));//跳转到主页
            }

            //实例化Editor对象
            SharedPreferences.Editor editor = preferences.edit();
            //存入数据
            editor.putBoolean("isFirstUse", false);
            //提交当前数据
            editor.commit();


        } catch (InterruptedException e){

        }

    }
}