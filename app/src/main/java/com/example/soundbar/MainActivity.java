package com.example.soundbar;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.soundbarlib.SoundBarPlayerView;

public class MainActivity extends AppCompatActivity {

    private SoundBarPlayerView soundBarView;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateRunnable;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundBarView = findViewById(R.id.soundBarView);
        Button playPauseBtn = findViewById(R.id.playButton);

        // טעינת הקובץ WAV מ־res/raw
        soundBarView.loadAudioResource(this, R.raw.mysong3);

        // הכנת MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.mysong3);

        playPauseBtn.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
            } else {
                mediaPlayer.start();
                isPlaying = true;
                startUpdating();
            }
        });

        // תמיכה בגרירה על הסאונדבר
        soundBarView.setOnSeekListener(percent -> {
            if (mediaPlayer != null && soundBarView.getTotalBars() > 0) {
                int seekTo = (int) (mediaPlayer.getDuration() * percent);
                mediaPlayer.seekTo(seekTo);
                soundBarView.setPlayedBars((int) (percent * soundBarView.getTotalBars()));
            }
        });

        // הפסקת עדכון כאשר השיר נגמר
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
