package com.example.yiliaoapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SyncTaskDao {
    @Insert
    long insert(SyncTaskEntity entity);

    @Query("SELECT COUNT(*) FROM sync_task WHERE status = 'PENDING'")
    int countPending();

    @Query("SELECT * FROM sync_task ORDER BY createdAt DESC")
    List<SyncTaskEntity> findAll();

    @Query("SELECT * FROM sync_task WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<SyncTaskEntity> findPending();

    @Query("UPDATE sync_task SET status = 'DONE' WHERE status = 'PENDING'")
    int markAllDone();

    @Query("UPDATE sync_task SET status = :status WHERE id = :id")
    int updateStatus(long id, String status);

    @Query("UPDATE sync_task SET status = 'FAILED', errorMessage = :error WHERE id = :id")
    int markFailed(long id, String error);

    @Query("DELETE FROM sync_task WHERE taskType = 'INSPECTION_UPLOAD'")
    void deleteInspectionUploadTasks();

    @Query("DELETE FROM sync_task WHERE taskType = 'REPAIR_ORDER_CREATE' AND payload LIKE '%inspection%'")
    void deleteInspectionTriggeredRepairCreates();

    @Query("DELETE FROM sync_task WHERE taskType IN ('REPAIR_ORDER_CREATE', 'REPAIR_ORDER_DONE')")
    void deleteRepairRelatedTasks();
}
