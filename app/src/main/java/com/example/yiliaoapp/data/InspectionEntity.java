package com.example.yiliaoapp.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "inspection")
public class InspectionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String result;

    @NonNull
    public String note;

    @Nullable
    public String imagePath;

    public boolean imageSynced;

    public long createdAt;

    @Ignore
    public InspectionEntity(@NonNull String result, @NonNull String note, long createdAt) {
        this.result = result;
        this.note = note;
        this.createdAt = createdAt;
    }

    public InspectionEntity(@NonNull String result, @NonNull String note, @Nullable String imagePath, long createdAt) {
        this.result = result;
        this.note = note;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }
}
