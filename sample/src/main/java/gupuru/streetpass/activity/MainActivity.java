package gupuru.streetpass.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import gupuru.streetpass.R;
import gupuru.streetpass.constans.Constants;
import gupuru.streetpassble.StreetPassBle;
import gupuru.streetpassble.parcelable.DeviceData;
import gupuru.streetpassble.parcelable.StreetPassSettings;
import gupuru.streetpassble.parcelable.TransferData;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, StreetPassBle.OnStreetPassBleListener {

    private StreetPassBle streetPassBle;
    private TextView logTextView;
    private Button startStopBtn;
    private EditText editText;
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

        startStopBtn = (Button) findViewById(R.id.start_stop);
        startStopBtn.setOnClickListener(this);
        logTextView = (TextView) findViewById(R.id.log_text_view);
        editText = (EditText) findViewById(R.id.send_data_edit_text);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassBleListener(this);

        //BLE対応端末か
        if (!streetPassBle.isBle()) {
            //非対応
            startStopBtn.setVisibility(View.GONE);
            logTextView.setText(getString(R.string.incompatible_street_pass));
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        isStart = false;
        streetPassBle.stop();
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
                    showAlertDialog(getString(R.string.version_title), streetPassBle.getLibraryVersion());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_stop:

                startStopBtn.setFocusable(true);
                startStopBtn.setFocusableInTouchMode(true);
                startStopBtn.requestFocus();

                String sendData = editText.getText().toString();
                if (!sendData.equals("")) {
                    if (!isStart) {
                        isStart = true;
                        logTextView.setText(getString(R.string.log_text_view_status));
                        startStopBtn.setText(getString(R.string.stop_button_text));
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
                                .data(sendData)
                                .build();

                        streetPassBle.start(streetPassSettings);
                    } else {
                        isStart = false;
                        startStopBtn.setText(getString(R.string.start_button_text));
                        streetPassBle.stop();
                    }
                } else {
                    showAlertDialog(getString(R.string.caution_title), getString(R.string.caution_message));
                }
                break;
            default:
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void nearByDevices(DeviceData deviceData) {
        setStatusText(deviceData.getDeviceName());
    }

    @Override
    public void error(gupuru.streetpassble.parcelable.Error error) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void receivedData(final TransferData data) {
        setStatusText(data.getMessage());
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

    /**
     * ステータスTextView
     *
     * @param data
     */
    private void setStatusText(final String data) {
        runOnUiThread(new Runnable() {
            public void run() {
                logTextView.setText(
                        getString(R.string.street_pass_status, logTextView.getText(), data)
                );
            }
        });
    }

}
