package com.example.yiliaoapp.module;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.yiliaoapp.MedicalApp;
import com.example.yiliaoapp.R;
import com.example.yiliaoapp.data.DeviceEntity;
import com.example.yiliaoapp.util.DeviceQrParser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceArchiveActivity extends AppCompatActivity {
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private int archiveCount = 0;

    private TextInputEditText etName;
    private TextInputEditText etCode;
    private TextInputEditText etDept;
    private TextView tvResult;

    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<ScanOptions> scanLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchScan();
                    } else {
                        Toast.makeText(this, "需要相机权限才能扫描二维码", Toast.LENGTH_SHORT).show();
                    }
                });

        scanLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                return;
            }
            applyScanResult(result.getContents());
        });

        setContentView(R.layout.activity_device_archive);

        etName = findViewById(R.id.etDeviceName);
        etCode = findViewById(R.id.etDeviceCode);
        etDept = findViewById(R.id.etDepartment);
        tvResult = findViewById(R.id.tvArchiveResult);
        MaterialButton btnSave = findViewById(R.id.btnSaveArchive);
        MaterialButton btnScan = findViewById(R.id.btnScanDeviceQr);

        ioExecutor.execute(() -> {
            archiveCount = ((MedicalApp) getApplication()).getDatabase().deviceDao().count();
            runOnUiThread(() -> tvResult.setText(getStatusText("等待录入")));
        });

        btnSave.setOnClickListener(v -> saveFromInputs(false));
        btnScan.setOnClickListener(v -> checkCameraAndScan());
    }

    private void checkCameraAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchScan();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(Collections.singletonList(BarcodeFormat.QR_CODE.name()));
        options.setPrompt("对准设备二维码扫描");
        options.setBeepEnabled(true);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(true);
        scanLauncher.launch(options);
    }

    private void applyScanResult(@NonNull String raw) {
        DeviceQrParser.Parsed p = DeviceQrParser.parse(raw);
        etName.setText(p.name);
        etCode.setText(p.code);
        etDept.setText(p.dept);
        if (p.isComplete()) {
            saveFromInputs(true);
        } else {
            tvResult.setText(getStatusText("已填入扫码内容，请补全后点「保存建档」"));
            Toast.makeText(this, "信息不完整：仅已填入可识别字段", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFromInputs(boolean fromQr) {
        String name = safeText(etName);
        String code = safeText(etCode);
        String dept = safeText(etDept);
        if (name.isEmpty() || code.isEmpty() || dept.isEmpty()) {
            tvResult.setText(getStatusText("请完善设备名称/编号/科室"));
            if (fromQr) {
                Toast.makeText(this, "扫码数据不完整，无法自动建档", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        ioExecutor.execute(() -> {
            try {
                DeviceEntity entity = new DeviceEntity(name, code, dept, System.currentTimeMillis());
                ((MedicalApp) getApplication()).getDatabase().deviceDao().insert(entity);
                archiveCount = ((MedicalApp) getApplication()).getDatabase().deviceDao().count();
                runOnUiThread(() -> {
                    tvResult.setText("录入成功: " + name + " (" + code + ")\n归属科室: " + dept + "\n当前建档总数: " + archiveCount
                            + (fromQr ? "\n来源: 二维码" : ""));
                    etName.setText("");
                    etCode.setText("");
                    etDept.setText("");
                    Toast.makeText(this, "建档成功", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText(getStatusText("保存失败: 设备编号可能已存在")));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }

    private String safeText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String getStatusText(String message) {
        return "设备建档模块\n" + message + "\n支持二维码/RFID 扩展接入。";
    }
}
