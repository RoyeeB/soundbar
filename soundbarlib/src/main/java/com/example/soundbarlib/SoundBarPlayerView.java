package com.example.soundbarlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
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
    private int barColor = Color.parseColor("#00E676"); // ברירת מחדל

    public interface OnSeekListener {
        void onSeekTo(float percent);
    }

    public SoundBarPlayerView(Context context) {
        super(context);
        init(null);
    }

    public SoundBarPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SoundBarPlayerView);
            barColor = a.getColor(R.styleable.SoundBarPlayerView_barColor, barColor);
            a.recycle();
        }

        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradientPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFFDDDDDD);
        backgroundPaint.setStyle(Paint.Style.FILL);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(applyAlpha(barColor, 0.27f)); // glow מתוך אותו צבע
        glowPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));

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

    public void setBarColor(int color) {
        this.barColor = color;
        glowPaint.setColor(applyAlpha(color, 0.27f));
        invalidate();
    }

    private int applyAlpha(int color, float alpha) {
        int alphaVal = Math.round(Color.alpha(color) * alpha);
        return Color.argb(alphaVal, Color.red(color), Color.green(color), Color.blue(color));
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

        // Gradient צבעוני לפי הצבע שנבחר
        LinearGradient gradient = new LinearGradient(
                0, 0, width, 0,
                new int[]{
                        lightenColor(barColor, 0.4f),
                        barColor,
                        darkenColor(barColor, 0.2f)
                },
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

    // פונקציות עזר לצבעים
    private int lightenColor(int color, float factor) {
        int r = Math.min(255, (int)(Color.red(color) + 255 * factor));
        int g = Math.min(255, (int)(Color.green(color) + 255 * factor));
        int b = Math.min(255, (int)(Color.blue(color) + 255 * factor));
        return Color.rgb(r, g, b);
    }

    private int darkenColor(int color, float factor) {
        int r = Math.max(0, (int)(Color.red(color) * (1 - factor)));
        int g = Math.max(0, (int)(Color.green(color) * (1 - factor)));
        int b = Math.max(0, (int)(Color.blue(color) * (1 - factor)));
        return Color.rgb(r, g, b);
    }
}
