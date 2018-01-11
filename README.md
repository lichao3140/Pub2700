# InDoor
* cmd:keytool -list -keystore debug.keystore<br>
* pwd:android
* adb uninstall com.dpower.pub.dp2700
* adb install	 C:/Users/Administrator/Desktop/linphone/UpdateApk.apk
* adb shell ps|grep com.dpower.pub.dp2700
* adb shell am start -n ｛包(package)名｝/｛包名｝.{活动(activity)名称,启动应用
* adb shell am start -n com.android.calculator2/com.android.calculator2.Calculator,启动计算器
* 删除系统应用：
* adb remount （重新挂载系统分区，使系统分区重新可写）。
* adb shell
* cd system/app
* rm *.apk （rm UpdateApk.apk）
* 把电脑文件传到手机文件夹里   不要进入adb shll里面执行
* adb push C:/Users/Administrator/Desktop/linphone/1/netcfg.dat /sdcard
* 把手机文件传到电脑文件夹里   不要进入adb shll里面执行
* adb pull /mnt/sdcard/netcfg.dat C:/Users/Administrator/Desktop/linphone/
---
![](/screenshot/device-2017-10-10-153933.png "主页" height="240" width="420")
![](/screenshot/device-2017-10-10-154009.png "云对讲")
![](/screenshot/device-2017-10-10-154019.png "家庭安防")
![](/screenshot/device-2017-10-10-154028.png "可视对讲")
![](/screenshot/device-2017-10-10-154039.png "智能家居")
![](/screenshot/device-2017-10-10-154049.png "设置")
![](/screenshot/device-2017-10-10-154059.png "娱乐天地")
