package com.xdja.inject.transform;

import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.setting.SettingHelper;
import com.xdja.inject.util.Util;

import java.io.IOException;
import java.util.List;

/**
 * Created by zlw on 2017/6/23.
 *  这个类负责主流程，将整个流程串联起来
 */
public class TransformManager {
    private static SettingEntity setting;

    /**
     *  对Apk进行处理
     *  @param apkPath  需要处理的apk path
     */
    public static void handleApk(String apkPath){
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

        // 3. 将dex转成jar
        // TODO: 2017/6/26  apk中有无其他需要插桩的文件，aar？现在只搞jar
        List<String> jars = TransformImpl.dex2jar(tempDir);

        // 4. 向jar中class注入代码
        try {
            TransformImpl.modifyJarFile(jars, setting, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
