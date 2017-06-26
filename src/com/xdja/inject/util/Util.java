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
    public static String dex2jarExec(File dexFile){
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

    /**
     *  判断 参数列表是否是empty的。
     * @return
     */
    public static boolean isListEmpty(List list){
        if (list == null || list.size() < 1){
            return true;
        }

        return false;
    }

    /**
     *  获取项目所在根目录
     * @return
     */
    public static String getBaseProjectPath(){
        return System.getProperty("user.dir");
    }
}
