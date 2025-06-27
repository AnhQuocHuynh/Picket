package com.tandev.locket.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.tandev.locket.common.database.dao.FriendDao;
import com.tandev.locket.common.database.dao.MomentDao;
import com.tandev.locket.common.database.entities.FriendEntity;
import com.tandev.locket.common.database.entities.MomentEntity;

@Database(entities = {MomentEntity.class, FriendEntity.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract MomentDao momentDao();
    public abstract FriendDao friendDao();  // Thêm DAO cho FriendEntity
}
