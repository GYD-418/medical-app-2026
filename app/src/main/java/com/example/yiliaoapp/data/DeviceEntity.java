package com.example.yiliaoapp.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "device", indices = {@Index(value = {"deviceCode"}, unique = true)})
public class DeviceEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;

    @NonNull
    public String deviceCode;

    @NonNull
    public String department;

    public long createdAt;

    public DeviceEntity(@NonNull String name, @NonNull String deviceCode, @NonNull String department, long createdAt) {
        this.name = name;
        this.deviceCode = deviceCode;
        this.department = department;
        this.createdAt = createdAt;
    }
}
