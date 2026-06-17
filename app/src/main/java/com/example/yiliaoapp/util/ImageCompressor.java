package com.example.yiliaoapp.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class ImageCompressor {
    private static final int MAX_WIDTH = 1024;
    private static final int JPEG_QUALITY = 80;

    private ImageCompressor() {
    }

    public static String compressAndSave(Context context, Uri sourceUri) {
        File dir = new File(context.getExternalFilesDir(null), "fault_images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = "temp_" + System.currentTimeMillis() + ".jpg";
        File destFile = new File(dir, fileName);

        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            try (InputStream in = context.getContentResolver().openInputStream(sourceUri)) {
                BitmapFactory.decodeStream(in, null, opts);
            }

            opts.inSampleSize = calculateSampleSize(opts.outWidth, opts.outHeight);
            opts.inJustDecodeBounds = false;

            Bitmap bitmap;
            try (InputStream in = context.getContentResolver().openInputStream(sourceUri)) {
                bitmap = BitmapFactory.decodeStream(in, null, opts);
            }

            if (bitmap == null) {
                return null;
            }

            try (FileOutputStream out = new FileOutputStream(destFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
            }
            bitmap.recycle();

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            if (destFile.exists()) {
                destFile.delete();
            }
            return null;
        }
    }

    private static int calculateSampleSize(int width, int height) {
        int sampleSize = 1;
        while (width / sampleSize > MAX_WIDTH || height / sampleSize > MAX_WIDTH) {
            sampleSize *= 2;
        }
        return sampleSize;
    }
}
