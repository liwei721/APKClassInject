package com.xdja.inject.setting;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.xdja.inject.Constants;
import com.xdja.inject.util.FilesUtil;
import com.xdja.inject.util.InjectUtil;
import com.xdja.inject.util.LogUtil;
import com.xdja.inject.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.xdja.inject.setting.SettingEntity.InjectSettingsBean.InjectMethodBean;
import com.xdja.inject.setting.SettingEntity.InjectSettingsBean.InjectFieldBean;
import com.xdja.inject.setting.SettingEntity.InjectSettingsBean.InjectMethodBean.InjectContentBean;

/**
 * Created by zlw on 2017/6/26.
 * 准备设计成单例
 */
public class SettingHelper {
    private static volatile SettingHelper sInstance = null;
    private final static String SETTING_PATH = FilesUtil.getConfigPath() + File.separator + "InjectSetting.json";
    private Map<String, SettingEntity.InjectSettingsBean> InjectMaps = new HashMap<>();
    private SettingEntity mEntity;
    private static Map<String, Integer> targetClasses = new HashMap<>();

    private SettingHelper() {
        if (mEntity == null) {
            mEntity = getSetting();
            initClassToBean();
        }
    }

    public static SettingHelper getInstance() {
        if (sInstance == null) {
            synchronized (SettingHelper.class) {
                if (sInstance == null) {
                    sInstance = new SettingHelper();
                }
            }
        }

        return sInstance;
    }

    private SettingEntity getSetting() {
        Gson gson = new Gson();
        SettingEntity settingEntity = null;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(new FileInputStream(SETTING_PATH)));
            settingEntity = gson.fromJson(reader, SettingEntity.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return settingEntity;
    }

    /**
     * 是否配置了要inject的数据
     *
     * @return  返回错误信息
     *          如果成功返回""
     *
     */
    public String isSettingEnable() {
        // 如果没有配置文件，表示不进行注入操作，就什么都不做。
        if (mEntity == null) {
            return "没有获取到对应的配置文件";
        }
        // 如果没有配置需要注入的类
        if (Util.isListEmpty(mEntity.getInjectSettings())) {
            return "没有配置往哪里插入代码";
        }

        // 如果配置不执行注入，则啥都不做。
        if (!mEntity.isIsInject()) {
            return "配置中插桩代码开关是关闭的。";
        }
        // 配置日志是否可以打印
        LogUtil.setQuiet(!mEntity.isShowLog());
        return "";
    }

    /**
     * 是否需要InjectClass
     *
     * @param className
     * @return
     */
    public String shouldModifyClass(String className) {
        String keyClass = classNameToPattern(className);
        if (Util.isStrEmpty(keyClass)){
            return "";
        }

        return hasClassExcluds(keyClass, className);
    }

    /**
     *  将className转为配置的classPatern
     * @param className
     * @return
     */
    private String classNameToPattern(String className){
        if (Util.isStrEmpty(className)) {
            return "";
        }
        for (Map.Entry<String, Integer> entry : targetClasses.entrySet()) {
            // matchType 是匹配的类型：正则、通配符、相等
            int matchType = entry.getValue();
            String key = entry.getKey();

            // 根据匹配类型判断是否进行插入
            switch (matchType){
                case Constants.MT_FULL:
                    if (className.equals(key)){
                        return key;
                    }
                    break;
                case Constants.MT_REGEX:
                    if (InjectUtil.regMatch(key, className)){
                        return key;
                    }
                    break;
                case Constants.MT_WILDCARD:
                    if (InjectUtil.wildcardMatchPro(key, className)){
                        return key;
                    }
                    break;
                default:
                    return "";
            }
        }
        return "";


    }
    /**
     *  判断是否有需要过滤掉的class
     * @param classPatern
     * @return
     */
    private String hasClassExcluds(String classPatern, String className){
        SettingEntity.InjectSettingsBean settingsBean = InjectMaps.get(classPatern);
        // 是否有排除的class
        List<String> classExcluds = settingsBean.getClassExclude();
        if (!Util.isListEmpty(classExcluds)){
            for (String clsssexclud : classExcluds){
                if (InjectUtil.isPatternMatch(clsssexclud, className)){
                    return "";
                }
            }
        }

        return classPatern;
    }




