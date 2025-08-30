package com.example.blocksubtitle.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.example.blocksubtitle.service.FloatingWindowService;

/**
 * 透明启动Activity，用于请求悬浮窗权限并启动服务。
 */
public class LauncherActivity extends Activity {

    private static final String TAG = "LauncherActivity";
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置一个完全透明的ContentView，避免黑屏或白屏
        setContentView(new android.view.View(this));

        if (checkOverlayPermission()) {
            startFloatingWindowService();
        } else {
            requestOverlayPermission();
        }
    }

    /**
     * 检查是否已授予悬浮窗权限
     */
    private boolean checkOverlayPermission() {
        return Settings.canDrawOverlays(this);
    }

    /**
     * 请求悬浮窗权限
     */
    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            if (checkOverlayPermission()) {
                Log.d(TAG, "Overlay permission granted by user.");
                startFloatingWindowService();
            } else {
                Log.w(TAG, "Overlay permission denied by user.");
                // 权限被拒绝，应用无法工作，直接退出
                finish();
            }
        }
    }

    /**
     * 启动前台服务来显示悬浮窗
     */
    private void startFloatingWindowService() {
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        // 启动服务后，将Activity移至后台而不是关闭
        moveTaskToBack(true);
    }
}