package gupuru.streetpassble.constants;

import gupuru.streetpassble.service.StreetPassService;

public class Constants {

    public static final String ACTION_SCAN = "street.pass.action_scan";
    public static final String ACTION_SCAN_ADV_ERROR = "street.pass.scan_adv_error";
    public static final String ACTION_ADV = "street.pass.action_adv";
    public static final String SERVICE_NAME = StreetPassService.class.getCanonicalName();

    public static final String DATA = "data";
    public static final String INTERVAL_TIME = "interval_time";
    public static final String UUID = "uuid";
    public static final String SCAN_DATA = "scan_data";
    public static final String ADV_DATA = "adv_data";
    public static final String ERROR_SCAN_ADV = "error_scan_adv";

    public static final long INTERVAL_TIME_MILL_SECOND = 300000L;

}
