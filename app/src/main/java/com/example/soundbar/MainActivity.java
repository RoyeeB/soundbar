package com.example.soundbar;

import android.graphics.Color;
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

    private final int[] songResIds = {
            R.raw.mysong,
            R.raw.mysong2,
            R.raw.mysong3
    };

    private final String[] songNames = {
            "MC Menor JP - Menina de Vermlho",
            "Bad Bunny - Te Bote",
            "Sia - Unstoppable"
    };

    private int currentSongIndex = 0;
    private SongAdapter adapter;
    private Button playPauseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initColorButtons();
        initSongList();
        initPlayPauseButton();
        initSeekListener();
    }

    private void initViews() {
        soundBarView = findViewById(R.id.soundBarView);
        playPauseBtn = findViewById(R.id.playButton);
    }

    private void initColorButtons() {
        Button colorButton1 = findViewById(R.id.colorButton1);
        Button colorButton2 = findViewById(R.id.colorButton2);
        Button colorButton3 = findViewById(R.id.colorButton3);

        colorButton1.setOnClickListener(v -> soundBarView.setBarColor(Color.parseColor("#FFEB3B"))); // Yellow
        colorButton2.setOnClickListener(v -> soundBarView.setBarColor(Color.parseColor("#E91E63"))); // Pink
        colorButton3.setOnClickListener(v -> soundBarView.setBarColor(Color.parseColor("#2196F3"))); // Blue
    }

    private void initSongList() {
        ListView songListView = findViewById(R.id.songList);
        adapter = new SongAdapter(this, songNames);
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener((parent, view, position, id) -> {
            currentSongIndex = position;
            adapter.setPlayingIndex(position);
            playSelectedSong();
        });
    }

    private void initPlayPauseButton() {
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
    }

    private void initSeekListener() {
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
