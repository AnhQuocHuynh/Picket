package com.example.locket.common.repository.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.locket.common.database.entities.FriendEntity;
import com.example.locket.common.repository.FriendRepository;

import java.util.List;

public class FriendViewModel extends AndroidViewModel {
    private final LiveData<List<FriendEntity>> friendEntities;

    // Constructor với loginResponse được truyền vào
    public FriendViewModel(@NonNull Application application) {
        super(application);
        FriendRepository repository = new FriendRepository(application);
        friendEntities = repository.getAllFriends();
        // Đồng bộ dữ liệu khi ViewModel khởi tạo (nếu có kết nối)
        repository.refreshDataFromServer();
    }

    public LiveData<List<FriendEntity>> getFriendEntities() {
        return friendEntities;
    }
}

