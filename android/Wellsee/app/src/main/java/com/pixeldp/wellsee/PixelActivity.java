package com.pixeldp.wellsee;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pixeldp.model.EyeModel;

public class PixelActivity extends AppCompatActivity {
    public static int age;
    public static int sex;
    public static EyeModel eyeModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    protected void analyticsStart(Activity activity) {
        WellSeeApplication application = (WellSeeApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        String className = activity.getClass().getName();
        String[] activityName = className.split("\\.");
        mTracker.setScreenName(activityName[activityName.length - 1]);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}