package com.example.locket.common.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.locket.common.database.entities.MomentEntity;

import java.util.List;

@Dao
public interface MomentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MomentEntity moment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MomentEntity> moments);

    @Query("SELECT * FROM moment_table ORDER BY dateSeconds DESC")
    LiveData<List<MomentEntity>> getAllMoments();

    @Query("SELECT * FROM moment_table ORDER BY dateSeconds DESC")
    List<MomentEntity> getAllMomentsSync();

    @Query("DELETE FROM moment_table")
    void deleteAll();

    @Query("DELETE FROM moment_table WHERE id = :momentId")
    void deleteById(String momentId);

    @Query("SELECT COUNT(*) FROM moment_table")
    int getCount();
}
