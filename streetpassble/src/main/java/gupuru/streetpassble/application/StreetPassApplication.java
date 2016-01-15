package gupuru.streetpassble.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import java.util.List;

import gupuru.streetpassble.service.StreetPassService;

public class StreetPassApplication extends Application {

    private static final String SERVICE_NAME = StreetPassService.class.getCanonicalName();
    private static StreetPassApplication mInstance;

    public static StreetPassApplication get() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public boolean isRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo info : services) {
            if (SERVICE_NAME.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
