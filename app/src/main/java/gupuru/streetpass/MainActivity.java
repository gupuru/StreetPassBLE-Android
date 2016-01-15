package gupuru.streetpass;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import gupuru.streetpassble.StreetPassBle;

public class MainActivity extends AppCompatActivity implements StreetPassBle.OnStreetPassListener, View.OnClickListener {

    private StreetPassBle streetPassBle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //マシュマロ判別
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //マシュマロ
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //未許可
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassListener(this);

        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(this);
    }

    @Override
    public void streetPassResult(int callbackType, String deviceAddress, String deviceName, String uuid, double distance, String serviceData) {
        Log.d("ここ", deviceAddress);
    }

    @Override
    public void advertiseSuccess(int txPowerLevel, int mode, int timeOut) {
        Log.d("ここ", "送信成功");
    }

    @Override
    public void error(int errorCode, String errorMessage) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                streetPassBle.start("0000180a-0000-1000-8000-00805f9b34fb", "test");
                break;
            case R.id.stop:
                streetPassBle.stop();
                break;
            default:
                break;
        }
    }
}
