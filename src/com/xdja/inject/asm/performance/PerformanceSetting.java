package com.xdja.inject.asm.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlw on 2017/7/7.
 * Email: zlw@xdja.com
 * function:  采集性能数据的配置
 */
public class PerformanceSetting {


    public static List<String> getKpiExcludsClss(){
        List<String> kpiExcludsClss = new ArrayList<>();
        kpiExcludsClss.add("com.android.");
        kpiExcludsClss.add("android.support.");
        return kpiExcludsClss;
    }
}
