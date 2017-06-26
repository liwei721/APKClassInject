package com.xdja.inject.transform;

import com.xdja.inject.Constants;
import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.util.InjectUtil;
import com.xdja.inject.util.Log;
import com.xdja.inject.util.Util;
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
    private static Map<String ,Integer> targetClasses = new HashMap<>();
    private static Map<String, List<String>> classExcluds = null;

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
        List<String> dexPaths = new ArrayList<>();
        if (Util.isStrEmpty(zipFolder)){
            return dexPaths;
        }

        File apkunZipFile = new File(zipFolder);
        if (!apkunZipFile.exists()){
            return dexPaths;
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
            return dexPaths;
        }
        /**
         * 遍历dexFile，将dex转成jar
         */
        for (int i = 0; i < dexFiles.length; i++){
            File dexFile = dexFiles[i];
            String dex2jarpath = Util.dex2jarExec(dexFile);
            System.out.println("dex2jar ==  dex2jarpath = " + dex2jarpath);
            dexPaths.add(dex2jarpath);
        }

        return dexPaths;
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
        initTargetClasses(entity);

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
                    String key = shouldModifyClass(className);
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
     * 是否应该去对class文件进行修改
     * @param className
     * @return
     */
    private static String shouldModifyClass(String className){
        if (Util.isStrEmpty(className)) return "";
        for (Map.Entry<String, Integer> entry : targetClasses.entrySet()){
            // matchType 是匹配的类型：正则、通配符、相等
            int matchType = entry.getValue();
            String key = entry.getKey();
            List<String> clssExcs = classExcluds.get(key);
            for (String clsssexclud : clssExcs){
                if (InjectUtil.isPatternMatch(clsssexclud, className)){
                    return "";
                }
            }
            switch (matchType){
                case Constants.MT_FULL:
                    if (className.equals(key)){
                        return key;
                    }
                case Constants.MT_REGEX:
                    if (InjectUtil.regMatch(key, className)){
                        return key;
                    }
                    break;
                case Constants.MT_WILDCARD:
                    if (InjectUtil.wildcardMatchPro(key, className)){
                        return key;
                    }
                    break;
                default:
                    return "";

            }

        }

        return "";

    }


    /***
     * 将要匹配的class及匹配类型存起来，方便后面调用
     * @param entity
     */
    private static void initTargetClasses(SettingEntity entity){
        if (entity == null) return;
        targetClasses.clear();
        classExcluds.clear();
        for (SettingEntity.InjectSettingsBean settingsBean : entity.getInjectSettings()){
            // 根据className的值来判断
            int type = InjectUtil.getMatchTypeByValue(settingsBean.getClassName());
            targetClasses.put(settingsBean.getClassName(), type);

            // 将className，对应的classExclude放到map中
            classExcluds.put(settingsBean.getClassName(), settingsBean.getClassExclude());
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

}
