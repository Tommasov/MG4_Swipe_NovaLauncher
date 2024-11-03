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
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class SwipeService extends Service {
    private PreferencesManager preferencesManager;
    private static final String CHANNEL_ID = "SwipeServiceChannel";
    private WindowManager windowManager;
    private View leftSwipeArea;
    private View rightSwipeArea;
    private View floatingButton;
    private GestureDetector leftGestureDetector;
    private GestureDetector rightGestureDetector;

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

    private class LeftSwipeGestureListener extends SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float startY = e1.getRawY();
            float endY = e2.getRawY();
            float diffY = endY - startY;

            if (diffY < 0 && Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                performBackAction();
                return true;
            }

            return false;
        }
    }

    private class RightSwipeGestureListener extends SimpleOnGestureListener {
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

    private void performBackAction() {
        Intent intent = new Intent("com.tommasov.mg4swipenovalauncher.ACTION_BACK");
        sendBroadcast(intent);
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

    private void swipe() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        leftSwipeArea = new View(this);
        rightSwipeArea = new View(this);

        int layoutFlags;
        layoutFlags = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        int halfScreenWidth = screenWidth / 2;

        WindowManager.LayoutParams leftParams = new WindowManager.LayoutParams(
                halfScreenWidth,
                10,
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        leftParams.gravity = Gravity.BOTTOM | Gravity.LEFT;

        WindowManager.LayoutParams rightParams = new WindowManager.LayoutParams(
                halfScreenWidth,
                10,
                layoutFlags,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        rightParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        windowManager.addView(leftSwipeArea, leftParams);
        windowManager.addView(rightSwipeArea, rightParams);

        leftGestureDetector = new GestureDetector(this, new LeftSwipeGestureListener());
        rightGestureDetector = new GestureDetector(this, new RightSwipeGestureListener());

        leftSwipeArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return leftGestureDetector.onTouchEvent(event);
            }
        });

        rightSwipeArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return rightGestureDetector.onTouchEvent(event);
            }
        });
    }

    private void backButton() {
        preferencesManager = new PreferencesManager(this);

        String backButtonVisibility = preferencesManager.getBackButtonVisibility();

        if (backButtonVisibility.equals("INVISIBLE")) {
            return;
        }

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

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 25;
        params.y = 5;

        windowManager.addView(floatingButton, params);

        floatingButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private static final int CLICK_ACTION_THRESHOLD = 10;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        floatingButton.setPressed(true);
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);
                        params.x = initialX + deltaX;
                        params.y = initialY + deltaY;
                        windowManager.updateViewLayout(floatingButton, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        floatingButton.setPressed(false);

                        if (Math.abs(event.getRawX() - initialTouchX) <= CLICK_ACTION_THRESHOLD &&
                                Math.abs(event.getRawY() - initialTouchY) <= CLICK_ACTION_THRESHOLD) {
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
                performBackAction();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (leftSwipeArea != null) {
            windowManager.removeView(leftSwipeArea);
        }
        if (rightSwipeArea != null) {
            windowManager.removeView(rightSwipeArea);
        }

        if (floatingButton != null) {
            windowManager.removeView(floatingButton);
        }
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
}
