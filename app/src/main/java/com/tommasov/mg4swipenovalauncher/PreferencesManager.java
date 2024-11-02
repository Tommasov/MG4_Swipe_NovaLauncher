package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFS_NAME = "SwipeServicePrefs";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private static final String KEY_BACK_BUTTON_VISIBILITY= "backButtonVisibility";

    private SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSelectedPackage(String packageName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PACKAGE_NAME, packageName);
        editor.apply();
    }

    public String getSelectedPackage() {
        return sharedPreferences.getString(KEY_PACKAGE_NAME, null);
    }

    public void saveBackButtonVisibility(String visibility) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_BACK_BUTTON_VISIBILITY, visibility);
        editor.apply();
    }

    public String getBackButtonVisibility() {
        return sharedPreferences.getString(KEY_BACK_BUTTON_VISIBILITY, "VISIBLE");
    }
}
