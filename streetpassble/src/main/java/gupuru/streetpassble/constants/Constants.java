package gupuru.streetpassble.constants;

import gupuru.streetpassble.service.StreetPassService;

public class Constants {

    public static final String SERVICE_NAME = StreetPassService.class.getCanonicalName();

    public static final String ACTION_SCAN = "street.pass.action_scan";
    public static final String ACTION_GATT_SERVER_STATE_CHANGE = "street.pass.action_gatt_server_state_change";
    public static final String ACTION_GATT_SERVER_WRITE_REQUEST = "street.pass.action_gatt_server_write_request";
    public static final String ACTION_GATT_SERVER_READ_REQUEST = "street.pass.action_gatt_server_read_request";
    public static final String ACTION_GATT_SERVICE_ADDED = "street.pass.action_gatt_service_added";
    public static final String ACTION_SCAN_ADV_ERROR = "street.pass.scan_adv_error";
    public static final String ACTION_ADV = "street.pass.action_adv";
    public static final String ACTION_CONNECT_DEVICE = "street.pass.connect_device";
    public static final String ACTION_CLOSE_GATT = "street.pass.close_gatt";
    public static final String ACTION_OPEN_GATT = "street.pass.open_gatt";
    public static final String ACTION_SEND_DATA_TO_DEVICE = "street.pass.send_data_to_device";
    public static final String ACTION_BLE_SERVER_READ = "street.pass.server_read";
    public static final String ACTION_BLE_SERVER_WRITE = "street.pass.server_write";
    public static final String ACTION_BLE_SERVER_CONNECTED = "street.pass.server_connected";
    public static final String ACTION_BLE_SERVER_ERROR = "street.pass.server_error";

    public static final String DATA = "data";
    public static final String STREET_PASS_SETTINGS = "street_pass_settings";
    public static final String CAN_CONNECT = "can_connect";
    public static final String SCAN_DATA = "scan_data";
    public static final String ADV_DATA = "adv_data";
    public static final String ERROR_SCAN_ADV = "error_scan_adv";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String CONNECTION_DATA = "connection_data";
    public static final String IS_CONNECTION = "is_connection";
    public static final String READ_REQUEST = "read_request";
    public static final String WRITE_REQUEST = "write_request";
    public static final String SERVICE_ADDED = "service_added";
    public static final String BLE_SERVER_READ = "ble_server_read";
    public static final String BLE_SERVER_WRITE = "ble_server_write";
    public static final String BLE_SERVER_CONNECTED = "ble_server_connected";
    public static final String BLE_SERVER_ERROR = "ble_server_error";

    public static final int CODE_UN_REGISTER_RECEIVER_ERROR = 1000;
    public static final int CODE_BLE_SERVER_ERROR = 2000;

}
