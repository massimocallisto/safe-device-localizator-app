package it.filippetti.safe.localizator;

import android.content.Context;
import android.util.Log;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Date;
import java.util.List;

import it.filippetti.safe.localizator.model.CoordinatorIoT;
import it.filippetti.safe.localizator.model.DaoSession;
import it.filippetti.safe.localizator.model.DeviceIoT;
import it.filippetti.safe.localizator.model.PersistModel;

public class DataService {
    private String TAG = DataService.class.getSimpleName();
    App app;
    public DataService(Context context){
        this.app = (App)context;
    }

    /**
     *
     * @param deviceName
     * @param coordinatorName
     * @param rssi
     * @param latitude
     * @param longitude
     */
    public void addRecord(
            String deviceName,
            String coordinatorName,
            Double rssi,
            Double latitude,
            Double longitude
    ) {
        PersistModel entity = new PersistModel();
        entity.setCoordinatorId(coordinatorName);
        entity.setDeviceId(deviceName);
        Date d = new Date();
        entity.setEventTime(d);
        entity.setEventTimeStr(DateUtils.dateString(d));
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setRssi(rssi);
        long id = getDao().getPersistModelDao().insert(entity);
        Log.i(TAG, "Added record with id " + id);
    }


    DaoSession getDao(){
        return app.getDaoSession();
    }


}
