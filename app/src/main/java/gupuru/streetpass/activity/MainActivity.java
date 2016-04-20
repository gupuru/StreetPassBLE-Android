package gupuru.streetpass.activity;

import android.Manifest;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import gupuru.streetpass.R;
import gupuru.streetpass.constans.Constants;
import gupuru.streetpassble.StreetPassBle;
import gupuru.streetpassble.parcelable.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, StreetPassBle.OnStreetPassBleListener {

    private StreetPassBle streetPassBle;
    private TextView logTextView;
    private boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ツールバー
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setTitle(getResources().getString(R.string.app_name));
            toolbar.setTitleTextColor(ContextCompat.getColor(MainActivity.this, R.color.white));
        }

        //マシュマロ判別
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //マシュマロ
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //未許可
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        Button startStopBtn = (Button) findViewById(R.id.start_stop);
        startStopBtn.setOnClickListener(this);
        logTextView = (TextView) findViewById(R.id.log_text_view);

        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassBleListener(this);

        //BLE対応端末か
        if (streetPassBle.isBle()) {
            if (streetPassBle.isAdvertise()) {
                //送信可能
                logTextView.setText(getString(R.string.send_receive_status_message));
            } else {
                //受信のみ
                logTextView.setText(getString(R.string.receive_status_message));
            }
        } else {
            //非対応
            startStopBtn.setVisibility(View.GONE);
            startStopBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.contact:
                showAlertDialog(getString(R.string.contact_title), getString(R.string.contact_message));
                return true;
            case R.id.version:
                if (streetPassBle != null) {
                    showAlertDialog(getString(R.string.versioin_title), streetPassBle.getLibraryVersion());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * アラートダイヤログを表示
     *
     * @param title
     * @param message
     */
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.alert_positive_button), null)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_stop:
                if (!isStart) {
                    isStart = true;
                    logTextView.setText(getString(R.string.starting_status_message));

                    StreetPassSettings streetPassSettings
                            = new StreetPassSettings.Builder()
                            .advertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                            .scanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                            .txPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                            .advertiseConnectable(true)
                            .advertiseIncludeDeviceName(true)
                            .advertiseIncludeTxPowerLevel(true)
                            .serviceUuid(Constants.SERVICE_UUID)
                            .sendDataMaxSize(true)
                            .data(getString(R.string.test_message))
                            .build();

                    streetPassBle.start(streetPassSettings);
                } else {
                    isStart = false;
                    streetPassBle.stop();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void nearByDevices(DeviceData deviceData) {
        Log.d("ここ", deviceData.getDeviceName());
    }

    @Override
    public void error(gupuru.streetpassble.parcelable.Error error) {
        Log.d("ここ", error.getErrorMessage());
    }

    @Override
    public void receivedData(TransferData data) {
        Log.d("ここ", data.getMessage());
    }

}
