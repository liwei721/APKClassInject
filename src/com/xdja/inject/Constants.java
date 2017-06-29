package com.xdja.inject;

/**
 * Created by zlw on 2017/6/23.
 */
public class Constants {
    public final static int MT_FULL = 0;
    public final static int MT_WILDCARD = 1;
    public final static int MT_REGEX = 2;
    public final static int TY_AAR = 11;
    public final static int TY_JAR = 12;
    public final static String KEY_CLASSMATCHTYPE="classMatchType";
    public final static String KEY_MODIFYMETHODS="modifyMethods";
    public final static String KEY_METHODNAME="methodName";
    public final static String KEY_METHODMATCHTYPE="methodMatchType";
    public final static String KEY_METHODDESC="methodDesc";
    public final static String KEY_ADAPTER="adapter";
    public final static String VALUE_WILDCARD="wildcard";
    public final static String VALUE_REGEX="regEx";
    public final static String VALUE_ALL="all";
    public final static String WILDCARD_STAR = "\\*{1,3}";
    public final static String WILDCARD_VLINE = "\\|{1,2}";
    public final static String REGEX_STARTER = "r:";

    // default tag
    public final static String TAG = "xdjaClassInject";

    // META-INFO
    public final static String META_INFO = "META-INF/";

    // UI相关
    public final static String TITLE = "代码插桩工具";
    public final static String VERSION = "1.0";

    // 签名默认相关
    public final static String signFileName = "androidsign.jks";
    public final static String SIGNPWD = "60621210";
    public final static String SIGNALIAS = "androidsign.keystore";

    // 默认的工具类
    public final static String MONITOR_CLASSNAME = "com.xdja.monitor.AppMonitor";
    public final static String MONITOR_CLASS = "AppMonitor.class";

}
