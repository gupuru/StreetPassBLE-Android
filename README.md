![header](https://raw.githubusercontent.com/gupuru/StreetPassBLE-iOS/assets/icon.png)

# StreetPassBLE すれ違い通信

**すれ違い通信のAndroidライブラリ**

開発中のものなので、不安定な所があります。[iOS](https://github.com/gupuru/StreetPassBLE-iOS)のほうが安定しているので、iOSで良ければ、そちらをご利用ください。

すれ違い通信については、[こちら](https://ja.wikipedia.org/wiki/%E3%81%99%E3%82%8C%E3%81%A1%E3%81%8C%E3%81%84%E9%80%9A%E4%BF%A1)を参照してください。

端末同士がすれ違った時にデータ交換します。

１００バイト程度の送受信が可能です。

Android SDK version 21以上かつ、multiple advertisementのHCIコマンドが使える端末でのみ使用できます。

上記のことについては[こちら](http://qiita.com/eggman/items/6a13f5be7deb363c800d)の記事に詳しく書かれています。

# 導入方法

Gradle

```
dependencies {
  compile 'com.gupuru.streetpass:streetpass:0.1.1'
}
```

# 使い方

manifestに以下を追加してください。

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

このような形で使えます。

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
            public void nearByDevices(DeviceData deviceData) {
                //近くの端末
            }

            @Override
            public void error(StreetPassError streetPassError) {
              //エラー
            }

            @Override
            public void receivedData(TransferData data) {
              //受信データ
            }
        });
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
