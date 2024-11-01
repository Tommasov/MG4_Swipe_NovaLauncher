package com.tommasov.mg4swipenovalauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startSwipeService(context);
        }
    }

    private void startSwipeService(Context context) {
        Intent serviceIntent = new Intent(context, SwipeService.class);
        context.startForegroundService(serviceIntent);
    }
}