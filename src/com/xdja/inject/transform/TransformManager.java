package com.xdja.inject.transform;

import com.googlecode.d2j.dex.Dex2jar;
import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.setting.SettingHelper;
import com.xdja.inject.util.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by zlw on 2017/6/23.
 * ����ฺ�������̣����������̴�������
 */
public class TransformManager {
    private SettingEntity setting;
    private TransformListener transformListener;

    /**
     * ��Apk���д���
     *
     * @param apkPath      ��Ҫ�����apk path
     * @param signAlias
     * @param signFilePath
     * @param signPwd
     */
    private void handleApk(String apkPath, String signFilePath, String signPwd, String signAlias) {
        try {
            // ���û������apkpath���ͷ���
            if (Util.isStrEmpty(apkPath)) return;
            transformListener.start();
            // 1. �ж������Ƿ���ȷ
            String result = SettingHelper.getInstance().isSettingEnable();
            if (!"".equals(result)){
                transformListener.showError(result);
                return;
            }

            // 2. ��ѹapk��tempĿ¼
            transformListener.upzipApk(1, apkPath);
            String tempDir = FilesUtil.decompressApk(apkPath);
            // �����ʱĿ¼Ϊnull�����ܽ�ѹû�гɹ���ֱ�ӷ��ء�
            if (Util.isStrEmpty(tempDir)) {
                transformListener.showError("��ѹapkʧ���ˡ�");
                LogUtil.info("��ѹapkʧ���ˡ�");
                return;
            }

            // 3. ��dexת��jar,ͬʱ��dex2jar����ɴ���ע��
            // TODO: 2017/7/7  �����쳣������Ҫ���ǡ�
            transformListener.dex2jar(2, tempDir);
            List<String> jars = Dex2jarUtil.dex2jarImpl(tempDir);
            if (Util.isListEmpty(jars)){
                transformListener.showError("dex ת�� jar ʧ����");
                LogUtil.info("dex ת�� jar ʧ����");
                return;
            }

            // 3.1 ��������д��jar��
            transformListener.codeToJar(2, tempDir);
            boolean addFileSuc = FilesUtil.addFileToJar(FilesUtil.getResourcePath() + File.separator + "AppMonitor.java", tempDir );
            if (!addFileSuc){
                transformListener.showError("��������AppMonitor��ӵ�jar��ʧ����");
                LogUtil.info("��������AppMonitor��ӵ�jar��ʧ����");
                return;
            }

            // 4. ��jarת��dex��ʹ��dx���ߡ�
            transformListener.jar2dex(3, apkPath);
            boolean isSuc2 = Dex2jarUtil.jar2Dex(jars, tempDir);
            if (!isSuc2) {
                transformListener.showError("jar ת�� dex ʧ����");
                LogUtil.info("jar ת�� dex ʧ����");
                return;
            }

            // 5. �������dex�ŵ�apk��
            transformListener.dexToapk(4, apkPath);
            boolean isSuc1 = Dex2jarUtil.addDexToApk(apkPath, tempDir);
            if (!isSuc1) {
                transformListener.showError("��dex�ŵ�apk��ʧ����");
                LogUtil.info("��dex�ŵ�apk��ʧ����");
                return;
            }

            // ����ɹ�
            // 6. ɾ��apk��ԭ����ǩ���ļ�
            transformListener.deleteMeta(5, apkPath);
            boolean isSuc = Dex2jarUtil.deleteMetaInfo(tempDir, apkPath);
            if (!isSuc) {
                transformListener.showError("ɾ��ԭapk�е�meta-info ʧ����");
                LogUtil.info("ɾ��ԭapk�е�meta-info ʧ����");
                return;
            }

            // 7. ��apk����ǩ��
            String signApkPath = Dex2jarUtil.signApk(apkPath, signFilePath, signPwd, signAlias);
            transformListener.apkSign(6, signApkPath);
            if (Util.isStrEmpty(signApkPath)) {
                transformListener.showError("��apkǩ��ʧ����");
                LogUtil.info("��apkǩ��ʧ����");
                return;
            }

            // �������񶼳ɹ��ʹ�Ŀ¼
            boolean succ = FilesUtil.openDir(signApkPath);
            if (!succ){
                transformListener.showError("��ǩ��֮���apkĿ¼ʧ��");
                LogUtil.info("��ǩ��֮���apkĿ¼ʧ��");
            }
            transformListener.finish(signApkPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     *  ��ʼ��APK���в�׮
     * @param apkPath
     * @param signFilePath
     * @param signPwd
     * @param signAlias
     */
    public void injectApk(String apkPath, String signFilePath, String signPwd, String signAlias){
        handleApk(apkPath,signFilePath, signPwd, signAlias);

        // �����������
        SettingHelper.getInstance().clearData();

        // ɾ�����õ���ʱ�ļ�
        //  ɾ��tempĿ¼
        boolean suc = FilesUtil.deleteTempDir();
        if (!suc){
            transformListener.showError("ɾ����ʱĿ¼ʧ����");
        }

    }

    /**
     * ���ü���
     *
     * @param listener
     */
    public void setTransformListener(TransformListener listener) {
        transformListener = listener;
    }


    public static void main(String[] args){
//        FilesUtil.deleteTempDir(FilesUtil.getTempDirPath());
//        LogUtil.info("Test");
//        List<String> jars = Dex2jarUtil.dex2jarImpl(FilesUtil.getTempDirPath());
        try {
            String tempdir =   FilesUtil.decompressApk("F:\\com.hupu.games_7.0.17.9574_liqucn.com.apk");
            List<String> jars = Dex2jarUtil.dex2jarImpl(tempdir);
            Dex2jarUtil.jar2Dex(jars, tempdir);
            Dex2jarUtil.addDexToApk("F:\\com.hupu.games_7.0.17.9574_liqucn.com.apk", tempdir);
        } catch (IOException e) {
            e.printStackTrace();
        }


//        FilesUtil.addFileToJar(FilesUtil.getResourcePath() + File.separator + "AppMonitor.java", FilesUtil.getTempDirPath() + File.separator + "classes.jar");
    }
}
