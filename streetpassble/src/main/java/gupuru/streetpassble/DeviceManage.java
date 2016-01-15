package gupuru.streetpassble;

import android.content.Context;
import android.os.SystemClock;

import gupuru.streetpassble.models.Device;
import gupuru.streetpassble.parcelable.ScanDataParcelable;
import io.realm.Realm;
import io.realm.RealmResults;

public class DeviceManage {

    private Context context;

    public DeviceManage(Context context) {
        this.context = context;
    }

    public void save(final ScanDataParcelable scanDataParcelable) {
        final Realm realm = Realm.getInstance(context);

        final Device deviceData = isDeviceData(realm, scanDataParcelable.getDeviceAddress());
        if (deviceData != null) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    deviceData.setTime(SystemClock.elapsedRealtime());
                    realm.copyToRealm(deviceData);
                }
            });
        } else {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Device device = realm.createObject(Device.class);
                    device.setDeviceAddress(scanDataParcelable.getDeviceAddress());
                    device.setDeviceName(scanDataParcelable.getDeviceName());
                    device.setTime(SystemClock.elapsedRealtime());
                    device.setDistance(scanDataParcelable.getDistance());
                }
            });
        }
        realm.close();
    }

    public long read(String deviceAddress) {
        Realm realm = Realm.getInstance(context);
        Device data = realm.where(Device.class).equalTo("deviceAddress", deviceAddress).findFirst();
        long time = 0L;
        if (data != null) {
            time = data.getTime();
        }
        realm.close();
        return time;
    }

    public void clear() {
        Realm realm = Realm.getInstance(context);
        RealmResults<Device> results = realm.where(Device.class).findAll();

        realm.beginTransaction();
        results.clear();
        realm.commitTransaction();

        realm.close();
    }

    private Device isDeviceData(Realm realm, String deviceAddress) {
        return realm.where(Device.class).equalTo("deviceAddress", deviceAddress).findFirst();
    }

}
