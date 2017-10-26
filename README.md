# mm_demo

关于如何获取app页面中控件的id：在Android Studio中开启Android Device Monitor,选择设备后点击Dump View Hierarchy for UI Automator即可查看。<br>  
<p align="center">
  <img src="app\src\main\assets\20170313214108128.png"  alt="Banner" />
</p>
![pic](20170313214108128.png)

关于使用AccessibilityService前的配置：
在manifest中的配置：
```xml
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
```

meta-data中的xml资源文件：
```xml
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeNotificationStateChanged|typeWindowStateChanged|typeWindowContentChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags=""
    android:canRetrieveWindowContent="true"
    android:description="@string/app_name"
    android:notificationTimeout="100"
    android:packageNames="com.tencent.mm,com.huawei.android.launcher" />
```

其中:<br>  
packageName用于配置你想要监测的包名，如果多个则用逗号隔开，未配置此项时默认监测所有程序。<br>  
accessibilityEventTypes表示该服务可监测界面中哪些事件类型,如窗口打开,滑动等,具体值可查看api。<br>  
accessibilityFeedbackType 表示反馈方式,比如是语音播放,还是震动。<br>  
canRetrieveWindowContent 表示该服务能否访问活动窗口中的内容，为false时getRootInActiveWindow()获取结果为null。<br>  
notificationTimeout 接受事件的时间间隔。<br>   
当然，除了以meta-data的方式静态配置，也可通过在服务启动时的onServiceConnected()方法中调用setServiceInfo(AccessibilityServiceInfo)进行动态配置。
## 补充：
### 几种常用accessibilityEventType事件类型：
TYPE_WINDOW_STATE_CHANGED	窗口状态改变事件类型，打开PopupWindow、dialog、menu等<br>  
TYPE_NOTIFICATION_STATE_CHANGED		通知栏事件<br>  
TYPE_WINDOW_CONTENT_CHANGED		窗口中内容改变<br>  
TYPE_VIEW_SCROLLED		控件滑动事件<br>  
TYPE_WINDOWS_CHANGED		显示窗口改变<br>  
TYPE_VIEW_TEXT_CHANGED		editText控件的内容发生改变<br>  
TYPE_TOUCH_INTERACTION_START		用户开始触摸屏幕<br>  
TYPE_TOUCH_INTERACTION_END		用户停止触摸屏幕<br>  
#### 其中TYPE_WINDOW_CONTENT_CHANGED	又可以细分为4个二级类型：	
  * CONTENT_CHANGE_TYPE_SUBTREE	节点发生增减。<br>  
  * CONTENT_CHANGE_TYPE_TEXT	节点文本发生改变。<br>  
  * CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION		内容描述发生改变，即控件的contentDescription属性发生改变。<br>  
  * CONTENT_CHANGE_TYPE_UNDEFINED	未定义类型，即除上面三种之外的类型。<br>  
