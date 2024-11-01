package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "SwipeServicePrefs";
    private static final String KEY_PACKAGE_NAME = "packageName";
    private PackageManager packageManager;
    private AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView explanationText = findViewById(R.id.explanation_text);
        explanationText.setText(R.string.explanation_text);

        String currentPackageName = getPackageName();
        packageManager = getPackageManager();
        List<ApplicationInfo> userApps = new ArrayList<>();
        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (!appInfo.packageName.equals(currentPackageName)) {
                userApps.add(appInfo);
            }
        }

        Collections.sort(userApps, new Comparator<ApplicationInfo>() {
            @Override
            public int compare(ApplicationInfo app1, ApplicationInfo app2) {
                String label1 = app1.loadLabel(packageManager).toString();
                String label2 = app2.loadLabel(packageManager).toString();
                return label1.compareToIgnoreCase(label2);
            }
        });

        ListView listView = findViewById(R.id.app_list);
        adapter = new AppListAdapter(this, userApps, getSelectedPackage());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo selectedApp = userApps.get(position);
            saveSelectedPackage(selectedApp.packageName);
            adapter.setSelectedPackage(selectedApp.packageName);
            adapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Selected: " + selectedApp.packageName, Toast.LENGTH_SHORT).show();
        });

        startSwipeService();
    }

    private void saveSelectedPackage(String packageName) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PACKAGE_NAME, packageName);
        editor.apply();
    }

    private String getSelectedPackage() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_PACKAGE_NAME, null);
    }

    private void startSwipeService() {
        Intent intent = new Intent(this, SwipeService.class);
        startService(intent);
    }
}
