package com.example.yiliaoapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RepairOrderDao {
    @Insert
    long insert(RepairOrderEntity entity);

    @Query("SELECT COUNT(*) FROM repair_order WHERE status = 'PENDING'")
    int countPending();

    @Query("SELECT COUNT(*) FROM repair_order WHERE status = 'DONE'")
    int countDone();

    @Query("SELECT id FROM repair_order WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT 1")
    Long findOldestPendingId();

    @Query("UPDATE repair_order SET status = 'DONE', finishedAt = :finishedAt WHERE id = :id")
    int finishById(long id, long finishedAt);

    @Query("DELETE FROM repair_order")
    void deleteAll();

    @Query("DELETE FROM repair_order WHERE source = 'INSPECTION'")
    void deleteInspectionGenerated();

    @Query("SELECT * FROM repair_order WHERE status = 'PENDING' ORDER BY createdAt ASC")
    List<RepairOrderEntity> findPendingAll();

    @Query("SELECT * FROM repair_order ORDER BY createdAt DESC")
    List<RepairOrderEntity> findAll();
}
