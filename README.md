SmartProxy
==========

安卓下的智能代理 

======

由于原repo很久没有更新，而Shadowsocks的客户端在Lollipop上经常会崩溃，导致上网过程十分不流畅，所以我给SmartProxy添加了Shadowsocks的支持还有其他一些小功能：

1. Shadowsocks支持，使用shadowsocks-libv中的加密方法，理论上支持所有的加密方式
2. 开机启动支持，仅在5.0+系统工作，因为4.4每次都要弹出来VPN的对话框所以没办法，用Xposed里面的VPN auto confirm可能可以工作
3. 多配置文件支持，再也不用每次换配置都扫码了。

因为代码改的有点乱，而且不知道作者还有没有在维护，所以就先没提PR。要使用Shadowsocks的话，可以直接填入ss://的uri或者把pac文件中的proxy填写为ss://的地址都可以。


