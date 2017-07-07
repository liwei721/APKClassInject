package com.xdja.inject.view;

import com.xdja.inject.consant.Constants;
import com.xdja.inject.transform.TransformListener;
import com.xdja.inject.transform.TransformManager;
import com.xdja.inject.util.LogUtil;
import com.xdja.inject.util.Util;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Created by zlw on 2017/6/23.
 */
public class LaunchView extends JFrame implements ActionListener{
    JButton chooseBtn, chooseSignBtn = null;
    JTextField apkPathField , signFileField = null;
    JTextField signPwdField, signAlias = null;
    JLabel tipsLabel = null;
    private JPanel frame = null;
    private final static String CHOOSE_CMD = "chooseFile";
    private final static String CHOOSE_SING_CMD = "chooseSignFile";
    private final static String COMMIT_CMD = "CommitFile";
    private final static String CONFIRM = "ȷ��";
    private final static String CANCEL = "ȡ��";

    // ��¼���������
    private String mApkPath = "";
    private String mSignFilePath = "";
    private String mSignPwd = "";
    private String mSignAlias = "";

    private TransformManager mTransforManager;
    private TransformListener mListener;
    private boolean isTaskRunning = false;

    public LaunchView(){
        this.setTitle(Constants.TITLE + "_" + Constants.VERSION);
        this.frame = new JPanel();
        this.setBounds(400, 200, 600, 400);
        add(frame);
        setResizable(false);
        setVisible(true);
        addActionListener();
        mTransforManager = new TransformManager();
        initTransformListener();
        mTransforManager.setTransformListener(mListener);
    }

    private void initTransformListener(){
        mListener = new TransformListener() {
            @Override
            public void start() {
                isTaskRunning = true;
                refreshCommitBtnState(isTaskRunning);
                updateTips("���ڿ�ʼ��׮���񣡣�");
            }

            @Override
            public void upzipApk(int i, String path) {
                updateTips("ִ�е� " + i + " ������ѹapk��������ѹ֮��·����" + path);
                LogUtil.info("ִ�е� " + i + " ������ѹapk��������ѹ֮��·����" + path);
            }

            @Override
            public void dex2jar(int i, String path) {
                LogUtil.info("ִ�е� " + i + " ������dexת��jar����");
                updateTips("ִ�е� " + i + " ������dexת��jar����");
            }

            @Override
            public void deleteMeta(int i, String var) {
                LogUtil.info("ִ�е� " + i + " ����ɾ��apk��ԭ�е�ǩ����Ϣ����");
                updateTips("ִ�е� " + i + " ����ɾ��apk��ԭ�е�ǩ����Ϣ����");
            }

            @Override
            public void dexToapk(int i, String var) {
                LogUtil.info("ִ�е� " + i + " ��������׮���dex push��apk�У���");
                updateTips("ִ�е� " + i + " ��������׮���dex push��apk�У���");
            }

            @Override
            public void codeToJar(int i, String var) {
                LogUtil.info("ִ�е� " + i + " ��������������ӵ�jar�У�" + var);
                updateTips("ִ�е� " + i + " ��������������ӵ�jar�У�" + var);
            }

            @Override
            public void apkSign(int i, String var) {
                LogUtil.info("ִ�е� " + i + " �����Բ�׮��apk����ǩ������ǩ�����apk��ַ��" + var);
                updateTips("ִ�е� " + i + " �����Բ�׮��apk����ǩ������ǩ�����apk��ַ��" + var);
            }

            @Override
            public void jar2dex(int i, String var) {
                LogUtil.info("ִ�е� " + i + " ������jarת��dex��" + var);
                updateTips("ִ�е� " + i + " ������jarת��dex��" + var);
            }

            @Override
            public void finish(String apkPath) {
                isTaskRunning = false;
                refreshCommitBtnState(isTaskRunning);
                updateTips("��׮������ɣ������� ��" + apkPath + "�鿴");
            }

            @Override
            public void showError(String errorMsg) {
                isTaskRunning = false;
                refreshCommitBtnState(isTaskRunning);

                updateTips("���г����ˣ�������Ϣ�ǣ�" + errorMsg);
            }

        };
    }

