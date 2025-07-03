package com.example.locket.common.repository.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.locket.common.database.entities.MomentEntity;
import com.example.locket.common.repository.MomentRepository;

import java.util.List;

public class MomentViewModel extends AndroidViewModel {
    private final LiveData<List<MomentEntity>> allMoments;
    private final MomentRepository repository;

    // Constructor với loginResponse được truyền vào
    public MomentViewModel(@NonNull Application application) {
        super(application);
        repository = new MomentRepository(application);
        allMoments = repository.getAllMoments();

        // 🧪 Test database connection first
        repository.testDatabaseConnection();

        // 🔄 Đồng bộ dữ liệu posts từ server khi ViewModel khởi tạo
        repository.refreshDataFromServer();
    }

    public LiveData<List<MomentEntity>> getAllMoments() {
        return allMoments;
    }

    /**
     * 🔄 Manually refresh data from server
     */
    public void refreshData() {
        repository.refreshDataFromServer();
    }

    /**
     * 🏷️ Fetch available categories from API
     */
    public void fetchAvailableCategories(MomentRepository.CategoriesCallback callback) {
        repository.fetchAvailableCategories(callback);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup repository resources
        repository.cleanup();
    }
}

