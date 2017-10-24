package com.tencent.test;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import static com.tencent.test.AutoService.TAG;
import static com.tencent.test.AutoService.enableFunc2;
import static com.tencent.test.AutoService.enableFunc3;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dealStatusBar();

        CheckBox cb_assist = (CheckBox) findViewById(R.id.cb_assist_permission);
        if (cb_assist != null) {
            cb_assist.setOnCheckedChangeListener(this);
        }
        CheckBox cb_window = (CheckBox) findViewById(R.id.cb_show_window);
        if (cb_window != null) {
            cb_window.setOnCheckedChangeListener(this);
        }
        createFloatView();
    }

    private void requestPermission() {
        try {
            //打开系统设置中辅助功能
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "找到抢红包，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MoveTextView floatBtn1;
    private MoveTextView floatBtn2;
    private WindowManager wm;

    //创建悬浮按钮
    private void createFloatView() {
        WindowManager.LayoutParams pl = new WindowManager.LayoutParams();
        wm = (WindowManager) getSystemService(getApplication().WINDOW_SERVICE);
        pl.type = WindowManager.LayoutParams.TYPE_PHONE;//修改为此TYPE_TOAST，可以不用申请悬浮窗权限就能创建悬浮窗,但在部分手机上会崩溃
        pl.format = PixelFormat.RGBA_8888;
        pl.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        pl.gravity = Gravity.END | Gravity.BOTTOM;
        pl.x = 0;
        pl.y = 0;

        pl.width = WindowManager.LayoutParams.WRAP_CONTENT;
        pl.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(this);
        floatBtn1 = (MoveTextView) inflater.inflate(R.layout.floatbtn, null);
        floatBtn1.setText("打招呼");
        floatBtn2 = (MoveTextView) inflater.inflate(R.layout.floatbtn, null);
        floatBtn2.setText("抢红包");
        wm.addView(floatBtn1, pl);
        pl.gravity = Gravity.BOTTOM | Gravity.START;
        wm.addView(floatBtn2, pl);

        floatBtn1.setOnClickListener(this);
        floatBtn2.setOnClickListener(this);
        floatBtn1.setWm(wm, pl);
        floatBtn2.setWm(wm, pl);
    }

    @Override
    public void onClick(View v) {
        if (isAccessibilitySettingsOn()) {
            if (v == floatBtn1) {
                if (enableFunc3) {
                    floatBtn1.setText("打招呼");
                } else {
                    floatBtn1.setText("off");
                }
                enableFunc3 = !enableFunc3;
            } else {
                if (enableFunc2) {
                    floatBtn2.setText("抢红包");
                } else {
                    floatBtn2.setText("off");
                }
                enableFunc2 = !enableFunc2;
            }
        }
    }


    /**
     * 检测辅助功能是否开启
     */
    private boolean isAccessibilitySettingsOn() {
        int accessibilityEnabled = 0;
        String service = getPackageName() + "/" + AutoService.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.d(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    /**
     * 沉浸式状态栏
     */
    public void dealStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }


    @Override
    protected void onDestroy() {
        wm.removeView(floatBtn1);
        wm.removeView(floatBtn2);
        wm = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_assist_permission:
                requestPermission();
//                new AlertDialog.Builder(this)
//                        .setMessage(R.string.dialog_enable_accessibility_msg)
//                        .setPositiveButton(R.string.dialog_enable_accessibility_positive_btn
//                                , new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        setIsShowWindow(MainActivity.this, true);
//                                        Intent intent = new Intent();
//                                        intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
//                                        startActivity(intent);
//                                    }
//                                })
//                        .setNegativeButton(android.R.string.cancel
//                                , new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
////                                            refreshWindowSwitch();
//                                    }
//                                })
//                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
////                                    refreshWindowSwitch();
//                            }
//                        })
//                        .create()
//                        .show();
                break;
            case R.id.cb_show_window:
                if (isChecked) {
                    new AlertDialog.Builder(this)
                        .setMessage(R.string.dialog_enable_overlay_window_msg)
                        .setPositiveButton(R.string.dialog_enable_overlay_window_positive_btn
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                        intent.setData(Uri.parse("package:"+getPackageName()));
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        setIsShowWindow(MainActivity.this, false);
                                    }
                                })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                setIsShowWindow(MainActivity.this, false);
                            }
                        })
                        .create()
                        .show();
                }

                setIsShowWindow(this, isChecked);
                if (!isChecked) {
                    TasksWindow.dismiss();
                } else {
                    TasksWindow.show(this, getPackageName() + "\n" + getClass().getName());
                }
                break;
        }
    }

    public static boolean isShowWindow(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("is_show_window", true);
    }

    public static void setIsShowWindow(Context context, boolean isShow) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("is_show_window", isShow).commit();
    }
}
