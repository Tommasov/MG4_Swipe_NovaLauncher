package com.tommasov.mg4swipenovalauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends ArrayAdapter<ApplicationInfo> {
    private final PackageManager packageManager;
    private String selectedPackage;
    private List<ApplicationInfo> allApps;
    private boolean showSystemApps = false;

    public AppListAdapter(@NonNull Context context, List<ApplicationInfo> apps, String selectedPackage) {
        super(context, 0, apps);
        this.packageManager = context.getPackageManager();
        this.selectedPackage = selectedPackage;
        allApps = new ArrayList<>(apps);
        filterApps();
    }

    public void setSelectedPackage(String packageName) {
        this.selectedPackage = packageName;
    }

    public void toggleSystemAppsVisibility() {
        showSystemApps = !showSystemApps;
        filterApps();
    }

    public boolean isSystemAppsVisible() {
        return showSystemApps;
    }

    private void filterApps() {
        List<ApplicationInfo> filteredApps = new ArrayList<>();

        for (ApplicationInfo appInfo : allApps) {
            if (isLauncherApp(appInfo)) {
                filteredApps.add(appInfo);
            }
        }

        for (ApplicationInfo appInfo : allApps) {
            if (!isLauncherApp(appInfo) && (showSystemApps || !isSystemApp(appInfo))) {
                filteredApps.add(appInfo);
            }
        }

        clear();
        addAll(filteredApps);
        notifyDataSetChanged();
    }

    private boolean isLauncherApp(ApplicationInfo appInfo) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.activityInfo.packageName.equals(appInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSystemApp(ApplicationInfo appInfo) {
        return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_app, parent, false);
        }

        ApplicationInfo appInfo = getItem(position);
        ImageView iconView = convertView.findViewById(R.id.app_icon);
        TextView nameView = convertView.findViewById(R.id.app_name);
        ImageView checkmarkView = convertView.findViewById(R.id.app_checkmark);

        iconView.setImageDrawable(appInfo.loadIcon(packageManager));
        nameView.setText(appInfo.loadLabel(packageManager));

        checkmarkView.setVisibility(appInfo.packageName.equals(selectedPackage) ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }
}
