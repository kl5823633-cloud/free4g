package com.dinhxuanthang.free4g;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.Method;

public class HotspotService extends Service {
    private WifiManager wifiManager;
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        startHotspot();
        new Thread(() -> {
            while (true) {
                try { Thread.sleep(15000); } catch (InterruptedException e) {}
                try {
                    if (Build.VERSION.SDK_INT < 26) {
                        Method getState = wifiManager.getClass().getMethod("getWifiApState");
                        int state = (int) getState.invoke(wifiManager);
                        if (state != 13) startHotspot();
                    }
                } catch (Exception e) { Log.e("Free4G", "Fix lỗi: " + e.getMessage()); }
            }
        }).start();
        return START_STICKY;
    }
    private void startHotspot() {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                Class<?> callbackClass = Class.forName("android.net.wifi.WifiManager$LocalOnlyHotspotCallback");
                Method method = wifiManager.getClass().getDeclaredMethod("startLocalOnlyHotspot", callbackClass, android.os.Handler.class);
                method.setAccessible(true);
                Object callback = new Object() {
                    public void onStarted(Object reservation) { Log.d("Free4G", "Hotspot 4G đã phát"); }
                    public void onStopped() { startHotspot(); }
                    public void onFailed(int reason) { new android.os.Handler().postDelayed(() -> startHotspot(), 3000); }
                };
                method.invoke(wifiManager, callback, null);
            } else {
                WifiConfiguration config = new WifiConfiguration();
                config.SSID = "Free4G_ByDinhXuanThang";
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method.invoke(wifiManager, config, true);
            }
        } catch (Exception e) { Log.e("Free4G", "Lỗi khởi tạo: " + e.getMessage()); }
    }
}
