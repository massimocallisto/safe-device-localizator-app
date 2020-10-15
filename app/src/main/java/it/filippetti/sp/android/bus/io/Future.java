package it.filippetti.sp.android.bus.io;

import android.util.Log;

public class Future<T> {
    private String TAG = "AAA_" + Future.class.getSimpleName();

    boolean completed = false;
    Handler handler = null;
    it.filippetti.sp.android.bus.io.impl.AsyncResult asyncResult = new it.filippetti.sp.android.bus.io.impl.AsyncResult<>();

    private Future() {

    }


    public static Future future() {
        return new Future();
    }

    public void setHandler(Handler<AsyncResult<T>> handler) {
        // TODO...
        this.handler = handler;
//        Log.d(TAG, "setting handler completed " + completed + " handler " + handler);
        if (completed) {
            try {
                handler.handle(asyncResult);
            } catch (Exception e) {
                //e.printStackTrace();
                Log.e(TAG, e.getMessage());

            }
        }
    }

    public void complete() {
        complete(null);
    }

    public void complete(T result) {
        // TODO...
        //completed = true;
        Log.d(TAG, "complete handler " + handler + " " + result);
        if (handler != null) {
            it.filippetti.sp.android.bus.io.impl.AsyncResult asyncResult = new it.filippetti.sp.android.bus.io.impl.AsyncResult<>();
            try {
                asyncResult.result = result;
                handler.handle(asyncResult);

            } catch (Exception e) {
                //e.printStackTrace();
                Log.e(TAG, e.getMessage());

            }
        }
        completed = true;
    }

    public void fail(String cause) {
        Exception e =  new Exception(cause);
        fail(e);
    }

    public void fail(Throwable cause) {
        if (handler != null) {
            it.filippetti.sp.android.bus.io.impl.AsyncResult asyncResult = new it.filippetti.sp.android.bus.io.impl.AsyncResult<>();
            try {
                asyncResult.cause = cause;
                handler.handle(asyncResult);

            } catch (Exception e) {
                //e.printStackTrace();
                Log.e(TAG, e.getMessage());

            }
        }
        completed = true;
    }
}
