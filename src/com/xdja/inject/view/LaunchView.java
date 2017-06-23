package com.xdja.inject.view;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Created by zlw on 2017/6/23.
 */
public class LaunchView extends JFrame implements ActionListener{
    JButton chooseBtn = null;
    private final static String CHOOSE_CMD = "chooseFile";
    private final static String CONFIRM = "确定";
    private final static String CANCEL = "取消";

    /**
     *  选择apk文件
     */
    public void createChooseApk(){
        chooseBtn = new JButton("选择apk文件");

        chooseBtn.setActionCommand(CHOOSE_CMD);
        // 用jpanel包裹btn
        JPanel jPanel = new JPanel();
        jPanel.add(chooseBtn);
        jPanel.setBounds(0,0, 600,60);
        // panel添加到面板上
        this.add(jPanel);
        this.setBounds(400, 200, 600, 300);
        this.setVisible(true);
        chooseBtn.addActionListener(this);
        addActionListener();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCmd = e.getActionCommand();
        switch (actionCmd){
            case CHOOSE_CMD:
                JFileChooser jfc=new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
                jfc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        if (f.getName().endsWith(".apk")){
                            return true;
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
                    break;
                }

                if(file.isDirectory()){
                    System.out.println("文件夹:"+file.getAbsolutePath());
                }else if(file.isFile()){
                    System.out.println("文件:"+file.getAbsolutePath());
                }
                break;
        }
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
    }
}
