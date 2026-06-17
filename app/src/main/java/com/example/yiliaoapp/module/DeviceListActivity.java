package com.example.yiliaoapp.module;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.MedicalApp;
import com.example.yiliaoapp.R;
import com.example.yiliaoapp.data.DeviceEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceListActivity extends AppCompatActivity {
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final List<DeviceEntity> entityCache = new ArrayList<>();
    private final List<String> showItems = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        ListView lvDevices = findViewById(R.id.lvDevices);
        TextView tvCount = findViewById(R.id.tvCount);
        TextInputEditText etKeyword = findViewById(R.id.etKeyword);
        MaterialButton btnSearch = findViewById(R.id.btnSearch);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, showItems);
        lvDevices.setAdapter(adapter);

        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= entityCache.size()) {
                return;
            }
            DeviceEntity entity = entityCache.get(position);
            Intent intent = new Intent(this, DeviceDetailActivity.class);
            intent.putExtra("name", entity.name);
            intent.putExtra("code", entity.deviceCode);
            intent.putExtra("department", entity.department);
            intent.putExtra("createdAt", entity.createdAt);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(v -> {
            String keyword = etKeyword.getText() == null ? "" : etKeyword.getText().toString().trim();
            loadData(keyword, tvCount);
        });

        loadData("", tvCount);
    }

    private void loadData(String keyword, TextView tvCount) {
        ioExecutor.execute(() -> {
            List<DeviceEntity> data;
            if (keyword == null || keyword.isEmpty()) {
                data = ((MedicalApp) getApplication()).getDatabase().deviceDao().findAll();
            } else {
                data = ((MedicalApp) getApplication()).getDatabase().deviceDao().search(keyword);
            }
            runOnUiThread(() -> {
                entityCache.clear();
                showItems.clear();
                entityCache.addAll(data);
                for (DeviceEntity entity : data) {
                    showItems.add(entity.name + " | " + entity.deviceCode + " | " + entity.department);
                }
                adapter.notifyDataSetChanged();
                tvCount.setText("共 " + data.size() + " 条");
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }
}
