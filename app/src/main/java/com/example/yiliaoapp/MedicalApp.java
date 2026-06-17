package com.example.yiliaoapp;

import android.app.Application;

import androidx.room.Room;

import com.example.yiliaoapp.db.AppDatabase;

public class MedicalApp extends Application {
    private AppDatabase database;
    private String currentUsername;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "yiliao-db")
                .fallbackToDestructiveMigration()
                .build();
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {
        this.currentUsername = currentUsername;
    }
}
