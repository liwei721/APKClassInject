package com.xdja.inject.setting.performance;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlw on 2017/7/7.
 * Email: zlw@xdja.com
 * function:  �ɼ��������ݵ�����
 */
public class PerformanceSetting {


    public static List<String> getKpiExcludsClss(){
        List<String> kpiExcludsClss = new ArrayList<>();
        kpiExcludsClss.add("com.android.");
        kpiExcludsClss.add("android.support.");
        return kpiExcludsClss;
    }
}
