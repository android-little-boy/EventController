package com.androidlittleboy.eventcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;


import java.util.Calendar;

import static android.widget.ImageView.ScaleType.*;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    ImageButton home;
    ImageButton back;
    int width = 640;
    int height = 400;
    private int statusBarHeight;
    private static final String TAG = "MainActivity";
    private int localX;
    private int localY;
    private boolean isFirstRc = true;
    private int overflow = 250;
    private boolean isFullWithHeight;
    private int remoteShowWidth;
    private int remoteShowHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.preview);
        home = findViewById(R.id.imageButton2);
        back = findViewById(R.id.imageButton3);
        WifiHotspot.getWifiHotspot().init(this, new WifiHotspot.Callback() {
            @Override
            public void onPreview(Bitmap bitmap) {
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                if (isFirstRc) {
                    calculateOverflow();
                    isFirstRc = false;
                }
                imageView.setImageBitmap(bitmap);
//                Log.d("TAG", "onPreview: +"+bitmap.getWidth());
                imageView.setScaleType(FIT_CENTER);
            }
        });
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(outSize);
        localX = outSize.x;
        localY = outSize.y;
        Log.d(TAG, "x = " + localX + ",y = " + localY + ",statusBarHeight:" + statusBarHeight);
        initEvent();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiHotspot.getWifiHotspot().sendEvent(6, 0, 0);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiHotspot.getWifiHotspot().sendEvent(5, 0, 0);
            }
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {

            long mCurrentDownTime;
            long mCurrentUpTime;
            int mCurrentDownX;
            int mCurrentDownY;
            int mCurrentUpX;
            int mCurrentUpY;
            int mCurrentMoveX;
            int mCurrentMoveY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mCurrentDownX = (int) event.getRawX();
                        mCurrentDownY = (int) event.getRawY();
                        int x = calculateX(mCurrentDownX);
                        int y = calculateY(mCurrentDownY);
                        if (isFullWithHeight) {
                            if (x < 0 || x > remoteShowWidth) {
                                break;
                            } else {
                                WifiHotspot.getWifiHotspot().sendEvent(2, x, y);
                            }
                        } else {
                            if (y < 0 || y > remoteShowHeight) {
                                break;
                            } else {
                                WifiHotspot.getWifiHotspot().sendEvent(2, x, y);
                            }
                        }
                        Log.d(TAG, "onTouch: ACTION_DOWN calculate：" + x + "," + y);
                        Log.d(TAG, "onTouch: ACTION_DOWN:" + event.getRawX() + "," + event.getRawY());
                        break;
                    case MotionEvent.ACTION_UP:
                        mCurrentUpX = (int) event.getRawX();
                        mCurrentUpY = (int) event.getRawY();
                        int xUp = calculateX(mCurrentUpX);
                        int yUp = calculateY(mCurrentUpY);
                        if (isFullWithHeight) {
                            if (xUp < 0 || xUp > width) {
                                break;
                            } else {
                                WifiHotspot.getWifiHotspot().sendEvent(3, xUp, yUp);
                            }
                        } else {
                            if (yUp < 0 || yUp > height) {
                                break;
                            } else {
                                WifiHotspot.getWifiHotspot().sendEvent(3, xUp, yUp);
                            }
                        }
                        Log.d(TAG, "onTouch: ACTION_UP:" + event.getRawX() + "," + event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mCurrentMoveX - ((int) event.getRawX())) < 5 && Math.abs(mCurrentMoveY - ((int) event.getRawY())) < 5) {
                            mCurrentMoveX = (int) event.getRawX();
                            mCurrentMoveY = (int) event.getRawY();
                            break;
                        }
                        mCurrentMoveX = (int) event.getRawX();
                        mCurrentMoveY = (int) event.getRawY();
                        int xMv = calculateX(mCurrentMoveX);
                        int yMv = calculateY(mCurrentMoveY);
                        if (isFullWithHeight) {
                            if (xMv < 0 || xMv > remoteShowWidth) {
                                break;
                            } else {
                                WifiHotspot.getWifiHotspot().sendEvent(4, xMv, yMv);
                            }
                        } else {
                            if (yMv < 0 || yMv > remoteShowHeight) {
                                break;
                            } else {
                                WifiHotspot.getWifiHotspot().sendEvent(4, xMv, yMv);
                            }
                        }
                        Log.d(TAG, "onTouch:ACTION_MOVE:" + event.getRawX() + "," + event.getRawY());
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.d(TAG, "onTouch: ACTION_CANCEL:" + event.getRawX() + "," + event.getRawY());
                        break;
                }
                return true;
            }
        });
    }

    private int calculateX(int x) {
        if (isFullWithHeight) {

            return (int) (((float) x - (float) overflow) * ((float) height / (float) localY));
        } else {
            return (int) ((float) x * ((float) width / (float) localX));
        }
    }

    private int calculateY(int y) {
        if (isFullWithHeight) {
            return (int) (((float) y - (float) statusBarHeight) * ((float) height / (float) localY));
        } else {
            return (int) (((float) y - (float) statusBarHeight - (float) overflow) * ((float) width / (float) localX));
        }
    }

    private void calculateOverflow() {
        float remoteRatio = (float) height / width;
        if ((((float) (localY - statusBarHeight)) / localX) < remoteRatio) {
            isFullWithHeight = true;
        } else {
            isFullWithHeight = false;
        }
        if (isFullWithHeight) {
            remoteShowWidth = ((int) ((localY - statusBarHeight) / remoteRatio));
            overflow = (localX - remoteShowWidth) / 2;
        } else {
            remoteShowHeight = (int) (localX * remoteRatio);
            overflow = (localY - statusBarHeight - remoteShowHeight) / 2;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiHotspot.getWifiHotspot().unInit();
    }
}