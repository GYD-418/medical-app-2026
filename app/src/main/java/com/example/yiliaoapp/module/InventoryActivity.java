package com.example.yiliaoapp.module;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.yiliaoapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class InventoryActivity extends AppCompatActivity {
    private int stock = 120;
    private int warningLine = 100;

    private TextView tvInventory;
    private TextInputEditText etDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        tvInventory = findViewById(R.id.tvInventory);
        etDelta = findViewById(R.id.etInventoryDelta);
        MaterialButton btnInbound = findViewById(R.id.btnInbound);
        MaterialButton btnOutbound = findViewById(R.id.btnOutbound);
        MaterialButton btnManualIncrease = findViewById(R.id.btnManualIncrease);
        MaterialButton btnManualDecrease = findViewById(R.id.btnManualDecrease);

        render();
        btnInbound.setOnClickListener(v -> {
            stock += 5;
            render();
        });
        btnOutbound.setOnClickListener(v -> {
            stock -= 8;
            if (stock < 0) {
                stock = 0;
            }
            render();
        });
        btnManualIncrease.setOnClickListener(v -> applyManualDelta(true));
        btnManualDecrease.setOnClickListener(v -> applyManualDelta(false));
    }

    private void applyManualDelta(boolean increase) {
        int n = parsePositiveDelta();
        if (n <= 0) {
            Toast.makeText(this, "请输入大于 0 的整数", Toast.LENGTH_SHORT).show();
            return;
        }
        if (increase) {
            stock += n;
        } else {
            stock -= n;
            if (stock < 0) {
                stock = 0;
            }
        }
        render();
        Toast.makeText(this, (increase ? "已增加 " : "已减少 ") + n + " 件", Toast.LENGTH_SHORT).show();
    }

    private int parsePositiveDelta() {
        if (etDelta.getText() == null) {
            return 0;
        }
        String t = etDelta.getText().toString().trim();
        if (t.isEmpty()) {
            return 0;
        }
        try {
            int v = Integer.parseInt(t);
            return v > 0 ? v : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void render() {
        String state = stock <= warningLine ? "库存预警: 请及时补货" : "库存正常";
        tvInventory.setText("耗材库存: " + stock + " 件\n预警线: " + warningLine + " 件\n" + state);
    }
}
