package com.example.yiliaoapp.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_task")
public class SyncTaskEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String taskType;

    @NonNull
    public String payload;

    @NonNull
    public String status;

    public long createdAt;

    @NonNull
    public String operator;

    public String errorMessage;

    public SyncTaskEntity(@NonNull String taskType, @NonNull String payload, @NonNull String status, long createdAt, @NonNull String operator) {
        this.taskType = taskType;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
        this.operator = operator;
    }
}
