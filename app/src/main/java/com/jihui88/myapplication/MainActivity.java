package com.jihui88.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.*;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    public final static int FILECHOOSER_RESULTCODE = 1; //图片上传
    private ValueCallback<Uri> mUploadMessage; //图片上传保存

    private LongClickCallBack mCallBack;

    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);//开启Javascript支持
        mWebView.getSettings().setDomStorageEnabled(true);//开启DOM
        mWebView.setWebViewClient(new WebViewClient(){ //处理简单的内容渲染
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  //点击链接
                Log.d("","shouldOverrideUrlLoading->"+url);
                if(url.endsWith(".apk")){ //下载APK文件
                    Uri uri = Uri.parse(url);
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(viewIntent);
                    return true;
                }else if( url.startsWith("http:") || url.startsWith("https:") ) {
                    return false;
                }

                // Otherwise allow the OS to handle things like tel, mailto, etc.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity( intent );
                return true;
            }
        });

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

            //打开相册
            // For Android 3.0-
            /*public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);*/
                //i.setType("*/*");
               /* MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }*/
            // For Android 3.0+
            /*public void openFileChooser( ValueCallback uploadMsg, String acceptType ) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);*/
                //i.setType("*/*");
               /* MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"),FILECHOOSER_RESULTCODE);
            }*/
            //For Android 4.1
            /*public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);*/
                //i.setType("image/*"); //选择的文件类型，例如：image/*表示图片
               /* MainActivity.this.startActivityForResult( Intent.createChooser( i, "文件选择" ), MainActivity.FILECHOOSER_RESULTCODE );
            }*/
            // For Android 5.0+
            /*public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);*/
                //i.setType("*/*");
               /* MainActivity.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
                return true;
            }*/

        });

        //长按识别二维码
        mWebView.setOnLongClickListener( new OnLongClickListener() {
            public boolean onLongClick (View v) {
                WebView.HitTestResult result = ((WebView) v).getHitTestResult() ; //获取所点击的内容
                if ( null != result) {
                    int type = result.getType() ;
                    if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) { //判断被点击的类型为图片
                        //mCallBack.onLongClickCallBack(result.getExtra()); //result.getExtra()图片地址src
                        imgSrc(result.getExtra());
                    }
                }
                return false;
            }
        }) ;

        mWebView.loadUrl("http://m1.jihui88.com/");// 设置域名

    }
    public void imgSrc(String imgUrl){
        //Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
        mCallBack.onLongClickCallBack(imgUrl);
    }

    /**
     * 长按事件回调接  口，传递图片地址
     * @author wes
     */
    public interface LongClickCallBack{
        /**用于传递图片地址*/
        //Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
        void onLongClickCallBack(String imgUrl);
    }






    // 连续点击两次Back键退出程序
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




    /**
     * 　相册图片上传事件
     *   点击“选择文件”按钮时，网页会缓存一个ValueCallback对象，必须触发了该对象的onReceiveValue()方法，WebView才会释放，进而才能再一次的选择文件。
     */
   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==FILECHOOSER_RESULTCODE){
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(data == null ? null : data.getData());
                mUploadMessage = null;
            }
        }
    }*/


}
