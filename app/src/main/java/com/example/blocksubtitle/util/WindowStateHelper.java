package com.example.blocksubtitle.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用于保存和恢复悬浮窗状态的工具类。
 */
public class WindowStateHelper {
    private static final String PREFS_NAME = "FloatingWindowPrefs";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_SERVICE_RUNNING = "service_running";

    private SharedPreferences prefs;

    public WindowStateHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveWindowState(int width, int height, int x, int y) {
        prefs.edit()
                .putInt(KEY_WIDTH, width)
                .putInt(KEY_HEIGHT, height)
                .putInt(KEY_X, x)
                .putInt(KEY_Y, y)
                .apply();
    }

    public WindowStateHelper.WindowState loadWindowState() {
        int width = prefs.getInt(KEY_WIDTH, 300); // 默认宽度
        int height = prefs.getInt(KEY_HEIGHT, 300); // 默认高度
        int x = prefs.getInt(KEY_X, 0); // 默认X坐标
        int y = prefs.getInt(KEY_Y, 0); // 默认Y坐标
        return new WindowStateHelper.WindowState(width, height, x, y);
    }

    public void setServiceRunning(boolean isRunning) {
        prefs.edit()
                .putBoolean(KEY_SERVICE_RUNNING, isRunning)
                .apply();
    }

    public boolean isServiceRunning() {
        return prefs.getBoolean(KEY_SERVICE_RUNNING, false);
    }

    public static class WindowState {
        public int width, height, x, y;

        public WindowState(int width, int height, int x, int y) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }
    }
}
