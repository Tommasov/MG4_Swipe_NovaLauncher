package com.tommasov.mg4swipenovalauncher;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;

public class SwipeService extends Service {
    private static final String CHANNEL_ID = "SwipeServiceChannel";
    private WindowManager windowManager;
    private View swipeArea;
    private GestureDetector gestureDetector;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        checkOverlayPermission();

        if (Settings.canDrawOverlays(this)) {
            createNotificationChannel();
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("MG4 Nova Launcher Swipe Service")
                    .setSmallIcon(R.mipmap.ismart_launcher)
                    .build();
            startForeground(1, notification);

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            swipeArea = new View(this);

            int layoutFlags;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutFlags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutFlags = WindowManager.LayoutParams.TYPE_PHONE;
            }

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    10, //100 for the EMULATOR, 10 for MG4
                    layoutFlags,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.BOTTOM;

            windowManager.addView(swipeArea, params);

            gestureDetector = new GestureDetector(this, new SwipeGestureListener());

            swipeArea.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        } else {
            stopSelf();
        }
    }

    private class SwipeGestureListener extends SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float startY = e1.getRawY();
            float endY = e2.getRawY();
            float diffY = endY - startY;

            if (diffY < 0 && Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                openNovaLauncher();
                return true;
            }

            return false;
        }
    }

    private void openNovaLauncher() {

        SharedPreferences sharedPreferences = getSharedPreferences("SwipeServicePrefs", Context.MODE_PRIVATE);
        String packageName = sharedPreferences.getString("packageName", null);

        if (packageName == null) {
            packageName = "com.teslacoilsw.launcher";
        }

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Package not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (swipeArea != null) windowManager.removeView(swipeArea);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Swipe Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
