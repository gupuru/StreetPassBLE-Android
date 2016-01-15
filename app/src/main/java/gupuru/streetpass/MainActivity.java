package gupuru.streetpass;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

        Button startBtn = (Button) findViewById(R.id.start);
        startBtn.setOnClickListener(this);
        Button stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                break;
            case R.id.stop:
                break;
            default:
                break;
        }
    }

}
