package com.praise.musixx;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.MyViewHolder> {

    static ArrayList<MusicFiles> mFiles;
    Context context;

    public SongsAdapter(Context context, ArrayList<MusicFiles> musicFiles) {
        this.context = context;
        this.mFiles = musicFiles;
    }

    @NonNull

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txtSongName.setText(mFiles.get(position).getTitle());
        byte[] image = new byte[0];
        try {
            image = getAlbumArt(mFiles.get(position).getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (image != null) {
            Glide.with(context).asBitmap()
                    .load(image)
                    .into(holder.albumArt);

        } else {
            Glide.with(context).load(R.drawable.music).into(holder.albumArt);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("pos", position);
                context.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener((item -> {
                    switch (item.getItemId()) {

                        case R.id.delete:
                            Toast.makeText(context, "deleteClicked", Toast.LENGTH_SHORT).show();
                            delete(position, v);
                            break;
                    }
                    return true;
                }));
            }
        });

    }

    private void delete(int position, View v) {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId()));

        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if (deleted) {
            context.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(v, "File deleted", Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(v, "File not deleted", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    void updateList(ArrayList<MusicFiles> musicFilesArrayList) {
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtSongName;
        ImageView albumArt, menuMore;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            txtSongName = itemView.findViewById(R.id.txtSongName);
            albumArt = itemView.findViewById(R.id.imgSong);
            menuMore = itemView.findViewById(R.id.more);
        }
    }
}
