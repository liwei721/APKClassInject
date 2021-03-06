package com.xdja.inject.util;

import com.android.dx.command.Main;
import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.xdja.inject.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlw on 2017/6/28.
 * Dex2jar的一些工具类，封装常用的操作
 */
public class Dex2jarUtil {

    /**
     *  将dex转换成jar，命名是以jar的名字命名，且还放在原来的目录下
     * @param dexPath
     */
    public static String dex2jar(String dexPath){
        if (Util.isStrEmpty(dexPath)) return "";

        File dexFile = new File(dexPath);
        if (!dexFile.exists()){
            return "";
        }

        try {
            return new Dex2jarCmd().execDex2jar(dexPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     *  将至少一个dex转成对应的jar
     * @param dexs
     * @return
     */
    public static List<String> dexs2jars(File[] dexs){
        List<String> jars = new ArrayList<>(3);
        if (dexs == null || dexs.length < 1){
            return jars;
        }

        for (File dexFile : dexs){
            String jarPath = dex2jar(dexFile.getAbsolutePath());
            jars.add(jarPath);
        }

        return jars;
    }

    /**
     *  将jar转成dex，使用dx工具
     * @param jarPathList
     * @return 返回jar的临时目录
     */
    public static boolean  jar2Dex(List<String> jarPathList) throws IOException {
        if (Util.isListEmpty(jarPathList)){
            LogUtil.info("jar2Dex input jars path is null");
            return false;
        }

        for (String jarPath : jarPathList){
            File jarFile = new File(jarPath);
            if (!jarFile.exists()){
                continue;
            }

            String dexName = jarName2DexName(jarFile.getName());
            String rootPath = FilesUtil.getBaseProjectPath();
            String outDexPath = rootPath + File.separator+ dexName;
            File outDexFile = new File(outDexPath);
            if (!outDexFile.getParentFile().exists()){
                boolean isSuc = outDexFile.getParentFile().mkdirs();
                if (!isSuc){
                    return false;
                }
            }

            if (!outDexFile.exists()){
                boolean isSuc = outDexFile.createNewFile();
                if (!isSuc){
                    return  false;
                }
            }

            String[] cmd = { "--dex", "--output=" + outDexPath, jarFile.getAbsolutePath() };
            try{
                Main.main(cmd);
            }catch (Exception ex){
                LogUtil.info(" dexName = " + " handle failed");
            }
        }

        return true;
    }

    /**
     *  将jarname转成dex name
     * @param jarName
     * @return
     */
    private static String jarName2DexName(String jarName){
        return jarName.replace(".jar", ".dex");
    }

    /**
     *  删除apk文件中的签名信息。
     * @param unzipDir
     * @param inputApkPath
     * @return
     */
    public static boolean deleteMetaInfo(String unzipDir, String inputApkPath){
        try{
            File metaInfoFile = new File(unzipDir + File.separator + Constants.META_INFO);
            if (metaInfoFile.exists()){
                File[] metaList = metaInfoFile.listFiles();
                if (metaList == null || metaList.length < 1){
                    return false;
                }

                String aaptCmd = FilesUtil.getAaptcmdPath();
                String cmd = aaptCmd + " r "  + new File(inputApkPath).getAbsolutePath();
                for (File metaFile : metaList){
                    // TODO: 2017/6/28 这里不加META-INFO,之前用命令行时貌似不需要加
                    cmd = cmd + " "+Constants.META_INFO + metaFile.getName();
                }

                return Util.execCmd(cmd, true);
            }
        }catch (Exception ex){
            ex.printStackTrace();
            LogUtil.info("deleteMetaInfo fail " + ex.getMessage());
        }

        return false;
    }

    /**
     *  将 dex文件压入到apk中。
     *
     * @param apkPath
     */
    public static boolean addDexToApk(String apkPath, String tempDir){
        try {
            String rmCmd = FilesUtil.getAaptcmdPath() + " r " + new File(apkPath).getAbsolutePath();
            String addCmd = FilesUtil.getAaptcmdPath() + " a " + new File(apkPath).getAbsolutePath();
            File[] dexFiles = new File(tempDir).listFiles();
            if (dexFiles  != null){
                for (File dexFile : dexFiles){
                    if (dexFile.getName().endsWith(".dex")){
                        rmCmd = rmCmd + " " + dexFile.getName();
                    }
                }

                if (!Util.execCmd(rmCmd, true)){
                    LogUtil.info("addDexToAPK delete origin dex fail");
                    return false;
                }
            }

            File tempDexFolder = new File(FilesUtil.getBaseProjectPath());
            if (tempDexFolder.exists()){
                File[] tempDexFiles = tempDexFolder.listFiles();
                if (tempDexFiles == null || tempDexFiles.length < 1) return false;

                for (File dexFile : tempDexFiles){
                    if (dexFile.getName().endsWith(".dex")){
                        addCmd = addCmd + " " + dexFile.getName();
                    }
                }

                if (!Util.execCmd(addCmd, true)){
                    LogUtil.info("addDexToAPK add dex to apk fail");
                    return false;
                }
            }
        }catch (Exception ex){
            LogUtil.info("addDexToApk fail " + ex.getMessage());
        }
        return true;
    }

    /**
     *  对apk重新进行签名
     *  使用公司的签名文件
     * @param apkPath
     * @param signFilePath
     * @param pwd  签名文件密码
     * @return
     */
    public static String signApk(String apkPath, String signFilePath, String pwd, String signName){
        try {
            File signFile = new File(signFilePath);
            File apkFile = new File(apkPath);
            if (!apkFile.exists()){
                LogUtil.info("签名apk时，原apk不存在，请检查");
                return "";
            }

            if (!signFile.exists()){
                LogUtil.info("签名文件不存在，将使用默认的签名配置");
                signFilePath = new File(FilesUtil.getBaseProjectPath() + File.separator + "config" + File.separator + Constants.signFileName).getAbsolutePath();
                pwd = Constants.SIGNPWD;
                signName = Constants.SIGNALIAS;
            }

            //构造签名之后的apk名字
            String origAPkPath = apkFile.getAbsolutePath();
            String singedapk = apkFile.getAbsolutePath().substring(0, origAPkPath.indexOf(".apk")) + "_inject_sign.apk";

            StringBuilder signCmd = new StringBuilder("jarsigner.exe");
            signCmd.append(" -verbose -keystore ");
            signCmd.append(signFilePath);
            signCmd.append(" -storepass ");
            signCmd.append(pwd);
            signCmd.append(" -signedjar ");
            signCmd.append(singedapk + " ");
            signCmd.append(apkPath + " ");
            signCmd.append(signName + " ");
            signCmd.append("-digestalg SHA1 -sigalg MD5withRSA");

            boolean isSuc = Util.execCmd(signCmd.toString(), false);
            if (!isSuc){
                return "";
            }


            return singedapk;
        }catch (Exception ex){
            ex.printStackTrace();
            LogUtil.info("签名apk失败 " + ex.getMessage());
        }

        return "";
    }
    /**
     *  将dex转成jar
     * @param zipFolder  解压apk之后的folder
     * @return  生成的dex路径
     */
    public static List<String> dex2jarImpl(String zipFolder){
        List<String> jarPaths = new ArrayList<>(3);
        if (Util.isStrEmpty(zipFolder)){
            return jarPaths;
        }

        File apkunZipFile = new File(zipFolder);
        if (!apkunZipFile.exists()){
            return jarPaths;
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
            return jarPaths;
        }
        /**
         * 遍历dexFile，将dex转成jar
         */
        jarPaths = Dex2jarUtil.dexs2jars(dexFiles);

        return jarPaths;
    }
}
