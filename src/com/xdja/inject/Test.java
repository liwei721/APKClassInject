package com.xdja.inject;


import com.xdja.inject.util.FilesUtil;
import com.xdja.inject.util.Util;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by zlw on 2017/6/27.
 */
public class Test {
    public static void main(String[] args){
        FilesUtil.addFileToJar(FilesUtil.getResourcePath() + File.separator + "AppMonitor.java", FilesUtil.getTempDirPath() + File.separator + "classes.jar");
    }

    private static void TestTwoStr(String str, String str1){
        System.out.println("str" + str);
    }


}
