package com.example.soundbarlib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SoundBarPlayerView extends View {

    private List<Integer> amplitudes = new ArrayList<>();
    private int playedBars = 0;
    private Paint gradientPaint;
    private Paint backgroundPaint;
    private Paint glowPaint;
    private Paint timePaint;
    private Paint bubblePaint;
    private Paint bubbleTextPaint;
    private OnSeekListener seekListener;
    private float durationInSeconds = 0f;
    private boolean isSeeking = false;
    private float seekX = 0f;

    public interface OnSeekListener {
        void onSeekTo(float percent);
    }

    public SoundBarPlayerView(Context context) {
        super(context);
        init();
    }

    public SoundBarPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFDDDDDD);
        backgroundPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(0x4400E676);
        glowPaint.setMaskFilter(new android.graphics.BlurMaskFilter(10, android.graphics.BlurMaskFilter.Blur.NORMAL));

        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(0xFF333333);
        timePaint.setTextSize(40f);
        timePaint.setFakeBoldText(true);

        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(0xFFFAFAFA);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setShadowLayer(10f, 0f, 4f, 0x55000000);

        bubbleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubbleTextPaint.setColor(0xFF000000);
        bubbleTextPaint.setTextSize(34f);
        bubbleTextPaint.setFakeBoldText(true);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void loadAudio(File file) {
        this.durationInSeconds = extractDurationInSeconds(file);
        int barCount = getWidth() > 0 ? getWidth() / 4 : 300;
        amplitudes = WaveformGenerator.generateFromWav(file, barCount);
        playedBars = 0;
        invalidate();
    }

    public void loadAudioResource(Context context, int resId) {
        File wavFile = FileUtil.rawToWavFile(context, resId);
        if (wavFile != null) {
            loadAudio(wavFile);
        }
    }

    private float extractDurationInSeconds(File file) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(file.getAbsolutePath());
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            mmr.release();
            return Float.parseFloat(durationStr) / 1000f;
        } catch (Exception e) {
            return 0f;
        }
    }

    public void setPlayedBars(int bars) {
        if (bars > amplitudes.size()) bars = amplitudes.size();
        if (bars < 0) bars = 0;
        playedBars = bars;
        invalidate();
    }

    public int getTotalBars() {
        return amplitudes.size();
    }

    public void setOnSeekListener(OnSeekListener listener) {
        this.seekListener = listener;
    }

    private String formatTime(float seconds) {
        int mins = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", mins, secs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudes == null || amplitudes.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int barCount = amplitudes.size();
        float barWidth = (float) width / barCount;
        float centerY = height * 0.4f;

        for (int i = 0; i < barCount; i++) {
            float left = i * barWidth;
            float amp = amplitudes.get(i) / 100f * height * 0.6f;
            float top = centerY - amp / 2;
            float bottom = centerY + amp / 2;
            canvas.drawRoundRect(left, top, left + barWidth, bottom, 6f, 6f, backgroundPaint);
        }

        LinearGradient gradient = new LinearGradient(
                0, 0, width, 0,
                new int[]{0xFF00E676, 0xFF1DE9B6, 0xFF00BFA5},
                null,
                Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);

        Path waveformPath = new Path();
        for (int i = 0; i < playedBars; i++) {
            float left = i * barWidth;
            float amp = amplitudes.get(i) / 100f * height * 0.6f;
            float top = centerY - amp / 2;
            float bottom = centerY + amp / 2;
            waveformPath.addRoundRect(left, top, left + barWidth, bottom, 6f, 6f, Path.Direction.CW);
        }
        canvas.drawPath(waveformPath, gradientPaint);

        if (playedBars > 0 && playedBars < amplitudes.size()) {
            float glowLeft = playedBars * barWidth;
            float glowAmp = amplitudes.get(playedBars) / 100f * height * 0.6f;
            float glowTop = centerY - glowAmp / 2;
            float glowBottom = centerY + glowAmp / 2;
            canvas.drawRoundRect(glowLeft, glowTop, glowLeft + barWidth, glowBottom, 6f, 6f, glowPaint);
        }

        float currentSeconds = (playedBars / (float) barCount) * durationInSeconds;
        String currentTime = formatTime(currentSeconds);
        String totalTime = formatTime(durationInSeconds);

        float margin = 5f;
        float yTime = getHeight() - margin;
        canvas.drawText(currentTime, margin, yTime, timePaint);
        float totalWidth = timePaint.measureText(totalTime);
        canvas.drawText(totalTime, width - totalWidth - margin, yTime, timePaint);

        if (isSeeking) {
            float percent = seekX / getWidth();
            if (percent < 0f) percent = 0f;
            if (percent > 1f) percent = 1f;
            float bubbleX = seekX;
            float bubbleY = centerY - height * 0.3f;
            float radius = 75f;
            String timeStr = formatTime(durationInSeconds * percent);
            canvas.drawCircle(bubbleX, bubbleY, radius, bubblePaint);
            float textWidth = bubbleTextPaint.measureText(timeStr);
            canvas.drawText(timeStr, bubbleX - textWidth / 2, bubbleY + bubbleTextPaint.getTextSize() / 3, bubbleTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (seekListener == null || amplitudes.isEmpty()) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isSeeking = true;
                seekX = event.getX();
                float percent = seekX / getWidth();
                if (percent < 0f) percent = 0f;
                if (percent > 1f) percent = 1f;
                seekListener.onSeekTo(percent);
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isSeeking = false;
                invalidate();
                return true;
        }
        return false;
    }
}
