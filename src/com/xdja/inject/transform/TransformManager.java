package com.xdja.inject.transform;

import com.googlecode.d2j.dex.Dex2jar;
import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.setting.SettingHelper;
import com.xdja.inject.util.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by zlw on 2017/6/23.
 * 这个类负责主流程，将整个流程串联起来
 */
public class TransformManager {
    private SettingEntity setting;
    private TransformListener transformListener;

    /**
     * 对Apk进行处理
     *
     * @param apkPath      需要处理的apk path
     * @param signAlias
     * @param signFilePath
     * @param signPwd
     */
    private void handleApk(String apkPath, String signFilePath, String signPwd, String signAlias) {
        try {
            // 如果没有输入apkpath，就返回
            if (Util.isStrEmpty(apkPath)) return;
            transformListener.start();
            // 1. 判断配置是否正确
            String result = SettingHelper.getInstance().isSettingEnable();
            if (!"".equals(result)){
                transformListener.showError(result);
                return;
            }

            // 2. 解压apk到temp目录
            transformListener.upzipApk(1, apkPath);
            String tempDir = FilesUtil.decompressApk(apkPath);
            // 如果临时目录为null，可能解压没有成功，直接返回。
            if (Util.isStrEmpty(tempDir)) {
                transformListener.showError("解压apk失败了。");
                LogUtil.info("解压apk失败了。");
                return;
            }

            // 3. 将dex转成jar,同时在dex2jar中完成代码注入
            transformListener.dex2jar(2, tempDir);
            List<String> jars = Dex2jarUtil.dex2jarImpl(tempDir);
            if (Util.isListEmpty(jars)){
                transformListener.showError("dex 转成 jar 失败了");
                LogUtil.info("dex 转成 jar 失败了");
                return;
            }

            // 3.1 将工具类写到jar中
            boolean addFileSuc = FilesUtil.addFileToJar(FilesUtil.getResourcePath() + File.separator + "AppMonitor.java", tempDir + File.separator + "classes.jar");
            if (!addFileSuc){
                transformListener.showError("将工具类AppMonitor添加到jar中失败了");
                LogUtil.info("将工具类AppMonitor添加到jar中失败了");
                return;
            }

            // 4. 将jar转成dex，使用dx工具。
            boolean isSuc2 = Dex2jarUtil.jar2Dex(jars);
            transformListener.jar2dex(3, apkPath);

            if (!isSuc2) {
                transformListener.showError("jar 转成 dex 失败了");
                LogUtil.info("jar 转成 dex 失败了");
                return;
            }

            // 5. 将上面的dex放到apk中
            transformListener.dexToapk(4, apkPath);
            boolean isSuc1 = Dex2jarUtil.addDexToApk(apkPath, tempDir);
            if (!isSuc1) {
                transformListener.showError("将dex放到apk中失败了");
                LogUtil.info("将dex放到apk中失败了");
                return;
            }

            // 如果成功
            // 6. 删除apk中原来的签名文件
            transformListener.deleteMeta(5, apkPath);
            boolean isSuc = Dex2jarUtil.deleteMetaInfo(tempDir, apkPath);
            if (!isSuc) {
                transformListener.showError("删除原apk中的meta-info 失败了");
                LogUtil.info("删除原apk中的meta-info 失败了");
                return;
            }

            // 7. 对apk进行签名
            String signApkPath = Dex2jarUtil.signApk(apkPath, signFilePath, signPwd, signAlias);
            transformListener.apkSign(6, signApkPath);
            if (Util.isStrEmpty(signApkPath)) {
                transformListener.showError("对apk签名失败了");
                LogUtil.info("对apk签名失败了");
                return;
            }

            // 所有任务都成功就打开目录
            boolean succ = FilesUtil.openDir(signApkPath);
            if (!succ){
                transformListener.showError("打开签名之后的apk目录失败");
                LogUtil.info("打开签名之后的apk目录失败");
            }
            transformListener.finish(signApkPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *  开始对APK进行插桩
     * @param apkPath
     * @param signFilePath
     * @param signPwd
     * @param signAlias
     */
    public void injectApk(String apkPath, String signFilePath, String signPwd, String signAlias){
        handleApk(apkPath,signFilePath, signPwd, signAlias);

        // 清除缓存数据
        SettingHelper.getInstance().clearData();

        // 删除无用的临时文件
        //  删除temp目录
        boolean suc = FilesUtil.deleteTempDir();
        if (!suc){
            transformListener.showError("删除临时目录失败了");
        }

    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public void setTransformListener(TransformListener listener) {
        transformListener = listener;
    }


    public static void main(String[] args){
//        FilesUtil.deleteTempDir(FilesUtil.getTempDirPath());
        LogUtil.info("Test");
    }
}
