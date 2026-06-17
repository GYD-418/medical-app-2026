package com.example.yiliaoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.module.DeviceArchiveActivity;
import com.example.yiliaoapp.module.DeviceListActivity;
import com.example.yiliaoapp.module.InspectionActivity;
import com.example.yiliaoapp.module.InventoryActivity;
import com.example.yiliaoapp.module.RepairActivity;
import com.example.yiliaoapp.module.StatisticsActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String role = getIntent().getStringExtra("role");
        String username = getIntent().getStringExtra("username");
        if (role == null) {
            role = "ADMIN";
        }
        if (username == null) {
            username = "admin";
        }

        TextView tvUserRole = findViewById(R.id.tvUserRole);
        tvUserRole.setText("当前用户: " + username + "  角色: " + roleLabel(role));

        bindRoute(R.id.btnDeviceList, DeviceListActivity.class);
        bindRoute(R.id.btnDeviceArchive, DeviceArchiveActivity.class);
        bindRoute(R.id.btnInspection, InspectionActivity.class);
        bindRoute(R.id.btnRepair, RepairActivity.class);
        bindRoute(R.id.btnInventory, InventoryActivity.class);
        bindRoute(R.id.btnStatistics, StatisticsActivity.class);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        applyRoleUi(role);
    }

    private static String roleLabel(String role) {
        if ("ADMIN".equals(role)) {
            return "管理员";
        }
        if ("INSPECTOR".equals(role)) {
            return "巡检员";
        }
        if ("REPAIR".equals(role)) {
            return "维修员";
        }
        return role;
    }

    private void bindRoute(int buttonId, Class<?> targetActivity) {
        MaterialButton button = findViewById(buttonId);
        button.setOnClickListener(v -> startActivity(new Intent(this, targetActivity)));
    }

    private void applyRoleUi(String role) {
        MaterialButton btnList = findViewById(R.id.btnDeviceList);
        MaterialButton btnArchive = findViewById(R.id.btnDeviceArchive);
        MaterialButton btnInspection = findViewById(R.id.btnInspection);
        MaterialButton btnRepair = findViewById(R.id.btnRepair);
        MaterialButton btnInventory = findViewById(R.id.btnInventory);
        MaterialButton btnStatistics = findViewById(R.id.btnStatistics);
        TextView tvHint = findViewById(R.id.tvRoleModulesHint);

        if ("INSPECTOR".equals(role)) {
            tvHint.setText("当前角色入口：设备列表、日常巡检、数据统计（其余模块已隐藏）。");
            btnList.setVisibility(View.VISIBLE);
            btnArchive.setVisibility(View.GONE);
            btnInspection.setVisibility(View.VISIBLE);
            btnRepair.setVisibility(View.GONE);
            btnInventory.setVisibility(View.GONE);
            btnStatistics.setVisibility(View.VISIBLE);
            return;
        }
        if ("REPAIR".equals(role)) {
            tvHint.setText("当前角色入口：设备列表、故障报修、数据统计（其余模块已隐藏）。");
            btnList.setVisibility(View.VISIBLE);
            btnArchive.setVisibility(View.GONE);
            btnInspection.setVisibility(View.GONE);
            btnRepair.setVisibility(View.VISIBLE);
            btnInventory.setVisibility(View.GONE);
            btnStatistics.setVisibility(View.VISIBLE);
            return;
        }
        tvHint.setText("管理员可使用全部模块：建档、巡检、报修、库存、统计。");
        btnList.setVisibility(View.VISIBLE);
        btnArchive.setVisibility(View.VISIBLE);
        btnInspection.setVisibility(View.VISIBLE);
        btnRepair.setVisibility(View.VISIBLE);
        btnInventory.setVisibility(View.VISIBLE);
        btnStatistics.setVisibility(View.VISIBLE);
    }
}
