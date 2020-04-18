package com.chehanr.trakr.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import com.chehanr.trakr.helpers.FirebaseHelpers;

public class SyncJobService extends JobService {
  public static final int JOB_ID = 1;
  private static final String TAG = SyncJobService.class.getSimpleName();

  @Override
  public boolean onStartJob(final JobParameters params) {
    Log.i(TAG, "onStartJob: Job started");
    FirebaseHelpers.performSync(this);

    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    Log.i(TAG, "onStopJob: Job cancelled");

    return true;
  }
}
