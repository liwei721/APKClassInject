package com.xdja.inject.util;

import java.io.File;

/**
 * Created by zlw on 2017/6/28.
 * Email: zlw@xdja.com
 * function: 对文件的操作放到这里
 */
public class FilesUtil {

    /**
     *  删除目录
     * @param dir
     * @return
     */
    public static boolean deleteDirectory(String dir){
        if (Util.isStrEmpty(dir)){
            return true;
        }

        if (!dir.endsWith(File.separator)){
            dir = dir + File.separator;
        }

        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()){
            return false;
        }

        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++){
            if (files[i].isFile()){
                flag = deleteFile(files[i].getName());
                if (!flag){
                    // 假如有一个删除失败，这里就返回了。
                    break;
                }
            }else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag){
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }

        if (dirFile.delete()) {
            return true;
        }
        return false;
    }


    /**
     *  删除一个文件
     * @param fileName
     * @return
     */
    public static boolean deleteFile(String fileName)
    {
        File file = new File(fileName);

        if ((file.exists()) && (file.isFile())) {
            if (file.delete()) {
                return true;
            }
            return false;
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

    public static String getTempDirPath(){
        return getBaseProjectPath() + File.separator + "temp";
    }

    /**
     *  获取aapt的路径
     * @return
     */
    public static String getAaptcmdPath(){
        return getBaseProjectPath() + "\\tools\\aapt.exe";
    }
}
