package com.xdja.inject.util;

import com.xdja.inject.Constants;
import com.xdja.inject.setting.SettingEntity;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import com.xdja.inject.setting.SettingEntity.InjectSettingsBean.InjectMethodBean;

/**
 * Created by zlw on 2017/6/26.
 *
 * 用于代码注入相关的工具类
 */
public class InjectUtil {
    private static Map<String ,Integer> targetClasses = new HashMap<>();
    private static Map<String, List<String>> classExcluds = new HashMap<>();
    private static Map<String, Integer> targetMethods = new HashMap<>();
    private static Map<String, List<String>> methodExcluds = new HashMap<>();
    private static Map<String, List<InjectMethodBean.InjectContentBean> > targetInjectParams = new HashMap<>();

    public static SettingEntity mSettingentity = null;
    /**
     *  判断method是否符合正则表达式
     * @param pattern
     * @param target
     * @return
     */
    public static boolean isPatternMatch(String pattern, String target) {
        if (Util.isStrEmpty(pattern) || Util.isStrEmpty(target)) {
            return false;
        }
        int intType = getMatchTypeByValue(pattern);

        switch (intType) {
            case Constants.MT_FULL:
                if (target.equals(pattern)) {
                    return true;
                }
                break;
            case Constants.MT_REGEX:
                if (regMatch(pattern, target)) {
                    return true;
                }
                break;
            case Constants.MT_WILDCARD:
                if (wildcardMatchPro(pattern, target)) {
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     *  判断是否能匹配正则表达式
     * @param pattern
     * @param target
     * @return
     */
    public static boolean regMatch(String pattern, String target) {
        if (Util.isStrEmpty(pattern) || Util.isStrEmpty(target)) {
            return false;
        }
        if (pattern.startsWith(Constants.REGEX_STARTER)) {
            pattern = pattern.substring(2);
        }
        return Pattern.matches(pattern, target);
    }

    /**
     *  如果没有配置methodMatchType，那么就根据配置的key判断，比如： *Activity
     * @param value
     * @return
     */
    public static int getMatchTypeByValue(String value) {
        if (Util.isStrEmpty(value)) {
            throw new RuntimeException("className cannot be null");
        } else if (value.startsWith(Constants.REGEX_STARTER)) {
            return Constants.MT_REGEX;
        } else if (value.contains("*") || value.contains("|")) {
            return Constants.MT_WILDCARD;
        } else {
            return Constants.MT_FULL;
        }
    }

    /**
     *  如果配置了 methodMatchType， 这里就直接转成type
     * @param type
     * @return
     */
    public static int typeString2Int(String type) {
        if (type == null || Constants.VALUE_ALL.equals(type)) {
            return Constants.MT_FULL;
        } else if (Constants.VALUE_REGEX.equals(type)) {
            return Constants.MT_REGEX;
        } else if (Constants.VALUE_WILDCARD.equals(type)) {
            return Constants.MT_WILDCARD;
        } else {
            return Constants.MT_FULL;
        }
    }

    /**
     *  通配符匹配，看是否满足格式。是否需要修改class
     * @param pattern
     * @param target
     * @return
     */
    public static boolean wildcardMatchPro(String pattern, String target) {
        if (pattern.contains("|")) {  //可以通过|连接多个通配符表达式。
            String[] patterns = pattern.split(Constants.WILDCARD_VLINE);
            String part;
            for (int i = 0; i < patterns.length; i++) {
                part = patterns[i];
                if (!Util.isStrEmpty(part)) {
                    // ! 用于排除在外
                    if (part.startsWith("!")) {
                        part = part.substring(1);
                        if (wildcardMatch(part, target)) {
                            return false;
                        }
                    }
                }
            }
            for (int i = 0; i < patterns.length; i++) {
                part = patterns[i];
                if (!Util.isStrEmpty(part)) {
                    if (!part.startsWith("!")) {
                        if (wildcardMatch(part, target)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } else {
            // 这里其实没有处理！的逻辑，是因为在没有 | 指定多个通配符表达式时，！是没有啥意思的。
            return wildcardMatch(pattern, target);
        }
    }
    /**
     * 星号匹配
     * @param pattern
     * @param target
     * @return
     * new StringBuilder()
    .append(wildcardMatch("com.**.act.*.github.*Activity", "com.jj.act.jj.github.MainActivity")).append(",") //true
    .append(wildcardMatch("*Activity", "com.jj.act.jj.github.MainActivity")).append(",")//true
    .append(wildcardMatch("*Activity", "com.jj.act.jjActivity")).append(",")//true
    .append(wildcardMatch("*Activity*", "com.jj.act.jjActivity")).append(",")//false
    .append(wildcardMatch(".*Activity", "com.Activity")).append(",")//false
    .append(wildcardMatch("com.**.a*t.*.github.*Activity", "com.jj.act.jj.github.MainActivity")).append(",")//true
    .append(wildcardMatch("com.**.act.*.gi*ub.*Act*vity", "com.jj.MainActivity.act")).append(",")//false
    .append(wildcardMatch("com.**.act.*.gi*ub.*Act*vity", "com.jj.act.jj.github.Mactivity")).append(",")//false
    .toString()
     */
    private static boolean wildcardMatch(String pattern, String target) {
        if (Util.isStrEmpty(pattern) || Util.isStrEmpty(target)) {
            return false;
        }
        try {
            String[] split = pattern.split(Constants.WILDCARD_STAR);
            //如果以分隔符开头和结尾，第一位会为空字符串，最后一位不会为空字符，所以*Activity和*Activity*的分割结果一样
            if (pattern.endsWith("*")) {//因此需要在结尾拼接一个空字符,为了区分上面注释中的两种情况
                List<String> strings = new LinkedList<>(Arrays.asList(split));
                strings.add("");
                split = new String[strings.size()];
                strings.toArray(split);
            }

            // 下面逻辑的核心思想是匹配通配符中的字符串。
            for (int i = 0; i < split.length; i++) {
                String part = split[i];
                if (Util.isStrEmpty(target)) {
                    return false;
                }
                if (i == 0 && !Util.isStrEmpty(part)) {
                    if (!target.startsWith(part)) {
                        return false;
                    }
                }
                if (i == split.length - 1 && !Util.isStrEmpty(part)) {
                    if (!target.endsWith(part)) {
                        return false;
                    } else {
                        return true;
                    }
                }
                if (part == null || part.trim().length() < 1) {
                    continue;
                }
                int index = target.indexOf(part);
                if (index < 0) {
                    return false;
                }
                int newStart = index + part.length() + 1;
                if (newStart < target.length()) {
                    target = target.substring(newStart);
                } else {
                    target = "";
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *  将a\b\c 转成a.b.c 并且去掉.class
     * @param entryName
     * @return
     */
    public static String path2Classname(String entryName) {
        return  entryName.replace(File.separator, ".").replace(".class", "");
    }

    /**
     *  将a\b\c 转成a.b.c 并且去掉.class
     * @param entryName
     * @return
     */
    public static String class2Pathname(String entryName) {
        return  entryName.replace(".", File.separator);
    }

    /**
     *  通过class获取对应的ASM sign
     * @param typeClass
     * @return
     */
    public static String getTypeSign(Class<?> typeClass)
    {
        if (Boolean.TYPE == typeClass) {
            return "Z";
        }
        if (Byte.TYPE == typeClass) {
            return "B";
        }
        if (Short.TYPE == typeClass) {
            return "S";
        }
        if (Integer.TYPE == typeClass) {
            return "I";
        }
        if (Float.TYPE == typeClass) {
            return "F";
        }
        if (Double.TYPE == typeClass) {
            return "D";
        }
        if (Long.TYPE == typeClass) {
            return "J";
        }
        if (Character.TYPE == typeClass) {
            return "C";
        }

        String className = typeClass.getName();
        if (className == null) {
            return "";
        }

        return className.replace(".", "/");
    }

}
