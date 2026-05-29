package com.dilanne.bypass.data.remote;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.dilanne.bypass.data.repository.PasswordRepository;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            PasswordRepository repository = new PasswordRepository((android.app.Application) getApplicationContext());
            repository.syncFromRemote();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
