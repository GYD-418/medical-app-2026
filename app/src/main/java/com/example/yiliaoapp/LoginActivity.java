package com.example.yiliaoapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText etUser = findViewById(R.id.etUsername);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String user = text(etUser);
            String pwd = text(etPassword);
            if (user.isEmpty() || pwd.isEmpty()) {
                Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            String role = parseRole(user, pwd);
            if (role == null) {
                Toast.makeText(this, "账号或密码错误（admin/inspector/repair，密码均123456）", Toast.LENGTH_SHORT).show();
                return;
            }
            ((MedicalApp) getApplication()).setCurrentUsername(user);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("role", role);
            intent.putExtra("username", user);
            startActivity(intent);
            finish();
        });
    }

    private String parseRole(String user, String pwd) {
        if (!"123456".equals(pwd)) {
            return null;
        }
        if ("admin".equals(user)) {
            return "ADMIN";
        }
        if ("inspector".equals(user)) {
            return "INSPECTOR";
        }
        if ("repair".equals(user)) {
            return "REPAIR";
        }
        return null;
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
