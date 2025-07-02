package com.example.locket.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.locket.common.database.dao.FriendDao;
import com.example.locket.common.database.dao.MomentDao;
import com.example.locket.common.database.entities.FriendEntity;
import com.example.locket.common.database.entities.MomentEntity;

@Database(entities = {MomentEntity.class, FriendEntity.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MomentDao momentDao();
    public abstract FriendDao friendDao();  // Thêm DAO cho FriendEntity
}
