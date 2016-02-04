package gupuru.streetpassble.util;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import gupuru.streetpassble.parcelable.DeviceData;

/**
 * StreetPassService Util
 */
public class StreetPassServiceUtil {

    public StreetPassServiceUtil() {}

    /**
     * 端末情報を取得する
     * @param device
     * @return
     */
    public DeviceData getDeviceData(BluetoothDevice device){
        if (device != null) {
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
        return null;
    }

}
