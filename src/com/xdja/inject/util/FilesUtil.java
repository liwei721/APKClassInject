package com.xdja.inject.util;

import com.xdja.inject.consant.Constants;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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

        File targetFile = new File(targetDir);
        if (targetFile.exists() && targetFile.isFile()){
            targetDir = targetFile.getParent();
        }
        try {
            String cmd = "explorer " + targetDir;
            ExecShellUtil.CommandResult result = ExecShellUtil.getInstance().execCmdCommand(cmd, false,true);
            if (result!= null && Util.isStrEmpty(result.errorMsg)){
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     *  删除原文件中的dex文件
     * @param tempDir
     * @return
     */
    public static boolean deleteDexFiles(String tempDir){
        File file = new File(tempDir);
        if (!file.exists()){
            return false;
        }

        File[] dexFiles = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isFile() && pathname.getName().endsWith(".dex")){
                    return true;
                }
                return false;
            }
        });

        if (dexFiles == null){
            return false;
        }

        boolean result = true;
        for (File dexFile : dexFiles){
            if(!deleteFile(dexFile.getAbsolutePath())){
                result = false;
            }
        }

        return result;
    }

    /**
     * 删除不用的目录
     */
    public static boolean deleteTempDir(){
        boolean isSuc = false;
        try{
            // 删除temp目录
            isSuc =  deleteDirectory(FilesUtil.getTempDirPath());
            // 删除根目录下面的 classes.dex
            File[] dexFiles = new File(FilesUtil.getBaseProjectPath()).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().endsWith(".dex")){
                        return true;
                    }
                    return false;
                }
            });

            if (dexFiles != null && dexFiles.length > 0){
                for (File dexFile : dexFiles){
                    deleteFile(dexFile.getAbsolutePath());
                }
            }

            // 删除resource下面的class文件
            File[] classFiles = new File(FilesUtil.getResourcePath()).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().endsWith(".class")){
                        return true;
                    }
                    return false;
                }
            });

            if (classFiles != null && classFiles.length > 0){
                for (File classFile : classFiles){
                    deleteFile(classFile.getAbsolutePath());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return isSuc;
    }

    /**
     *  将File放到zip文件中
     * @param fileName
     * @param zipFileName
     * @param newZipFileName
     */
    private static void addFileToZipFile(String fileName, String zipFileName, String newZipFileName)
    {
        ZipFile war = null;
        ZipOutputStream append = null;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }

            war = new ZipFile(zipFileName);
            append = new ZipOutputStream(new FileOutputStream(newZipFileName));

            Enumeration entries = war.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = (ZipEntry)entries.nextElement();
                append.putNextEntry(e);
                if (!e.isDirectory()) {
                    copy(war.getInputStream(e), append);
                }
                append.closeEntry();
            }
            ZipEntry e = new ZipEntry(Constants.MONITOR_CLASSNAME.replace(".", "/") + ".class");
            append.putNextEntry(e);
            append.write(bos.toByteArray());
            append.closeEntry();

            war.close();
            append.close();
        } catch (Exception localException) {
        } finally {
            if (war != null){
                try {
                    war.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (append != null){
                try {
                    append.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  copy 数据
     * @param input
     * @param output
     * @throws IOException
     */
    private static void copy(InputStream input, OutputStream output) throws IOException
    {
        byte[] BUFFER = new byte[4194304];
        int bytesRead;
        while ((bytesRead = input.read(BUFFER)) != -1)
        {
            output.write(BUFFER, 0, bytesRead);
        }
    }
    /**
     *  对java文件进行编译
     * @param javaFilePath
     * @return
     */
    private static boolean compileJavaFile(String javaFilePath){
        try{
            String cmd = "javac -encoding UTF-8 -cp " + FilesUtil.getAndroidJarPath() + " " + javaFilePath ;
            ExecShellUtil.CommandResult result = ExecShellUtil.getInstance().execCmdCommand(cmd, false, true);
            if (result != null && Util.isStrEmpty(result.errorMsg)){
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     *  将class文件放到jar中
     * @param JarPath
     * @param classFilePath
     * @return
     */
    private static boolean addClassFileToJar(String JarPath, String classFilePath){
        try{
            // 获取所有的jar
            File jarFile = new File(JarPath);
            File[] jarFiles = jarFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().endsWith(".jar")){
                        return true;
                    }
                    return false;
                }
            });

            if (jarFiles == null || jarFiles.length < 1){
                return false;
            }

            for (File oldjarFile : jarFiles){
                if (!oldjarFile.exists()){
                    return false;
                }
                File newJarFile = new File(oldjarFile.getParent() + File.separator + "temp_" + oldjarFile.getName());
                if (!newJarFile.exists()){
                    newJarFile.createNewFile();
                }

                FilesUtil.addFileToZipFile(classFilePath, oldjarFile.getAbsolutePath(), newJarFile.getAbsolutePath());
                if (oldjarFile.delete()){
                    if (newJarFile.renameTo(oldjarFile)){
                        return true;
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }

    /**
     *  将java file 放到jar文件中。
     * @param javaFilePath
     * @return
     */
    public static boolean addFileToJar(String javaFilePath, String jarPath){
        // 1.第一步，编译
        if (Util.isStrEmpty(javaFilePath)){
            return false;
        }

        File javaFile = new File(javaFilePath);
        if (!javaFile.exists()){
            return false;
        }

        if (compileJavaFile(javaFilePath)){
            // 第二步，将class放到jar中。
            return addClassFileToJar(jarPath,FilesUtil.getResourcePath() + File.separator + Constants.MONITOR_CLASS);
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

    /**
     *  获取项目的资源目录
     * @return
     */
    public static String getResourcePath(){
        return System.getProperty("user.dir") + File.separator + "resource";
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

    public static String getAndroidJarPath(){
        return getBaseProjectPath() + File.separator + "tools" + File.separator + "android.jar";
    }

    /**
     *  获取配置的路径
     * @return
     */
    public static String getConfigPath(){
        return getBaseProjectPath() + File.separator + "config" ;
    }
}
