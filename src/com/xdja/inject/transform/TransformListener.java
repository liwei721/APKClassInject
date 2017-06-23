package com.xdja.inject.transform;

/**
 * Created by zlw on 2017/6/23.
 * 任务进行的监听
 */
public interface TransformListener {
     void start();
     void upzipApk(String path);
     void dex2class(String path);
     void classInject(String var);
     void dir2Apk(String path);
     void finish();
}
