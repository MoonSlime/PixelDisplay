package com.pixeldp.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class AppModel {
    private final Context context;
    private String packageName;
    private String url;
    private String label;
    private Drawable icon;

    public AppModel(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    public String getPackageName() {
        return packageName;
    }
    public String getUrl() {
        return url;
    }
    public String getLabel() {
        return label;
    }
    public Drawable getIcon() {
        return icon;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
