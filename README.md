![header](https://raw.githubusercontent.com/gupuru/StreetPassBLE-iOS/assets/icon.png)

# StreetPassBLE すれ違い通信

**Android's StreetPass Communication library**

## Caution

This library is developing now.
[iOS's StreetPass library](https://github.com/gupuru/StreetPassBLE-iOS) is stable.If you use the iOS, Please use the iOS's StreetPass library.

## What is StreetPass Communication?

StreetPass is a Nintendo 3DS functionality which allows passive communication between Nintendo 3DS systems held by users in close proximity, an example being the sharing of Mii avatars in the StreetPass Mii Plaza application, and other game data. New data received from StreetPass is indicated via a green status light on the system.

[Wiki](https://en.wikipedia.org/wiki/SpotPass_and_StreetPass)

## About this library

When the terminal with each other has become close to, it is capable of transmitting and receiving data of about 100 bytes.

Android SDK version 21 or higher and SmartPhone can use HCI command of multiple advertisement.

## Installation

Gradle:

```
dependencies {
  compile 'com.gupuru.streetpass:streetpass:0.1.4'
}
```

## Usage

Please add the following to the `manifest`.

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### Start StreetPass

```java
new StreetPassBle(this).start();
```

### Settings

```java
StreetPassSettings streetPassSettings
  = new StreetPassSettings.Builder()
        .advertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
        .scanMode(ScanSettings.SCAN_MODE_BALANCED)
        .txPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .build();

new StreetPassBle(this).start(streetPassSettings);
```

### Stop StreetPass

Please stop sure.

```java
new StreetPassBle(this).stop();
```

### Callbacks

Received data.

```java
@Override
public void receivedData(final TransferData data) {
    Log.d("receivedData", data.getData());
}
```

Error

```java
@Override
public void error(StreetPassError streetPassError) {
    Log.d("error", streetPassError.getErrorMessage());
}
```

### Sample

```java
public class MainActivity extends AppCompatActivity {

    private StreetPassBle streetPassBle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        streetPassBle = new StreetPassBle(MainActivity.this);
        streetPassBle.setOnStreetPassBleListener(new StreetPassBle.OnStreetPassBleListener() {
            @Override
            public void error(StreetPassError streetPassError) {
              //エラー
            }

            @Override
            public void receivedData(TransferData data) {
              //受信データ
            }
        });
        streetPassBle.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        streetPassBle.stop();
    }

}
```

# License

```
The MIT License (MIT)

Copyright (c) 2016 Kohei Niimi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
