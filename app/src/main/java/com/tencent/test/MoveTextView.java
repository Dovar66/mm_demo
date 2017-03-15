package com.tencent.test;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.Button;


/**
 * Created by Administrator on 2017-03-14.
 */

public class MoveTextView extends Button {
    private float mRawX;
    private float mRawY;

    private float mStartX;
    private float mStartY;

    public MoveTextView(Context context) {
        super(context);
    }

    public MoveTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoveTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 当前值以屏幕左上角为原点
        mRawX = event.getRawX();
        mRawY = event.getRawY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 以当前父视图左上角为原点
                mStartX = event.getX();
                mStartY = event.getY();
                isMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mStartX - event.getX()) > 10 || Math.abs(mStartY - event.getY()) > 10) {
                    updateWindowPosition();
                    isMoved = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return isMoved || super.onTouchEvent(event);
    }

    private boolean isMoved;

    private void updateWindowPosition() {
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        int screenHeight = size.y;
        if (lp != null) {
            // 更新坐标
            lp.x = (int) (mRawX - mStartX);
            lp.y = screenHeight - (int) (mRawY - mStartY)-getHeight();
            Log.d("tag", "updateWindowPosition: " + mRawY + "\n" + mStartY + "\n" + lp.y);
            // 使参数生效
            wm.updateViewLayout(this, lp);
        }
    }

    private WindowManager wm;
    private WindowManager.LayoutParams lp;

    public void setWm(WindowManager wm, WindowManager.LayoutParams lp) {
        this.wm = wm;
        this.lp = lp;
    }
}
