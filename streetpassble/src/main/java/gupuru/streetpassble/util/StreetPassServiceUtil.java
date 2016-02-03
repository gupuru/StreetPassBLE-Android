package gupuru.streetpassble.util;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import gupuru.streetpassble.parcelable.DeviceData;

/**
 * StreetPassService Util
 */
public class StreetPassServiceUtil {

    public StreetPassServiceUtil() {

    }

    public DeviceData getDeviceData(BluetoothDevice device){
        //uuid取得
        ParcelUuid[] uuids = device.getUuids();
        String uuid = "";
        if (uuids != null) {
            for (ParcelUuid puuid : uuids) {
                uuid += puuid.toString() + " ";
            }
        }
        return new DeviceData(device.getType(), device.getAddress(), device.getName(), uuid, 0.0, null);
    }


}
