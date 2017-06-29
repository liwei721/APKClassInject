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

    public static void monitorPageStart(String pageName){
        mStartTime = SystemClock.elapsedRealtime();
    }
    /**
     *  页面加载时间的方法
     * @param endTime
     * @param pageName
     */
    public static void monitorPageLoaded(long endTime, String pageName){
        if (mStartTime != 0){
            long loadTime = endTime - mStartTime;
            Log.d("zlw", pageName + "cost :" + loadTime);
        }
        // 将StartTime清空
        mStartTime = 0;
    }
}
