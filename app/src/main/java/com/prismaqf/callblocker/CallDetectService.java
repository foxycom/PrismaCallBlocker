package com.prismaqf.callblocker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Call detect service, a bound service
 * @author originally by Moskvichev Andrey V., later adapted by ConteDiMonteCristo
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */public class CallDetectService extends Service {

    public class LocalBinder extends Binder {

        CallDetectService getService() {
            // Return this instance of CallDetectService so clients can call public methods
            return CallDetectService.this;
        }
    }

    private final IBinder myBinder;
    private final CallHelper myCallHelper;
    
    public CallDetectService() {
        myBinder = new LocalBinder();
        myCallHelper = new CallHelper(this);
    }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendNotification(); //set a notification to the status bar
        myCallHelper.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        myCallHelper.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private void sendNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.police_32)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification));
        Intent resultIntent = new Intent(this, CallBlockerManager.class);
        //artificial back stack for the navigation to go back to the app
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(CallBlockerManager.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(R.integer.notification_id, mBuilder.build());
    }

}
