package com.zr.ijkAudioPlayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zr.ijkAudioPlayer.R;
import com.zr.ijkaudioplayer.bean.SongInfo;

import java.util.List;

public class LocalListAdapter extends RecyclerView.Adapter<LocalListAdapter.ViewHolder> {
    private final List<SongInfo> songList;
    private OnItemClickListener onItemClickListener;

    public LocalListAdapter(List<SongInfo> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_local_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongInfo song = songList.get(position);
        holder.tvName.setText(song.getSong_Name());
        holder.tvSize.setText(song.getDuration());
        holder.layout.setOnClickListener(v -> {
            if (onItemClickListener != null){
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView tvName;
        TextView tvSize;

        public ViewHolder(@NonNull View v) {
            super(v);
            layout = v.findViewById(R.id.lin_item);
            tvName = v.findViewById(R.id.tv_name);
            tvSize = v.findViewById(R.id.tv_size);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}