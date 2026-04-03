package com.dinhxuanthang.free4g;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WifiManager wifiManager;
    private ListView deviceListView;
    private Button scanBtn, startHotspotBtn;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> deviceList = new ArrayList<>();
    private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                List<ScanResult> results = wifiManager.getScanResults();
                deviceList.clear();
                for (ScanResult result : results) {
                    deviceList.add(result.SSID + " (RSSI:" + result.level + "dBm)");
                }
                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Đã tìm thấy " + deviceList.size() + " thiết bị", Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        deviceListView = findViewById(R.id.deviceListView);
        scanBtn = findViewById(R.id.scanBtn);
        startHotspotBtn = findViewById(R.id.startHotspotBtn);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        deviceListView.setAdapter(adapter);
        scanBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                registerReceiver(wifiScanReceiver, filter);
                wifiManager.startScan();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        });
        startHotspotBtn.setOnClickListener(v -> {
            startService(new Intent(this, HotspotService.class));
            Toast.makeText(this, "Đang kích hoạt Free4G...", Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(wifiScanReceiver, filter);
            wifiManager.startScan();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(wifiScanReceiver); } catch (IllegalArgumentException e) {}
    }
}
