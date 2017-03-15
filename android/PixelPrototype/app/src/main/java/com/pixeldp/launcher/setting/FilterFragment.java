package com.pixeldp.launcher.setting;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anarchy.library.DoubleSeekBar;
import com.pixeldp.launcher.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterFragment extends Fragment implements DoubleSeekBar.onSeekBarChangeListener {
    @Bind(R.id.fragment_filter_apply)
    TextView filter_apply;
    @Bind(R.id.fragment_filter_clear)
    TextView filter_clear;

    @Bind(R.id.fragment_filter_seek1)
    DoubleSeekBar seekBar1; //0
    @Bind(R.id.fragment_filter_seek2)
    DoubleSeekBar seekBar2; //10-20
    @Bind(R.id.fragment_filter_seek3)
    DoubleSeekBar seekBar3; //20-40
    @Bind(R.id.fragment_filter_seek4)
    DoubleSeekBar seekBar4; //40- 60

    private Data[] seek_data;
    private Data check;

    private AlarmManager manager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        ButterKnife.bind(this, view);

        seekBar1.setOnSeekBarChangeListener(this);
        seekBar2.setOnSeekBarChangeListener(this);
        seekBar3.setOnSeekBarChangeListener(this);
        seekBar4.setOnSeekBarChangeListener(this);
        seek_data = new Data[4];
        for (int i = 0; i < seek_data.length; i++)
            seek_data[i] = new Data();
        manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        return view;
    }

    @Override
    public void onProgressChanged(DoubleSeekBar doubleSeekBar, float firstThumbRatio, float secondThumbRatio) {
        int id = Integer.valueOf(doubleSeekBar.getTag().toString()) - 1;
        seek_data[id].setLeft(firstThumbRatio);
        seek_data[id].setRight(secondThumbRatio);
    }

    private Calendar timeInit() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    private long getResultTime(Calendar cal, float settingTime) {
        long time = cal.getTimeInMillis();
        return time + toTime(settingTime);
    }

    private void showTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        Log.e("debugging_filter", "SHOWTIME::" + cal.getTime().toString());
    }

    @OnClick({R.id.fragment_filter_apply})
    public void apply() {
        boolean checkValue = checkValue(seek_data);
        Log.e("debugging_filter", "APPLY::" + checkValue);
        if (checkValue) {
            Intent intent = new Intent(getActivity(), FilterService.class);
            getActivity().startService(intent);

            for (int i = 0; i < seek_data.length; i++) {
                if (seek_data[i].getLeft() == 0f && seek_data[i].getRight() == 1f)
                    continue;
                long currentTime = Calendar.getInstance().getTimeInMillis();
                Calendar initCalendar = timeInit();
                long leftTime = getResultTime(initCalendar, seek_data[i].getLeft());
                long rightTime = getResultTime(initCalendar, seek_data[i].getRight());
                long oneday = 24 * 60 * 60 * 1000;// 24시간
                manager.setRepeating(AlarmManager.RTC_WAKEUP, leftTime, oneday, getButtonPending(i + 1, i + 1));
                showTime(leftTime);
                showTime(rightTime);
                manager.setRepeating(AlarmManager.RTC_WAKEUP, rightTime, oneday, getButtonPending(4, 4));
                if (leftTime < currentTime && rightTime > currentTime)
                    manager.set(AlarmManager.RTC_WAKEUP, currentTime, getButtonPending(i + 1, i + 1));
            }
        }
    }


    private PendingIntent getButtonPending(int filter, int requestCode) {
        String action = "com.pixeldp.launcher.HomeScreen.Noti" + String.valueOf(filter);
        Intent intent = new Intent(action);
        intent.putExtra("FILTER_MODE", filter);
        return PendingIntent.getBroadcast(getActivity(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @OnClick({R.id.fragment_filter_clear})
    public void clear() {
        Log.e("debugging_filter", "CLEAR");
        manager.cancel(getButtonPending(1, 1));
        manager.cancel(getButtonPending(2, 2));
        manager.cancel(getButtonPending(3, 3));
        manager.cancel(getButtonPending(4, 4));
        manager.set(AlarmManager.RTC_WAKEUP, 1000, getButtonPending(4, 4));
        seekBar1.reset();
        seekBar2.reset();
        seekBar3.reset();
        seekBar4.reset();
    }

    private class Data {
        private float left;
        private float right;

        private Data() {
            this.left = 0;
            this.right = 1;
        }

        float getLeft() {
            return left;
        }

        public float getRight() {
            return right;
        }

        void setLeft(float left) {
            this.left = left;
        }

        public void setRight(float right) {
            this.right = right;
        }

    }

    public long toTime(float value) {
        return (long) (value * 1440 * 60 * 1000);
    }

    public String timeToString(int time) {
        return (time / 60) + ":" + (time % 60);
    }

    public void createArray(Data[] src, ArrayList<Float> arrList, ArrayList<Integer> index) {
        for (int i = 0; i < src.length; i++) {
            if (src[i].getLeft() == 0f && src[i].getRight() == 1f)
                continue;
            arrList.add(src[i].getLeft());
            index.add(i);
        }
    }

    public boolean checkValue(Data[] src) {
        ArrayList<Float> arrList = new ArrayList<>();
        ArrayList<Integer> index = new ArrayList<>();
        createArray(src, arrList, index);
        int indices[] = getIndicesInOrder(arrList, index);
        for (int i = 0; i < indices.length; i++) {
            if (i == (indices.length - 1)) break;
            if (src[indices[i]].getRight() > src[indices[i + 1]].getLeft())
                return false;
        }
        return true;
    }

    private int[] getIndicesInOrder(ArrayList<Float> arrList, ArrayList<Integer> index) {
        Float[] c = arrList.toArray(new Float[arrList.size()]);
        Arrays.sort(c);
        int sortIndex[] = new int[c.length];
        for (int i = 0; i < c.length; ++i) {
            int temp = arrList.indexOf(c[i]);
            arrList.set(temp, Float.MIN_VALUE);
            sortIndex[i] = index.get(temp);
        }
        return sortIndex;
    }
}
