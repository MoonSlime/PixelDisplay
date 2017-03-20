package com.pixeldp.launcher.setting;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.pixeldp.prototype.MainActivity;
import com.pixeldp.prototype.R;

public class FilterService extends Service {
    private WindowManager winManager;
    private View mView;
    private final String TAG = "FilterService";
    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.display_service_window_filter, null);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        mParams.height = 2100;
        mParams.width = 1080;
        winManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        winManager.addView(mView, mParams);
        winManager.getDefaultDisplay().getRefreshRate();
        //notiRegister();
        registerReceiver();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        if(intent != null) {
            int r_value = intent.getIntExtra("r", 255);
            int g_value = intent.getIntExtra("g", 255);
            int b_value = intent.getIntExtra("b", 255);
            int a_value = intent.getIntExtra("a", 0);
            mView.setBackgroundColor(Color.argb(a_value, r_value, g_value, b_value));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        Log.e(TAG, "onDestory");
        if (mView != null) {
            winManager.removeView(mView);
            mView = null;
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.pixeldp.launcher.HomeScreen.Noti1");
        filter.addAction("com.pixeldp.launcher.HomeScreen.Noti2");
        filter.addAction("com.pixeldp.launcher.HomeScreen.Noti3");
        filter.addAction("com.pixeldp.launcher.HomeScreen.Noti4");
        mReceiver = new ButtonReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private PendingIntent getActivityPending() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, intent, 0);
    }

    private PendingIntent getButtonPending(int filter) {
        String action = "com.pixeldp.launcher.HomeScreen.Noti" + String.valueOf(filter);
        Intent intent = new Intent(action);
        intent.putExtra("FILTER_MODE", filter);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*private void notiRegister() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.launcher_noti);

        Notification notification;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.launcher_small_noti);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setContentIntent(getActivityPending());

        contentView.setOnClickPendingIntent(R.id.remote_option_1, getButtonPending(1));
        contentView.setOnClickPendingIntent(R.id.remote_option_2, getButtonPending(2));
        contentView.setOnClickPendingIntent(R.id.remote_option_3, getButtonPending(3));
        contentView.setOnClickPendingIntent(R.id.remote_option_4, getButtonPending(4));
        notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.contentView = contentView;
        nm.notify(1, notification);
    }*/

    private class ButtonReceiver extends BroadcastReceiver {
        Handler mHandler;
        Context mContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler = new Handler();
            mContext = context;
            String action = intent.getAction();
            int mode = intent.getExtras().getInt("FILTER_MODE");
            switch (action) {
                case "com.pixeldp.launcher.HomeScreen.Noti1":
                    Log.e(TAG, "ACTION 1");
                    mView.setBackgroundColor(Color.argb(60, 70, 50, 0));
                    break;
                case "com.pixeldp.launcher.HomeScreen.Noti2":
                    Log.e(TAG, "ACTION 2");
                    mView.setBackgroundColor(Color.argb(95, 80, 50, 0));
                    break;
                case "com.pixeldp.launcher.HomeScreen.Noti3":
                    Log.e(TAG, "ACTION 3");
                    mView.setBackgroundColor(Color.argb(120, 100, 50, 0));
                    break;
                case "com.pixeldp.launcher.HomeScreen.Noti4":
                    Log.e(TAG, "ACTION 4");
                    mView.setBackgroundColor(Color.argb(0, 255, 255, 255));
                    break;
            }
        }
    }
}
