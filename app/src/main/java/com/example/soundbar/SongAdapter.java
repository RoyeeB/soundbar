package com.example.soundbar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SongAdapter extends BaseAdapter {

    private Context context;
    private String[] songNames;
    private int playingIndex = -1;

    public SongAdapter(Context context, String[] songNames) {
        this.context = context;
        this.songNames = songNames;
    }

    public void setPlayingIndex(int index) {
        this.playingIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return songNames.length;
    }

    @Override
    public Object getItem(int position) {
        return songNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        }

        TextView textView = view.findViewById(R.id.songName);
        ImageView icon = view.findViewById(R.id.icon);

        textView.setText(songNames[position]);

        if (position == playingIndex) {
            view.setBackgroundColor(0xFFE0E0E0); // תכלת
            icon.setVisibility(View.VISIBLE);
        } else {
            view.setBackgroundColor(0x00000000); // שקוף
            icon.setVisibility(View.GONE);
        }

        return view;
    }
}
