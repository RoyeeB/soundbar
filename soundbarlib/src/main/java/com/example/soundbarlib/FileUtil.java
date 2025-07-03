package com.example.soundbarlib;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

    public static File rawToWavFile(Context context, int rawResId) {
        try {
            InputStream inputStream = context.getResources().openRawResource(rawResId);
            File tempFile = File.createTempFile("audio", ".wav", context.getCacheDir());
            tempFile.deleteOnExit();

            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
