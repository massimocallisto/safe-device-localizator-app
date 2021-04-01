package it.filippetti.safe.localizator.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(
        // Flag to make an entity "active": Active entities have update,
        // delete, and refresh methods.
        active = true,

        // Whether an all properties constructor should be generated.
        // A no-args constructor is always required.
        generateConstructors = true,

        // Whether getters and setters for properties should be generated if missing.
        generateGettersSetters = true
)


public class PersistModel {
    @Id(autoincrement = true)
    private Long id;

    private String deviceId;
    private String coordinatorId;
    private Double rssi;
    private Double latitude;
    private Double longitude;
    private Date eventTime;
    private String eventTimeStr;

/** Used to resolve relations */
@Generated(hash = 2040040024)
private transient DaoSession daoSession;

/** Used for active entity operations. */
@Generated(hash = 456900968)
private transient PersistModelDao myDao;
@Generated(hash = 1723167565)
public PersistModel(Long id, String deviceId, String coordinatorId, Double rssi,
        Double latitude, Double longitude, Date eventTime, String eventTimeStr) {
    this.id = id;
    this.deviceId = deviceId;
    this.coordinatorId = coordinatorId;
    this.rssi = rssi;
    this.latitude = latitude;
    this.longitude = longitude;
    this.eventTime = eventTime;
    this.eventTimeStr = eventTimeStr;
}
@Generated(hash = 460641664)
public PersistModel() {
}
public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getDeviceId() {
    return this.deviceId;
}
public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
}
public String getCoordinatorId() {
    return this.coordinatorId;
}
public void setCoordinatorId(String coordinatorId) {
    this.coordinatorId = coordinatorId;
}
public Double getRssi() {
    return this.rssi;
}
public void setRssi(Double rssi) {
    this.rssi = rssi;
}
public Double getLatitude() {
    return this.latitude;
}
public void setLatitude(Double latitude) {
    this.latitude = latitude;
}
public Double getLongitude() {
    return this.longitude;
}
public void setLongitude(Double longitude) {
    this.longitude = longitude;
}
public Date getEventTime() {
    return this.eventTime;
}
public void setEventTime(Date eventTime) {
    this.eventTime = eventTime;
}
public String getEventTimeStr() {
    return this.eventTimeStr;
}
public void setEventTimeStr(String eventTimeStr) {
    this.eventTimeStr = eventTimeStr;
}
/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 128553479)
public void delete() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.delete(this);
}
/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 1942392019)
public void refresh() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.refresh(this);
}
/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 713229351)
public void update() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.update(this);
}
/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 2065245094)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getPersistModelDao() : null;
}
}
