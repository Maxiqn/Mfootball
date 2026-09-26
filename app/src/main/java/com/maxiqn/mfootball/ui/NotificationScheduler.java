package com.maxiqn.mfootball.ui;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class NotificationScheduler {
    
    public static void scheduleMatchNotifications(Context context) {
        Constraints constraints = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();
        
        PeriodicWorkRequest matchCheckWork = new PeriodicWorkRequest.Builder(
            MatchNotificationWorker.class,
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build();
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "match_notifications",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            matchCheckWork
        );
    }
}
