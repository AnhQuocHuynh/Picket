package com.example.locket.common.database;

import android.app.Application;
import android.content.Context;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.locket.common.repository.MomentRepository;

public class DataSyncWorker extends Worker {
    private MomentRepository momentRepository;

    public DataSyncWorker(Context context, WorkerParameters params) {
        super(context, params);
        momentRepository = new MomentRepository((Application) context.getApplicationContext());
    }

    @Override
    public Result doWork() {
        // Gọi API để tải dữ liệu
        momentRepository.refreshDataFromServer(); // Method signature đã thay đổi

        // Nếu công việc thành công
        return Result.success();
    }
}