    /**
     *  ���ڸ�����ʾ��Ϣ
     * @param msg
     */
    public void updateTips(String msg){
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    if (tipsLabel != null){
                        tipsLabel.setText(msg);
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     *  ˢ������ִ������ʱ��״̬
     * @param isTrue
     */
    private void refreshCommitBtnState(boolean isTrue){
        if (isTrue){
            chooseBtn.setEnabled(false);
            chooseSignBtn.setEnabled(false);
            signAlias.setEnabled(false);
            signPwdField.setEnabled(false);
        }else {
            chooseBtn.setEnabled(true);
            chooseSignBtn.setEnabled(true);
            signAlias.setEnabled(true);
            signPwdField.setEnabled(true);
        }
    }
    /**
     *  ѡ��apk�ļ�
     */
    private void createChooseApk(){
        // ������ʽ���ò��ָ�ʽΪ�գ����򲻻ᰴ���������úõĸ�ʽ����
        frame.setLayout(null);

        // ����һ��������ʾѡ��·�����ı���
        apkPathField = new JTextField();
        apkPathField.setEnabled(false);
        frame.add(apkPathField);
        Rectangle rect = new Rectangle(10, 10, 400, 30);
        apkPathField.setBounds(rect);

        // ����һ����ť
        chooseBtn = new JButton("ѡ��Ҫ��׮apk�ļ�");
        frame.add(chooseBtn);
        chooseBtn.setActionCommand(CHOOSE_CMD);
        chooseBtn.setBounds(430 ,10, 150, 30);
        chooseBtn.addActionListener(this);
    }

    /**
     *  ����ѡ��apkǩ���ļ�������
     */
    private void createChooseSignFile(){
        // ����һ��������ʾѡ��·�����ı���
        signFileField = new JTextField();
        signFileField.setEnabled(false);
        frame.add(signFileField);
        Rectangle rect = new Rectangle(10, 50, 400, 30);
        signFileField.setBounds(rect);

        // ����һ����ť
        chooseSignBtn = new JButton("ѡ��apkǩ���ļ�");
        frame.add(chooseSignBtn);
        chooseSignBtn.setActionCommand(CHOOSE_SING_CMD);
        chooseSignBtn.setBounds(430 ,50, 150, 30);
        chooseSignBtn.addActionListener(this);

        // ��������ǩ�������keyAlias�������
        JLabel pwdLabel = new JLabel("APKǩ���ļ�����:");
        frame.add(pwdLabel);
        pwdLabel.setBounds(10, 100, 150, 30);


        signPwdField = new JTextField("");
        frame.add(signPwdField);
        Rectangle rect1 = new Rectangle(180, 100, 400, 30);
        signPwdField.setBounds(rect1);

        JLabel aliasLabel = new JLabel("apkǩ���ļ�keyAlias��");
        frame.add(aliasLabel);
        aliasLabel.setBounds(10, 150, 150, 30);

        signAlias = new JTextField();
        frame.add(signAlias);
        Rectangle rect2 = new Rectangle(180, 150, 400, 30);
        signAlias.setBounds(rect2);
    }


    /**
     * ������ʼ�İ�ť
     */
    private void createCommitButton(){
        // ����һ����ť
        JButton commitBtn = new JButton("��ʼ��׮apk");
        frame.add(commitBtn);
        commitBtn.setActionCommand(COMMIT_CMD);
        commitBtn.setBounds(150 ,250, 300, 30);
        commitBtn.addActionListener(this);
    }

    /**
     *  ����չʾ��ʾ��Ϣ���İ�
     */
    private void createTips(){
        tipsLabel = new JLabel("����δ��ʼ!!");
        frame.add(tipsLabel);
        tipsLabel.setBounds(60, 300, 500, 30);
        tipsLabel.setForeground(Color.red);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCmd = e.getActionCommand();
        switch (actionCmd){
            case CHOOSE_CMD:
                mApkPath = chooseFile(new String[]{".apk"});
                if (apkPathField != null){
                    apkPathField.setText(mApkPath);
                }
                break;
            case CHOOSE_SING_CMD:
                mSignFilePath = chooseFile(new String[]{".keystore", ".jks"});
                if (signFileField != null){
                    signFileField.setText(mSignFilePath);
                }
                break;
            case COMMIT_CMD:
                handleCommit();
                break;
        }
    }

    /**
     *  ����commit�߼�
     */
    private void handleCommit(){
        mSignPwd = signPwdField.getText();
        mSignAlias = signAlias.getText();
        if (Util.isStrEmpty(mApkPath)){
            // û������apk�ļ�
            showMessageDialog("�����ѡ���׮apk֮���ټ���", "ע��");
            return;
        }

        if (Util.isStrEmpty(mSignAlias) || Util.isStrEmpty(mSignFilePath) || Util.isStrEmpty(mSignPwd)){
            // û������ǩ���ļ�
            showMessageDialog("����������APKǩ����Ϣ��������Ĭ�ϵ�ǩ����Ϣ", "ע��");
        }

        // ��ʼ��APK���д���
        mTransforManager.injectApk(mApkPath, mSignFilePath, mSignPwd, mSignAlias);
    }

    /**
     *  ѡ��һ���ļ�
     *  @param endSuffix ��׺
     * @return
     */
    private String chooseFile(String[] endSuffix){
        JFileChooser jfc=new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (endSuffix == null || endSuffix.length < 1){
                    return true;
                }

                for (String endSuf : endSuffix){
                    if (f.getName().endsWith(endSuf)){
                        return true;
                    }
                }

                if (f.isDirectory()){
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        jfc.showDialog(new JLabel(), "ѡ��");
        File file=jfc.getSelectedFile();
        if (file == null){
            return "";
        }

        if(file.isDirectory()){
            System.out.println("�ļ���:"+file.getAbsolutePath());
        }else if(file.isFile()){
            System.out.println("�ļ�:"+file.getAbsolutePath());
        }

        return file.getAbsolutePath();
    }
    /*
     * add action listener on exit the dialog
     * */
    private void exit() {
        Object[] options = { CONFIRM, CANCEL };
        JOptionPane warnPane = new JOptionPane("�����˳���?",
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
                options, options[1]);
        JDialog dialog = warnPane.createDialog(this, "��ʾ");
        dialog.setVisible(true);
        Object selectedValue = warnPane.getValue();
        if (selectedValue == null || selectedValue == options[1]) {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // ����ǹؼ�
        } else if (selectedValue == options[0]) {
            System.out.print("exit..");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
    }

    /**
     *  ��ʾ��Ϣ��ʾ��
     */
    private void showMessageDialog(String msg, String title){
        JOptionPane.showMessageDialog(null, msg, title,
                JOptionPane.WARNING_MESSAGE);
    }


    private void addActionListener(){
        //��������ӹرռ�����
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    public static void main(String[] args){
        LaunchView launchView = new LaunchView();
        launchView.createChooseApk();
        launchView.createChooseSignFile();
        launchView.createCommitButton();
        launchView.createTips();
        launchView.setVisible(true);
    }
}