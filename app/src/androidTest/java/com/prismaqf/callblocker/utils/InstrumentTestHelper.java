package com.prismaqf.callblocker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class InstrumentTestHelper {

    public static Activity getCurrentActivity() {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                java.util.Collection<Activity> activites = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                activity[0] = Iterables.getOnlyElement(activites);
            }
        });
        return activity[0];
    }

    public static void rotateScreen(Activity currentActivity) {
        Context context = InstrumentationRegistry.getTargetContext();
        int orientation = context.getResources().getConfiguration().orientation;
        currentActivity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
