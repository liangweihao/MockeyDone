package com.oio.mokey.ui;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.Toast;

import com.oio.mokey.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import androidx.annotation.NonNull;

public class FloatingWidgetService extends Service {
    private WindowManager mWindowManager;
    private View mRootWidget;

    public static final int REQUEST_OVERLAY_PERMISSION = 1;

    public static boolean requestOverlayPermission(Activity context) {
        if (!Settings.canDrawOverlays(context)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            context.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
            return true;
        }
        return false;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                boolean accessibilityServiceEnabled = BaseAccessibilityService.isAccessibilityServiceEnabled(
                        getApplicationContext(),
                        AccessibilityServiceImpl.class);
                if (accessibilityServiceEnabled) {
                    btState.setBackgroundDrawable(getApplicationContext().getResources().getDrawable(
                            R.mipmap.ic_launcher));
                } else {
                    btState.setBackgroundDrawable(new ColorDrawable(Color.RED));
                }
                handler.sendEmptyMessageDelayed(1, 1000);
            } else if (msg.what == 2) {

                AccessibilityNodeInfo rootInActiveWindow = AccessibilityServiceImpl.rootInActiveWindow;
                if (rootInActiveWindow != null && capXML) {
                    capXML = false;
//                    String dumpWindowToString = AccessibilityNodeInfoDumper.dumpWindowToString(
//                            AccessibilityServiceImpl.rootInActiveWindow,
//                            0,
//                            mFloatingWidget.getMeasuredWidth(),
//                            mFloatingWidget.getMeasuredHeight());
//                    System.out.println(dumpWindowToString);

                    Point outSize = new Point();
                    mWindowManager.getDefaultDisplay().getSize(outSize);

                    AccessibilityNodeInfoDumper.dumpWindowToFile(rootInActiveWindow,
                            90,
                            outSize.x,
                            outSize.y);


                    AccessibilityNodeInfoDumper.Node toNode = AccessibilityNodeInfoDumper.dumpWindowToNode(
                            rootInActiveWindow,
                            outSize.x,
                            outSize.y);

                    Bitmap bitmap = Bitmap.createBitmap(outSize.x,
                            outSize.y,
                            Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(4);
                    paint.setColor(Color.parseColor("#46FB0000"));
                    drawRect(toNode.children,canvas,paint);

                    try {
                        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS);
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,new FileOutputStream(new File(externalStoragePublicDirectory,"out.png")));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getBaseContext(),"捕获完毕",Toast.LENGTH_LONG).show();
                }
                handler.sendEmptyMessageDelayed(2, 3000);

            }
        }
    };

    private void drawRect(List<AccessibilityNodeInfoDumper.Node> children, Canvas canvas, Paint paint) {
        for (int i = 0; i < children.size(); i++) {
            AccessibilityNodeInfoDumper.Node node = children.get(i);
            canvas.drawRect(node.bounds,paint);
            drawRect(node.children,canvas,paint);
        }
    }

    public static boolean canDrawOverlays(Context context) {
        return Settings.canDrawOverlays(context);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (!viewCreate) {
                initView();
            }
        } catch (Throwable e) {
            viewCreate = true;
        }
        return START_STICKY;
    }

    private boolean capXML = false;
    private ImageView btState;

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        // 创建浮窗布局参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 设置浮窗显示位置
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
//        params.width = WindowManager.LayoutParams.MATCH_PARENT;
//        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        // 初始化 WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 获取浮窗布局
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mRootWidget = inflater.inflate(R.layout.floating_widget_layout, null);

        btState = mRootWidget.findViewById(R.id.bt_state);
        // 添加浮窗到 WindowManager
        mWindowManager.addView(mRootWidget, params);


        // 设置浮窗的触摸监听器，以便用户可以拖动浮窗
        btState.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mRootWidget, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        capXML = true;
                        return true;

                }
                return false;
            }
        });
        handler.sendEmptyMessageDelayed(1, 1000);
        handler.sendEmptyMessageDelayed(2, 3000);

    }

    private boolean viewCreate = false;


    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRootWidget != null) {
            mWindowManager.removeView(mRootWidget);
        }
    }
}

