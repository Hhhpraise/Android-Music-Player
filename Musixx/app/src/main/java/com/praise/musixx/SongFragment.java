package com.praise.musixx;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.praise.musixx.MainActivity.musicFiles;

public class SongFragment extends Fragment {
    static SongsAdapter songsAdapter;
    RecyclerView recyclerView;


    public SongFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.song_frag, container, false);
        recyclerView = view.findViewById(R.id.recycler);


        recyclerView.setHasFixedSize(true);
        if (!(musicFiles.size() < 1)) {
            songsAdapter = new SongsAdapter(getContext(), musicFiles);
            recyclerView.setAdapter(songsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        }

        return view;
    }
}
