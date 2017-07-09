package com.xdja.inject.util;

import com.googlecode.dex2jar.tools.Dex2jarCmd;
import com.googlecode.dex2jar.tools.Jar2Dex;
import com.xdja.inject.consant.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zlw on 2017/6/28.
 * Dex2jar��һЩ�����࣬��װ���õĲ���
 */
public class Dex2jarUtil {

    /**
     *  ��dexת����jar����������jar�������������һ�����ԭ����Ŀ¼��
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
     *  ������һ��dexת�ɶ�Ӧ��jar
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
     *  ��jarת��dex��ʹ��dx����
     * @param jarPathList
     * @param tempDir  ��ѹ֮���Ŀ¼
     * @return ����jar����ʱĿ¼
     */
    public static boolean  jar2Dex(List<String> jarPathList, String tempDir) throws IOException {
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
            // out dex����ŵ���Ŀ¼�У����������潫dex�ŵ�apk��������ѡ�
            String outDexPath = FilesUtil.getBaseProjectPath() + File.separator+ dexName;
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

            String[] cmd = {"-f", "-o"  , outDexPath, jarFile.getAbsolutePath() };
            try{
                new Jar2Dex().doMain(cmd);
            }catch (Exception ex){
                LogUtil.info(" dexName = " + " handle failed");
            }
        }

        return true;
    }

    /**
     *  ��jarnameת��dex name
     * @param jarName
     * @return
     */
    private static String jarName2DexName(String jarName){
        return jarName.replace(".jar", ".dex");
    }

    /**
     *  ɾ��apk�ļ��е�ǩ����Ϣ��
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
                    cmd = cmd + " "+Constants.META_INFO + metaFile.getName();
                }

                ExecShellUtil.CommandResult result = ExecShellUtil.getInstance().execCmdCommand(cmd, false, true);
                if (result != null && Util.isStrEmpty(result.errorMsg)){
                    return true;
                }else {
                    LogUtil.info("deleteMetaInfo fail");
                    return false;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            LogUtil.info("deleteMetaInfo fail " + ex.getMessage());
        }

        return false;
    }

    /**
     *  �� dex�ļ�ѹ�뵽apk�С�
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

                ExecShellUtil.getInstance().execCmdCommand(rmCmd, false, true);
            }

            File tempDexFolder = new File(tempDir);
            if (tempDexFolder.exists()){
                File[] tempDexFiles = tempDexFolder.listFiles();
                if (tempDexFiles == null || tempDexFiles.length < 1) return false;

                for (File dexFile : tempDexFiles){
                    if (dexFile.getName().endsWith(".dex")){
                        addCmd = addCmd + " " + dexFile.getName();
                    }
                }

                ExecShellUtil.CommandResult result = ExecShellUtil.getInstance().execCmdCommand(addCmd, false, true);
                if (result != null && Util.isStrEmpty(result.errorMsg)){
                    return true;
                }else {
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
     *  ��apk���½���ǩ��
     *  ʹ�ù�˾��ǩ���ļ�
     * @param apkPath
     * @param signFilePath
     * @param pwd  ǩ���ļ�����
     * @return
     */
    public static String signApk(String apkPath, String signFilePath, String pwd, String signName){
        try {
            File signFile = new File(signFilePath);
            File apkFile = new File(apkPath);
            if (!apkFile.exists()){
                LogUtil.info("ǩ��apkʱ��ԭapk�����ڣ�����");
                return "";
            }

            if (!signFile.exists()){
                LogUtil.info("ǩ���ļ������ڣ���ʹ��Ĭ�ϵ�ǩ������");
                signFilePath = new File(FilesUtil.getBaseProjectPath() + File.separator + "config" + File.separator + Constants.signFileName).getAbsolutePath();
                pwd = Constants.SIGNPWD;
                signName = Constants.SIGNALIAS;
            }

            //����ǩ��֮���apk����
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


            ExecShellUtil.CommandResult result = ExecShellUtil.getInstance().execCmdCommand(signCmd.toString(), false, true);
            if (result != null && Util.isStrEmpty(result.errorMsg)){
                return singedapk;
            }else {
                LogUtil.info("addDexToAPK delete origin dex fail");
                return "";
            }

        }catch (Exception ex){
            ex.printStackTrace();
            LogUtil.info("ǩ��apkʧ�� " + ex.getMessage());
        }

        return "";
    }
    /**
     *  ��dexת��jar
     * @param zipFolder  ��ѹapk֮���folder
     * @return  ���ɵ�dex·��
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
         *  ���˳�������dex��β���ļ�
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
         * ����dexFile����dexת��jar
         */
        jarPaths = Dex2jarUtil.dexs2jars(dexFiles);

        /**
         * ��ԭĿ¼�е�dex�ļ�ɾ��
         */
        FilesUtil.deleteDexFiles(zipFolder);

        return jarPaths;
    }


    public static void main(String[] args){
        String[] cmd = {"-f", "-o" , "E:\\javaworkspace\\ApkInjectTools\\classes.dex", "E:\\javaworkspace\\ApkInjectTools\\temp\\app-debug\\classes.jar" };
        try{
            new Jar2Dex().doMain(cmd);
        }catch (Exception ex){
            LogUtil.info(" dexName = " + " handle failed");
        }
    }
}
