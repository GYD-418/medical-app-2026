package com.example.yiliaoapp.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.MedicalApp;
import com.example.yiliaoapp.R;
import com.example.yiliaoapp.data.RepairOrderEntity;
import com.example.yiliaoapp.data.SyncTaskEntity;
import com.example.yiliaoapp.db.AppDatabase;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepairActivity extends AppCompatActivity {
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private int pending = 0;
    private int done = 0;
    private RepairOrderEntity currentPendingOrder;
    private TextView tvRepair;
    private ImageView ivFaultImage;
    private TextView tvNoFaultImage;
    private TextView tvOrderDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repair);

        tvRepair = findViewById(R.id.tvRepair);
        ivFaultImage = findViewById(R.id.ivFaultImage);
        tvNoFaultImage = findViewById(R.id.tvNoFaultImage);
        tvOrderDescription = findViewById(R.id.tvOrderDescription);
        MaterialButton btnCreateOrder = findViewById(R.id.btnCreateRepair);
        MaterialButton btnFinishOrder = findViewById(R.id.btnFinishRepair);
        MaterialButton btnReset = findViewById(R.id.btnResetRepair);

        loadCounter();
        btnCreateOrder.setOnClickListener(v -> createOrder());
        btnFinishOrder.setOnClickListener(v -> finishOrder());
        btnReset.setOnClickListener(v -> resetPageData());
    }

    private void resetPageData() {
        ioExecutor.execute(() -> {
            AppDatabase db = ((MedicalApp) getApplication()).getDatabase();
            db.repairOrderDao().deleteAll();
            db.syncTaskDao().deleteRepairRelatedTasks();
            pending = db.repairOrderDao().countPending();
            done = db.repairOrderDao().countDone();
            currentPendingOrder = null;
            runOnUiThread(() -> {
                render();
                showCurrentOrderImage();
                Toast.makeText(this, "已清空报修工单及关联同步任务", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadCounter() {
        ioExecutor.execute(() -> {
            AppDatabase db = ((MedicalApp) getApplication()).getDatabase();
            pending = db.repairOrderDao().countPending();
            done = db.repairOrderDao().countDone();
            List<RepairOrderEntity> pendingList = db.repairOrderDao().findPendingAll();
            currentPendingOrder = pendingList.isEmpty() ? null : pendingList.get(0);
            runOnUiThread(() -> {
                render();
                showCurrentOrderImage();
            });
        });
    }

    private void createOrder() {
        ioExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            String operator = ((MedicalApp) getApplication()).getCurrentUsername();
            if (operator == null) operator = "unknown";
            ((MedicalApp) getApplication()).getDatabase().repairOrderDao()
                    .insert(new RepairOrderEntity("PENDING", "MANUAL", "手动创建维修工单", now, 0L));
            ((MedicalApp) getApplication()).getDatabase().syncTaskDao()
                    .insert(new SyncTaskEntity("REPAIR_ORDER_CREATE", "{\"source\":\"manual\"}", "PENDING", now, operator));
            pending = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().countPending();
            done = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().countDone();
            List<RepairOrderEntity> pendingList = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().findPendingAll();
            currentPendingOrder = pendingList.isEmpty() ? null : pendingList.get(0);
            runOnUiThread(() -> {
                render();
                showCurrentOrderImage();
                Toast.makeText(this, "已新增工单", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void finishOrder() {
        ioExecutor.execute(() -> {
            Long id = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().findOldestPendingId();
            if (id != null) {
                long now = System.currentTimeMillis();
                String operator = ((MedicalApp) getApplication()).getCurrentUsername();
                if (operator == null) operator = "unknown";
                ((MedicalApp) getApplication()).getDatabase().repairOrderDao().finishById(id, now);
                ((MedicalApp) getApplication()).getDatabase().syncTaskDao()
                        .insert(new SyncTaskEntity("REPAIR_ORDER_DONE", "{\"id\":" + id + "}", "PENDING", now, operator));
            }
            pending = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().countPending();
            done = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().countDone();
            List<RepairOrderEntity> pendingList = ((MedicalApp) getApplication()).getDatabase().repairOrderDao().findPendingAll();
            currentPendingOrder = pendingList.isEmpty() ? null : pendingList.get(0);
            runOnUiThread(() -> {
                render();
                showCurrentOrderImage();
                Toast.makeText(this, id == null ? "暂无待处理工单" : "已完成最早待处理工单", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showCurrentOrderImage() {
        if (currentPendingOrder == null || currentPendingOrder.imagePath == null) {
            ivFaultImage.setImageBitmap(null);
            ivFaultImage.setVisibility(android.view.View.GONE);
            tvNoFaultImage.setVisibility(android.view.View.VISIBLE);
            tvOrderDescription.setVisibility(android.view.View.GONE);
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(currentPendingOrder.imagePath);
        if (bitmap != null) {
            ivFaultImage.setImageBitmap(bitmap);
            ivFaultImage.setVisibility(android.view.View.VISIBLE);
            tvNoFaultImage.setVisibility(android.view.View.GONE);
            tvOrderDescription.setVisibility(android.view.View.VISIBLE);
            tvOrderDescription.setText("工单描述: " + currentPendingOrder.description
                    + "\n来源: " + ("INSPECTION".equals(currentPendingOrder.source) ? "巡检自动生成" : "手动创建"));
        } else {
            ivFaultImage.setImageBitmap(null);
            ivFaultImage.setVisibility(android.view.View.GONE);
            tvNoFaultImage.setVisibility(android.view.View.VISIBLE);
            tvOrderDescription.setVisibility(android.view.View.GONE);
        }
    }

    private void render() {
        tvRepair.setText("报修工单: 待处理 " + pending + " 单, 已完成 " + done + " 单");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
