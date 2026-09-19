package com.github.tvbox.osc.base;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static CrashHandler INSTANCE;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {}

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String crashReport = handleException(ex);

        // 尝试启动展示崩溃堆栈的专用界面
        try {
            Intent intent = new Intent(mContext, CrashActivity.class);
            intent.putExtra("crash_log", crashReport);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(intent);
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start CrashActivity", t);
            if (mDefaultHandler != null) {
                mDefaultHandler.uncaughtException(thread, ex);
                return;
            }
        }

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private String handleException(Throwable ex) {
        if (ex == null) return "";

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sb.append("Time: ").append(sdf.format(new Date())).append("\n");
        sb.append("Android SDK: ").append(Build.VERSION.SDK_INT).append(" (").append(Build.VERSION.RELEASE).append(")\n");
        sb.append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n");
        sb.append("CPU ABI: ").append(Build.CPU_ABI).append(", ").append(Build.CPU_ABI2).append("\n\n");

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        sb.append(writer.toString());

        String logStr = sb.toString();
        Log.e(TAG, logStr);

        // 1. 写入 SD 卡根目录
        saveToFile(new File(Environment.getExternalStorageDirectory(), "crash.log"), logStr);
        // 2. 写入 App 私有目录（双保险）
        if (mContext != null && mContext.getExternalFilesDir(null) != null) {
            saveToFile(new File(mContext.getExternalFilesDir(null), "crash.log"), logStr);
        }

        return logStr;
    }

    private void saveToFile(File file, String content) {
        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(content.getBytes("UTF-8"));
            fos.flush();
            fos.getFD().sync();
            fos.close();
        } catch (Throwable ignored) {
        }
    }
}
