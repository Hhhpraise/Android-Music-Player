package com.praise.musixx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST";
    public static final String SONG_NAME = "SONG NAME";
    public static boolean SHOW_MINI_PLAYER = false;
    public static String PATH_TO_FRAG = null;
    public static String ARTIST_TO_FRAG = null;
    public static String SONG_TO_FRAG = null;
    public static int POSITION = -1;
    static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean = false, repeatBool = false, shakeBoolean = false;
    public boolean MY_SORT_PREFERENCE = false;
    String[] items;
    SearchView searchView;
    ImageView imageView;
    SwipeRefreshLayout swipeRefreshLayout;
    FrameLayout frameLayout;

    public ArrayList<MusicFiles> getAllAudio(Context context) {
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        String order = null;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        if (MY_SORT_PREFERENCE) {
            order = MediaStore.MediaColumns.DATE_ADDED + " ASC";

        } else {
            order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";

        }

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media._ID
        };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, order);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String album = cursor.getString(0);
                String artist = cursor.getString(1);
                String duration = cursor.getString(2);
                String title = cursor.getString(3);
                String path = cursor.getString(4);
                String id = cursor.getString(5);
                MusicFiles musicFiles = new MusicFiles(path, album, artist, title, duration, id);
                // Log.e("path : "+path,"album :"+album);
                tempAudioList.add(musicFiles);
            }
            cursor.close();
        }

        return tempAudioList;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupWindowAnimations();
        searchView = (SearchView) findViewById(R.id.search_view);
        swipeRefreshLayout = findViewById(R.id.swipe);
        imageView = findViewById(R.id.sorter);
        frameLayout = findViewById(R.id.frame);

        runtimePermission();
        Fragment fragment1 = new SongFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.songfrag, fragment1).commit();
        musicFiles = getAllAudio(this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setEnabled(false);
                        recreate();
                        getAllAudio(MainActivity.this);
                        Toast.makeText(MainActivity.this, "Music updated", Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String userInput = newText.toLowerCase();
                ArrayList<MusicFiles> myFiles = new ArrayList<>();
                for (MusicFiles song : musicFiles) {
                    if (song.getTitle().toLowerCase().contains(userInput)) {
                        myFiles.add(song);
                    }
                }
                SongFragment.songsAdapter.updateList(myFiles);
                return true;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MY_SORT_PREFERENCE) {
                    imageView.setImageResource(R.drawable.accend);
                    // Toast.makeText(MainActivity.this,"Sorted by date",Toast.LENGTH_SHORT).show();
                    MY_SORT_PREFERENCE = false;


                } else {
                    imageView.setImageResource(R.drawable.ic_sort);
                    // Toast.makeText(MainActivity.this,"Sorted by Name",Toast.LENGTH_SHORT).show();
                    MY_SORT_PREFERENCE = true;

                }
                // getAllAudio(MainActivity.this);

            }
        });
    }

    public void runtimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NOTIFICATION_POLICY, Manifest.permission.RECORD_AUDIO
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                getAllAudio(MainActivity.this);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setRefreshing(false);
        SharedPreferences preferences = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
        String path = preferences.getString(MUSIC_FILE, null);
        String artist = preferences.getString(ARTIST_NAME, null);
        String song = preferences.getString(SONG_NAME, null);
        int pos = preferences.getInt("POSITION", -1);

        if (path != null) {
            SHOW_MINI_PLAYER = true;
            PATH_TO_FRAG = path;
            ARTIST_TO_FRAG = artist;
            SONG_TO_FRAG = song;
            POSITION = pos;
            showMini(SHOW_MINI_PLAYER);

        } else {
            SHOW_MINI_PLAYER = false;
            PATH_TO_FRAG = null;
            ARTIST_TO_FRAG = null;
            SONG_TO_FRAG = null;
            POSITION = -1;
            showMini(SHOW_MINI_PLAYER);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(3000);
        slide.setSlideEdge(Gravity.TOP);
        getWindow().setEnterTransition(slide);
    }

    private void showMini(boolean istoshow) {
        Fragment fragment = new NowPlaying();
        if (istoshow) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
            frameLayout.setVisibility(View.VISIBLE);
        } else {
            frameLayout.setVisibility(View.GONE);
        }


    }

    class customAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textSongName = view.findViewById(R.id.txtSongName);
            textSongName.setSelected(true);
            textSongName.setText(items[position]);
            return view;
        }
    }
}