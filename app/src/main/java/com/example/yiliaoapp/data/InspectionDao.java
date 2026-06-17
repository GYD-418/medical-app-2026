package com.example.yiliaoapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InspectionDao {
    @Insert
    long insert(InspectionEntity entity);

    @Query("SELECT COUNT(*) FROM inspection WHERE result = 'NORMAL'")
    int countNormal();

    @Query("SELECT COUNT(*) FROM inspection WHERE result = 'ABNORMAL'")
    int countAbnormal();

    @Query("DELETE FROM inspection")
    void deleteAll();

    @Query("SELECT * FROM inspection WHERE id = :id")
    InspectionEntity findById(long id);

    @Query("SELECT * FROM inspection WHERE result = 'ABNORMAL' ORDER BY createdAt DESC")
    List<InspectionEntity> findAbnormalAll();

    @Query("UPDATE inspection SET imagePath = :imagePath WHERE id = :id")
    int updateImagePath(long id, String imagePath);

    @Query("UPDATE inspection SET imageSynced = 1 WHERE id = :id")
    int markImageSynced(long id);
}
