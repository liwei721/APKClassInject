package com.xdja.inject.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static com.android.dx.cf.code.ByteOps.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

/**
 * Created by zlw on 2017/7/6.
 * Email: zlw@xdja.com
 * function: 针对具体业务的一些插入操作的实现，本来计划通过配置实现，但貌似配置不太好实现。
 *
 */
public class AsmDoWork {


    /**
     *  插入记录页面开始加载的方法
     * @param mv
     *
     */
    public static void monitorPageStart(MethodVisitor mv, String className){
        mv.visitLdcInsn(Type.getType(className));
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESTATIC, "com/xdja/monitor/AppMonitor", "monitorPageStart", "(Ljava/lang/String;)V", false);
    }
}
