package com.jihui88.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.*;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.setWebViewClient(new WebViewClient(){ //处理简单的内容渲染
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if( url.startsWith("http:") || url.startsWith("https:") ) {
                    return false;
                }

                // Otherwise allow the OS to handle things like tel, mailto, etc.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity( intent );
                return true;
            }

        });

        mWebView.getSettings().setJavaScriptEnabled(true);//开启Javascript支持

        //处理复杂的各种内容渲染
        mWebView.setWebChromeClient(new WebChromeClient() {
            //设置响应js 的Alert()函数
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                b.create().show();
                return true;
            }
            //设置响应js 的Confirm()函数
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                b.create().show();
                return true;
            }
            //设置window.open
           /* public WebView newWebView = null;
            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture,Message resultMsg) {
                newWebView= new WebView(view.getContext());
                view.addView(newWebView);
                WebSettings settings =newWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                //这个setWebViewClient要加上，否则window.open弹出浏览器打开。
                newWebView.setWebViewClient(new WebViewClient());
                newWebView.setWebChromeClient(this);

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newWebView);
                resultMsg.sendToTarget();
                return true;
            }
            public void onCloseWindow(WebView view) {
                if(newWebView != null) {
                    newWebView.setVisibility(View.GONE);
                    view.removeView(newWebView);
                }
            }*/



        });

        mWebView.loadUrl("http://m1.jihui88.com/#/");// 设置域名

    }

    // Prevent the back-button from closing the app 连续点击两次Back键退出程序
    long startTime = 0;
    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {//canGoBack来判断是否能回退网页
            mWebView.goBack();
        } else {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime) >= 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                startTime = currentTime;
            } else {
                finish();//退出APP
            }
        }
    }
}
