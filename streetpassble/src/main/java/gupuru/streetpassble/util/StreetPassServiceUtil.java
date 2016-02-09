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

    /**
     * 端末情報を取得する
     *
     * @param device
     * @return
     */
    public DeviceData getDeviceData(BluetoothDevice device) {
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

    /**
     * バイト数が20バイトより上かどうか 20バイトより上ならtrueを返す
     * @param str
     * @return
     */
    public boolean isLimitDataSize(String str, int size) {
        return str.getBytes().length > size;
    }

    /**
     * 文字のバイト数を減らす
     * @param str
     * @param len
     * @param charset
     * @return
     */
    public String trimByte(String str, Integer len, String charset) {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;

        try {
            for (int i = 0; i < str.length(); i++) {
                String tmpStr = str.substring(i, i + 1);
                byte[] b = tmpStr.getBytes(charset);
                if (cnt + b.length > len) {
                    return sb.toString();
                } else {
                    sb.append(tmpStr);
                    cnt += b.length;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
