package com.jihui88.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.zxing.Result;
import com.jihui88.myapplication.widget.CustomDialog;
import com.jihui88.myapplication.widget.CustomWebView;
import com.jihui88.myapplication.widget.CustomWebView.LongClickCallBack;
import com.jihui88.myapplication.zxing.DecodeImage;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.platformtools.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;


@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity implements LongClickCallBack {
    private CustomWebView mCustomWebView;
    private CustomDialog mCustomDialog;
    private ArrayAdapter<String> adapter;
    private boolean isQR;//判断是否为二维码
    private Result result;//二维码解析结果
    private String url;
    private File file;

    //微信设置
    private static final String APP_ID = "wxd939360915065e46";
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWebView();
        regToWx();

        //android3.0以上凡是涉及到网络，下载等耗时操作，都不能在主线程中运行，加上以下代码，可以取消严格限制
        StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private void initWebView() {
        // 初始WebView化控件
        mCustomWebView = new CustomWebView(this, this);
        mCustomWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  //点击链接
                Log.d("A链接", "shouldOverrideUrlLoading->" + url);
                if (url.endsWith("WeChatLogin")){
                    onWeChatLogin();
                    return true;
                } else if (url.endsWith(".apk")) { //下载APK文件
                    Uri uri = Uri.parse(url);
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(viewIntent);
                    return true;
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }

                // Otherwise allow the OS to handle things like tel, mailto, etc.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });

        //处理复杂的各种内容渲染
        mCustomWebView.setWebChromeClient(new WebChromeClient() {
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
        });
        mCustomWebView.loadUrl("http://app.jihui88.com/");// 设置域名
        mCustomWebView.setFocusable(true);
        mCustomWebView.setFocusableInTouchMode(true);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addContentView(mCustomWebView, lp);
    }

    //微信注册
    private void regToWx(){
        api = WXAPIFactory.createWXAPI(this,APP_ID,true);
        api.registerApp(APP_ID);
    }

    // 连续点击两次Back键退出程序
    long startTime = 0;
    @Override
    public void onBackPressed() {
        if (mCustomWebView.canGoBack()) {//canGoBack来判断是否能回退网页
            mCustomWebView.goBack();
        } else {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - startTime) >= 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                startTime = currentTime;
            } else {
                finish();//退出APP 当前Activity
            }
        }
    }


    // 微信登录
    public void  onWeChatLogin() {
        // send oauth request
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";  //授权读取用户信息
        req.state = "wechat_sdk_demo_test";  //自定义信息
        api.sendReq(req);  //向微信发送请求
    }

    // 长按图片事件
    @Override
    public void onLongClickCallBack(final String imgUrl) {
        url = imgUrl;
        // 获取到图片地址后做相应的处理
        MyAsyncTask mTask = new MyAsyncTask();
        mTask.execute(imgUrl);
        showDialog();
    }

    /**
     * 判断是否为二维码
     * param url 图片地址
     * return
     */
    private boolean decodeImage(String sUrl) {
        result = DecodeImage.handleQRCodeFormBitmap(getBitmap(sUrl));
        if (result == null) {
            isQR = false;
        } else {
            isQR = true;
        }
        return isQR;
    }


    public class MyAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (isQR) {
                handler.sendEmptyMessage(0);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            decodeImage(params[0]);
            return null;
        }
    }

    /**
     * 根据地址获取网络图片
     *
     * @param sUrl 图片地址
     * @return
     * @throws IOException
     */
    public Bitmap getBitmap(String sUrl) {
        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                saveMyBitmap(bitmap, "code");//先把bitmap生成jpg图片
                return bitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 显示Dialog
     * param v
     */
    private void showDialog() {
        initAdapter();
        mCustomDialog = new CustomDialog(this) {
            @Override
            public void initViews() {
                // 初始CustomDialog化控件
                ListView mListView = (ListView) findViewById(R.id.lv_dialog);
                mListView.setAdapter(adapter);
                mListView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // 点击事件
                        switch (position) {
                            case 0:
                                sendToFriends();//把图片发送给好友
                                closeDialog();
                                break;
                            case 1:
                                wechatShare(0);
                                closeDialog();
                                break;
                            case 2:
                                wechatShare(1);
                                closeDialog();
                                break;
                            case 3:
                                saveImageToGallery(MainActivity.this);
                                closeDialog();
                                break;
                            case 4:
                                copyWebsite();
                                closeDialog();
                                break;
                            case 5:
                                goIntent();
                                closeDialog();
                                break;
                        }
                    }
                });
            }
        };
        mCustomDialog.show();
    }

    /**
     * 初始化数据
     */
    private void initAdapter() {
        adapter = new ArrayAdapter<String>(this, R.layout.item_dialog);
        adapter.add("发送给朋友");
        adapter.add("分享到微信好友");
        adapter.add("分享到微信朋友圈");
        adapter.add("保存到手机");
        //adapter.add("复制地址");
    }

    /**
     * 是二维码时，才添加为识别二维码
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (isQR) {
                    adapter.add("识别图中二维码");
                }
                adapter.notifyDataSetChanged();
            }
        }

        ;
    };

    /**
     * 发送给好友
     */
    private void sendToFriends() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri imageUri = Uri.parse(file.getAbsolutePath());
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }

    /**
     * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码）
     * @param flag(0:分享到微信好友，1：分享到微信朋友圈)
     */

    private void wechatShare(int flag){
        WXWebpageObject webpage = new WXWebpageObject();
        WXMediaMessage msg = new WXMediaMessage(webpage);
        if (isQR) {
            webpage.webpageUrl = result.toString();//分享链接
            if("".equals(getUrlParam("title"))){
                msg.title = result.toString();//分享标题
            }else{
                msg.title = getUrlParam("title");
                msg.description = result.toString();
            }
        } else {
            webpage.webpageUrl = url;
            msg.title = url;
        }
        // msg.description = context; // 分享内容
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ico); //这里替换一张自己工程里的图片资源 方法一
        //msg.setThumbImage(bitmap);
        ////利用HttpURLConnection对象,我们可以从网络中获取网页数据. 方法二
        Bitmap bitmap = null;
        URL urlImg = null;
        try {
            if("".equals(getUrlParam("pic"))){
                urlImg = new URL(url);
            }else{
                urlImg = new URL(getUrlParam("pic"));
            }
            HttpURLConnection conn = (HttpURLConnection)urlImg.openConnection();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            //bitmap = BitmapFactory.decodeStream((new URL(url).openStream()));
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        bitmap=Bitmap.createScaledBitmap(bitmap, 120, 120, true);//设置缩略图大小
        msg.thumbData = Util.bmpToByteArray(bitmap, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = flag==0?SendMessageToWX.Req.WXSceneSession:SendMessageToWX.Req.WXSceneTimeline;
        api.sendReq(req);
    }
    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 获取url 参数
     *
     */
    public String getUrlParam(String name) {
        String val ="" ;
        if(url.indexOf("?") > -1){
            String[] parm= url.split("\\?")[1].split("&");
            for (int j = 0; j<parm.length; j++){
                if(parm[j].split("=")[0].equals(name)){
                    try {
                        val = URLDecoder.decode(parm[j].split("=")[1], "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return val;
    }




    /**
     * bitmap 保存为jpg 图片
     *
     * @param mBitmap 图片源
     * @param bitName 图片名
     */
    public void saveMyBitmap(Bitmap mBitmap, String bitName) {
        //file= new File( Environment.getExternalStorageDirectory()+"/"+bitName + ".jpg");
        File appDir = new File(Environment.getExternalStorageDirectory(), "机汇网");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        file = new File(appDir, fileName);

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 先保存到本地再广播到图库
     */
    public void saveImageToGallery(Context context) {
        String fileName = System.currentTimeMillis() + ".jpg";
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
            Toast.makeText(MainActivity.this, "成功保存到相册", Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void copyWebsite() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        if (isQR) {
            cm.setText(result.toString().trim());
        } else {
            cm.setText(url);
        }
        Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_LONG).show();
    }

    public void goIntent() {
        Uri uri = Uri.parse(result.toString() + "?debug=01");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }


}
