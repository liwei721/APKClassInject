package com.xdja.inject.transform;

import com.googlecode.d2j.dex.Dex2jar;
import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.setting.SettingHelper;
import com.xdja.inject.util.Dex2jarUtil;
import com.xdja.inject.util.Util;

import java.io.IOException;
import java.util.List;

/**
 * Created by zlw on 2017/6/23.
 *  这个类负责主流程，将整个流程串联起来
 */
public class TransformManager {
    private SettingEntity setting;
    private TransformListener transformListener;

    /**
     *  对Apk进行处理
     *  @param apkPath  需要处理的apk path
     *  @param signAlias
     *  @param signFilePath
     *  @param signPwd
     */
    public void handleApk(String apkPath, String signFilePath, String signPwd, String signAlias){
        // 如果没有输入apkpath，就返回
        if (Util.isStrEmpty(apkPath)) return;
        // 1. 获取配置的注入属性
        setting = SettingHelper.getSetting();

        // 如果没有配置文件，表示不进行注入操作，就什么都不做。
        if (setting == null) return;
        // 如果没有配置需要注入的类
        if (Util.isListEmpty(setting.getInjectSettings())) return;

        // 如果配置不执行注入，则啥都不做。
        if (!setting.isIsInject()) return;

        // 2. 解压apk到temp目录
        String tempDir = TransformImpl.upzipApk(apkPath);
        // 如果临时目录为null，可能解压没有成功，直接返回。
        if (Util.isStrEmpty(tempDir)) return;

        // 3. 将dex转成jar,同时在dex2jar中完成代码注入
        List<String> jars = TransformImpl.dex2jar(tempDir);

        // 4. 将jar转成dex，使用dx工具。
        String tempDexPath = Dex2jarUtil.jar2Dex(jars);

        if (Util.isStrEmpty(tempDexPath)){
            return;
        }
        // 如果成功
        // 5. 删除apk中原来的签名文件
        boolean isSuc = Dex2jarUtil.deleteMetaInfo(tempDir, apkPath);
        if (!isSuc){
            return;
        }

        // 6. 将上面的dex放到apk中
        boolean isSuc1 = Dex2jarUtil.addDexToApk(apkPath, tempDir);
        if (!isSuc1){
            return;
        }

        // 7. 对apk进行签名
        String signApkPath = Dex2jarUtil.signApk(apkPath, signFilePath  , signPwd, signAlias);
        if (!Util.isStrEmpty(signApkPath)){
            return;
        }

        // 8. 删除temp目录
        TransformImpl.deleteTempDir(tempDir);
    }

    /**
     *  设置监听
     * @param listener
     */
    public void setTransformListener(TransformListener listener){
        transformListener = listener;
    }
}
