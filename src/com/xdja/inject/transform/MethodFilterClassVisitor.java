package com.xdja.inject.transform;

import com.xdja.inject.setting.SettingEntity;
import com.xdja.inject.util.InjectUtil;
import com.xdja.inject.util.LogUtil;
import com.xdja.inject.util.Util;
import org.objectweb.asm.*;

import java.util.List;

/**
 * Created by zlw on 2017/6/26.
 */
public class MethodFilterClassVisitor extends ClassVisitor implements Opcodes{
    private List<SettingEntity.InjectSettingsBean> settingsBeans;
    public MethodFilterClassVisitor(int api) {
        super(api);
    }

    public MethodFilterClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    public MethodFilterClassVisitor(ClassVisitor cv, List<SettingEntity.InjectSettingsBean> injectList){
        super(Opcodes.ASM4, cv);
        settingsBeans = injectList;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        LogUtil.logEach("* visit *", LogUtil.accCode2String(access), name, signature, superName, interfaces);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        LogUtil.logEach("* visitSource *", source, debug);
        super.visitSource(source, debug);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        LogUtil.logEach("* visitOuterClass *", owner, name, desc);
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        LogUtil.logEach("* visitAnnotation *", desc, visible);
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        LogUtil.logEach("* visitAttribute *", attr, attr.type);
        super.visitAttribute(attr);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        LogUtil.logEach("* visitInnerClass *", name, outerName, innerName, LogUtil.accCode2String(access));
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        LogUtil.logEach("* visitField *", LogUtil.accCode2String(access), name, desc, signature, value);
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        LogUtil.logEach("* visitMethod *");
        MethodVisitor myMv = null;
        for (SettingEntity.InjectSettingsBean settingsBean : settingsBeans){
            String className = settingsBean.getClassName();
           String methodName = settingsBean.getMethodName();
           String methodDesc = settingsBean.getMethodDesc();
           List<String> methodExclude = (List<String>) settingsBean.getMethodExclude();
           if (InjectUtil.isPatternMatch(methodName, name)){
                // 判断哪些方法是不执行的。
               if (!Util.isListEmpty(methodExclude)){
                   for (String methodExc : methodExclude){
                       if (!InjectUtil.isPatternMatch(methodExc, name)){
                           myMv = new MethodInjectAdapter(cv.visitMethod(access, name, desc, signature, exceptions), className, methodName, methodDesc);
                       }
                   }
               }
           }
        }

        if (myMv != null){
            return myMv;
        }

        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        LogUtil.logEach("* visitEnd *");
    }
}
