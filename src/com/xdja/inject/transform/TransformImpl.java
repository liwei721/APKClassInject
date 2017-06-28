package com.xdja.inject.transform;

import com.xdja.inject.Constants;
import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.util.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zlw on 2017/6/26.
 * 用于实现主要逻辑
 */
public class TransformImpl {

    private static final int BUFFER = 1024;

    /**
     *  将apk解压到临时目录中
     * @param apkPath apk的路径
     * @return 返回的是解压之后的临时目录
     */
    public static String upzipApk(String apkPath){
        if (Util.isStrEmpty(apkPath)){
            return "";
        }

        File apkFile = new File(apkPath);
        if (!apkFile.exists()){
            return "";
        }

        String apkFileName = apkFile.getName();

        ZipFile zipFile = null;
        String tempDir = System.getProperty("user.dir") + "/temp/" + apkFileName.substring(0, apkFileName.length() - 4) + "/";
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration emu = zipFile.entries();
            while (emu.hasMoreElements()){
                ZipEntry zipEntry = (ZipEntry) emu.nextElement();

                // 判断zipEntry的类型
                if (zipEntry.isDirectory()){
                    new File(tempDir + zipEntry.getName()).mkdirs();
                    continue;
                }

                bis  = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                File targetFile = new File(tempDir + zipEntry.getName());
                File parent = targetFile.getParentFile();
                if (parent != null && !parent.exists()){
                    parent.mkdirs();
                }

                //将Entity写到temp dir中
                FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                bos = new BufferedOutputStream(fileOutputStream, BUFFER);
                byte [] buf = new byte[BUFFER];
                int len = 0;
                while((len=bis.read(buf,0,BUFFER))!=-1){
                    fileOutputStream.write(buf,0,len);
                }
                bos.flush();
            }
            return tempDir;
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
     *  将dex转成jar
     * @param zipFolder  解压apk之后的folder
     * @return  生成的dex路径
     */
    public static List<String> dex2jar(String zipFolder){
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

    /**
     *  对jarFile进行插桩操作
     * @param jarFiles
     */
    public static void modifyJarFile(List<String> jarFiles, SettingEntity entity, boolean nameHex) throws IOException {
        // 1. 对jar进行解压，且判断是否需要插入代码，根据配置文件。
        if (Util.isListEmpty(jarFiles) || entity == null){
            return;
        }

        // 对配置中的classMatchType进行处理。
        InjectUtil.initTargetClasses(entity);

        for (String jarFileStr : jarFiles){
            // jar的File
            File jFile = new File(jarFileStr);

            // 读取原来的jar
            JarFile jarFile = new JarFile(jarFileStr);
            // 设置输出的jar
            String hexName = "";
            if (nameHex){
                hexName = DigestUtils.md5Hex(jFile.getAbsolutePath()).substring(0, 8);
            }

            String tempDir = jFile.getParent() + File.separator + "\\jartempDir\\";
            File tempdirFile = new File(tempDir);
            if (!tempdirFile.exists()){
                tempdirFile.mkdirs();
            }

            File outputJar = new File(tempdirFile, hexName + jFile.getName());
            Log.info("ModifyFiles ===== modifyJar ========= outputJar ==" + outputJar.getAbsolutePath());
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar));
            Enumeration enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()){
                // 读取jar中的文件
                JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                String entryName = jarEntry.getName();

                // 设置要写的jar文件
                ZipEntry zipEntry = new ZipEntry(entryName);
                jarOutputStream.putNextEntry(zipEntry);

                // 修改字节码
                byte[] modifiedClassBytes = null; // 修改过后的字节码
                if (entryName.endsWith(".class")){
                    String className = InjectUtil.path2Classname(entryName);
                    // 这里判断是否对class进行修改,返回的key是配置的className
                    String key = InjectUtil.shouldModifyClass(className);
                    byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);

                    if (!Util.isStrEmpty(key)){
                        // 真正开始去modify class
                        modifiedClassBytes = modifyClasses(className,sourceClassBytes, entity);
                    }

                    if (modifiedClassBytes == null){
                        jarOutputStream.write(sourceClassBytes);
                    }else {
                        jarOutputStream.write(modifiedClassBytes);
                    }
                    jarOutputStream.closeEntry();
                }
            }
            jarOutputStream.close();
            jarFile.close();

        }

    }

    /**
     *  这里实现对字节码的注入
     * @param className
     * @param sourceByteCode
     * @param entity
     * @return
     */
    private static byte[] modifyClasses(String className, byte[]sourceByteCode, SettingEntity entity){
        if (Util.isStrEmpty(className) || sourceByteCode == null || sourceByteCode.length < 1) return null;
        if (Util.isListEmpty(entity.getInjectSettings())) return null;

        Log.info("==== start modifying " + className + "======");
        byte[] classBytesCode = modifyClass(sourceByteCode, entity.getInjectSettings());
        Log.info("====revisit modified "+className+"====");

        return classBytesCode;
    }

    /**
     *  对class文件进行修改。
     * @param srcBytes
     * @param entityBeans
     * @return
     */
    private static byte[] modifyClass(byte[] srcBytes, List<SettingEntity.InjectSettingsBean> entityBeans){
        // 创建操作字节流值对象，ClassWriter.COMPUTE_MAXS:表示自动计算栈大小
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor methodFilterAdapter = new MethodFilterClassVisitor(classWriter,entityBeans);
        // 读取字节码的流对象
        ClassReader cr = new ClassReader(srcBytes);
        cr.accept(methodFilterAdapter, 0);
        return classWriter.toByteArray();
    }

    /**
     * 删除不用的目录
     * @param tempDir
     */
    public static void deleteTempDir(String tempDir){
        FilesUtil.deleteDirectory(tempDir);
    }
}
