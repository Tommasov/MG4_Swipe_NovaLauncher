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
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
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
    private View floatingButton;
    private GestureDetector gestureDetector;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        if (Settings.canDrawOverlays(this)) {
            createNotificationChannel();
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("MG4 Nova Launcher Swipe Service")
                    .setSmallIcon(R.mipmap.ismart_launcher)
                    .build();
            startForeground(1, notification);

            swipe();
            backButton();

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
                openLauncher();
                return true;
            }

            return false;
        }
    }

    private void openLauncher() {

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

    private void swipe(){
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        swipeArea = new View(this);

        int layoutFlags;
        layoutFlags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

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
    }

    private void backButton(){
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingButton = LayoutInflater.from(this).inflate(R.layout.layout_floating_button, null);

        int layoutFlags;
        layoutFlags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        params.x = 20;
        params.y = 20;

        windowManager.addView(floatingButton, params);

        floatingButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchY;
            private static final int CLICK_ACTION_THRESHOLD = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        floatingButton.setPressed(true);
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaY = (int) (initialTouchY - event.getRawY());
                        if (Math.abs(deltaY) > CLICK_ACTION_THRESHOLD) {
                            params.y = initialY + deltaY;
                            windowManager.updateViewLayout(floatingButton, params);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        floatingButton.setPressed(false);

                        if (Math.abs(event.getRawY() - initialTouchY) <= CLICK_ACTION_THRESHOLD) {
                            floatingButton.performClick();
                        }

                        return true;

                    case MotionEvent.ACTION_CANCEL:
                        floatingButton.setPressed(false);
                        return true;
                }
                return false;
            }
        });
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.tommasov.mg4swipenovalauncher.ACTION_BACK");
                sendBroadcast(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (swipeArea != null) {
            windowManager.removeView(swipeArea);
        }

        if (floatingButton != null) {
            windowManager.removeView(floatingButton);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent backIntent = new Intent("ACTION_BACK_PRESS");
        sendBroadcast(backIntent);

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
}
