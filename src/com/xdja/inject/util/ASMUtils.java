package com.xdja.inject.util;

import com.xdja.inject.Constants;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Created by zlw on 2017/6/27.
 *  主要用于对ASM字节码的一些操作
 */
public class ASMUtils {

    /**
     *  添加一个静态的field
     * @param cv
     * @param fieldName
     * @param typeClass
     */
    public static void addClassStaticField(ClassVisitor cv, String fieldName, Class<?> typeClass){
        cv.visitField(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, fieldName, Type.getDescriptor(typeClass), null, null);

    }

    /**
     *  添加System.out.println。
     * @param mv
     * @param msg
     */
    public static void addSystemOut(MethodVisitor mv, String msg){
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn(msg);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Ljava/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    /**
     * 添加Android中的Log.d
     * @param mv
     * @param tag
     * @param msg
     */
    public static void addAndroidLogD(MethodVisitor mv, String tag, String msg){
        if (Util.isStrEmpty(tag)) tag = Constants.TAG;
        mv.visitLdcInsn(tag);
        mv.visitLdcInsn(msg);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "android.util.LogUtil", "d", "(Ljava/lang/String;Ljava/lang/String;)V", false);
    }
}
