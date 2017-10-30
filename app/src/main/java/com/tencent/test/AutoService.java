package com.tencent.test;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 1.使用此服务需要获取手机特殊权限：部分手机点击本demo页面中“打开辅助服务”按钮进入辅助功能页即可找到名称为“抢红包”的服务，然后打开即可，
 * 其他手机需要在辅助功能中找到“无障碍”项，然后在“无障碍”中找到“抢红包”打开即可
 * note:APP获取到辅助功能权限后，一旦APP进程被强杀就会清除该权限，再次进入APP又需要重新申请，正常退出则不会
 * 2.确保手机的微信消息能在通知栏显示
 * Created by Dovar66
 */
public class AutoService extends AccessibilityService {
    public static final String TAG = "test";
    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 向附近的人自动打招呼的内容
     */
    private String hello = "测试APP自动打招呼功能，这是一条测试信息";

    public static boolean enableFunc2;          //标记是否开启抢红包功能
    public static boolean enableFunc3;          //是否开启自动添加附近的人为好友的功能;

    private boolean isRunning;      //是否正处于打开红包流程中
    private boolean isBackward;     //是否刚从开完红包页面返回到聊天界面

    private int i = 0;//记录已打招呼的人数
    private int page = 1;//记录附近的人列表页码,初始页码为1
    private int prepos = -1;//记录页面跳转来源，0--从附近的人页面跳转到详细资料页，1--从打招呼页面跳转到详细资料页

    private TextToSpeech mTts;

    private List<String> ids = Arrays.asList("bjj", "bi3", "brt");      //用于存储微信开红包按钮使用过的id，微信几乎每次版本更新都会修改此button的id
    private List<String> activityNames = Arrays.asList("com.tencent.mm.plugin.luckymoney.ui.En_fba4b94f",
            "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI");      //储存微信开红包activity使用过的className，最新的微信版本更新有修改过此activity的名称

