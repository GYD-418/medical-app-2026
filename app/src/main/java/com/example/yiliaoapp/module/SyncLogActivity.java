package com.example.yiliaoapp.module;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.MedicalApp;
import com.example.yiliaoapp.R;
import com.example.yiliaoapp.data.SyncTaskEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncLogActivity extends AppCompatActivity {
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final List<SyncTaskEntity> tasks = new ArrayList<>();
    private final Gson gson = new Gson();
    private SyncLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_log);

        ListView listView = findViewById(R.id.lvSyncLog);
        adapter = new SyncLogAdapter();
        listView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        ioExecutor.execute(() -> {
            List<SyncTaskEntity> taskList = ((MedicalApp) getApplication()).getDatabase().syncTaskDao().findAll();
            runOnUiThread(() -> {
                tasks.clear();
                tasks.addAll(taskList);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private String describeContent(SyncTaskEntity task) {
        try {
            JsonObject payload = gson.fromJson(task.payload, JsonObject.class);
            switch (task.taskType) {
                case "INSPECTION_UPLOAD": {
                    String result = payload.has("result") ? payload.get("result").getAsString() : "?";
                    String label = "NORMAL".equals(result) ? "正常" : "异常";
                    return "巡检记录上报 - 结果: " + label + " (巡检ID: " + payload.get("inspectionId").getAsLong() + ")";
                }
                case "IMAGE_UPLOAD":
                    return "上传故障照片 (巡检ID: " + payload.get("inspectionId").getAsLong() + ")";
                case "REPAIR_ORDER_CREATE": {
                    String source = payload.has("source") ? payload.get("source").getAsString() : "?";
                    String srcLabel = "inspection".equals(source) ? "巡检自动生成" : "手动创建";
                    return "创建维修工单 - 来源: " + srcLabel;
                }
                case "REPAIR_ORDER_DONE":
                    return "维修工单已完成 (工单ID: " + payload.get("id").getAsLong() + ")";
                default:
                    return task.payload;
            }
        } catch (Exception e) {
            return task.payload;
        }
    }

    private String extractImagePath(SyncTaskEntity task) {
        if (!"IMAGE_UPLOAD".equals(task.taskType)) return null;
        try {
            JsonObject payload = gson.fromJson(task.payload, JsonObject.class);
            if (payload.has("localPath")) {
                String path = payload.get("localPath").getAsString();
                if (new File(path).exists()) {
                    return path;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private class SyncLogAdapter extends BaseAdapter {
        private final SimpleDateFormat format = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public SyncTaskEntity getItem(int position) {
            return tasks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(SyncLogActivity.this)
                        .inflate(R.layout.item_sync_log, parent, false);
                holder = new ViewHolder();
                holder.tvHeader = convertView.findViewById(R.id.tvSyncHeader);
                holder.tvOperator = convertView.findViewById(R.id.tvSyncOperator);
                holder.tvContent = convertView.findViewById(R.id.tvSyncContent);
                holder.tvError = convertView.findViewById(R.id.tvSyncError);
                holder.ivImage = convertView.findViewById(R.id.ivSyncImage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SyncTaskEntity task = getItem(position);
            String time = format.format(new Date(task.createdAt));
            holder.tvHeader.setText("[" + task.status + "] " + task.taskType + "  " + time);
            holder.tvOperator.setText("操作人: " + task.operator);
            holder.tvContent.setText(describeContent(task));

            if (task.errorMessage != null && !task.errorMessage.isEmpty()) {
                holder.tvError.setText("错误: " + task.errorMessage);
                holder.tvError.setVisibility(View.VISIBLE);
            } else {
                holder.tvError.setVisibility(View.GONE);
            }

            String imagePath = extractImagePath(task);
            if (imagePath != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                Bitmap thumbnail = BitmapFactory.decodeFile(imagePath, opts);
                if (thumbnail != null) {
                    holder.ivImage.setImageBitmap(thumbnail);
                    holder.ivImage.setVisibility(View.VISIBLE);
                } else {
                    holder.ivImage.setVisibility(View.GONE);
                }
            } else {
                holder.ivImage.setVisibility(View.GONE);
            }

            return convertView;
        }

        private class ViewHolder {
            TextView tvHeader;
            TextView tvOperator;
            TextView tvContent;
            TextView tvError;
            ImageView ivImage;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
