package gupuru.streetpass.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;

import gupuru.streetpass.R;
import gupuru.streetpass.adapter.BleRecyclerAdapter;
import gupuru.streetpass.bean.BleData;
import gupuru.streetpass.utils.DividerItemDecoration;
import gupuru.streetpassble.StreetPassBle;

public class MainActivity extends AppCompatActivity /*implements StreetPassBle.OnStreetPassListener, View.OnClickListener*/ {

    private StreetPassBle streetPassBle;
    private TextView statusTextView;
    private ArrayList<BleData> bleDataArrayList;
    private BleRecyclerAdapter bleRecyclerAdapter;
    private RecyclerView recyclerView;

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
/*
        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(this);
        Button sendBtn = (Button) findViewById(R.id.send);
        sendBtn.setOnClickListener(this);*/
        statusTextView = (TextView) findViewById(R.id.status);
/*
        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassListener(this);

        if (streetPassBle.isStreetPass()) {
            if (streetPassBle.isAdvertise()) {
                statusTextView.setText("送受信可能");
            } else {
                statusTextView.setText("受信のみ可能");
            }
        } else {
            startBtn.setVisibility(View.GONE);
            startBtn.setVisibility(View.GONE);
        }*/

        //RecyclerView初期化
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //区切り線をつける
        recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, null));
        bleDataArrayList = new ArrayList<>();
        //Adapter初期化
        bleRecyclerAdapter = new BleRecyclerAdapter(MainActivity.this, bleDataArrayList, streetPassBle);
        //アダプターにセット
        recyclerView.setAdapter(bleRecyclerAdapter);
        //更新を通知
        bleRecyclerAdapter.notifyDataSetChanged();

    }
/*
    @Override
    public void streetPassResult(ScanDataParcelable scanDataParcelable) {
        statusTextView.setText("受信しています。");

        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
        final Date date = new Date(System.currentTimeMillis());

        BleData bleData = new BleData(scanDataParcelable.getDeviceName(), scanDataParcelable.getDeviceAddress(), scanDataParcelable.getServiceData(), df.format(date));
        bleDataArrayList.add(bleData);
        bleRecyclerAdapter.setbleDataArrayList(bleDataArrayList);
        bleRecyclerAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(bleDataArrayList.size() - 1);
    }

    @Override
    public void advertiseSuccess(int txPowerLevel, int mode, int timeOut) {
        statusTextView.setText("送信しています。");
    }

    @Override
    public void error(int errorCode, String errorMessage) {
        statusTextView.setText(errorMessage);
    }
*/
  /*  @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                statusTextView.setText("開始します。");
                streetPassBle.start("0000180a-0000-1000-8000-00805f9b34fb", "test_yp");
                streetPassBle.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
                streetPassBle.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                streetPassBle.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW);
                break;
            case R.id.stop:
                streetPassBle.stop();
                bleRecyclerAdapter.clear();
                bleRecyclerAdapter.notifyDataSetChanged();
                statusTextView.setText("停止しました。");
                break;
            case R.id.send:
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_SEND_DATA_TO_DEVICE);
                intent.putExtra(Constants.DATA, "niku");
                sendBroadcast(intent);
                break;
            default:
                break;
        }
    }*/

}
