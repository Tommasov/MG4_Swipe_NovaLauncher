package com.tommasov.mg4swipenovalauncher;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;

public class AccService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.tommasov.mg4swipenovalauncher.ACTION_BACK");
        registerReceiver(backReceiver, filter);
    }

    private final BroadcastReceiver backReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            performBackAction();
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    public void performBackAction() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(backReceiver);
        super.onDestroy();
    }
}
