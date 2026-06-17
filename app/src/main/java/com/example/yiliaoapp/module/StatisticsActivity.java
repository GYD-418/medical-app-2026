package com.example.yiliaoapp.module;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.MedicalApp;
import com.example.yiliaoapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.yiliaoapp.sync.SyncManager;

public class StatisticsActivity extends AppCompatActivity {
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private int totalDevices = 0;
    private int runningRate = 100;
    private int avgRepairHours = 8;
    private int pendingSync = 0;
    private TextView tvStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        tvStats = findViewById(R.id.tvStatistics);
        MaterialButton btnRefresh = findViewById(R.id.btnRefreshStats);
        MaterialButton btnSyncNow = findViewById(R.id.btnSyncNow);
        MaterialButton btnOpenSyncLog = findViewById(R.id.btnOpenSyncLog);

        loadAndRender();
        btnRefresh.setOnClickListener(v -> loadAndRender());
        btnSyncNow.setOnClickListener(v -> syncNow());
        btnOpenSyncLog.setOnClickListener(v -> startActivity(new Intent(this, SyncLogActivity.class)));
    }

    private void loadAndRender() {
        ioExecutor.execute(() -> {
            totalDevices = ((MedicalApp) getApplication()).getDatabase().deviceDao().count();
            int abnormal = ((MedicalApp) getApplication()).getDatabase().inspectionDao().countAbnormal();
            int totalInspection = abnormal + ((MedicalApp) getApplication()).getDatabase().inspectionDao().countNormal();
            pendingSync = ((MedicalApp) getApplication()).getDatabase().syncTaskDao().countPending();
            runningRate = totalInspection == 0 ? 100 : Math.max(0, 100 - (abnormal * 100 / totalInspection));
            avgRepairHours = abnormal == 0 ? 6 : Math.max(4, 12 - abnormal);
            runOnUiThread(this::render);
        });
    }

    private void syncNow() {
        ioExecutor.execute(() -> {
            SyncManager.SyncResult result = new SyncManager(((MedicalApp) getApplication()).getDatabase()).syncPending();
            pendingSync = ((MedicalApp) getApplication()).getDatabase().syncTaskDao().countPending();
            runOnUiThread(() -> {
                render();
                Toast.makeText(
                        this,
                        "同步完成，成功 " + result.successCount + " 条，失败 " + result.failedCount + " 条",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }

    private void render() {
        tvStats.setText(
                "设备总数: " + totalDevices + " 台\n" +
                        "设备完好率: " + runningRate + "%\n" +
                        "平均维修时长: " + avgRepairHours + " 小时\n" +
                        "待同步任务: " + pendingSync + " 条\n" +
                        "建议: 保持每周巡检频次，优先维护高故障设备。"
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
