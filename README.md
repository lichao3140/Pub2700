# InDoor
* cmd:keytool -list -keystore debug.keystore<br>
* pwd:android
* adb uninstall com.dpower.pub.dp2700
* adb install	 C:/Users/Administrator/Desktop/linphone/UpdateApk.apk
* adb shell ps|grep com.dpower.pub.dp2700
* adb shell am start -n ｛包(package)名｝/｛包名｝.{活动(activity)名称,启动应用
* adb shell am start -n com.android.calculator2/com.android.calculator2.Calculator,启动计算器
* 杀死进程:
* am force-stop com.dpower.pub.dp2700
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
* sqlite3操作
* cd data/data/com.dpower.pub.dp2700/files
* chmod 777 files
* cd files
* sqlite3 lib2700_db.db
* .tables 查看数据库 lib2700_db里所有的表
* 显示字段
* .mode column 
* .header on
---
