package com.praise.musixx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;
import static com.praise.musixx.ApplicationClass.ACTION_NEXT;
import static com.praise.musixx.ApplicationClass.ACTION_PLAY;
import static com.praise.musixx.ApplicationClass.ACTION_PREVIOUS;
import static com.praise.musixx.ApplicationClass.ACTION_RESUME;
import static com.praise.musixx.MusicService.MUSIC_FILE_LAST_PLAYED;

public class NotificationReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String actionName = intent.getAction();
        Intent serviceIntent = new Intent(context,MusicService.class);

        if(actionName != null){
            switch (actionName){
                case ACTION_PLAY:
                    serviceIntent.putExtra("ActionName" , "playPause");
                    context.startService(serviceIntent);
                    break;
                case ACTION_NEXT:
                    serviceIntent.putExtra("ActionName" , "next");
                    context.startService(serviceIntent);
                    break;
                case ACTION_PREVIOUS:
                    serviceIntent.putExtra("ActionName" , "previous");
                    context.startService(serviceIntent);
                    break;
                case ACTION_RESUME:
                    serviceIntent.putExtra("ActionName", "resume");
                   SharedPreferences preferences = context.getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                    int pos = preferences.getInt("POSITION", -1);
                    serviceIntent.putExtra("servicePosition",pos);
                    context.startService(serviceIntent);
            }
        }
    }
}
