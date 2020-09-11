package it.filippetti.safe.localizator;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

/**
 * Example implementation of a JobIntentService.
 */
public class SimpleJob  extends JobIntentService {
    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, SimpleJob.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // We have received work to do.  The system or framework is already
        // holding a wake lock for us at this point, so we can just go.
        System.out.println("HandleIt");
        /*Log.i("SimpleJobIntentService", "Executing work: " + intent);
        String label = intent.getStringExtra("label");
        if (label == null) {
            label = intent.toString();
        }
        toast("Executing: " + label);
        for (int i = 0; i < 5; i++) {
            Log.i("SimpleJobIntentService", "Running service " + (i + 1)
                    + "/5 @ " + SystemClock.elapsedRealtime());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        Log.i("SimpleJobIntentService", "Completed service @ " + SystemClock.elapsedRealtime());*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("All work complete");
    }

    @SuppressWarnings("deprecation")
    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(SimpleJob.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}