    /**
     * 1)将className和InjectSettingBean对应起来
     * 2）将所有的要注入的class放到一个列表中。
     * 可以在最开始的地方调用
     */
    public void initClassToBean() {
        if (mEntity != null) {
            List<SettingEntity.InjectSettingsBean> injectSettingsBeans = mEntity.getInjectSettings();
            if (!Util.isListEmpty(injectSettingsBeans)) {
                for (SettingEntity.InjectSettingsBean settingsBean : injectSettingsBeans) {
                    InjectMaps.put(settingsBean.getClassName(), settingsBean);
                    int type = InjectUtil.getMatchTypeByValue(settingsBean.getClassName());
                    targetClasses.put(settingsBean.getClassName(), type);
                }
            }
        }
    }


    /**
     * 是否需要注入方法
     * @param methodName 方法的名称
     * @return
     */
    public String shouldModifyMethod(String keyclass, String methodName) {
        if (Util.isStrEmpty(methodName)){
            return "";
        }

        SettingEntity.InjectSettingsBean settingsBean = InjectMaps.get(keyclass);
        if (settingsBean == null){
            return "";
        }


        List<InjectMethodBean> methodBeanList = settingsBean.getInjectMethod();
        if (!Util.isListEmpty(methodBeanList)){
            for (InjectMethodBean methodBean : methodBeanList){
                // 先判断method是否有排除的。
                List<String> methodExcluds = (List<String>) methodBean.getMethodExclude();
                if (!Util.isListEmpty(methodExcluds)){
                    for (String methodPatern : methodExcluds){
                        if (InjectUtil.isPatternMatch(methodExcludHandle(methodPatern)[0], methodName)){
                            return "";
                        }
                    }
                }

                int methodType = InjectUtil.getMatchTypeByValue(methodBean.getMethodName());
                switch (methodType){
                    case Constants.MT_FULL:
                        if (methodName.equals(methodBean.getMethodName())){
                            return methodBean.getMethodName();
                        }
                    case Constants.MT_REGEX:
                        if (InjectUtil.regMatch(methodBean.getMethodName(), methodName)){
                            return methodBean.getMethodName();
                        }
                        break;
                    case Constants.MT_WILDCARD:
                        if (InjectUtil.wildcardMatchPro(methodBean.getMethodName(), methodName)){
                            return methodBean.getMethodName();
                        }
                        break;
                    default:
                        return "";

                }
            }
        }

        return "";

    }

    /**
     *  判断是否插入对应的field
     * @param className
     * @return
     */
    public List<InjectFieldBean> shouldInjectField(String className){
        if (Util.isStrEmpty(className)){
            return null;
        }
//        String key = shouldModifyClass(className);
//        if (Util.isStrEmpty(key)){
//            return null;
//        }

        SettingEntity.InjectSettingsBean settingsBean = InjectMaps.get(className);
        if (settingsBean != null){
            return settingsBean.getInjectField();
        }
        return null;
    }


    /**
     *  将使用过程中的容器进行清空
     */
    public void clearData(){
        if (InjectMaps.size() > 1){
            InjectMaps.clear();
        }

        if (targetClasses.size() > 1){
            targetClasses.clear();
        }
    }

    /**
     *  对methodExclude 的字符串进行处理。 正常的格式是： methodName#methodDesc
     * @param methodExcludStr
     * @return
     */
    private String[] methodExcludHandle(String methodExcludStr){
        if (methodExcludStr.contains("#")){
            return methodExcludStr.split("#");
        }

        // 只配置了方法名称，但是没有配置方法desc。
        return new String[]{methodExcludStr};
    }

    /**
     *  获取className对应的插桩参数
     * @param className
     * @return
     */
    public List<InjectContentBean> getInjectParams(String className, String methodName){
        if (Util.isStrEmpty(className) || Util.isStrEmpty(methodName)){
            return null;
        }

        SettingEntity.InjectSettingsBean settingsBean = InjectMaps.get(className);
        for (InjectMethodBean methodBean : settingsBean.getInjectMethod()){
            if (methodName.equals(methodBean.getMethodName())){ //对应的methodName相等。则继续
                return methodBean.getInjectContent();
            }
        }
        return null;
    }


    public static void main(String[] args) {

    }


}

