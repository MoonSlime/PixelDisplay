package com.pixeldp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import java.util.Calendar;


public class FilterService extends Service {
    private WindowManager winManager;
    public FilterView mView;

    private static final String REAL_WIDTH = "REAL_WIDTH";
    private static final String REAL_HEIGHT = "REAL_HEIGHT";
    public static final String START_MILLI = "START_MILLI ";
    public static final String END_MILLI = "END_MILLI ";
    public static final String CHECK_FILTER_APPLY = "CHECK_FILTER_APPLY";

    public static final String COLOR_A = "COLOR_A";
    public static final String COLOR_R = "COLOR_R";
    public static final String COLOR_G = "COLOR_G";
    public static final String COLOR_B = "COLOR_B";

    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mView = new FilterView(this);
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

        mParams.height = PreferenceUtil.instance(this).get(REAL_HEIGHT, 1920) + 300;
        mParams.width = mParams.height;
        winManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        winManager.addView(mView, mParams);
        winManager.getDefaultDisplay().getRefreshRate();
        registerReceiver();

    }

    // 0 종료
    // 1 시작
    private PendingIntent getButtonPending(int filter, int requestCode) {
        String action = "com.pixeldp.util.filterService" + String.valueOf(filter);
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }
    private long initStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    private long initEndTime () {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.pixeldp.util.filterService0");
        filter.addAction("com.pixeldp.util.filterService1");
        mReceiver = new FilterReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean check_switch_bl = PreferenceUtil.instance(this).get(CHECK_FILTER_APPLY, false);
        if (check_switch_bl) {

            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.cancel(getButtonPending(0, 0));
            manager.cancel(getButtonPending(1, 1));

            long currentTime = Calendar.getInstance().getTimeInMillis();
            long startTime = PreferenceUtil.instance(this).get(START_MILLI, initStartTime());
            long endTime = PreferenceUtil.instance(this).get(END_MILLI, initEndTime());
            long oneday = 24 * 60 * 60 * 1000;// 24시간

            int ColorA = PreferenceUtil.instance(this).get(COLOR_A, 0);
            int ColorR = PreferenceUtil.instance(this).get(COLOR_R, 255);
            int ColorG = PreferenceUtil.instance(this).get(COLOR_G, 255);
            int ColorB = PreferenceUtil.instance(this).get(COLOR_B, 255);
            manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, oneday, getButtonPending(1, 1));
            manager.setRepeating(AlarmManager.RTC_WAKEUP, endTime, oneday, getButtonPending(0, 0));
            if (startTime < currentTime && (endTime > currentTime || endTime <startTime)) {
                mView.setARGB(ColorA, ColorR, ColorG, ColorB);
                manager.set(AlarmManager.RTC_WAKEUP, currentTime, getButtonPending(1, 1));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mView != null) {
            winManager.removeView(mView);
            mView = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    class FilterView extends View {
        private int r;
        private int g;
        private int b;
        private int a;

        public FilterView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawARGB(a, r, g, b);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        public void setARGB(int a, int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            invalidate();
        }
    }

    class FilterReceiver extends BroadcastReceiver {
        Handler mHandler;
        Context mContext;

        @Override
        public void onReceive(Context context, Intent intent) {
            mHandler = new Handler();
            mContext = context;
            String action = intent.getAction();
            if (action.equals("com.pixeldp.util.filterService0")) {
                mView.setARGB(0, 255, 255, 255);
            } else if (action.equals("com.pixeldp.util.filterService1")) {
                int ColorA = PreferenceUtil.instance(context).get(COLOR_A, 0);
                int ColorR = PreferenceUtil.instance(context).get(COLOR_R, 255);
                int ColorG = PreferenceUtil.instance(context).get(COLOR_G, 255);
                int ColorB = PreferenceUtil.instance(context).get(COLOR_B, 255);
                mView.setARGB(ColorA, ColorR, ColorG, ColorB);
            }
        }
    }
}