package com.example.android.hydrationreminder;

import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;
import android.content.*;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by Swapnil on 25-12-2016.
 */

public class ReminderUtilities {
    private static final int REMINDER_INTERVAL_MINUTES = 30;
    private static final int REMINDER_INTERVAL_SECONDS = (int) TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES);
    private static final int SYNC_FELXTIME_SECONDS = REMINDER_INTERVAL_SECONDS;

    private static final String REMINDER_JOB_TAG = "hydration_reminder_tag";
    private static boolean sIntialized = false;
    synchronized public static void scheduleChargingReminder(@NonNull final Context context){
        if(sIntialized) return;
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);
        Job constraintReminderJob = firebaseJobDispatcher.newJobBuilder()
                .setService(WaterReminderFirebaseJobService.class)
                .setTag(REMINDER_JOB_TAG)
                .setConstraints(Constraint.DEVICE_CHARGING)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(REMINDER_INTERVAL_SECONDS,REMINDER_INTERVAL_SECONDS+SYNC_FELXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();
        firebaseJobDispatcher.schedule(constraintReminderJob);
        sIntialized = true;
    }
}
