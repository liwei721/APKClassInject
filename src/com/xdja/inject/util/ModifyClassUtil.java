package com.xdja.inject.util;
import com.xdja.inject.Constants;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by bryansharp(bsp0911932@163.com) on 2016/5/10.
 *
 * @author bryansharp
 * Project: FirstGradle
 * introduction:
 */
public class ModifyClassUtil {

    public static byte[] modifyClasses(String className, byte[] srcByteCode, Object container) {
        List<Map<String, Object>> methodMatchMaps = getList(container);
        byte[] classBytesCode = null;
        if (methodMatchMaps != null && methodMatchMaps.size() > 0) {
            try {
                Log.info("====start modifying ${className}====");
                classBytesCode = modifyClass(srcByteCode, methodMatchMaps);
                Log.info("====revisit modified ${className}====");
                onlyVisitClassMethod(classBytesCode, methodMatchMaps);
                Log.info("====finish modifying ${className}====");
                return classBytesCode;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (classBytesCode == null) {
            classBytesCode = srcByteCode;
        }
        return classBytesCode;
    }

    static List<Map<String, Object>> getList(Object container) {
        if (container instanceof List) {
            return (List<Map<String, Object>>) container;
        } else if (container instanceof Map) {
            Map<String, List<Map<String, Object>>> maps = (Map<String, List<Map<String, Object>>>) container;
            return maps.get(Constants.KEY_MODIFYMETHODS);
        }
        return null;
    }

    private
    static byte[] modifyClass(byte[] srcClass, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        // 创建操作字节流值对象，ClassWriter.COMPUTE_MAXS:表示自动计算栈大小
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor adapter = new MethodFilterClassVisitor(classWriter, modifyMatchMaps);
        // 读取字节码的流对象
        ClassReader cr = new ClassReader(srcClass);
        //cr.accept(visitor, ClassReader.SKIP_DEBUG);
        //接受一个ClassVisitor子类进行字节码修改
        cr.accept(adapter, 0);
        return classWriter.toByteArray();
    }

    private
    static void onlyVisitClassMethod(byte[] srcClass, List<Map<String, Object>> modifyMatchMaps) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        MethodFilterClassVisitor visitor = new MethodFilterClassVisitor(classWriter, modifyMatchMaps);
        visitor.onlyVisit = true;
        ClassReader cr = new ClassReader(srcClass);
        cr.accept(visitor, 0);
    }

    /**
     *  这个类主要实现字节码的修改
     *  Visitor 是访问者模式
     *
     */
    static class MethodFilterClassVisitor extends ClassVisitor implements Opcodes {
        //        private String className;
        private List<Map<String, Object>> methodMatchMaps;
        public boolean onlyVisit = false;

        public MethodFilterClassVisitor(
                final ClassVisitor cv, List<Map<String, Object>> methodMatchMaps) {
            super(Opcodes.ASM4, cv);
//            this.className = className;
            this.methodMatchMaps = methodMatchMaps;
        }

        @Override
        public void visitEnd() {
            Log.logEach("* visitEnd *");
            super.visitEnd();
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            Log.logEach("* visitAttribute *", attribute, attribute.type);
            super.visitAttribute(attribute);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            Log.logEach("* visitAnnotation *", desc, visible);
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public void visitInnerClass(String name, String outerName,
                                    String innerName, int access) {
            Log.logEach("* visitInnerClass *", name, outerName, innerName, Log.accCode2String(access));
            super.visitInnerClass(name, outerName, innerName, access);
        }

        @Override
        public void visitOuterClass(String owner, String name, String desc) {
            Log.logEach("* visitOuterClass *", owner, name, desc);
            super.visitOuterClass(owner, name, desc);
        }

        @Override
        public void visitSource(String source, String debug) {
            Log.logEach("* visitSource *", source, debug);
            super.visitSource(source, debug);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            Log.logEach("* visitField *", Log.accCode2String(access), name, desc, signature, value);
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            Log.logEach("* visit *", Log.accCode2String(access), name, signature, superName, interfaces);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        /**
         *  扫描每个方法都会进入这里
         *
         * @param access
         * @param name
         * @param desc
         * @param signature
         * @param exceptions
         * @return
         */
        @Override
        public MethodVisitor visitMethod(int access, String name,
                                         String desc, String signature, String[] exceptions) {
            MethodVisitor myMv = null;
            if (!onlyVisit) {
                Log.logEach("* visitMethod *", Log.accCode2String(access), name, desc, signature, exceptions);
            }

            for (Map<String, Object> map : methodMatchMaps){
                for (String key : map.keySet()){
                    String metName = (String) map.get(Constants.KEY_METHODNAME);
                    String metMatchType = (String) map.get(Constants.KEY_METHODMATCHTYPE);
                    String methodDesc = (String) map.get(Constants.KEY_METHODDESC);
                    if (Util.isPatternMatch(metName, metMatchType, name)){

                    }
                }
            }
            methodMatchMaps.each {
                if (Util.isPatternMatch(metName, metMatchType, name)) {
                    Closure visit = map.get(PluginConst.KEY_ADAPTER);
                    if (visit != null) {
                        //methodDesc 不设置，为空，即代表对methodDesc不限制
                        if (methodDesc != null) {
                            if (Util.isPatternMatch(methodDesc, metMatchType, desc)) {
                                if (onlyVisit) {
                                    myMv = new MethodLogAdapter(cv.visitMethod(access, name, desc, signature, exceptions));
                                } else {
                                    try {
                                        myMv = visit(cv, access, name, desc, signature, exceptions);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        myMv = null;
                                    }
                                }
                            }
                        } else {
                            try {
                                myMv = visit(cv, access, name, desc, signature, exceptions);
                            } catch (Exception e) {
                                e.printStackTrace();
                                myMv = null
                            }
                        }
                    }
                }
            }
            if (myMv != null) {
                if (onlyVisit) {
                    Log.logEach("* revisitMethod *", Log.accCode2String(access), name, desc, signature);
                }
                return myMv;
            } else {
                return cv.visitMethod(access, name, desc, signature, exceptions);
            }
        }

    }

}