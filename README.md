# StreetPass すれ違い通信

すれ違い通信用のライブラリ

**現在開発中です。**

**導入される場合は、自己責任でお願いします。**


# 導入方法

```
allprojects {
    repositories {
        maven { url 'https://raw.github.com/gupuru/StreetPassBLE/master/repository' }
    }
}

```


```
dependencies {
    compile 'gupuru:streetpassble:0.0.15'
}

```

# 使い方

manifestに以下を追加してください。

```
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

```
<service android:name="gupuru.streetpassble.service.StreetPassService" />
```

ActivityやFragmentで以下を呼び出してください。
```
StreetPassBle streetPassBle = new StreetPassBle(MainActivity.this);

streetPassBle.start("UUIDを入れてください。");

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
