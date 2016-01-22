package gupuru.streetpassble.constants;

import gupuru.streetpassble.service.StreetPassService;

public class Constants {

    public static final String ACTION_SCAN = "street.pass.action_scan";
    public static final String ACTION_SCAN_ADV_ERROR = "street.pass.scan_adv_error";
    public static final String ACTION_ADV = "street.pass.action_adv";
    public static final String ACTION_CONNECT_DEVICE = "street.pass.connect_device";
    public static final String ACTION_SEND_DATA_TO_DEVICE = "street.pass.send_data_to_device";
    public static final String SERVICE_NAME = StreetPassService.class.getCanonicalName();

    public static final String DATA = "data";
    public static final String STREET_PASS_SETTINGS = "street_pass_settings";
    public static final String CHARACTERISTIC_UUID = "characteristic_uuid";
    public static final String SCAN_DATA = "scan_data";
    public static final String ADV_DATA = "adv_data";
    public static final String ERROR_SCAN_ADV = "error_scan_adv";
    public static final String DEVICE_ADDRESS = "device_address";

}
