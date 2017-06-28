package com.xdja.inject;


import java.io.File;
import java.nio.file.Path;

/**
 * Created by zlw on 2017/6/27.
 */
public class Test {
    public static void main(String[] args){
        Path curPath = new File("d:\\\\data\\\\projects\\\\a-project\\\\..\\\\another-project").toPath();
        Path outPath = curPath.resolve("this.jar");
        System.out.println("outPath = " + outPath.normalize().toString());
    }

    private static void TestTwoStr(String str, String str1){
        System.out.println("str" + str);
    }


}
