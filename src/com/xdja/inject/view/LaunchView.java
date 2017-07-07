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
    private final static String CONFIRM = "确定";
    private final static String CANCEL = "取消";

    // 记录输入的数据
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
                updateTips("正在开始插桩任务！！");
            }

            @Override
            public void upzipApk(int i, String path) {
                updateTips("执行第 " + i + " 步：解压apk！！，解压之后路径：" + path);
                LogUtil.info("执行第 " + i + " 步：解压apk！！，解压之后路径：" + path);
            }

            @Override
            public void dex2jar(int i, String path) {
                LogUtil.info("执行第 " + i + " 步：将dex转成jar！！");
                updateTips("执行第 " + i + " 步：将dex转成jar！！");
            }

            @Override
            public void deleteMeta(int i, String var) {
                LogUtil.info("执行第 " + i + " 步：删除apk中原有的签名信息！！");
                updateTips("执行第 " + i + " 步：删除apk中原有的签名信息！！");
            }

            @Override
            public void dexToapk(int i, String var) {
                LogUtil.info("执行第 " + i + " 步：将插桩后的dex push到apk中！！");
                updateTips("执行第 " + i + " 步：将插桩后的dex push到apk中！！");
            }

            @Override
            public void codeToJar(int i, String var) {
                LogUtil.info("执行第 " + i + " 步：将工具类添加到jar中：" + var);
                updateTips("执行第 " + i + " 步：将工具类添加到jar中：" + var);
            }

            @Override
            public void apkSign(int i, String var) {
                LogUtil.info("执行第 " + i + " 步：对插桩后apk进行签名！！签名后的apk地址：" + var);
                updateTips("执行第 " + i + " 步：对插桩后apk进行签名！！签名后的apk地址：" + var);
            }

            @Override
            public void jar2dex(int i, String var) {
                LogUtil.info("执行第 " + i + " 步：将jar转成dex：" + var);
                updateTips("执行第 " + i + " 步：将jar转成dex：" + var);
            }

            @Override
            public void finish(String apkPath) {
                isTaskRunning = false;
                refreshCommitBtnState(isTaskRunning);
                updateTips("插桩任务完成！！请在 ：" + apkPath + "查看");
            }

            @Override
            public void showError(String errorMsg) {
                isTaskRunning = false;
                refreshCommitBtnState(isTaskRunning);

                updateTips("运行出错了，错误信息是：" + errorMsg);
            }

        };
    }

    /**
     *  用于更新提示信息
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
     *  刷新正在执行任务时的状态
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
     *  选择apk文件
     */
    private void createChooseApk(){
        // 必须显式设置布局格式为空，否则不会按照我们设置好的格式布局
        frame.setLayout(null);

        // 创建一个可以显示选择路径的文本框
        apkPathField = new JTextField();
        apkPathField.setEnabled(false);
        frame.add(apkPathField);
        Rectangle rect = new Rectangle(10, 10, 400, 30);
        apkPathField.setBounds(rect);

        // 创建一个按钮
        chooseBtn = new JButton("选择要插桩apk文件");
        frame.add(chooseBtn);
        chooseBtn.setActionCommand(CHOOSE_CMD);
        chooseBtn.setBounds(430 ,10, 150, 30);
        chooseBtn.addActionListener(this);
    }

    /**
     *  创建选择apk签名文件的内容
     */
    private void createChooseSignFile(){
        // 创建一个可以显示选择路径的文本框
        signFileField = new JTextField();
        signFileField.setEnabled(false);
        frame.add(signFileField);
        Rectangle rect = new Rectangle(10, 50, 400, 30);
        signFileField.setBounds(rect);

        // 创建一个按钮
        chooseSignBtn = new JButton("选择apk签名文件");
        frame.add(chooseSignBtn);
        chooseSignBtn.setActionCommand(CHOOSE_SING_CMD);
        chooseSignBtn.setBounds(430 ,50, 150, 30);
        chooseSignBtn.addActionListener(this);

        // 创建输入签名密码和keyAlias的输入框
        JLabel pwdLabel = new JLabel("APK签名文件密码:");
        frame.add(pwdLabel);
        pwdLabel.setBounds(10, 100, 150, 30);


        signPwdField = new JTextField("");
        frame.add(signPwdField);
        Rectangle rect1 = new Rectangle(180, 100, 400, 30);
        signPwdField.setBounds(rect1);

        JLabel aliasLabel = new JLabel("apk签名文件keyAlias：");
        frame.add(aliasLabel);
        aliasLabel.setBounds(10, 150, 150, 30);

        signAlias = new JTextField();
        frame.add(signAlias);
        Rectangle rect2 = new Rectangle(180, 150, 400, 30);
        signAlias.setBounds(rect2);
    }


    /**
     * 创建开始的按钮
     */
    private void createCommitButton(){
        // 创建一个按钮
        JButton commitBtn = new JButton("开始插桩apk");
        frame.add(commitBtn);
        commitBtn.setActionCommand(COMMIT_CMD);
        commitBtn.setBounds(150 ,250, 300, 30);
        commitBtn.addActionListener(this);
    }

    /**
     *  创建展示提示信息的文案
     */
    private void createTips(){
        tipsLabel = new JLabel("任务还未开始!!");
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
     *  处理commit逻辑
     */
    private void handleCommit(){
        mSignPwd = signPwdField.getText();
        mSignAlias = signAlias.getText();
        if (Util.isStrEmpty(mApkPath)){
            // 没有输入apk文件
            showMessageDialog("请务必选择插桩apk之后再继续", "注意");
            return;
        }

        if (Util.isStrEmpty(mSignAlias) || Util.isStrEmpty(mSignFilePath) || Util.isStrEmpty(mSignPwd)){
            // 没有输入签名文件
            showMessageDialog("请输入最终APK签名信息，否则将用默认的签名信息", "注意");
        }

        // 开始对APK进行处理
        mTransforManager.injectApk(mApkPath, mSignFilePath, mSignPwd, mSignAlias);
    }

    /**
     *  选择一个文件
     *  @param endSuffix 后缀
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
        jfc.showDialog(new JLabel(), "选择");
        File file=jfc.getSelectedFile();
        if (file == null){
            return "";
        }

        if(file.isDirectory()){
            System.out.println("文件夹:"+file.getAbsolutePath());
        }else if(file.isFile()){
            System.out.println("文件:"+file.getAbsolutePath());
        }

        return file.getAbsolutePath();
    }
    /*
     * add action listener on exit the dialog
     * */
    private void exit() {
        Object[] options = { CONFIRM, CANCEL };
        JOptionPane warnPane = new JOptionPane("真想退出吗?",
                JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
                options, options[1]);
        JDialog dialog = warnPane.createDialog(this, "提示");
        dialog.setVisible(true);
        Object selectedValue = warnPane.getValue();
        if (selectedValue == null || selectedValue == options[1]) {
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 这个是关键
        } else if (selectedValue == options[0]) {
            System.out.print("exit..");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
        }
    }

    /**
     *  显示信息提示框
     */
    private void showMessageDialog(String msg, String title){
        JOptionPane.showMessageDialog(null, msg, title,
                JOptionPane.WARNING_MESSAGE);
    }


    private void addActionListener(){
        //主窗口添加关闭监听器
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