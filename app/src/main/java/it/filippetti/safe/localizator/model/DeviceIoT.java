package it.filippetti.safe.localizator.model;

import android.location.Location;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//import it.smartspace.jz.communication.interfaces.InductiveCommunication;

/**
 * Created by matteo on 22/09/2015.
 */
public class DeviceIoT implements Serializable {

    private String name;
    private short address;
    private double power;
    //private InductiveCommunication.DeviceType type;
    String type = "device";
    private byte status;
    private Double latitude, longitude;
    private Double altitude;
    private Location location;

    public class LastLocation{
        private Double latitude, longitude;
        private double power;

        public LastLocation(Double latitude, Double longitude, double power) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.power = power;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    //    public Device(){
//        short address = (short)((value >> 16) & 0xFFFF);
//        byte rssi = (byte)((value >> 8) & 0xFF);
//        byte devicetype = (byte)((value >> 0) & 0xFF);
//    }
//
    public DeviceIoT(short address) {
        super();
        this.address = address;
        name = String.format("0x%04X", address);
    }

    public DeviceIoT(){
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public short getAddress() {
        return address;
    }

    public void setAddress(short address) {
        this.address = address;
        name = String.format("0x%04X", address);
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getPower() {
        return power;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("*** Devce IoT:\n " +
                "* Name: \t%s\n* Power: \t%.2f\n* (Lat,Lon): \t(%.5f,%.5f)",
                name, power, latitude, longitude);
    }
}
