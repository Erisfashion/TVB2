package com.github.tvbox.osc.ui.tv;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Hashtable;

public class QRCodeGen {

    // 适配 PushActivity.java 的 4 参数调用
    public static Bitmap generateBitmap(String content, int width, int height, int margin) {
        try {
            if (content == null || content.isEmpty()) {
                return null;
            }
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.MARGIN, margin);
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK;
                    } else {
                        pixels[y * width + x] = Color.WHITE;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // 适配 ApiDialog.java 的 3 参数调用
    public static Bitmap generateBitmap(String content, int width, int height) {
        return generateBitmap(content, width, height, 1);
    }

    public static Bitmap createQRCode(String content) {
        return generateBitmap(content, 300, 300, 1);
    }
}
