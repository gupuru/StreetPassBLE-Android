package gupuru.streetpass.bean;


public class BleData {

    private String deviceName;
    private String deviceAddress;
    private String serviceData;
    private String time;

    public BleData(String deviceName, String deviceAddress, String serviceData, String time){
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
        this.serviceData = serviceData;
        this.time = time;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getServiceData() {
        return serviceData;
    }

    public String getTime() {
        return time;
    }

}
