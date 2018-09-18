package net.agnusvox.lydl1;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

/**
 * Created by ken on 30/9/2017.
 * http://blog.teamtreehouse.com/scheduling-work-jobscheduler
 * Reference: https://github.com/romannurik/muzei/blob/master/main/src/main/java/com/google/android/apps/muzei/sync/DownloadArtworkJobService.java
 */

public class LYDLBgService extends JobService {
    private GetAudioTask mGetAudioTask = null;
    private static final String TAG = LYDLBgService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(TAG, "Job started!");
        // We need 'jobParameters' so we can call 'jobFinished'
        GetAudioTask mGetAudioTask = new GetAudioTask(this)
        {
            @Override
            protected void onPostExecute(Boolean success) {
                jobFinished(jobParameters, !success);
            }
        };
        mGetAudioTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        if (mGetAudioTask != null) {
            mGetAudioTask.cancel(true);
        }
        return true;
    }
}
