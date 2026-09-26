package com.maxiqn.mfootball.ui;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import timber.log.Timber;

public class MatchNotificationWorker extends Worker {
    
    public MatchNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.d("MatchNotificationWorker: Checking for live matches");
            // TODO: Fetch live matches and send notifications
            return Result.success();
        } catch (Exception e) {
            Timber.e(e, "MatchNotificationWorker failed");
            return Result.retry();
        }
    }
}
