package com.zcshou.gogogo;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.common.BaiduMapSDKException;

import dagger.hilt.android.HiltAndroidApp;
import tech.jour.template.base.BaseApplication;

@HiltAndroidApp
public class GoApplication extends BaseApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    // 百度地图 7.5 开始，要求必须同意隐私政策，默认为false
    SDKInitializer.setAgreePrivacy(this, true);
    // 百度定位 7.5 开始，要求必须同意隐私政策，默认为false(官方说可以统一为以上接口，但实际测试并不行，定位还是需要单独设置)
    LocationClient.setAgreePrivacy(true);
    try {
      // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
      SDKInitializer.initialize(this);
      SDKInitializer.setCoordType(CoordType.BD09LL);
    } catch (BaiduMapSDKException e) {
    }
  }
}