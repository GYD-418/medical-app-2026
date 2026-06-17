package com.example.yiliaoapp.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.yiliaoapp.data.DeviceDao;
import com.example.yiliaoapp.data.DeviceEntity;
import com.example.yiliaoapp.data.InspectionDao;
import com.example.yiliaoapp.data.InspectionEntity;
import com.example.yiliaoapp.data.RepairOrderDao;
import com.example.yiliaoapp.data.RepairOrderEntity;
import com.example.yiliaoapp.data.SyncTaskDao;
import com.example.yiliaoapp.data.SyncTaskEntity;

@Database(
        entities = {
                DeviceEntity.class,
                InspectionEntity.class,
                RepairOrderEntity.class,
                SyncTaskEntity.class
        },
        version = 5,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceDao deviceDao();
    public abstract InspectionDao inspectionDao();
    public abstract RepairOrderDao repairOrderDao();
    public abstract SyncTaskDao syncTaskDao();
}
