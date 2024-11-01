package com.tommasov.mg4swipenovalauncher;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.List;

public class AppListAdapter extends ArrayAdapter<ApplicationInfo> {
    private final PackageManager packageManager;
    private String selectedPackage;

    public AppListAdapter(@NonNull Context context, List<ApplicationInfo> apps, String selectedPackage) {
        super(context, 0, apps);
        this.packageManager = context.getPackageManager();
        this.selectedPackage = selectedPackage;
    }

    public void setSelectedPackage(String packageName) {
        this.selectedPackage = packageName;
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
