package org.smartregister.reveal.job;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.smartregister.job.SyncServiceJob;
import org.smartregister.reveal.sync.RevealSyncIntentService;

/**
 * Created by samuelgithengi on 11/21/18.
 */
public class RevealJobCreator implements JobCreator {
    @Nullable
    @Override
    public Job create(@NonNull String tag) {
        switch (tag) {
            case SyncServiceJob.TAG:
                return new SyncServiceJob(RevealSyncIntentService.class);
            case RevealCampaignServiceJob.TAG:
                return new RevealCampaignServiceJob();
            case LocationTaskServiceJob.TAG:
                return new LocationTaskServiceJob();
            default:
                Log.w(RevealJobCreator.class.getCanonicalName(), tag + " is not declared in RevealJobCreator Job Creator");
                return null;
        }
    }
}