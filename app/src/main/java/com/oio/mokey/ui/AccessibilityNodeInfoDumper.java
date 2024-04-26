package com.oio.mokey.ui;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TableLayout;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/* loaded from: classes.dex */
public class AccessibilityNodeInfoDumper {
    private static final String LOGTAG = AccessibilityNodeInfoDumper.class.getSimpleName();
    private static final String[] NAF_EXCLUDED_CLASSES = {GridView.class.getName(), GridLayout.class.getName(), ListView.class.getName(), TableLayout.class.getName()};

    public static void dumpWindowToFile(AccessibilityNodeInfo root,
                                        int rotation,
                                        int width,
                                        int height) {


        File baseDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "window_dump");
        System.out.println(baseDir);

        dumpWindowToFile(root,
                new File(baseDir.getPath(), "window_dump.xml"),
                rotation,
                width,
                height);
    }

    public static void dumpWindowToFile(AccessibilityNodeInfo root,
                                        File dumpFile,
                                        int rotation,
                                        int width,
                                        int height) {
        if (root == null) {
            return;
        }
        try {
            FileWriter writer = new FileWriter(dumpFile);
            writer.write(dumpWindowToString(root, rotation, width, height));
            writer.close();
        } catch (IOException e) {
            Log.e(LOGTAG, "failed to dump window to file", e);
        }
    }


    public static String dumpWindowToString(AccessibilityNodeInfo root,
                                            int rotation,
                                            int width,
                                            int height) {
        if (root == null) {
            return "";
        }
        long startTime = SystemClock.uptimeMillis();
        try {
            XmlSerializer serializer = Xml.newSerializer();
            StringWriter stringWriter = new StringWriter();
            serializer.setOutput(stringWriter);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "hierarchy");
            serializer.attribute("", "rotation", Integer.toString(rotation));
            dumpNodeRec(root, serializer, 0, width, height);
            serializer.endTag("", "hierarchy");
            serializer.endDocument();
            return (stringWriter.toString());
        } catch (IOException e) {
            Log.e(LOGTAG, "failed to dump window to file", e);
        }
        long endTime = SystemClock.uptimeMillis();
        Log.w(LOGTAG, "Fetch time: " + (endTime - startTime) + "ms");
        return "";
    }

    public static Node dumpWindowToNode(AccessibilityNodeInfo root, int width,
                                        int height) {
        Node node = new Node();
        try {
            dumpNodeRec(root, node, 0, width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return node;
    }

    public static void dumpWindowsToFile(SparseArray<List<AccessibilityWindowInfo>> allWindows,
                                         File dumpFile,
                                         Display display) {
        long startTime;
        int d;
        int nd;
        SparseArray<List<AccessibilityWindowInfo>> sparseArray = allWindows;
        if (allWindows.size() == 0) {
            return;
        }
        long startTime2 = SystemClock.uptimeMillis();
        try {
            FileWriter writer = new FileWriter(dumpFile);
            XmlSerializer serializer = Xml.newSerializer();
            StringWriter stringWriter = new StringWriter();
            serializer.setOutput(stringWriter);
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", "displays");
            int nd2 = allWindows.size();
            int d2 = 0;
            while (d2 < nd2) {
                int displayId = sparseArray.keyAt(d2);
                if (display == null) {
                    startTime = startTime2;
                    d = d2;
                    nd = nd2;
                } else {
                    List<AccessibilityWindowInfo> windows = sparseArray.valueAt(d2);
                    if (windows.isEmpty()) {
                        startTime = startTime2;
                        d = d2;
                        nd = nd2;
                    } else {
                        serializer.startTag("", "display");
                        serializer.attribute("", "id", Integer.toString(displayId));
                        int rotation = display.getRotation();
                        List<AccessibilityWindowInfo> windows2 = windows;
                        Point size = new Point();
                        display.getSize(size);
                        int n = windows2.size();
                        startTime = startTime2;
                        int i = 0;
                        while (true) {
                            int n2 = n;
                            if (i >= n2) {
                                break;
                            }
                            try {
                                int n3 = size.x;
                                dumpWindowRec(windows2.get(i), serializer, i, n3, size.y, rotation);
                                i++;
                                nd2 = nd2;
                                displayId = displayId;
                                windows2 = windows2;
                                display = display;
                                d2 = d2;
                                n = n2;
                            } catch (IOException e) {
                                e = e;
                                Log.e(LOGTAG, "failed to dump window to file", e);
                                long endTime = SystemClock.uptimeMillis();
                                Log.w(LOGTAG, "Fetch time: " + (endTime - startTime) + "ms");
                            }
                        }
                        d = d2;
                        nd = nd2;
                        serializer.endTag("", "display");
                    }
                }
                d2 = d + 1;
                sparseArray = allWindows;
                nd2 = nd;
                startTime2 = startTime;
            }
            startTime = startTime2;
            serializer.endTag("", "displays");
            serializer.endDocument();
            writer.write(stringWriter.toString());
            writer.close();
        } catch (IOException e2) {
            e2.printStackTrace();
            startTime = startTime2;
        }
        long endTime2 = SystemClock.uptimeMillis();
        Log.w(LOGTAG, "Fetch time: " + (endTime2 - startTime) + "ms");
    }

    public static String typeToString(int type) {
        switch (type) {
            case 1: {
                return "TYPE_APPLICATION";
            }
            case 2: {
                return "TYPE_INPUT_METHOD";
            }
            case 3: {
                return "TYPE_SYSTEM";
            }
            case 4: {
                return "TYPE_ACCESSIBILITY_OVERLAY";
            }
            case 5: {
                return "TYPE_SPLIT_SCREEN_DIVIDER";
            }
            case 6: {
                return "TYPE_MAGNIFICATION_OVERLAY";
            }
            default:
                return "<UNKNOWN:" + type + ">";
        }
    }

    private static void dumpWindowRec(AccessibilityWindowInfo winfo,
                                      XmlSerializer serializer,
                                      int index,
                                      int width,
                                      int height,
                                      int rotation) throws IOException {
        serializer.startTag("", "window");
        serializer.attribute("", "index", Integer.toString(index));
        CharSequence title = winfo.getTitle();
        serializer.attribute("", "title", title != null ? title.toString() : "");
        Rect tmpBounds = new Rect();
        winfo.getBoundsInScreen(tmpBounds);
        serializer.attribute("", "bounds", tmpBounds.toShortString());
        serializer.attribute("", "active", Boolean.toString(winfo.isActive()));
        serializer.attribute("", "focused", Boolean.toString(winfo.isFocused()));
        serializer.attribute("",
                "accessibility-focused",
                Boolean.toString(winfo.isAccessibilityFocused()));
        serializer.attribute("", "id", Integer.toString(winfo.getId()));
        serializer.attribute("", "layer", Integer.toString(winfo.getLayer()));
        serializer.attribute("", "type", typeToString(winfo.getType()));
        int count = winfo.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityWindowInfo child = winfo.getChild(i);
            if (child == null) {
                Log.i(LOGTAG,
                        String.format("Null window child %d/%d, parent: %s",
                                Integer.valueOf(i),
                                Integer.valueOf(count),
                                winfo.getTitle()));
            } else {
                dumpWindowRec(child, serializer, i, width, height, rotation);
                child.recycle();
            }
        }
        AccessibilityNodeInfo root = winfo.getRoot();
        if (root != null) {
            serializer.startTag("", "hierarchy");
            serializer.attribute("", "rotation", Integer.toString(rotation));
            dumpNodeRec(root, serializer, 0, width, height);
            root.recycle();
            serializer.endTag("", "hierarchy");
        }
        serializer.endTag("", "window");
    }


    public static class Node {
        public Rect bounds;
        public boolean checkable;
        public boolean checked;
        public String clazz;
        public boolean clickable;
        public String contentDesc;
        public boolean enabled;
        public boolean focusable;
        public boolean focused;
        public int index;
        public boolean longClickable;
        public String packageName;
        public boolean password;
        public String resourceId;
        public boolean scrollable;
        public boolean selected;
        public String text;
        public List<Node> children = new ArrayList<>();

        public Node() {
        }



        @Override
        public String toString() {
            return new StringJoiner("")
                    .add("bounds=" + bounds)
                    .add("checkable=" + checkable)
                    .add("checked=" + checked)
                    .add("clazz='" + clazz + "'")
                    .add("clickable=" + clickable)
                    .add("contentDesc='" + contentDesc + "'")
                    .add("enabled=" + enabled)
                    .add("focusable=" + focusable)
                    .add("focused=" + focused)
                    .add("index=" + index)
                    .add("longClickable=" + longClickable)
                    .add("packageName='" + packageName + "'")
                    .add("password=" + password)
                    .add("resourceId='" + resourceId + "'")
                    .add("scrollable=" + scrollable)
                    .add("selected=" + selected)
                    .add("text='" + text + "'")
                    .add("childrenSize=" + children.size())
                    .toString();
        }
        // 构造函数等方法...
    }

    public static void dumpNodeRec(AccessibilityNodeInfo node,
                                   XmlSerializer serializer,
                                   int index,
                                   int width,
                                   int height) throws IOException {
        serializer.startTag("", "node");
        if (!nafExcludedClass(node) && !nafCheck(node)) {
            serializer.attribute("", "NAF", Boolean.toString(true));
        }
        serializer.attribute("", "index", Integer.toString(index));
        serializer.attribute("", "text", safeCharSeqToString(node.getText()));
        serializer.attribute("", "resource-id", safeCharSeqToString(node.getViewIdResourceName()));
        serializer.attribute("", "class", safeCharSeqToString(node.getClassName()));
        serializer.attribute("", "package", safeCharSeqToString(node.getPackageName()));
        serializer.attribute("", "content-desc", safeCharSeqToString(node.getContentDescription()));
        serializer.attribute("", "checkable", Boolean.toString(node.isCheckable()));
        serializer.attribute("", "checked", Boolean.toString(node.isChecked()));
        serializer.attribute("", "clickable", Boolean.toString(node.isClickable()));
        serializer.attribute("", "enabled", Boolean.toString(node.isEnabled()));
        serializer.attribute("", "focusable", Boolean.toString(node.isFocusable()));
        serializer.attribute("", "focused", Boolean.toString(node.isFocused()));
        serializer.attribute("", "scrollable", Boolean.toString(node.isScrollable()));
        serializer.attribute("", "long-clickable", Boolean.toString(node.isLongClickable()));
        serializer.attribute("", "password", Boolean.toString(node.isPassword()));
        serializer.attribute("", "selected", Boolean.toString(node.isSelected()));
        serializer.attribute("",
                "bounds",
                AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node,
                        width,
                        height).toShortString());
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (child.isVisibleToUser()) {
                    dumpNodeRec(child, serializer, i, width, height);
                    child.recycle();
                } else {
                    Log.i(LOGTAG, String.format("Skipping invisible child: %s", child.toString()));
                }
            } else {
                Log.i(LOGTAG,
                        String.format("Null child %d/%d, parent: %s",
                                Integer.valueOf(i),
                                Integer.valueOf(count),
                                node.toString()));
            }
        }
        serializer.endTag("", "node");
    }

    public static void dumpNodeRec(AccessibilityNodeInfo node,
                                   Node outNode,
                                   int index,
                                   int width,
                                   int height) throws IOException {
        outNode.index = (index);
        outNode.text = safeCharSeqToString(node.getText());
        outNode.resourceId = safeCharSeqToString(node.getViewIdResourceName());
        outNode.clazz = safeCharSeqToString(node.getClassName());
        outNode.packageName = safeCharSeqToString(node.getPackageName());
        outNode.contentDesc = safeCharSeqToString(node.getContentDescription());
        outNode.checkable = (node.isCheckable());
        outNode.checked = (node.isChecked());
        outNode.clickable = (node.isClickable());
        outNode.enabled = (node.isEnabled());
        outNode.focusable = (node.isFocusable());
        outNode.focused = (node.isFocused());
        outNode.scrollable = (node.isScrollable());
        outNode.longClickable = (node.isLongClickable());
        outNode.password = (node.isPassword());
        outNode.selected = (node.isSelected());
        outNode.bounds = AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(node, width, height);
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                if (child.isVisibleToUser()) {
                    Node newNode = new Node();
                    outNode.children.add(newNode);
                    dumpNodeRec(child, newNode, i, width, height);
                    child.recycle();
                } else {
                    Log.i(LOGTAG, String.format("Skipping invisible child: %s", child.toString()));
                }
            } else {
                Log.i(LOGTAG,
                        String.format("Null child %d/%d, parent: %s",
                                Integer.valueOf(i),
                                Integer.valueOf(count),
                                node.toString()));
            }
        }
    }


    private static boolean nafExcludedClass(AccessibilityNodeInfo node) {
        String[] strArr;
        String className = safeCharSeqToString(node.getClassName());
        for (String excludedClassName : NAF_EXCLUDED_CLASSES) {
            if (className.endsWith(excludedClassName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nafCheck(AccessibilityNodeInfo node) {
        boolean isNaf = node.isClickable() && node.isEnabled() && safeCharSeqToString(node.getContentDescription()).isEmpty() && safeCharSeqToString(
                node.getText()).isEmpty();
        if (isNaf) {
            return childNafCheck(node);
        }
        return true;
    }

    private static boolean childNafCheck(AccessibilityNodeInfo node) {
        int childCount = node.getChildCount();
        for (int x = 0; x < childCount; x++) {
            AccessibilityNodeInfo childNode = node.getChild(x);
            if (!safeCharSeqToString(childNode.getContentDescription()).isEmpty() || !safeCharSeqToString(
                    childNode.getText()).isEmpty() || childNafCheck(childNode)) {
                return true;
            }
        }
        return false;
    }

    private static String safeCharSeqToString(CharSequence cs) {
        if (cs == null) {
            return "";
        }
        return stripInvalidXMLChars(cs);
    }

    private static String stripInvalidXMLChars(CharSequence cs) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < cs.length(); i++) {
            char ch = cs.charAt(i);
            if ((ch >= 1 && ch <= '\b') || ((ch >= 11 && ch <= '\f') || ((ch >= 14 && ch <= 31) || ((ch >= 127 && ch <= 132) || ((ch >= 134 && ch <= 159) || ((ch >= 64976 && ch <= 64991) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || ((ch >= 65534 && ch <= 65535) || (ch >= 65534 && ch <= 65535)))))))))))))))))))))) {
                ret.append(".");
            } else {
                ret.append(ch);
            }
        }
        return ret.toString();
    }
}
