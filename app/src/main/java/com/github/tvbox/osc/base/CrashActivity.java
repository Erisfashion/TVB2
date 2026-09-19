package com.github.tvbox.osc.base;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class CrashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String crashLog = getIntent().getStringExtra("crash_log");
        if (crashLog == null) {
            crashLog = "No crash log available.";
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(0xFF1E1E1E); // 深灰背景
        scrollView.setPadding(30, 30, 30, 30);

        TextView textView = new TextView(this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setTextColor(0xFFFF5252); // 红色报警文本
        textView.setTextSize(14);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText("【TVBox 启动崩溃日志 (已保存至 /sdcard/crash.log)】\n\n" + crashLog);

        scrollView.addView(textView);
        setContentView(scrollView);
    }
}
