package com.example.yiliaoapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "repair_order")
public class RepairOrderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String status;

    @NonNull
    public String source;

    @NonNull
    public String description;

    public long inspectionId;

    @Nullable
    public String imagePath;

    public long createdAt;
    public long finishedAt;

    @Ignore
    public RepairOrderEntity(@NonNull String status, @NonNull String source, @NonNull String description, long createdAt, long finishedAt) {
        this.status = status;
        this.source = source;
        this.description = description;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }

    public RepairOrderEntity(@NonNull String status, @NonNull String source, @NonNull String description,
                             long inspectionId, @Nullable String imagePath, long createdAt, long finishedAt) {
        this.status = status;
        this.source = source;
        this.description = description;
        this.inspectionId = inspectionId;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
    }
}