    private String currentActivity = "";

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();
        String str_eventType;
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.i(TAG, "==============Start====================");
                str_eventType = "TYPE_VIEW_CLICKED";
                AccessibilityNodeInfo noteInfo = event.getSource();
                Log.i(TAG, noteInfo.toString());
                Log.i(TAG, "=============END=====================");
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                str_eventType = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                str_eventType = "TYPE_VIEW_LONG_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                str_eventType = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                str_eventType = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                str_eventType = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                str_eventType = "TYPE_NOTIFICATION_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                str_eventType = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
                break;
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                str_eventType = "TYPE_ANNOUNCEMENT";
                break;
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                str_eventType = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                str_eventType = "TYPE_VIEW_HOVER_ENTER";
                break;
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                str_eventType = "TYPE_VIEW_HOVER_EXIT";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                str_eventType = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                str_eventType = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                str_eventType = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
            default:
                str_eventType = String.valueOf(eventType);
        }
        String action;
        switch (event.getAction()) {
            case AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS:
                action = "ACTION_ACCESSIBILITY_FOCUS";
                break;
            case AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS:
                action = "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
                break;
            case AccessibilityNodeInfo.ACTION_CLEAR_FOCUS:
                action = "ACTION_CLEAR_FOCUS";
                break;
            case AccessibilityNodeInfo.ACTION_CLEAR_SELECTION:
                action = "ACTION_CLEAR_SELECTION";
                break;
            case AccessibilityNodeInfo.ACTION_CLICK:
                action = "ACTION_CLICK";
                break;
            case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:
                action = "ACTION_SCROLL_FORWARD";
                break;
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD:
                action = "ACTION_SCROLL_BACKWARD";
                break;
            case AccessibilityNodeInfo.ACTION_FOCUS:
                action = "ACTION_FOCUS";
                break;
            case 0://com.android.systemui 一般是系统标题栏内容发生改变
                //在微信内页但不在聊天详情页时，收到红包不会产生通知栏事件，只有系统标题栏内容发生改变
                //标题栏中已经有微信未读消息图标时：TYPE_WINDOW_CONTENT_CHANGED	package:com.android.systemui	Class:android.widget.ImageView
                //标题栏中尚未有未读消息图标时：TYPE_WINDOW_CONTENT_CHANGED	package:com.android.systemui	Class:android.widget.FrameLayout
                Log.i(TAG, "EventType: " + str_eventType + "\tpackage:" + event.getPackageName() + "\tClass:" + event.getClassName() + "\t");
                return;
            default:
                action = event.getAction() + "";
                break;
        }
        Log.v(TAG, "EventType: " + str_eventType + "\tAction:" + action + "\tpackage:" + event.getPackageName() + "\tClass:" + event.getClassName() + "\t");

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && MainActivity.canShowWindow(this)) {
            currentActivity = String.valueOf(event.getClassName());
            TasksWindow.show(this, event.getPackageName() + "\n" + event.getClassName());
        }

        if (enableFunc2) {          //抢红包
            //通知栏、Toast事件
            // TODO: 2017/10/26  MIUI9不会触发AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                if (event.getParcelableData() != null || event.getParcelableData() instanceof Notification) {//通知栏事件
                    List<CharSequence> texts = event.getText();
                    if (!texts.isEmpty()) {
                        for (CharSequence t : texts) {
                            String text = String.valueOf(t);

                            if (text.contains("微信红包")) {
                                isRunning = true;
                                isBackward = false;
                                openNotification(event);
                            }
                        }
                    } else {
                        Toast.makeText(this, "没有text", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {//过滤掉通知栏和Toast事件
                for (int j = 0; j < activityNames.size(); j++) {
                    if (activityNames.get(j).equals(currentActivity)) {
                        isRunning = true;
                        //当前在红包待开页面，去拆红包
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getLuckyMoney(event);
                            }
                        }, 100);
                        break;
                    }
                }

                if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(currentActivity)) {
                    isRunning = false;
                    //拆完红包后看详细纪录的界面
//                openNext("查看我的红包记录");
                    isBackward = true;
                    mTts.speak("抢到红包了", TextToSpeech.QUEUE_FLUSH, null);
                } else if ("com.tencent.mm.ui.LauncherUI".equals(currentActivity)) {
                    //在聊天界面,去点中红包
                    if (!isBackward) {//不是从拆完红包页返回时
                        isRunning = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                openLuckyEnvelope();
                            }
                        }, 100);
                    } else {
                        isRunning = false;
                    }
                    isBackward = false;
                }
            }
        }

        //自动加人
        if (enableFunc3) {
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && event.getClassName().equals("com.tencent.mm.ui.LauncherUI")) {
                //记录打招呼人数置零
                i = 0;
                //当前在微信聊天页就点开发现
                openNext("发现");
                //然后跳转到附近的人
                openDelay(1000, "附近的人");
            } else if (event.getClassName().equals("com.tencent.mm.plugin.nearby.ui.NearbyFriendsUI") && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                prepos = 0;
                //当前在附近的人界面就点选人打招呼
                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("米以内");
                Log.d("name", "附近的人列表人数: " + list.size());
                if (i < (list.size() * page)) {
                    list.get(i % list.size()).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    list.get(i % list.size()).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else if (i == list.size() * page) {
                    //本页已全部打招呼，所以下滑列表加载下一页，每次下滑的距离是一屏
                    for (int i = 0; i < nodeInfo.getChild(0).getChildCount(); i++) {
                        if (nodeInfo.getChild(0).getChild(i).getClassName().equals("android.widget.ListView")) {
                            AccessibilityNodeInfo node_lsv = nodeInfo.getChild(0).getChild(i);
                            node_lsv.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                            page++;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException mE) {
                                        mE.printStackTrace();
                                    }
                                    AccessibilityNodeInfo nodeInfo_ = getRootInActiveWindow();
                                    List<AccessibilityNodeInfo> list_ = nodeInfo_.findAccessibilityNodeInfosByText("米以内");
                                    Log.d("name", "列表人数: " + list_.size());
                                    //滑动之后，上一页的最后一个item为当前的第一个item，所以从第二个开始打招呼
                                    list_.get(1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    list_.get(1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }).start();
                        }
                    }
                }
            } else if (event.getClassName().equals("com.tencent.mm.plugin.profile.ui.ContactInfoUI") && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (prepos == 1) {
                    //从打招呼界面跳转来的，则点击返回到附近的人页面
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    i++;
                } else if (prepos == 0) {
                    //从附近的人跳转来的，则点击打招呼按钮
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo == null) {
                        Log.d(TAG, "rootWindow为空");
                        return;
                    }
                    List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("打招呼");
                    if (list.size() > 0) {
                        list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        list.get(list.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    } else {
                        //如果遇到已加为好友的则界面的“打招呼”变为“发消息"，所以直接返回上一个界面并记录打招呼人数+1
                        performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        i++;
                    }
                }
            } else if (event.getClassName().equals("com.tencent.mm.ui.contact.SayHiEditUI") && eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                //当前在打招呼页面
                prepos = 1;
                //输入打招呼的内容并发送
                inputHello(hello);
                openNext("发送");
            }
        }

        //自动从桌面打开微信，利用微信多开助手可实现多个微信账号之间的切换
