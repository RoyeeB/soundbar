package com.example.soundbarlib;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class WaveformGenerator {

    private static final int NOISE_THRESHOLD = 5;
    private static final double OFFSET_BARS = -0.5 ;

    public static List<Integer> generateFromWav(File wavFile, int targetBarsCount) {
        List<Integer> rawAmplitudes = new ArrayList<>();
        int maxAmp = 1;

        try (FileInputStream inputStream = new FileInputStream(wavFile)) {
            byte[] header = new byte[44];
            inputStream.read(header);

            int bytesPerSample = 2;
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

        List<Integer> finalAmplitudes = new ArrayList<>();
        for (int avg : rawAmplitudes) {
            float normalized = avg / (float) maxAmp;
            int scaled = (int) (normalized * 100);
            finalAmplitudes.add(scaled < NOISE_THRESHOLD ? 0 : scaled);
        }


        for (int i = 0; i < OFFSET_BARS; i++) {
            finalAmplitudes.add(0, 0);
        }

        return finalAmplitudes;
    }
}
