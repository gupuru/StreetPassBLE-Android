package gupuru.streetpassble.constants;

import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;

public class Settings {

    public static final String SERVICE_UUID = "0000180A-0000-1000-8000-00805F9B34FB";
    public static final String WRITE_CHARACTERISTIC_UUID = "0C136FCC-3381-4F1E-9602-E2A3F8B70CEB";
    public static final String READ_CHARACTERISTIC_UUID = "1BE31CB9-9E07-4892-AA26-30E87ABE9F70";
    public static final int SCAN__MODE = ScanSettings.SCAN_MODE_LOW_POWER;
    public static final int ADVERTISE_MODE = AdvertiseSettings.ADVERTISE_MODE_BALANCED;
    public static final int TX_POWER_LEVEL = AdvertiseSettings.ADVERTISE_TX_POWER_LOW;
    public static final boolean ADVERTISE_INCLUDE_DEVICE_NAME = true;
    public static final boolean ADVERTISE_INCLUDE_TX_POWER_LEVEL = true;

}