//        if(topActivity.equals("com.huawei.android.launcher.Launcher")){
//            openNext(event,"微信");
//            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
//            nodeInfo.getChildCount();
//            for (int i=0;i<nodeInfo.getChildCount();i++){
//                String name=nodeInfo.getChild(i).getViewIdResourceName();
//            }
//        }
    }

    /**
     * 打开通知栏消息
     */
    private void openNotification(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //将通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击匹配的nodeInfo
     *
     * @param str text关键字
     */
    private void openNext(String str) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Toast.makeText(this, "rootWindow为空", Toast.LENGTH_SHORT).show();
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText(str);
        if (list != null && list.size() > 0) {
            list.get(list.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            list.get(list.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            Toast.makeText(this, "找不到有效的节点", Toast.LENGTH_SHORT).show();
        }
    }

    //延迟打开界面
    private void openDelay(final int delaytime, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delaytime);
                } catch (InterruptedException mE) {
                    mE.printStackTrace();
                }
                openNext(text);
            }
        }).start();
    }

    //自动输入打招呼内容
    private void inputHello(String hello) {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        //找到当前获取焦点的view
        AccessibilityNodeInfo target = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
        if (target == null) {
            Log.d(TAG, "inputHello: null");
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", hello);
        clipboard.setPrimaryClip(clip);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            target.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "服务已中断", Toast.LENGTH_SHORT).show();
        mTts.shutdown();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();
        mTts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTts.setLanguage(Locale.CHINESE);
                }
            }
        });
    }

    @Override
    public boolean onUnbind(Intent intent) {
        TasksWindow.dismiss();
        return super.onUnbind(intent);
    }

    //    private void sendNotificationEvent() {
//        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
//        if (!manager.isEnabled()) {
//            return;
//        }
//        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
//        event.setPackageName(WECHAT_PACKAGENAME);
//        event.setClassName(Notification.class.getName());
//        CharSequence tickerText = PUSH_TEXT_KEY;
//        event.getText().add(tickerText);
//        manager.sendAccessibilityEvent(event);
//    }


    //开红包
    private void getLuckyMoney(AccessibilityEvent event) {
//        getWindows();
//        findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY);
//        event.getSource();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            List<AccessibilityWindowInfo> nodeInfos = getWindows();
            for (AccessibilityWindowInfo window : nodeInfos
                    ) {
                AccessibilityNodeInfo nodeInfo = window.getRoot();
                if (nodeInfo == null) {
                    break;
                }
                Toast.makeText(this, "getWindows()不为空", Toast.LENGTH_SHORT).show();
                List<AccessibilityNodeInfo> list = null;
                for (String id : ids
                        ) {
                    list = nodeInfo.findAccessibilityNodeInfosByViewId(id);
                    if (list != null && list.size() > 0) {
                        list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        Toast.makeText(this, "clickBy getWindows()", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();  //获得整个窗口对象
            if (nodeInfo == null) {
                Toast.makeText(this, "rootWindow为空", Toast.LENGTH_SHORT).show();
                return;
            }
            //bi3是本人写代码时微信拆红包的button的id,该id可能会在更新微信版本后发生变更,可通过Android Device Monitor查看获取
            //可创建一个hashMap,在微信发生版本变更时储存对应微信版本号与id值，用于适配多个微信版本
            List<AccessibilityNodeInfo> list = null;
            for (String id : ids
                    ) {
                list = nodeInfo.findAccessibilityNodeInfosByViewId(id);
                if (list != null && list.size() > 0) {
                    list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Toast.makeText(this, "clickBy getRootInActiveWindow()", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //如果没找到拆红包的button，则将界面上所有子节点都点击一次
            for (int i = nodeInfo.getChildCount() - 1; i >= 0; i--) {
                if (("android.widget.Button").equals(nodeInfo.getChild(i).getClassName())) {
                    nodeInfo.getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Toast.makeText(this, "clickBy android.widget.Button", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Toast.makeText(this, "未找到开红包按钮", Toast.LENGTH_SHORT).show();
        }
    }

    //点击进入红包待开启界面
    private void openLuckyEnvelope() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Toast.makeText(this, "rootWindow为空", Toast.LENGTH_SHORT).show();
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if (list.isEmpty()) {
            openChatPage(nodeInfo);
        } else {
            //选择聊天记录中最新的红包
            for (int i = list.size() - 1; i >= 0; i--) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.d(TAG, "-->领取红包:" + parent);
                if (parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }

    //打开聊天内页
    private void openChatPage(AccessibilityNodeInfo nodeInfo) {
        //微信页能获取到4个Tab_Fragment的内容节点，但是获取不到"Tab_微信"的聊天列表item中用于显示最新一条聊天记录的那个TextView节点
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("[微信红包]恭喜发财，大吉大利");
        if (!list.isEmpty()) {
            for (int j = 0; j < list.size(); j++) {
                Log.d(TAG, "openLuckyEnvelope: " + list.get(j).getText());
                AccessibilityNodeInfo node = list.get(j);
                while (node != null && !node.isClickable()) {
                    node = node.getParent();
                }
                if (node != null) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        } else {
            Log.d(TAG, "openChatPage: 没有新红包");
        }
    }
}
