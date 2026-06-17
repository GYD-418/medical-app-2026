package com.example.yiliaoapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(DeviceEntity entity);

    @Query("SELECT COUNT(*) FROM device")
    int count();

    @Query("SELECT * FROM device ORDER BY createdAt DESC")
    List<DeviceEntity> findAll();

    @Query("SELECT * FROM device WHERE name LIKE '%' || :keyword || '%' OR deviceCode LIKE '%' || :keyword || '%' OR department LIKE '%' || :keyword || '%' ORDER BY createdAt DESC")
    List<DeviceEntity> search(String keyword);
}
