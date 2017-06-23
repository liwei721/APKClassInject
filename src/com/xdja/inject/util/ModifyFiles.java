package com.xdja.inject.util

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bryansharp
 * Project: FirstGradle
 * introduction:
 */
public class ModifyFiles {


    public static void modify(Map<String, Map<String, Object>> taskMap) {
        final File tempDir = DataHelper.ext.injectClassTempDir;
        taskMap.entrySet().forEach({
                entry ->
                        String path = entry.getKey();
        Map<String, Object> map = entry.getValue();
        Util.initTargetClasses(map)
        File targetFile = new File(path)
        def type = isSupportFile(targetFile)
        switch (type) {
            case PluginConst.TY_AAR:
                modifyAar(targetFile, map);
                break;
            case PluginConst.TY_JAR:
                File outJar = modifyJar(targetFile, map, tempDir, false);
                outJar.renameTo(new File(DataHelper.ext.injectClassDir, outJar.getName()))
                break;
        }
        })
    }

    public static File unzipEntryToTemp(ZipEntry element, ZipFile zipFile) {
        def stream = zipFile.getInputStream(element);
        def array = IOUtils.toByteArray(stream);
        String hex = DigestUtils.md5Hex(element.getName());
        final File tempDir = DataHelper.ext.injectClassTempDir;
        File targetFile = new File(tempDir, hex + ".jar");
        if (targetFile.exists()) {
            targetFile.delete()
        }
        new FileOutputStream(targetFile).write(array)
        return targetFile
    }

    /**
     *  在jar中植入自己的代码
     * @param jarFile
     * @param modifyMatchMaps
     * @param tempDir
     * @param nameHex
     * @return
     */
    public
    static File modifyJar(File jarFile, Map<String, Object> modifyMatchMaps, File tempDir, boolean nameHex) {
        /**
         * 读取原jar
         */
        def file = new JarFile(jarFile);
        /** 设置输出到的jar */
        def hexName = "";
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8);
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        Log.info("ModifyFiles ===== modifyJar ========= outputJar : ${outputJar.absolutePath}")
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
        Enumeration enumeration = file.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
            InputStream inputStream = file.getInputStream(jarEntry);

            String entryName = jarEntry.getName();
            String className

            ZipEntry zipEntry = new ZipEntry(entryName);

            jarOutputStream.putNextEntry(zipEntry);

            byte[] modifiedClassBytes = null;
            byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
            if (entryName.endsWith(".class")) {
                className = Util.path2Classname(entryName)
                // 这里判断是否对class进行修改
                String key = Util.shouldModifyClass(className)
                if (modifyMatchMaps != null && key != null) {
                    // 这里真正开始对class进行修改
                    modifiedClassBytes = ModifyClassUtil1.modifyClasses(className, sourceClassBytes, modifyMatchMaps.get(key));
                }
            }

            // 最后将修改后（或原byte）写回到文件中
            if (modifiedClassBytes == null) {
                jarOutputStream.write(sourceClassBytes);
            } else {
                jarOutputStream.write(modifiedClassBytes);
            }
            jarOutputStream.closeEntry();
        }
//            Log.info("${hexName} is modified");
        jarOutputStream.close();
        file.close();
        return outputJar;
    }

    public static void modifyAar(File targetFile, Map<String, Object> map) {
        final File injectClassDir = DataHelper.ext.injectClassDir;
        final File tempDir = DataHelper.ext.injectClassTempDir;
        ZipFile zipFile = new ZipFile(targetFile);
        Enumeration<ZipEntry> entries = zipFile.entries();

        def outputAar = new File(injectClassDir, targetFile.name)
        if (outputAar.exists()) {
            outputAar.delete()
        }

        ZipOutputStream outputAarStream = new ZipOutputStream(new FileOutputStream(outputAar))
        while (entries.hasMoreElements()) {
            ZipEntry element = entries.nextElement();
            def name = element.getName();
            ZipEntry zipEntry = new ZipEntry(name);

            outputAarStream.putNextEntry(zipEntry);
            Log.info("name is ${name}")
            if (name.endsWith(".jar")) {
                File innerJar = unzipEntryToTemp(element, zipFile);
                def outJar = modifyJar(innerJar, map, tempDir, true);
                outputAarStream.write(IOUtils.toByteArray(new FileInputStream(outJar)))
            } else {
                def stream = zipFile.getInputStream(element)
                byte[] array = IOUtils.toByteArray(stream)
                Log.info("length is ${array.length}")
                if (array != null) {
                    outputAarStream.write(array)
                }
            }
            outputAarStream.closeEntry();
        }
        zipFile.close()
        outputAarStream.close()
    }

    public static int isSupportFile(File targetFile) {
        def name = targetFile.getName();
        if (name.endsWith(".jar")) {
            return PluginConst.TY_JAR;
        } else if (name.endsWith(".aar")) {
            return PluginConst.TY_AAR;
        }
        return -1;
    }
}