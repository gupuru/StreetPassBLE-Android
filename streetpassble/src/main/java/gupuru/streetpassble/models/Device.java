package gupuru.streetpassble.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Device extends RealmObject {

    @PrimaryKey
    private String deviceAddress;
    private String deviceName;
    private long time;
    private double distance;

    public double getDistance() {
        return distance;
    }

    public long getTime() {
        return time;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}