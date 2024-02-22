package com.praise.musixx;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static com.praise.musixx.MainActivity.repeatBool;
import static com.praise.musixx.MainActivity.shakeBoolean;
import static com.praise.musixx.MainActivity.shuffleBoolean;
import static com.praise.musixx.MusicService.MUSIC_FILE_LAST_PLAYED;
import static com.praise.musixx.NowPlaying.IS_PLAYING;
import static com.praise.musixx.SongsAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {
    public static boolean isMuted;
    public static boolean player;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    ImageView btnnext, btnprev, btnShuffle, back, repeat, shake, volumer;
    TextView txtName, txtStart, txtStop, txtArtist, titleNow;
    SeekBar seekBar, volume;
    CardView cardView;
    int CURRENT_VOLUME;
    ImageView imageView;
    FloatingActionButton btnplay;
    int position = -1;
    int count = 0;
    AudioManager audioManager;
    MusicService musicService;
    private Thread playThread, prevThread, nextThread, shakeThread;
    private Handler handler = new Handler();
    private SensorManager mSensorManager;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    private final SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            if (checkShaker()) {
                if (mAccel > 12) {
                    count++;
                    if (count > 1) {
                        prevBtnClicked();
                    } else {
                        nextBtnClicked();
                    }
                    count = 0;

                }
            } else {

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        setupWindowAnimations();
        initialize();
        verifyCheck();
        getIntentMethod();
        player = true;
        verifyCheck();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(PlayerActivity.this, MainActivity.class);
                //  Bundle b =  ActivityOptions.makeSceneTransitionAnimation(PlayerActivity.this).toBundle();
                // startActivity(intent,b);
                onBackPressed();
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser) {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                verifyCheck();
                setMuter();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                verifyCheck();
                setMuter();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                verifyCheck();
                setMuter();
            }

        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int currentPos = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPos);
                    txtStart.setText(formattedTime(currentPos));
                }
                handler.postDelayed(this, 1000);
            }


        });
        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shuffleBoolean) {

                    shuffleBoolean = true;
                } else {
                    shuffleBoolean = false;
                }
                verifyCheck();
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!repeatBool) {

                    repeatBool = true;

                } else {

                    repeatBool = false;

                }
                verifyCheck();
            }
        });

    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private String formattedTime(int currentPos) {
        String totalOut = "";
        String totalNew = "";
        String sec = String.valueOf(currentPos % 60);
        String min = String.valueOf(currentPos / 60);
        totalOut = min + ":" + sec;
        totalNew = min + ":" + "0" + sec;
        if (sec.length() == 1) {
            return totalNew;
        } else {
            return totalOut;
        }
    }

    public void getIntentMethod() {
        position = getIntent().getIntExtra("pos", -1);
        listSongs = mFiles;
        if (listSongs != null) {
            btnplay.setBackgroundResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);

    }

    public void initialize() {
        btnnext = findViewById(R.id.btnNext);
        btnprev = findViewById(R.id.btnPrev);
        txtName = findViewById(R.id.txtsn);
        txtStart = findViewById(R.id.txtstart);
        txtStop = findViewById(R.id.txtstop);
        txtArtist = findViewById(R.id.textView);
        seekBar = findViewById(R.id.seekbar);
        volume = findViewById(R.id.volume_seeker);
        imageView = findViewById(R.id.imageX);
        back = findViewById(R.id.goBack);
        repeat = findViewById(R.id.repeater);
        btnShuffle = findViewById(R.id.shuffler);
        btnplay = findViewById(R.id.playBtn);
        titleNow = findViewById(R.id.tvNow);
        cardView = findViewById(R.id.cardView);
        shake = findViewById(R.id.shaker);
        volumer = findViewById(R.id.muter);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Objects.requireNonNull(mSensorManager).registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_STATUS_ACCURACY_LOW);
        mAccel = 10f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume.setMax(MAX_VOLUME);
        CURRENT_VOLUME = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume.setProgress(CURRENT_VOLUME);
    }

    public void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        txtStop.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if (art != null) {

            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimator(this, imageView, bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch != null) {
                        ImageView gradient = findViewById(R.id.image_effect);
                        LinearLayout linearLayout = findViewById(R.id.linearBg);
                        gradient.setBackgroundResource(R.drawable.bg_grad);
                        linearLayout.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        linearLayout.setBackground(gradientDrawableBg);
                        txtName.setTextColor(swatch.getBodyTextColor());
                        txtArtist.setTextColor(swatch.getTitleTextColor());
                        txtStart.setTextColor(swatch.getBodyTextColor());
                        txtStop.setTextColor(swatch.getBodyTextColor());
                        repeat.setColorFilter(swatch.getBodyTextColor());
                        btnnext.setColorFilter(swatch.getBodyTextColor());
                        btnprev.setColorFilter(swatch.getBodyTextColor());
                        btnShuffle.setColorFilter(swatch.getBodyTextColor());
                        volumer.setColorFilter(swatch.getBodyTextColor());
                        back.setColorFilter(swatch.getBodyTextColor());
                        shake.setColorFilter(swatch.getBodyTextColor());
                        titleNow.setTextColor(swatch.getTitleTextColor());
                        cardView.setCardBackgroundColor(swatch.getRgb());
                        seekBar.getProgressDrawable().setColorFilter(swatch.getBodyTextColor(), PorterDuff.Mode.MULTIPLY);
                        seekBar.getThumb().setColorFilter(swatch.getTitleTextColor(), PorterDuff.Mode.SRC_IN);
                        volume.getProgressDrawable().setColorFilter(swatch.getBodyTextColor(), PorterDuff.Mode.MULTIPLY);
                        volume.getThumb().setColorFilter(swatch.getTitleTextColor(), PorterDuff.Mode.SRC_IN);

                    } else {
                        ImageView gradient = findViewById(R.id.image_effect);
                        LinearLayout linearLayout = findViewById(R.id.linearBg);
                        gradient.setBackgroundResource(R.drawable.bg_grad);
                        linearLayout.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gradient.setBackground(gradientDrawable);
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        linearLayout.setBackground(gradientDrawableBg);
                        txtName.setTextColor(Color.WHITE);
                        txtArtist.setTextColor(Color.DKGRAY);
                        txtStart.setTextColor(Color.DKGRAY);
                        txtStop.setTextColor(Color.DKGRAY);
                        titleNow.setTextColor(Color.BLACK);
                        repeat.setColorFilter(Color.DKGRAY);
                        btnnext.setColorFilter(Color.DKGRAY);
                        btnprev.setColorFilter(Color.DKGRAY);
                        volumer.setColorFilter(Color.DKGRAY);
                        cardView.setCardBackgroundColor(Color.DKGRAY);
                        back.setColorFilter(Color.DKGRAY);
                        shake.setColorFilter(Color.DKGRAY);
                        btnShuffle.setColorFilter(Color.DKGRAY);
                        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
                        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
                        volume.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
                        volume.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
                    }
                }
            });
        } else {
            Glide.with(this).asBitmap().load(R.drawable.headphones).into(imageView);
            ImageView gradient = findViewById(R.id.image_effect);
            LinearLayout linearLayout = findViewById(R.id.linearBg);
            gradient.setBackgroundResource(R.drawable.bg_grad);
            linearLayout.setBackgroundResource(R.drawable.main_bg);
            txtName.setTextColor(Color.WHITE);
            txtArtist.setTextColor(Color.DKGRAY);
            txtStart.setTextColor(Color.WHITE);
            txtStop.setTextColor(Color.WHITE);
            titleNow.setTextColor(Color.WHITE);
            btnnext.setColorFilter(Color.DKGRAY);
            btnprev.setColorFilter(Color.DKGRAY);
            repeat.setColorFilter(Color.DKGRAY);
            volumer.setColorFilter(Color.DKGRAY);
            back.setColorFilter(Color.DKGRAY);
            back.setColorFilter(Color.DKGRAY);
            cardView.setCardBackgroundColor(Color.DKGRAY);
            btnShuffle.setColorFilter(Color.DKGRAY);
            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
            seekBar.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
            volume.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
            volume.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //   mSensorManager.unregisterListener(mSensorListener);
        unbindService(this);
        //  player = false;
    }

    @Override
    protected void onResume() {
        verifyCheck();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        shakeThreadBtn();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        verifyCheck();
        super.onResume();
    }

    private void shakeThreadBtn() {
        shakeThread = new Thread() {
            @Override
            public void run() {
                super.run();
                shake.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shakeBtnClicked();
                    }
                });
            }
        };
        shakeThread.start();
    }

    public void shakeBtnClicked() {
        if (!shakeBoolean) {

            shakeBoolean = true;
            //  shake.setImageResource(R.drawable.ic_sense);
        } else {

            shakeBoolean = false;
            //   shake.setImageResource(R.drawable.ic_vibrate);
        }
        verifyCheck();
    }

    private boolean checkShaker() {

        return shakeBoolean;
    }

    private void prevThreadBtn() {
        prevThread = new Thread() {
            @Override
            public void run() {
                super.run();
                btnprev.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }

    public void prevBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBool) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBool) {
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPLayer(position);
            metaData(uri);
            txtName.setText(listSongs.get(position).getTitle());
            txtArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);

                    }
                    handler.postDelayed(this, 1000);
                }


            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            btnplay.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
            int audioSessionId = musicService.getAudioSessionId();
        } else {
            musicService.stop();
            musicService.release();
            position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPLayer(position);
            metaData(uri);
            txtName.setText(listSongs.get(position).getTitle());
            txtArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);

                    }
                    handler.postDelayed(this, 1000);
                }


            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
            btnplay.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread() {
            @Override
            public void run() {
                super.run();
                btnnext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (musicService.isPlaying()) {
            musicService.stop();
            musicService.release();
            if (!shuffleBoolean && repeatBool) {
                position = getRandom(listSongs.size() - 1);
            } else if (shuffleBoolean && repeatBool) {
                position = ((position + 1) % listSongs.size());
            }
            position = ((position + 1) % listSongs.size());
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPLayer(position);
            metaData(uri);
            txtName.setText(listSongs.get(position).getTitle());
            txtArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);

                    }
                    handler.postDelayed(this, 1000);

                }


            });

            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            btnplay.setBackgroundResource(R.drawable.ic_pause);
            musicService.start();
            SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
            editor.putInt("pos", musicService.position);
            editor.apply();

        } else {

            if (shuffleBoolean && !repeatBool) {
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBool) {
                position = ((position + 1) % listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());

            musicService.createMediaPLayer(position);
            metaData(uri);
            txtName.setText(listSongs.get(position).getTitle());
            txtArtist.setText(listSongs.get(position).getArtist());
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
            btnplay.setBackgroundResource(R.drawable.ic_play);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    private void playThreadBtn() {
        playThread = new Thread() {
            @Override
            public void run() {
                super.run();
                btnplay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            IS_PLAYING = false;
            player = false;
            verifyCheck();
            secChecker();
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);

                    }
                    handler.postDelayed(this, 1000);
                }
            });

        } else {
            musicService.start();
            IS_PLAYING = true;
            player = true;
            verifyCheck();
            secChecker();
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int currentPos = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(currentPos);

                    }
                    handler.postDelayed(this, 1000);
                }


            });

        }
    }

    public void ImageAnimator(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallback(this);

        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        txtName.setText(listSongs.get(position).getTitle());
        txtArtist.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.ic_pause);


    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        Slide slide = new Slide();
        slide.setDuration(6000);
        slide.setSlideEdge(Gravity.TOP);
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(slide);
    }

    private void verifyCheck() {
        if (shuffleBoolean) {
            btnShuffle.setImageResource(R.drawable.ic_shuffle);
        } else {
            btnShuffle.setImageResource(R.drawable.ic_not_shuffle);
        }
        if (repeatBool) {

            repeat.setImageResource(R.drawable.ic_one_repeat);
        } else {
            repeat.setImageResource(R.drawable.ic_repeat);
        }
        if (shakeBoolean) {

            shake.setImageResource(R.drawable.ic_vibrate);
        }
        //when it's on i.e true
        else {
            shake.setImageResource(R.drawable.ic_sense);
        }
        if (player) {
            btnplay.setImageResource(R.drawable.ic_pause);

        } else {
            btnplay.setImageResource(R.drawable.ic_baseline_play_arrow_24);

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (CURRENT_VOLUME == audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC)) {
                isMuted = true;
                setMuter();
            } else {
                isMuted = false;
                setMuter();
            }
        }

    }

    void secChecker() {
        if (player) {

            musicService.showNotification(R.drawable.ic_pause);
        } else {

            musicService.showNotification(R.drawable.ic_baseline_play_arrow_24);
        }

    }

    void setMuter() {
        if (isMuted) {
            volumer.setImageResource(R.drawable.ic_baseline_volume_mute_24);
        } else {
            volumer.setImageResource(R.drawable.ic_baseline_volume_up_24);
        }
    }
}