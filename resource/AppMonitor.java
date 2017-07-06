package com.xdja.monitor;

import android.os.SystemClock;
import android.util.Log;

/**
 * Created by zlw on 2017/6/29.
 * Email: zlw@xdja.com
 * function: 用于统计操作
 */
public class AppMonitor {

    private static long mStartTime = 0;
    private static String mPageName = "";

    public static void monitorPageStart(String pageName){
        mStartTime = SystemClock.elapsedRealtime();
        mPageName = pageName;
    }
    /**
     *  页面加载时间的方法
     * @param endTime
     * @param pageName
     */
    public static void monitorPageLoaded(String pageName){
        if (mStartTime != 0){
            long endTime = SystemClock.elapsedRealtime();
            long loadTime = endTime - mStartTime;
            if (pageName == null || "".equals(pageName)){
                pageName = mPageName;
            }
            Log.d("zlw", pageName + "cost :" + loadTime);
        }
        // 将StartTime清空
        mStartTime = 0;
        // 清空当前的页面名称
        mPageName = "";
    }
}
