package com.praise.musixx;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.content.Context.MODE_PRIVATE;
import static com.praise.musixx.MainActivity.ARTIST_TO_FRAG;
import static com.praise.musixx.MainActivity.PATH_TO_FRAG;
import static com.praise.musixx.MainActivity.SHOW_MINI_PLAYER;
import static com.praise.musixx.MainActivity.SONG_TO_FRAG;

import java.io.IOException;


public class NowPlaying extends Fragment implements ServiceConnection {

    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST";
    public static final String SONG_NAME = "SONG NAME";
    public static int POSITION = -1;
    public static boolean IS_PLAYING;
    public int pos;
    ImageView nextBtn, albumArt;
    TextView artist, song;
    FloatingActionButton playpBtn;
    CardView cardView;
    View view;
    MusicService musicService;

    public NowPlaying() {

    }

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        LayoutInflater lf = getActivity().getLayoutInflater();
        view = lf.inflate(R.layout.music_playing, container, false); //pass the correct layout name for the fragment
        song = (TextView) view.findViewById(R.id.txtSongN);
        artist = (TextView) view.findViewById(R.id.txtSongNArtist);
        nextBtn = (ImageView) view.findViewById(R.id.next);
        albumArt = (ImageView) view.findViewById(R.id.imgSongg);
        playpBtn = view.findViewById(R.id.play);
        cardView = view.findViewById(R.id.cardOpen);


        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                pos = preferences.getInt("POSITION", -1);
                Intent intent = new Intent(getContext(), PlayerActivity.class);
                intent.putExtra("pos", pos);
                getContext().startActivity(intent);
                IS_PLAYING = true;
                verify();

            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService != null) {
                    musicService.nextBtn();
                    if (getActivity() != null) {
                        musicService.nextBtn();
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.get(musicService.position).getTitle());
                        editor.putInt("pos", musicService.position);
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE, null);
                        String artistname = preferences.getString(ARTIST_NAME, null);
                        String songname = preferences.getString(SONG_NAME, null);
                        pos = preferences.getInt("POSITION", -1);
                        // musicService.createMediaPLayer(pos);
                        musicService.UpdatePosition(pos);

                        if (path != null) {
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_TO_FRAG = artistname;
                            SONG_TO_FRAG = songname;
                            POSITION = pos;
                        } else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_TO_FRAG = null;
                            SONG_TO_FRAG = null;
                            POSITION = -1;
                        }
                        if (SHOW_MINI_PLAYER) {
                            if (PATH_TO_FRAG != null) {
                                byte[] art = new byte[0];
                                try {
                                    art = getAlbumArt(PATH_TO_FRAG);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                if (art != null) {
                                    Glide.with(getContext()).load(art).into(albumArt);

                                } else {
                                    Glide.with(getContext()).load(R.drawable.headphones).into(albumArt);
                                }
                                song.setText(SONG_TO_FRAG);
                                artist.setText(ARTIST_TO_FRAG);
                                pos = POSITION;
                                SharedPreferences.Editor editor1 = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
                                editor1.putInt("POSITION", pos);
                                editor1.apply();

                            }


                        }
                    } else {
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                        pos = preferences.getInt("POSITION", -1);
                        Intent intent = new Intent(getContext(), PlayerActivity.class);
                        intent.putExtra("pos", pos);
                        getContext().startActivity(intent);
                    }
                } else {

                    if (musicService.mediaPlayer != null) {
                        musicService.nextBtn();
                    } else {
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                        pos = preferences.getInt("POSITION", -1);
                        Intent intent = new Intent(getContext(), PlayerActivity.class);
                        intent.putExtra("pos", pos);
                        getContext().startActivity(intent);
                    }
                }

            }
        });
        playpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.mediaPlayer != null) {
                    if (musicService.mediaPlayer.isPlaying()) {
                        musicService.playBtn();
                        playpBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
                        IS_PLAYING = false;
                    } else {
                        musicService.playBtn();
                        playpBtn.setImageResource(R.drawable.ic_pause);
                        IS_PLAYING = true;
                    }
                } else {
                    SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
                    pos = preferences.getInt("POSITION", -1);
                    Intent intent = new Intent(getContext(), PlayerActivity.class);
                    intent.putExtra("pos", pos);
                    getContext().startActivity(intent);
                }

            }
        });

        return view;
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onResume() {
        super.onResume();
        verify();
        if (SHOW_MINI_PLAYER) {
            if (PATH_TO_FRAG != null) {
                byte[] art = new byte[0];
                try {
                    art = getAlbumArt(PATH_TO_FRAG);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (art != null) {
                    Glide.with(getContext()).load(art).into(albumArt);
                } else {
                    Glide.with(getContext()).load(R.drawable.headphones).into(albumArt);
                }
                song.setText(SONG_TO_FRAG);
                artist.setText(ARTIST_TO_FRAG);


                //  playpBtn.setImageResource(R.drawable.ic_pause);
                Intent intent = new Intent(getContext(), MusicService.class);
                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
//
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            getContext().unbindService(this);
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
        IS_PLAYING = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (getContext() == null) {
            musicService = new MusicService();
        } else {
            musicService = null;
        }
        IS_PLAYING = false;
    }

    void verify() {
        if (IS_PLAYING) {
            playpBtn.setImageResource(R.drawable.ic_pause);
            // IS_PLAYING = false;
        } else {
            playpBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            //  IS_PLAYING = true;
        }
    }


}
