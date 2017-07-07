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
    /**
     * 判断字符串是否是空的
     *
     * @param str
     * @return
     */
    public static boolean isStrEmpty(String str) {
        return str == null || "".equals(str);
    }


    /**
     * 这里 执行cmd命令没有返回值
     *
     * @param cmd
     * @param isOutputLog
     * @return
     */
    public static boolean execCmd(String cmd, boolean isOutputLog) {
        BufferedReader br = null;
        BufferedReader errorReader = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = null;
            while ((line = br.readLine()) != null){
                if (isOutputLog){
                    System.out.println(line);
                }
            }

            while ((line = errorReader.readLine()) != null){
                System.out.println(line);
//                isSuc = false;
            }
        } catch (Exception e) {
            System.out.println("cmd error:" + e.toString());

            if (br != null)
                try {
                    br.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 判断 参数列表是否是empty的。
     *
     * @return
     */
    public static boolean isListEmpty(List list) {
        if (list == null || list.size() < 1) {
            return true;
        }

        return false;
    }
}
