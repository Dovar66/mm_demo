# mm_demo
AccessibilityService重新整理：微信自动抢红包、微信自动向附近的人打招呼、微信自动打开文章推送
关于如何获取app页面中控件的id：在Android Studio中开启Android Device Monitor,选择设备后点击Dump View Hierarchy for UI Automator即可查看
<p align="center">
  <img src="http://img.blog.csdn.net/20170313214108128?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvRG92YXJfNjY=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/Center" width="220" height="220" alt="Banner" />
</p>

关于使用AccessibilityService前的配置：
在manifest中的配置：
<uses-permission android:name="android.permission.BIND_ACCESSIBILITY_SERVICE" />

<service
    android:enabled="true"
    android:exported="true"
    android:label="@string/app_name"
    android:name=".AutoService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService"/>
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/envelope_service_config"/>
</service>
meta-data中的xml资源文件：
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeNotificationStateChanged|typeWindowStateChanged|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags=""
    android:canRetrieveWindowContent="true"
    android:description="@string/app_name"
    android:notificationTimeout="100"
    android:packageNames="com.tencent.mm,com.huawei.android.launcher" />  
其中:
packageName用于配置你想要监测的包名，如果多个则用逗号隔开，未配置此项时默认监测所有程序。
accessibilityEventTypes表示该服务可监测界面中哪些事件类型,如窗口打开,滑动等,具体值可查看api。
accessibilityFeedbackType 表示反馈方式,比如是语音播放,还是震动。
canRetrieveWindowContent 表示该服务能否访问活动窗口中的内容，为false时getRootInActiveWindow()获取结果为null.
notificationTimeout 接受事件的时间间隔。

当然，除了以meta-data的方式静态配置，也可通过在服务启动时的onServiceConnected()方法中调用setServiceInfo(AccessibilityServiceInfo)进行动态配置。

# 补充：
## 几种常用accessibilityEventType事件类型：
TYPE_WINDOW_STATE_CHANGED	窗口状态改变事件类型，打开PopupWindow、dialog、menu等。
TYPE_NOTIFICATION_STATE_CHANGED		通知栏事件
TYPE_WINDOW_CONTENT_CHANGED		窗口中内容改变
TYPE_VIEW_SCROLLED		控件滑动事件
TYPE_WINDOWS_CHANGED		显示窗口改变
TYPE_VIEW_TEXT_CHANGED		editText控件的内容发生改变
TYPE_TOUCH_INTERACTION_START		用户开始触摸屏幕
TYPE_TOUCH_INTERACTION_END		用户停止触摸屏幕
其中TYPE_WINDOW_CONTENT_CHANGED	又可以细分为4个二级类型：	
1.CONTENT_CHANGE_TYPE_SUBTREE	节点发生增减
2.CONTENT_CHANGE_TYPE_TEXT	节点文本发生改变
3.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION		节点的内容描述发生改变，即控件的contentDescription属性发生改变
4.CONTENT_CHANGE_TYPE_UNDEFINED	未定义类型，即除上面三种之外的类型
