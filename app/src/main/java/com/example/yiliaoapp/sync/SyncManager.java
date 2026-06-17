package com.example.yiliaoapp.sync;

import com.example.yiliaoapp.data.SyncTaskEntity;
import com.example.yiliaoapp.db.AppDatabase;
import com.example.yiliaoapp.network.ApiClient;
import com.example.yiliaoapp.network.dto.ImageUploadResponse;
import com.example.yiliaoapp.network.dto.SyncRequest;
import com.example.yiliaoapp.network.dto.SyncResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class SyncManager {

    // 毕设演示：设为 true 直接本地完成，不走网络请求
    private static final boolean LOCAL_ONLY = true;

    private final AppDatabase database;
    private final Gson gson = new Gson();

    public SyncManager(AppDatabase database) {
        this.database = database;
    }

    public SyncResult syncPending() {
        int success = 0;
        int failed = 0;
        List<SyncTaskEntity> pending = database.syncTaskDao().findPending();
        for (SyncTaskEntity task : pending) {
            try {
                if (LOCAL_ONLY) {
                    database.syncTaskDao().updateStatus(task.id, "DONE");
                    if ("IMAGE_UPLOAD".equals(task.taskType)) {
                        JsonObject payload = gson.fromJson(task.payload, JsonObject.class);
                        database.inspectionDao().markImageSynced(payload.get("inspectionId").getAsLong());
                    }
                    success++;
                    continue;
                }
                if ("IMAGE_UPLOAD".equals(task.taskType)) {
                    if (syncImageUpload(task)) {
                        success++;
                    } else {
                        failed++;
                    }
                } else {
                    SyncRequest request = new SyncRequest(task.taskType, task.payload, task.createdAt);
                    Response<SyncResponse> response = ApiClient.syncApi().uploadTask(request).execute();
                    if (response.isSuccessful()) {
                        database.syncTaskDao().updateStatus(task.id, "DONE");
                        success++;
                    } else {
                        String err = "HTTP " + response.code() + " " + response.message();
                        String body = response.errorBody() != null ? response.errorBody().string() : "";
                        database.syncTaskDao().markFailed(task.id, err + (body.isEmpty() ? "" : " - " + body));
                        failed++;
                    }
                }
            } catch (Exception ex) {
                database.syncTaskDao().markFailed(task.id, getStackTrace(ex));
                failed++;
            }
        }
        return new SyncResult(success, failed);
    }

    @SuppressWarnings("unused")
    private boolean syncImageUpload(SyncTaskEntity task) {
        try {
            JsonObject payload = gson.fromJson(task.payload, JsonObject.class);
            long inspectionId = payload.get("inspectionId").getAsLong();
            String localPath = payload.get("localPath").getAsString();

            File imageFile = new File(localPath);
            if (!imageFile.exists()) {
                database.syncTaskDao().updateStatus(task.id, "DONE");
                return true;
            }

            RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), fileBody);
            RequestBody inspectionIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inspectionId));
            RequestBody createdAtBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(task.createdAt));

            Response<ImageUploadResponse> response = ApiClient.syncApi()
                    .uploadImage(filePart, inspectionIdBody, createdAtBody)
                    .execute();

            if (response.isSuccessful()) {
                database.syncTaskDao().updateStatus(task.id, "DONE");
                database.inspectionDao().markImageSynced(inspectionId);
                return true;
            } else {
                String err = "HTTP " + response.code() + " " + response.message();
                String body = response.errorBody() != null ? response.errorBody().string() : "";
                database.syncTaskDao().markFailed(task.id, err + (body.isEmpty() ? "" : " - " + body));
                return false;
            }
        } catch (Exception e) {
            database.syncTaskDao().markFailed(task.id, getStackTrace(e));
            return false;
        }
    }

    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String full = sw.toString();
        return full.length() > 500 ? full.substring(0, 500) : full;
    }

    public static class SyncResult {
        public final int successCount;
        public final int failedCount;

        public SyncResult(int successCount, int failedCount) {
            this.successCount = successCount;
            this.failedCount = failedCount;
        }
    }
}
