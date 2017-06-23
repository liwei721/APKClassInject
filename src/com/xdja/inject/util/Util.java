package com.xdja.inject.util;

import com.xdja.inject.Constants;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zlw on 2017/6/23.
 * 工具类
 */
public class Util {
    private static final int BUFFER = 1024;
    private static final String PROJECT_BASE_PATH = System.getProperty("user.dir");
    private static final String DEX_TOOL_PATH = System.getProperty("user.dir") + "\\dex2jar\\";
    private static final String DEX2JAR_TOOL_PATH = System.getProperty("user.dir") + "\\dex2jar\\dex2jar.bat";
    private static final String JAR2DEX_TOOL_PATH = System.getProperty("user.dir") + "\\dex2jar\\jar2dex.bat";
    private static final String REG_DEX2JAR = "-dex2jar";

    /**
     *  将apk解压到临时目录中
     * @param apkPath apk的路径
     * @return 返回的是解压之后的临时目录
     */
    public static String upzipApk(String apkPath){
        if (isStrEmpty(apkPath)){
            return "";
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()){
            return "";
        }

        String apkFileName = apkFile.getName();

        ZipFile zipFile = null;
        String tempDir = System.getProperty("user.dir") + "/temp/" + apkFileName.substring(0, apkFileName.length() - 4) + "/";
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration emu = zipFile.entries();
            while (emu.hasMoreElements()){
                ZipEntry zipEntry = (ZipEntry) emu.nextElement();

                // 判断zipEntry的类型
                if (zipEntry.isDirectory()){
                    new File(tempDir + zipEntry.getName()).mkdirs();
                    continue;
                }

                bis  = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                File targetFile = new File(tempDir + zipEntry.getName());
                File parent = targetFile.getParentFile();
                if (parent != null && !parent.exists()){
                    parent.mkdirs();
                }

                //将Entity写到temp dir中
                FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                bos = new BufferedOutputStream(fileOutputStream, BUFFER);
                byte [] buf = new byte[BUFFER];
                int len = 0;
                while((len=bis.read(buf,0,BUFFER))!=-1){
                    fileOutputStream.write(buf,0,len);
                }
                bos.flush();
            }
            return tempDir;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bis != null){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    /**
     *  将dex转成jar
     * @param zipFolder  解压apk之后的folder
     * @return  生成的dex路径
     */
    public static List<String> dex2jar(String zipFolder){
        List<String> dexPaths = new ArrayList<>();
        if (isStrEmpty(zipFolder)){
            return dexPaths;
        }

        File apkunZipFile = new File(zipFolder);
        if (!apkunZipFile.exists()){
            return dexPaths;
        }

        /**
         *  过滤出所有以dex结尾的文件
         */
        File[] dexFiles = apkunZipFile.listFiles(pathname -> {
            if (pathname.isFile() && pathname.getName().endsWith(".dex")){
                return true;
            }
            return false;
        });

        if (dexFiles == null || dexFiles.length < 1){
            return dexPaths;
        }
        /**
         * 遍历dexFile，将dex转成jar
         */
        for (int i = 0; i < dexFiles.length; i++){
            File dexFile = dexFiles[i];
            String dex2jarpath = dex2jarExec(dexFile);
            System.out.println("dex2jar ==  dex2jarpath = " + dex2jarpath);
            dexPaths.add(dex2jarpath);
        }

        return dexPaths;
    }


    /**
     *  判断字符串是否是空的
     * @param str
     * @return
     */
    public static boolean isStrEmpty(String str){
        return str == null || "".equals(str);
    }

    /**
     *  将dex转成jar，使用dex2jar工具
     * @param dexFile
     * @return
     * 
     *
     */
    // TODO: 2017/6/23 这里逻辑比较混乱，待整理
    private static String dex2jarExec(File dexFile){
        String jarPath = "";
        String cmd = DEX2JAR_TOOL_PATH + " -f " + dexFile;

        // 运行生成jar，注意会在原文件名后面加上-dex2jar
        runWindowCmdWithPath(cmd, dexFile.getParentFile());

        // 运行完成后
        File[] jarFiles = dexFile.getParentFile().listFiles(pathname -> {
            if (pathname.isFile() && pathname.getName().endsWith(".jar") && pathname.getName().contains(REG_DEX2JAR)){
                return true;
            }
            return false;
        });

        if (jarFiles == null || jarFiles.length < 1){
            return jarPath;
        }

        // 正常情况下应该过滤出一个这样的jar
        File jarFile = jarFiles[0];
        // 这里是为了去掉-dex2jar
        jarPath = jarFile.getAbsolutePath();
        jarPath = jarPath.substring(0, jarPath.indexOf(REG_DEX2JAR));
        jarPath = jarPath + ".jar";

        boolean suc = jarFile.renameTo(new File(jarPath));
        if (suc){
            // 删除原有的dex
            // TODO: 2017/6/23  这里dex删除失败后续再考虑
            dexFile.delete();
            return jarPath;
        }

        return "";
    }

    /**
     * 在指定目录中运行命令
     *  @param cmd 指定的命令
     *  @param path 指定的目录
     *
     */
    private static void runWindowCmdWithPath(String cmd, File path){
        if (isStrEmpty(cmd)){
            return;
        }

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd, null, path);
            InputStream stderr = process.getErrorStream();
            InputStreamReader isr = new InputStreamReader(stderr);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (process != null){
                process.destroy();
            }
        }
    }
    /**
     *  运行Windows的命令
     * @param cmd
     * @return
     */
    private static void runWindowCmd(String cmd){
        if (isStrEmpty(cmd)){
            return;
        }

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (process != null){
                process.destroy();
            }
        }
    }

    public static boolean isPatternMatch(String pattern, String type, String target) {
        if (isStrEmpty(pattern) || isStrEmpty(target)) {
            return false;
        }
        int intType;
        if (isStrEmpty(type)) {
            intType = getMatchTypeByValue(pattern);
        } else {
            intType = typeString2Int(type);
        }
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
        if (isStrEmpty(pattern) || isStrEmpty(target)) {
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
        if (isStrEmpty(value)) {
            throw new RuntimeException("Key cannot be null");
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
    
    public static void main(String[] args){
       String tempDir =  upzipApk("\\app-production-release-1.0.1.7377.apk");
       dex2jar(tempDir);
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
                if (!isStrEmpty(part)) {
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
                if (!isStrEmpty(part)) {
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
        if (isStrEmpty(pattern) || isStrEmpty(target)) {
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
                if (isStrEmpty(target)) {
                    return false;
                }
                if (i == 0 && !isStrEmpty(part)) {
                    if (!target.startsWith(part)) {
                        return false;
                    }
                }
                if (i == split.length - 1 && !isStrEmpty(part)) {
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
}
