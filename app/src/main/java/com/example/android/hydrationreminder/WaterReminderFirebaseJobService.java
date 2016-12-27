package com.example.android.hydrationreminder;

import android.content.Context;
import android.os.AsyncTask;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Created by Swapnil on 25-12-2016.
 */

public class WaterReminderFirebaseJobService extends JobService {
    private AsyncTask mAsyncTask;
    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     *
     * @param job
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters job) {
        mAsyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = WaterReminderFirebaseJobService.this;
                ReminderTasks.executeTask(context,ReminderTasks.ACTION_CHARGING_REMINDER);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };
        mAsyncTask.execute();
        return true; // Job is still doing some work
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @param job
     * @return whether the job should be retried
     * see Builder#setRetryStrategy(RetryStrategy)
     * see RetryStrategy
     */
    @Override
    public boolean onStopJob(JobParameters job) {
        if(mAsyncTask!=null){
            mAsyncTask.cancel(true);
        }
        //When conditions are met -- Wifi on -- retry Job
        return true;
    }
}
