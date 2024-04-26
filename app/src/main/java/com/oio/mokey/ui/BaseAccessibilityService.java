package com.oio.mokey.ui;

import android.accessibilityservice.AccessibilityGestureEvent;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.InputMethod;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import androidx.annotation.NonNull;

import static android.util.Log.d;

public class BaseAccessibilityService extends AccessibilityService {

   private final String TAG = "BaseAccessibilityServic";


   private static BaseAccessibilityService mInstance;



   @NonNull
   @Override
   public InputMethod onCreateInputMethod() {
      return super.onCreateInputMethod();
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      d(TAG,
              "onStartCommand  with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
      return super.onStartCommand(intent, flags, startId);
   }

   public static BaseAccessibilityService getInstance() {
      if (mInstance == null) {
         mInstance = new BaseAccessibilityService();
      }
      return mInstance;
   }

//   /**
//    * Check当前辅助服务是否启用
//    *
//    * @param serviceName serviceName
//    * @return 是否启用
//    */
//   private boolean checkAccessibilityEnabled(String serviceName) {
//      List<AccessibilityServiceInfo> accessibilityServices =
//              mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
//      for (AccessibilityServiceInfo info : accessibilityServices) {
//         if (info.getId().equals(serviceName)) {
//            return true;
//         }
//      }
//      return false;
//   }



   public static boolean isAccessibilityServiceEnabled(Context context, Class<? extends AccessibilityService> serviceClass) {
      AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
      if (accessibilityManager != null) {
         // 获取当前启用的 AccessibilityService 列表
         List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
         for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            // 检查服务是否在启用列表中
            if (serviceInfo.getId().contains(serviceClass.getSimpleName())) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * 前往开启辅助服务界面
    */
   public static void goAccess(Context context) {
      Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
   }

   /**
    * 模拟点击事件
    *
    * @param nodeInfo nodeInfo
    */
   public void performViewClick(AccessibilityNodeInfo nodeInfo) {
      if (nodeInfo == null) {
         return;
      }
//      while (nodeInfo != null) {
//         if (nodeInfo.isEnabled()) {
            boolean b = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            d(TAG, "performViewClick: " + b);
//            break;
//         }
//         nodeInfo = nodeInfo.getParent();
//      }
   }

   /**
    * 模拟返回操作
    */
   public void performBackClick() {
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      performGlobalAction(GLOBAL_ACTION_BACK);
   }

   /**
    * 模拟下滑操作
    */
   public void performScrollBackward() {
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
   }

   /**
    * 模拟上滑操作
    */
   public void performScrollForward() {
      try {
         Thread.sleep(500);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
      performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
   }

   /**
    * 查找对应文本的View
    *
    * @param text text
    * @return View
    */
   public AccessibilityNodeInfo findViewByText(String text) {
      return findViewByText(text, false);
   }

   /**
    * 查找对应文本的View
    *
    * @param text      text
    * @param clickable 该View是否可以点击
    * @return View
    */
   public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
      AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
      if (accessibilityNodeInfo == null) {
         return null;
      }
      List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
      if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
         for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
               return nodeInfo;
            }
         }
      }
      return null;
   }

   /**
    * 查找对应ID的View
    *
    * @param id id
    * @return View
    */
   @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
   public AccessibilityNodeInfo findViewByID(String id) {
      AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
      if (accessibilityNodeInfo == null) {
         return null;
      }
      List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
      if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
         for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo != null) {
               return nodeInfo;
            }
         }
      }
      return null;
   }

   public void clickTextViewByText(String text) {
      AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
      if (accessibilityNodeInfo == null) {
         return;
      }
      List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
      if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
         for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo != null) {
               performViewClick(nodeInfo);
               break;
            }
         }
      }
   }

   @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
   public void clickTextViewByID(String id) {
      AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
      if (accessibilityNodeInfo == null) {
         return;
      }
      List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
      if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
         for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
            if (nodeInfo != null) {
               performViewClick(nodeInfo);
               break;
            }
         }
      }
   }

   /**
    * 模拟输入
    *
    * @param nodeInfo nodeInfo
    * @param text     text
    */
   public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         Bundle arguments = new Bundle();
         arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
         nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
      } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
         ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
         ClipData clip = ClipData.newPlainText("label", text);
         clipboard.setPrimaryClip(clip);
         nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
         nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
      }
   }

   public void performClickAt(int x, int y) {
      // 构建要点击的路径
      Path path = new Path();
      path.moveTo(x, y);

      // 构建手势描述
      GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
      gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
      d(TAG,"performClickAt "+ x + "  " + y);

      // 发送手势
      dispatchGesture(gestureBuilder.build(), new AccessibilityService.GestureResultCallback() {
         @Override
         public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);
            // 点击完成后的操作
            d(TAG,"onCompleted "+ gestureDescription);
         }

         @Override
         public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);
            d(TAG,"onCancelled "+ gestureDescription);

            // 点击取消时的操作
         }
      }, new Handler(Looper.getMainLooper())); // 使用主线程的 Handler
   }

   // 实现一个方法来执行滑动手势
   private void performSwipe() {
      // 构建滑动路径
      Path swipePath = new Path();
      swipePath.moveTo(500, 1000); // 起始点坐标
      swipePath.lineTo(500, 500); // 终点坐标

      // 创建手势描述
      GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
      gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
              swipePath, 0, 500)); // 0 表示手势开始的时间，500 表示手势持续的时间

      // 执行手势
      dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
         @Override
         public void onCompleted(GestureDescription gestureDescription) {
            super.onCompleted(gestureDescription);
            Log.d(TAG, "Swipe gesture completed");
         }

         @Override
         public void onCancelled(GestureDescription gestureDescription) {
            super.onCancelled(gestureDescription);
            Log.d(TAG, "Swipe gesture cancelled");
         }
      }, null);
   }

   // 示例：在某个节点上执行滑动手势
   private void performSwipeOnNode(AccessibilityNodeInfo nodeInfo) {
      if (nodeInfo != null) {
         // 获取节点在屏幕上的可见矩形区域
         Rect bounds = new Rect();
         nodeInfo.getBoundsInScreen(bounds);

         // 计算滑动的起始点和终点
         int startX = bounds.centerX();
         int startY = bounds.centerY();
         int endX = bounds.centerX(); // 结束点的 X 坐标与起始点相同
         int endY = bounds.top; // 结束点的 Y 坐标为节点的顶部

         // 构建滑动路径
         Path swipePath = new Path();
         swipePath.moveTo(startX, startY); // 起始点
         swipePath.lineTo(endX, endY); // 终点

         // 创建手势描述
         GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
         gestureBuilder.addStroke(new GestureDescription.StrokeDescription(
                 swipePath, 0, 500)); // 0 表示手势开始的时间，500 表示手势持续的时间

         // 执行手势
         dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
               super.onCompleted(gestureDescription);
               Log.d(TAG, "Swipe gesture completed");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
               super.onCancelled(gestureDescription);
               Log.d(TAG, "Swipe gesture cancelled");
            }
         }, null);
      }
   }


   @Override
   public boolean onGesture(@NonNull AccessibilityGestureEvent gestureEvent) {
      return super.onGesture(gestureEvent);
   }

   @Override
   public void onAccessibilityEvent(AccessibilityEvent event) {
   }

   @Override
   public void onInterrupt() {
      d(TAG, "onInterrupt ");
   }
}