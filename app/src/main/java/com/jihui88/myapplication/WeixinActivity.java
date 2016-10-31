package com.jihui88.myapplication;

import android.app.Activity;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WeixinActivity extends Activity {

    //微信设置
    private static final String APP_ID = "wxd939360915065e46";
    private IWXAPI api;
    private void regToWx(){
        api = WXAPIFactory.createWXAPI(this,APP_ID,true);
        api.registerApp(APP_ID);
    }


}
