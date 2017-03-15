package com.pixeldp.launcher.screen;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixeldp.model.AppModel;
import com.pixeldp.launcher.R;

import java.util.ArrayList;
import java.util.Collection;

class AppListAdapter extends ArrayAdapter<AppModel> {
    private Context context;
    private int mode;
    private int scale[] = {43, 50, 57, 64, 76, 88};
    private int textSize[] = {14, 16, 18, 20, 24, 28};

    AppListAdapter(Context context, int mode) {
        super(context, android.R.layout.simple_list_item_2);
        this.context = context;
        this.mode = mode;
    }

    public void setData(ArrayList<AppModel> data) {
        clear();
        if (data != null) {
            addAll(data);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void addAll(@NonNull Collection<? extends AppModel> items) {
        super.addAll(items);
    }

    private void fitImageView(ViewHolder viewHolder, AppModel item) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        int size = (int) (scale[mode] * px);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.gravity = Gravity.CENTER;
        viewHolder.icon.setLayoutParams(layoutParams);
        viewHolder.icon.setImageDrawable(item.getIcon());
    }

    private void fitTextView(ViewHolder viewHolder, AppModel item) {
        viewHolder.text.setText(item.getLabel());
        viewHolder.text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize[mode]);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;
        AppModel item = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.launcher_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
            viewHolder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        fitImageView(viewHolder, item);
        fitTextView(viewHolder, item);
        return convertView;
    }

    private class ViewHolder {
        public ImageView icon;
        public TextView text;
    }
}
