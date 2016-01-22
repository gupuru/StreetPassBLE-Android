package gupuru.streetpassble.constants;

import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;

public class Settings {

    public static final String SERVICE_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final int SCAN__MODE = ScanSettings.SCAN_MODE_LOW_POWER;
    public static final int ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    public static final int TX_POWER_LEVEL = AdvertiseSettings.ADVERTISE_TX_POWER_LOW;
    public static final int TIME_OUT = 0;
    public static final boolean ADVERTISE_CONNECTABLE = true;
    public static final boolean ADVERTISE_INCLUDE_DEVICE_NAME = true;
    public static final boolean ADVERTISE_INCLUDE_TX_POWER_LEVEL = true;

}
