package com.example.blocksubtitle.service;

import android.content.Intent;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.provider.Settings;

import com.example.blocksubtitle.R;
import com.example.blocksubtitle.activity.LauncherActivity;
import com.example.blocksubtitle.util.WindowStateHelper;

/**
 * 快速设置磁贴服务，用于在下拉菜单中添加一个启动应用的快捷方式
 */
public class QuickSettingsTileService extends TileService {
    
    private static final String TAG = "QuickSettingsTile";
    private WindowStateHelper windowStateHelper;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "QuickSettingsTileService created");
        windowStateHelper = new WindowStateHelper(this);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "QuickSettingsTileService bound");
        return super.onBind(intent);
    }
    
    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.d(TAG, "QuickSettingsTileService started listening");
        // 更新磁贴状态
        updateTileState();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.d(TAG, "QuickSettingsTile added to Quick Settings");
        updateTileState();
    }
    
    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d(TAG, "QuickSettingsTileService stopped listening");
    }
    
    @Override
    public void onClick() {
        super.onClick();
        Log.d(TAG, "QuickSettingsTile clicked");
        if (windowStateHelper == null) {
            windowStateHelper = new WindowStateHelper(this);
        }

        boolean isRunning = windowStateHelper.isServiceRunning();
        if (isRunning) {
            Intent stopIntent = new Intent(this, FloatingWindowService.class);
            boolean stopped = stopService(stopIntent);
            if (stopped) {
                windowStateHelper.setServiceRunning(false);
            }
        } else {
            if (Settings.canDrawOverlays(this)) {
                Intent serviceIntent = new Intent(this, FloatingWindowService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
                windowStateHelper.setServiceRunning(true);
            } else {
                // 未授予悬浮窗权限，跳转到授权界面
                Intent intent = new Intent(this, LauncherActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityAndCollapse(intent);
                return;
            }
        }
        updateTileState();
    }
    
    /**
     * 更新磁贴状态
     */
    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;
        if (windowStateHelper == null) {
            windowStateHelper = new WindowStateHelper(this);
        }
        
        boolean isRunning = windowStateHelper.isServiceRunning();
        
        tile.setState(isRunning ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getString(isRunning ? R.string.quick_settings_tile_label_active
                                          : R.string.quick_settings_tile_label_inactive));
        tile.setIcon(Icon.createWithResource(this, isRunning
                ? R.drawable.ic_tile_icon_active
                : R.drawable.ic_tile_icon_inactive));
        tile.updateTile();
    }
}
