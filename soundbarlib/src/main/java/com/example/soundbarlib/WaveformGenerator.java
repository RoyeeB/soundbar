package com.example.soundbarlib;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class WaveformGenerator {

    private static final int NOISE_THRESHOLD = 5; // עוצמה מתחת לזה תיחשב כרעש רקע
    private static final double OFFSET_BARS = -0.5 ; // פיצוי על דיליי בניגון

    public static List<Integer> generateFromWav(File wavFile, int targetBarsCount) {
        List<Integer> rawAmplitudes = new ArrayList<>();
        int maxAmp = 1; // נשתמש בזה לנירמול בהמשך

        try (FileInputStream inputStream = new FileInputStream(wavFile)) {
            byte[] header = new byte[44];
            inputStream.read(header);

            int bytesPerSample = 2; // 16-bit PCM
            int frameSize = bytesPerSample;
            long totalSamples = (wavFile.length() - 44) / frameSize;
            long samplesPerBar = totalSamples / targetBarsCount;
            if (samplesPerBar < 1) samplesPerBar = 1;

            byte[] buffer = new byte[(int) (samplesPerBar * frameSize)];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                int sum = 0;
                int count = 0;
                for (int i = 0; i + 1 < read; i += 2) {
                    int value = (buffer[i + 1] << 8) | (buffer[i] & 0xFF);
                    sum += Math.abs(value);
                    count++;
                }
                if (count > 0) {
                    int average = sum / count;
                    maxAmp = Math.max(maxAmp, average);
                    rawAmplitudes.add(average);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // שלב נירמול לפי המקסימום שמצאנו
        List<Integer> finalAmplitudes = new ArrayList<>();
        for (int avg : rawAmplitudes) {
            float normalized = avg / (float) maxAmp;
            int scaled = (int) (normalized * 100); // בין 0–100
            finalAmplitudes.add(scaled < NOISE_THRESHOLD ? 0 : scaled);
        }

        // הוספת OFFSET קטן בתחילת הרשימה לפיצוי על דיליי
        for (int i = 0; i < OFFSET_BARS; i++) {
            finalAmplitudes.add(0, 0);
        }

        return finalAmplitudes;
    }
}
