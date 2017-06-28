package com.xdja.inject.transform;

/**
 * Created by zlw on 2017/6/23.
 * 任务进行的监听
 */
public interface TransformListener {
     void start();
     void upzipApk(int i, String path);
     void dex2jar(int j, String path);
     void deleteMeta(int i, String var);
     void dexToapk(int i, String var);
     void apkSign(int i , String var);
     void finish(String var);
     void showError(String errorMsg);
}
