package com.example.soundbar;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundbarlib.SoundBarPlayerView;

public class MainActivity extends AppCompatActivity {

    private SoundBarPlayerView soundBarView;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    private boolean isPlaying = false;

    private int[] songResIds = {R.raw.mysong, R.raw.mysong2, R.raw.mysong3};
    private String[] songNames = {"MC Menor JP - Menina de Vermlho", "Bad Bunny - Te Bote", "Sia - Unstoppable"};
    private int currentSongIndex = 0;

    private Button playPauseBtn;
    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundBarView = findViewById(R.id.soundBarView);
        playPauseBtn = findViewById(R.id.playButton);
        ListView songListView = findViewById(R.id.songList);

        adapter = new SongAdapter(this, songNames);
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener((parent, view, position, id) -> {
            currentSongIndex = position;
            adapter.setPlayingIndex(position);
            playSelectedSong();
        });

        playPauseBtn.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    isPlaying = false;
                } else {
                    mediaPlayer.start();
                    isPlaying = true;
                    startUpdating();
                }
            }
        });

        soundBarView.setOnSeekListener(percent -> {
            if (mediaPlayer != null && soundBarView.getTotalBars() > 0) {
                int seekTo = (int) (mediaPlayer.getDuration() * percent);
                mediaPlayer.seekTo(seekTo);
                soundBarView.setPlayedBars((int) (percent * soundBarView.getTotalBars()));
            }
        });
    }

    private void playSelectedSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            handler.removeCallbacks(updateRunnable);
        }

        int resId = songResIds[currentSongIndex];
        soundBarView.loadAudioResource(this, resId);
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.start();
        isPlaying = true;
        startUpdating();

        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            handler.removeCallbacks(updateRunnable);
        });
    }

    private void startUpdating() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    int position = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    if (duration > 0) {
                        float percent = position / (float) duration;
                        int playedBars = (int) (percent * soundBarView.getTotalBars());
                        soundBarView.setPlayedBars(playedBars);
                    }
                    handler.postDelayed(this, 50);
                }
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
