package com.xdja.inject.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zlw on 2017/6/28.
 * Email: zlw@xdja.com
 * function: 对文件的操作放到这里
 */
public class FilesUtil {
    private static final int BUFFER = 1024;
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
                flag = deleteFile(files[i].getAbsolutePath());
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
     *  将apk进行解压
     * @param apkPath  apkPath 文件的路径
     */
    public static String decompressApk(String apkPath) throws IOException {
        if (Util.isStrEmpty(apkPath)){
            return "";
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()){
           throw new FileNotFoundException("decompressApk but apk not exist");
        }

        String apkFileName = apkFile.getName();
        String tempDir = getTempDirPath() + File.separator + apkFileName.substring(0, apkFileName.length() - 4) + File.separator;
        File tempFolder = new File(tempDir);
        if (!tempFolder.exists()){
            tempFolder.mkdirs();
        }

        String targetPath = tempFolder.getAbsolutePath();

        ZipFile zipFile = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration emu = zipFile.entries();
            File outFile = null;
            while (emu.hasMoreElements()){
                ZipEntry zipEntry = (ZipEntry) emu.nextElement();
                outFile = new File(targetPath + File.separator + zipEntry.getName());
                // 判断zipEntry的类型
                if (zipEntry.isDirectory()){
                    outFile.mkdirs();
                    continue;
                }

                bis  = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                File parent = outFile.getParentFile();
                if (parent != null && !parent.exists()){
                    parent.mkdirs();
                }

                //将Entity写到temp dir中
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                bos = new BufferedOutputStream(fileOutputStream, BUFFER);
                byte [] buf = new byte[BUFFER];
                int len = 0;
                while((len=bis.read(buf,0,BUFFER))!=-1){
                    fileOutputStream.write(buf,0,len);
                }
                bos.flush();
            }

            return targetPath;
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
     * 打开对应的dir
     * @param targetDir
     * @return
     */
    public static boolean openDir(String targetDir){
        if (Util.isStrEmpty(targetDir)){
            return false;
        }

        try {
            String cmd = "explorer " + targetDir;
            Util.execCmd(cmd, false);
            return true;
        }catch (Exception ex){
            ex.printStackTrace();
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
