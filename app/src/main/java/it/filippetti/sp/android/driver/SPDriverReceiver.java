package it.filippetti.sp.android.driver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SPDriverReceiver extends BroadcastReceiver {
    private String TAG = "AAA_"+  SPDriverReceiver.class.getSimpleName();;

    @Override
    public void onReceive(Context ctx, Intent i ) {

        Log.v( TAG, "onReceive : ");
        Intent intent = new Intent( ctx, SPDriverService.class );
        ctx.startService(intent);
    }
}