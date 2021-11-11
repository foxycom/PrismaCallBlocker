package com.prismaqf.callblocker;


import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.ContactsContract;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prismaqf.callblocker.utils.DebugDBFileName;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CallBlockerManagerTest
{
    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    public final ActivityTestRule<CallBlockerManager> mActivityRule = new ActivityTestRule<>(CallBlockerManager.class);

    public final GrantPermissionRule permissionRule = GrantPermissionRule
          .grant(permission.READ_CONTACTS, permission.CALL_PHONE);

    @Rule
    public final RuleChain chian = RuleChain.outerRule(permissionRule).around(mActivityRule);

    @Before
    public void before() {
        stopRunningService();
    }


    @Test
    public void checkDisplay() {
        //simple Espresso test just to check that it all works.
        //This just test that the button appears on the screen
        onView(withId(R.id.buttonDetectToggle)).check(matches(isDisplayed()));
    }

    @Test
    public void checkThatTurningOnServiceChangesText() {
        //first is off, check not detecting labels
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.tx_no_detect)));
        onView(withId(R.id.buttonDetectToggle)).check(matches(withText(R.string.tx_turn_on)));
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        //now check that the text has changed
        onView(withId(R.id.textDetectState)).check(matches(withText(R.string.tx_detect)));
        onView(withId(R.id.buttonDetectToggle)).check(matches(withText(R.string.tx_turn_off)));
        onView(withId(R.id.buttonDetectToggle)).perform(click()); //turn off
    }

    @Test
    public void checkStatusOfService() {
        Activity myActivity = mActivityRule.getActivity();
        assertFalse("service off", CallBlockerManager.isServiceRunning(myActivity));
        //turn it on
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        assertTrue("service on", CallBlockerManager.isServiceRunning(myActivity));
        //turn it off
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        assertFalse("service off", CallBlockerManager.isServiceRunning(myActivity));
    }

    @Test
    @Ignore
    public void checkServiceStateOnSharedPreferences() {
        Activity myActivity = mActivityRule.getActivity();
        Context myCtx = myActivity.getApplicationContext();
        SharedPreferences prefs = myCtx.getSharedPreferences(
                myActivity.getString(R.string.file_shared_prefs_name),
                Context.MODE_PRIVATE);
        //before running the service
        String state = prefs.getString(myActivity.getString(R.string.pk_state), "not found");
        assertEquals("idle state", "idle", state);
        //after starting the service
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        state = prefs.getString(myActivity.getString(R.string.pk_state),"not found");
        assertEquals("running state", "running", state);
        //after stopping the service
        onView(withId(R.id.buttonDetectToggle)).perform(click());
        state = prefs.getString(myActivity.getString(R.string.pk_state),"not found");
        assertEquals("idle state", "idle", state);
    }

    @Test
    public void testCallsBroadcastReceiver() {
        Context ctx = mActivityRule.getActivity().getApplicationContext();
        Intent intent = new Intent();
        intent.setAction(ctx.getString(R.string.ac_call));
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra(ctx.getString(R.string.ky_number_called), "123");
        intent.putExtra(ctx.getString(R.string.ky_received),10);
        intent.putExtra(ctx.getString(R.string.ky_triggered),5);
        ctx.sendBroadcast(intent);
        SystemClock.sleep(500);
        onView(withId(R.id.button_received)).check(matches(withText("10")));
        onView(withId(R.id.button_triggered)).check(matches(withText("5")));
    }

    @Test
    public void testStartServiceWhenInBackground() {
        Activity myactivity = mActivityRule.getActivity();
        Context ctx = myactivity.getApplicationContext();
        //first stop the service
        stopRunningService();
        //now put the app in the background
        myactivity.moveTaskToBack(true);
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
        SystemClock.sleep(5000);
        //now try to start the service from the background
        Intent serviceIntent = new Intent(ctx, CallDetectService.class);
        myactivity.startService(serviceIntent);
    }

    @Test
    public void testContactResolverDoesNotThrow() {
        String incomingNumber = "123";
        Context ctx = mActivityRule.getActivity().getApplicationContext();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String[] projection = new String[]{ ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor c = ctx.getContentResolver().query(uri, projection, null, null, null);
    }

    private void stopRunningService() {
        Activity myActivity = mActivityRule.getActivity();
        /*       Intent intent = new Intent(myActivity, CallDetectService.class);
        myActivity.stopService(intent);*/
        if (CallBlockerManager.isServiceRunning(myActivity)) {
            onView(withId(R.id.buttonDetectToggle)).perform(click());
        }
    }


}
