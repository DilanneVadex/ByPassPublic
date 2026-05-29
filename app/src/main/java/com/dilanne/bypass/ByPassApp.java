package com.dilanne.bypass;

import android.app.Application;
import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.dilanne.bypass.data.remote.SyncWorker;
import com.dilanne.bypass.util.LocaleHelper;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ByPassApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleDailySync();
    }

    private void scheduleDailySync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        // On calcule le délai jusqu'à minuit
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        long initialDelay = calendar.getTimeInMillis() - now;

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class, 24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailySync",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }
}
