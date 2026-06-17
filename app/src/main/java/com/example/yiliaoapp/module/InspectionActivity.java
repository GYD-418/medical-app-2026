package com.example.yiliaoapp.module;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.yiliaoapp.MedicalApp;
import com.example.yiliaoapp.R;
import com.example.yiliaoapp.data.InspectionEntity;
import com.example.yiliaoapp.data.RepairOrderEntity;
import com.example.yiliaoapp.data.SyncTaskEntity;
import com.example.yiliaoapp.db.AppDatabase;
import com.example.yiliaoapp.util.ImageCompressor;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InspectionActivity extends AppCompatActivity {
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private int normalCount = 0;
    private int abnormalCount = 0;
    private String currentImagePath;
    private TextView tvStatus;
    private ImageView ivPreview;
    private TextView tvNoImage;
    private MaterialButton btnClearImage;

    private Uri cameraTempUri;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "需要相机权限才能拍照，请在系统设置中授予", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && cameraTempUri != null) {
                    String path = ImageCompressor.compressAndSave(this, cameraTempUri);
                    if (path != null) {
                        currentImagePath = path;
                        showImagePreview(path);
                    } else {
                        Toast.makeText(this, "图片保存失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "拍照未成功，请重试", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String path = ImageCompressor.compressAndSave(this, uri);
                    if (path != null) {
                        currentImagePath = path;
                        showImagePreview(path);
                    } else {
                        Toast.makeText(this, "图片加载失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection);

        tvStatus = findViewById(R.id.tvInspectionStatus);
        ivPreview = findViewById(R.id.ivFaultPreview);
        tvNoImage = findViewById(R.id.tvNoImage);
        btnClearImage = findViewById(R.id.btnClearImage);
        MaterialButton btnNormal = findViewById(R.id.btnMarkNormal);
        MaterialButton btnAbnormal = findViewById(R.id.btnMarkAbnormal);
        MaterialButton btnReset = findViewById(R.id.btnResetInspection);
        MaterialButton btnTakePhoto = findViewById(R.id.btnTakePhoto);
        MaterialButton btnPickImage = findViewById(R.id.btnPickImage);

        loadCounter();

        btnTakePhoto.setOnClickListener(v -> requestCameraAndTakePhoto());
        btnPickImage.setOnClickListener(v -> pickFromGallery());
        btnClearImage.setOnClickListener(v -> clearImage());
        btnNormal.setOnClickListener(v -> saveInspection(false));
        btnAbnormal.setOnClickListener(v -> saveInspection(true));
        btnReset.setOnClickListener(v -> resetPageData());
    }

    private void requestCameraAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        File dir = new File(getExternalFilesDir(null), "fault_images");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photoFile = new File(dir, "capture_" + System.currentTimeMillis() + ".jpg");
        // explicitly create file on disk so camera app can write to it
        try {
            photoFile.createNewFile();
        } catch (Exception e) {
            Toast.makeText(this, "创建图片文件失败", Toast.LENGTH_SHORT).show();
            return;
        }
        cameraTempUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", photoFile);
        try {
            cameraLauncher.launch(cameraTempUri);
        } catch (Exception e) {
            Toast.makeText(this, "无法启动相机: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void pickFromGallery() {
        galleryLauncher.launch("image/*");
    }

    private void clearImage() {
        if (currentImagePath != null) {
            new File(currentImagePath).delete();
        }
        currentImagePath = null;
        ivPreview.setImageBitmap(null);
        ivPreview.setVisibility(android.view.View.GONE);
        btnClearImage.setVisibility(android.view.View.GONE);
        tvNoImage.setVisibility(android.view.View.VISIBLE);
    }

    private void showImagePreview(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) {
            ivPreview.setImageBitmap(bitmap);
            ivPreview.setVisibility(android.view.View.VISIBLE);
            btnClearImage.setVisibility(android.view.View.VISIBLE);
            tvNoImage.setVisibility(android.view.View.GONE);
        }
    }

    private void resetPageData() {
        ioExecutor.execute(() -> {
            AppDatabase db = ((MedicalApp) getApplication()).getDatabase();
            db.inspectionDao().deleteAll();
            db.repairOrderDao().deleteInspectionGenerated();
            db.syncTaskDao().deleteInspectionUploadTasks();
            db.syncTaskDao().deleteInspectionTriggeredRepairCreates();
            normalCount = db.inspectionDao().countNormal();
            abnormalCount = db.inspectionDao().countAbnormal();
            runOnUiThread(() -> {
                clearImage();
                refreshStatus();
                Toast.makeText(this, "已清空巡检记录、巡检产生的工单及关联同步任务", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadCounter() {
        ioExecutor.execute(() -> {
            normalCount = ((MedicalApp) getApplication()).getDatabase().inspectionDao().countNormal();
            abnormalCount = ((MedicalApp) getApplication()).getDatabase().inspectionDao().countAbnormal();
            runOnUiThread(this::refreshStatus);
        });
    }

    private void saveInspection(boolean abnormal) {
        ioExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            AppDatabase db = ((MedicalApp) getApplication()).getDatabase();
            String operator = ((MedicalApp) getApplication()).getCurrentUsername();
            if (operator == null) operator = "unknown";
            String tempPath = currentImagePath;

            if (abnormal) {
                InspectionEntity inspection = new InspectionEntity("ABNORMAL", "巡检异常", null, now);
                long inspectionId = db.inspectionDao().insert(inspection);

                String finalImagePath = renameImageToFinal(tempPath, inspectionId);
                if (finalImagePath != null) {
                    db.inspectionDao().updateImagePath(inspectionId, finalImagePath);
                }

                db.repairOrderDao().insert(new RepairOrderEntity(
                        "PENDING", "INSPECTION", "巡检发现异常自动生成工单",
                        inspectionId, finalImagePath, now, 0L));

                db.syncTaskDao().insert(new SyncTaskEntity(
                        "REPAIR_ORDER_CREATE", "{\"source\":\"inspection\",\"inspectionId\":" + inspectionId + "}", "PENDING", now, operator));

                if (finalImagePath != null) {
                    db.syncTaskDao().insert(new SyncTaskEntity(
                            "IMAGE_UPLOAD",
                            "{\"inspectionId\":" + inspectionId + ",\"localPath\":\"" + finalImagePath + "\"}",
                            "PENDING", now, operator));
                }
            } else {
                InspectionEntity inspection = new InspectionEntity("NORMAL", "巡检正常", null, now);
                long inspectionId = db.inspectionDao().insert(inspection);

                String finalImagePath = renameImageToFinal(tempPath, inspectionId);
                if (finalImagePath != null) {
                    db.inspectionDao().updateImagePath(inspectionId, finalImagePath);
                }

                db.syncTaskDao().insert(new SyncTaskEntity(
                        "INSPECTION_UPLOAD", "{\"result\":\"NORMAL\",\"inspectionId\":" + inspectionId + "}", "PENDING", now, operator));

                if (finalImagePath != null) {
                    db.syncTaskDao().insert(new SyncTaskEntity(
                            "IMAGE_UPLOAD",
                            "{\"inspectionId\":" + inspectionId + ",\"localPath\":\"" + finalImagePath + "\"}",
                            "PENDING", now, operator));
                }
            }

            normalCount = db.inspectionDao().countNormal();
            abnormalCount = db.inspectionDao().countAbnormal();
            runOnUiThread(() -> {
                currentImagePath = null;
                clearImage();
                refreshStatus();
                if (abnormal) {
                    Toast.makeText(this, "已记录异常并自动创建报修工单", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "已记录正常巡检", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String renameImageToFinal(String tempPath, long inspectionId) {
        if (tempPath == null) return null;
        File tempFile = new File(tempPath);
        if (!tempFile.exists()) return null;

        File dir = new File(getExternalFilesDir(null), "fault_images");
        File finalFile = new File(dir, "inspection_" + inspectionId + "_" + System.currentTimeMillis() + ".jpg");
        if (tempFile.renameTo(finalFile)) {
            return finalFile.getAbsolutePath();
        }
        return tempPath;
    }

    private void refreshStatus() {
        tvStatus.setText("今日巡检: 正常 " + normalCount + " 台, 异常 " + abnormalCount + " 台\n异常设备会触发报修流程。");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
