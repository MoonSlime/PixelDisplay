package com.pixeldp.launcher.screen;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.content.AsyncTaskLoader;

import com.pixeldp.model.AppModel;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

class AppsLoader extends AsyncTaskLoader<ArrayList<AppModel>> {
    private ArrayList<AppModel> appModels;
    private final PackageManager packageManager;
    private PackageIntentReceiver packageIntentReceiver;

    AppsLoader(Context context) {
        super(context);
        packageManager = context.getPackageManager();
    }

    @Override
    public ArrayList<AppModel> loadInBackground() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
        final Context context = getContext();
        ArrayList<AppModel> items = new ArrayList<>(appList.size());
        for (int i = 0; i < appList.size(); i++) {
            AppModel app = new AppModel(context);
            app.setPackageName(appList.get(i).activityInfo.packageName);
            app.setUrl(appList.get(i).activityInfo.name);
            app.setIcon(appList.get(i).activityInfo.loadIcon(packageManager));
            app.setLabel(appList.get(i).activityInfo.loadLabel(packageManager).toString());
            items.add(app);
        }

        Collections.sort(items, ALPHA_COMPARATOR);

        return items;
    }

    @Override
    public void deliverResult(ArrayList<AppModel> apps) {
        if (isReset()) {
            if (apps != null) {
                onReleaseResources(apps);
            }
        }
        ArrayList<AppModel> oldApps = apps;
        appModels = apps;
        if (isStarted()) {
            super.deliverResult(apps);
        }
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    @Override
    protected void onStartLoading() {
        if (appModels != null) {
            deliverResult(appModels);
        }

        if (packageIntentReceiver == null) {
            packageIntentReceiver = new PackageIntentReceiver(this);
        }
        if (takeContentChanged() || appModels == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(ArrayList<AppModel> apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (appModels != null) {
            onReleaseResources(appModels);
            appModels = null;
        }
        if (packageIntentReceiver != null) {
            getContext().unregisterReceiver(packageIntentReceiver);
            packageIntentReceiver = null;
        }
    }

    private void onReleaseResources(ArrayList<AppModel> apps) {
    }

    private static final Comparator<AppModel> ALPHA_COMPARATOR = new Comparator<AppModel>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(AppModel object1, AppModel object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };
}
