package com.praise.musixx;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

import static com.praise.musixx.ApplicationClass.ACTION_NEXT;
import static com.praise.musixx.ApplicationClass.ACTION_PLAY;
import static com.praise.musixx.ApplicationClass.ACTION_PREVIOUS;
import static com.praise.musixx.ApplicationClass.ACTION_RESUME;
import static com.praise.musixx.ApplicationClass.CHANNEL_ID_2;
import static com.praise.musixx.NowPlaying.IS_PLAYING;
import static com.praise.musixx.PlayerActivity.listSongs;
import static com.praise.musixx.PlayerActivity.player;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST";
    public static final String SONG_NAME = "SONG NAME";
    IBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;

    // public static  int  LAST_POSITION = 1;
    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
                    playBtn();
                    break;
                case "next":
                    nextBtn();
                    break;
                case "previous":
                    prevBtn();
                    break;


            }
        }
        if (myPosition != -1) {
            playMedia(myPosition);
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = listSongs;
        if (position == startPosition) {
            // Toast.makeText(this,"already playing",Toast.LENGTH_SHORT).show();
            if (mediaPlayer != null) {
                if (musicFiles != null) {
                    //  mediaPlayer.pause();
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                        IS_PLAYING = true;
                        player = true;
                    } else {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                        IS_PLAYING = true;
                        player = false;
                    }
                } else {
                    createMediaPLayer(position);
                    mediaPlayer.start();

                }
            } else {
                createMediaPLayer(position);
                mediaPlayer.start();
            }
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            position = startPosition;
            createMediaPLayer(position);
            mediaPlayer.start();

        }

    }

    void start() {
        IS_PLAYING = true;
        player = true;
        mediaPlayer.start();
    }

    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    void stop() {
        IS_PLAYING = false;
        player = false;
        mediaPlayer.stop();

    }

    void release() {
        mediaPlayer.release();
    }

    int getDuration() {
        return mediaPlayer.getDuration();
    }

    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    void createMediaPLayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        // editor.putInt("last",getLast());
        UpdatePosition(position);
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }

    void pause() {
        IS_PLAYING = false;
        player = false;
        mediaPlayer.pause();
    }

    void onCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            if (mediaPlayer != null) {
                createMediaPLayer(position);
                mediaPlayer.start();
                onCompleted();
            }
        }


    }

    void setCallback(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    void showNotification(int playBtn) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent prevIntent = new Intent(this, NotificationReciever.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent pauseIntent = new Intent(this, NotificationReciever.class).setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent = new Intent(this, NotificationReciever.class).setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        byte[] picture = null;
        try {
            picture = getAlbumArt(listSongs.get(position).getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bitmap thumb = null;
        if (picture != null) {
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.headphones);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.notify)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_previous, "Previous", prevPending)
                .addAction(playBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setContentIntent(contentIntent)
                .build();
        startForeground(2, notification);
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void nextBtn() {
        // Toast.makeText(this,"next", Toast.LENGTH_SHORT).show();
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
    }

    void prevBtn() {
        //  Toast.makeText(this,"previous", Toast.LENGTH_SHORT).show();
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
        }
    }

    void playBtn() {
        // Toast.makeText(this,"playPause", Toast.LENGTH_SHORT).show();
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
        }
    }


    void UpdatePosition(int pos) {
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.putInt("POSITION", pos);
        editor.apply();
    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

}
