package com.praise.musixx;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class ApplicationClass extends Application {
    public static final String CHANNEL_ID_1 = "myChannel";
    public static final String CHANNEL_ID_2 = "appChannel";
    public static final String ACTION_PREVIOUS = "actionPrevious";
    public static final String ACTION_NEXT = "actionNext";
    public static final String ACTION_PLAY = "actionPlay";
    public static final String ACTION_RESUME = "actionResume";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationCahnnel();

    }

    private void createNotificationCahnnel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_1,
                    "myChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("description");
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID_2,
                    "appChannel", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("description two");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
