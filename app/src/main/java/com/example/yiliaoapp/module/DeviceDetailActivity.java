package com.example.yiliaoapp.module;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeviceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);

        String name = getIntent().getStringExtra("name");
        String code = getIntent().getStringExtra("code");
        String department = getIntent().getStringExtra("department");
        long createdAt = getIntent().getLongExtra("createdAt", 0L);

        TextView tvDetail = findViewById(R.id.tvDetail);
        String time = createdAt == 0L ? "-" : new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date(createdAt));
        tvDetail.setText(
                "设备详情\n\n" +
                        "设备名称: " + nullSafe(name) + "\n" +
                        "设备编号: " + nullSafe(code) + "\n" +
                        "所属科室: " + nullSafe(department) + "\n" +
                        "建档时间: " + time + "\n\n" +
                        "后续可扩展: 巡检记录、故障历史、维修工单、耗材关联。"
        );
    }

    private String nullSafe(String text) {
        return text == null ? "-" : text;
    }
}
