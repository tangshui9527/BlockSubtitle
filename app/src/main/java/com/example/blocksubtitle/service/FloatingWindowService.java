package com.example.blocksubtitle.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.example.blocksubtitle.R;
import com.example.blocksubtitle.util.WindowStateHelper;

/**
 * 前台服务，用于管理悬浮窗的生命周期。
 */
public class FloatingWindowService extends Service {

    private static final String TAG = "FloatingWindowService";
    private static final String CHANNEL_ID = "FloatingWindowChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_NAME = "悬浮窗服务";

    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    
    // 手势检测器
    private GestureDetector gestureDetector;
    
    // 状态保存助手
    private WindowStateHelper windowStateHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        windowStateHelper = new WindowStateHelper(this);
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification(), 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        createFloatingView();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        return START_NOT_STICKY; // 不自动重启服务
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不提供绑定
    }

    /**
     * 创建通知渠道 (Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW // 低优先级，不发出声音
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    /**
     * 创建前台服务所需的通知
     */
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text));
        
        // 尝试设置一个默认的图标
        try {
            // 使用系统默认的通知图标
            builder.setSmallIcon(android.R.drawable.ic_dialog_info);
        } catch (Exception e) {
            Log.w(TAG, "Failed to set notification icon", e);
        }
        
        return builder.build();
    }

    /**
     * 创建并添加悬浮窗视图
     */
    private void createFloatingView() {
        Log.d(TAG, "Creating floating view");
        
        // 检查windowManager是否为空
        if (windowManager == null) {
            Log.e(TAG, "WindowManager is null");
            return;
        }
        
        // 检查LayoutInflater是否能正确加载布局
        try {
            floatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);
            Log.d(TAG, "Inflated floating view");
        } catch (Exception e) {
            Log.e(TAG, "Failed to inflate floating view", e);
            return;
        }

        // 从 SharedPreferences 加载保存的状态
        WindowStateHelper.WindowState state = windowStateHelper.loadWindowState();
        Log.d(TAG, "Loaded window state: width=" + state.width + ", height=" + state.height + 
                ", x=" + state.x + ", y=" + state.y);
        
        // 设置 WindowManager.LayoutParams
        params = new WindowManager.LayoutParams(
                state.width, // 使用保存的宽度
                state.height, // 使用保存的高度
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START; // 使用绝对坐标
        params.x = state.x; // 使用保存的X坐标
        params.y = state.y; // 使用保存的Y坐标
        
        Log.d(TAG, "Created layout params with type: " + params.type);

        // 添加视图到窗口
        try {
            windowManager.addView(floatingView, params);
            Log.d(TAG, "Added view to window manager successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to add view to window manager. Error: " + e.getMessage(), e);
            // 尝试使用不同的窗口类型
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
            try {
                windowManager.addView(floatingView, params);
                Log.d(TAG, "Added view to window manager with TYPE_PHONE successfully");
            } catch (Exception e2) {
                Log.e(TAG, "Failed to add view to window manager even with TYPE_PHONE. Error: " + e2.getMessage(), e2);
            }
        }

        // 设置触摸监听器以实现移动、缩放和关闭
        if (floatingView != null) {
            setupTouchListener();
        } else {
            Log.e(TAG, "Floating view is null, cannot set touch listener");
        }
    }

    /**
     * 定义一个枚举来表示当前的触摸状态
     */
    private enum TouchState {
        NONE,       // 无操作
        MOVING,     // 移动窗口
        RESIZING_LEFT, RESIZING_TOP, RESIZING_RIGHT, RESIZING_BOTTOM,
        RESIZING_TOP_LEFT, RESIZING_TOP_RIGHT, RESIZING_BOTTOM_LEFT, RESIZING_BOTTOM_RIGHT
    }

    /**
     * 为悬浮窗设置触摸监听器
     */
    private void setupTouchListener() {
        // 初始化 GestureDetector 用于双击
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "Double tap detected, stopping service");
                stopSelf(); // 双击时停止服务
                return true;
            }
        });
        
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private int initialWidth;
            private int initialHeight;
            private TouchState currentTouchState = TouchState.NONE;
            
            // 定义边缘热区的大小，例如 30dp
            private final int handleSizeInDp = 30;
            private int handleSizeInPx = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 处理双击手势
                gestureDetector.onTouchEvent(event);
                
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录初始触摸坐标和窗口参数
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        initialWidth = params.width;
                        initialHeight = params.height;

                        // 将 dp 转换为 px (只需做一次)
                        if (handleSizeInPx == 0) {
                            handleSizeInPx = (int) (handleSizeInDp * getResources().getDisplayMetrics().density);
                        }

                        // 判断触摸点是否在某个边缘热区
                        float touchX = event.getX();
                        float touchY = event.getY();
                        
                        boolean isTouchingLeftEdge = touchX < handleSizeInPx;
                        boolean isTouchingTopEdge = touchY < handleSizeInPx;
                        boolean isTouchingRightEdge = touchX > initialWidth - handleSizeInPx;
                        boolean isTouchingBottomEdge = touchY > initialHeight - handleSizeInPx;

                        // 根据触摸点位置设置状态
                        if (isTouchingLeftEdge && isTouchingTopEdge) {
                            currentTouchState = TouchState.RESIZING_TOP_LEFT;
                        } else if (isTouchingRightEdge && isTouchingTopEdge) {
                            currentTouchState = TouchState.RESIZING_TOP_RIGHT;
                        } else if (isTouchingLeftEdge && isTouchingBottomEdge) {
                            currentTouchState = TouchState.RESIZING_BOTTOM_LEFT;
                        } else if (isTouchingRightEdge && isTouchingBottomEdge) {
                            currentTouchState = TouchState.RESIZING_BOTTOM_RIGHT;
                        } else if (isTouchingLeftEdge) {
                            currentTouchState = TouchState.RESIZING_LEFT;
                        } else if (isTouchingTopEdge) {
                            currentTouchState = TouchState.RESIZING_TOP;
                        } else if (isTouchingRightEdge) {
                            currentTouchState = TouchState.RESIZING_RIGHT;
                        } else if (isTouchingBottomEdge) {
                            currentTouchState = TouchState.RESIZING_BOTTOM;
                        } else {
                            // 否则，默认为移动窗口
                            currentTouchState = TouchState.MOVING;
                        }
                        
                        Log.d(TAG, "Touch state: " + currentTouchState);
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        // 计算手指移动的距离
                        float deltaX = event.getRawX() - initialTouchX;
                        float deltaY = event.getRawY() - initialTouchY;

                        switch (currentTouchState) {
                            case MOVING:
                                // 如果是移动状态，更新窗口的 x, y 坐标
                                params.x = (int) (initialX + deltaX);
                                params.y = (int) (initialY + deltaY);
                                break;
                                
                            case RESIZING_LEFT:
                                // 调整左边，需要同时改变宽度和x坐标
                                int newWidthLeft = (int) (initialWidth - deltaX);
                                if (newWidthLeft > 50) { // 最小宽度50
                                    params.width = newWidthLeft;
                                    params.x = (int) (initialX + deltaX);
                                }
                                break;
                                
                            case RESIZING_TOP:
                                // 调整顶部，需要同时改变高度和y坐标
                                int newHeightTop = (int) (initialHeight - deltaY);
                                if (newHeightTop > 50) { // 最小高度50
                                    params.height = newHeightTop;
                                    params.y = (int) (initialY + deltaY);
                                }
                                break;
                                
                            case RESIZING_RIGHT:
                                // 调整右边，只改变宽度
                                int newWidthRight = (int) (initialWidth + deltaX);
                                if (newWidthRight > 50) { // 最小宽度50
                                    params.width = newWidthRight;
                                }
                                break;
                                
                            case RESIZING_BOTTOM:
                                // 调整底部，只改变高度
                                int newHeightBottom = (int) (initialHeight + deltaY);
                                if (newHeightBottom > 50) { // 最小高度50
                                    params.height = newHeightBottom;
                                }
                                break;
                                
                            case RESIZING_TOP_LEFT:
                                // 调整左上角
                                int newWidthTL = (int) (initialWidth - deltaX);
                                int newHeightTL = (int) (initialHeight - deltaY);
                                if (newWidthTL > 50) {
                                    params.width = newWidthTL;
                                    params.x = (int) (initialX + deltaX);
                                }
                                if (newHeightTL > 50) {
                                    params.height = newHeightTL;
                                    params.y = (int) (initialY + deltaY);
                                }
                                break;
                                
                            case RESIZING_TOP_RIGHT:
                                // 调整右上角
                                int newWidthTR = (int) (initialWidth + deltaX);
                                int newHeightTR = (int) (initialHeight - deltaY);
                                if (newWidthTR > 50) {
                                    params.width = newWidthTR;
                                }
                                if (newHeightTR > 50) {
                                    params.height = newHeightTR;
                                    params.y = (int) (initialY + deltaY);
                                }
                                break;
                                
                            case RESIZING_BOTTOM_LEFT:
                                // 调整左下角
                                int newWidthBL = (int) (initialWidth - deltaX);
                                int newHeightBL = (int) (initialHeight + deltaY);
                                if (newWidthBL > 50) {
                                    params.width = newWidthBL;
                                    params.x = (int) (initialX + deltaX);
                                }
                                if (newHeightBL > 50) {
                                    params.height = newHeightBL;
                                }
                                break;
                                
                            case RESIZING_BOTTOM_RIGHT:
                                // 调整右下角
                                int newWidthBR = (int) (initialWidth + deltaX);
                                int newHeightBR = (int) (initialHeight + deltaY);
                                if (newWidthBR > 50) {
                                    params.width = newWidthBR;
                                }
                                if (newHeightBR > 50) {
                                    params.height = newHeightBR;
                                }
                                break;
                        }
                        
                        // 应用所有更改
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                        
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 重置状态，为下一次触摸做准备
                        currentTouchState = TouchState.NONE;
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        // 保存当前窗口状态
        if (params != null && floatingView != null) {
            windowStateHelper.saveWindowState(
                    params.width, 
                    params.height, 
                    params.x, 
                    params.y
            );
        }
        
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
}