SmartProxy
==========

Http Proxy and Shadowsocks Client

======

####  Feature provided by [hedaode/SmartProxy](https://github.com/hedaode/SmartProxy)

* Http proxy and pac support
* Shadowsocks with table as encrypt method

####  Added feature

* Shadowsocks all encrypt methods provided by [shadowsocks-libev](https://github.com/shadowsocks/shadowsocks-libev)
* Start on boot (Android version below 5.0 must have xposed and VPN auto confrim installed)
* Multiple proxy profile switch (include http and shadowsocks)
* Updated UI

####  Shadowsocks usage

* Replace your proxy server in pac or input directly in Config URL section with "ss://*base64ed_ss_uri*", format is described [here](https://github.com/shadowsocks/shadowsocks/wiki/Generate-QR-Code-for-Android-or-iOS-Clients)

#### build

build with Android Studio or with command line

    ./gradlew assembleDebug // or release if you prepared the resources
    
