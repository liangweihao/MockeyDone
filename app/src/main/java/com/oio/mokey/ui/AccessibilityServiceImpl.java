package com.oio.mokey.ui;


import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOWS_CHANGED;
import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

public class AccessibilityServiceImpl extends BaseAccessibilityService {
    boolean hasAction = false;
    boolean locked = false;
    boolean background = false;
    private String name;
    private String scontent;
    AccessibilityNodeInfo itemNodeinfo;
    public static AccessibilityNodeInfo rootInActiveWindow;
    private final String TAG = "AccessibilityServiceImp";

    //EventType: TYPE_WINDOW_CONTENT_CHANGED; EventTime: 1176424969; PackageName: com.ss.android.ugc.aweme; MovementGranularity: 0; Action: 0; ContentChangeTypes: [CONTENT_CHANGE_TYPE_SUBTREE]; WindowChangeTypes: [] [ ClassName: android.widget.SeekBar; Text: []; ContentDescription: 进度条; ItemCount: 10000; CurrentItemIndex: 3653; Enabled: true; Password: false; Checked: false; FullScreen: false; Scrollable: false; BeforeText: null; FromIndex: -1; ToIndex: -1; ScrollX: 0; ScrollY: 0; MaxScrollX: 0; MaxScrollY: 0; ScrollDeltaX: -1; ScrollDeltaY: -1; AddedCount: -1; RemovedCount: -1; ParcelableData: null ]; recordCount: 0
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        int eventType = event.getEventType();

        rootInActiveWindow = getRootInActiveWindow();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:

                break;
            case TYPE_WINDOWS_CHANGED:
            case TYPE_WINDOW_STATE_CHANGED :
                itemNodeinfo = null;
                if (event != null){
                    if (Objects.equals(event.getPackageName(),MainActivity.douyin)) {
                        AccessibilityNodeInfo eventSource = getRootInActiveWindow();
                        if (eventSource != null) {
                            Log.d(TAG, "start print");
                            printChild(eventSource, "");
                            Log.d(TAG, "end print");
                        }
                    }
                }

                break;
            default:
                Log.d(TAG, "window type " + event);
                break;
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());


    private void printChild(AccessibilityNodeInfo info, String space) {
        for (int i = 0; i < info.getChildCount(); i++) {
            AccessibilityNodeInfo child = info.getChild(i);
            if (child == null) {
                continue;
            }
            CharSequence text = child.getText();
            if (Objects.equals(text, "搜索")) {
                Log.d(TAG, space + String.format("%s", child));
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        performViewClick(child);

                        performClickAt(930,140);
//                        performClickAt(60,150);
                    }
                },3000);

            } else {
                CharSequence className = child.getClassName();
                String resourceName = child.getViewIdResourceName();
                CharSequence contentDescription = child.getContentDescription();
                Log.d(TAG,
                        space + String.format("text:%s name:%s des:%s res:%s",
                                text,
                                className,
                                String.valueOf(contentDescription),
                                resourceName));
            }
            printChild(child, space + " ");
        }
    }

    /**
     * 寻找窗体中的“发送”按钮，并且点击。
     */
    @SuppressLint("NewApi")
    private void send() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo
                    .findAccessibilityNodeInfosByText("发送");
            if (list != null && list.size() > 0) {
                for (AccessibilityNodeInfo n : list) {
                    if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }

            } else {
                List<AccessibilityNodeInfo> liste = nodeInfo
                        .findAccessibilityNodeInfosByText("Send");
                if (liste != null && liste.size() > 0) {
                    for (AccessibilityNodeInfo n : liste) {
                        if (n.getClassName().equals("android.widget.Button") && n.isEnabled()) {
                            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
            pressBackButton();
        }
    }

    /**
     * 模拟back按键
     */
    private void pressBackButton() {
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("NewApi")
    private boolean fill() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            return findEditText(rootNode, "Auto Reply");
        }
        return false;
    }

    private boolean findEditText(AccessibilityNodeInfo rootNode, String content) {
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = rootNode.getChild(i);
            if (nodeInfo == null) {
                continue;
            }
            if (nodeInfo.getContentDescription() != null) {
                int nindex = nodeInfo.getContentDescription().toString().indexOf(name);
                int cindex = nodeInfo.getContentDescription().toString().indexOf(scontent);
                if (nindex != -1) {
                    itemNodeinfo = nodeInfo;
                }
            }
            if ("android.widget.EditText".equals(nodeInfo.getClassName())) {
                Bundle arguments = new Bundle();
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                        AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD);
                arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                        true);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                        arguments);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                ClipData clip = ClipData.newPlainText("label", content);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clip);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                return true;
            }
            if (findEditText(nodeInfo, content)) {
                return true;
            }
        }
        return false;
    }
}
