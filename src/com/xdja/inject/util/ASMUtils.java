package com.xdja.inject.util;

import com.xdja.inject.Constants;
import org.objectweb.asm.*;

import static com.android.dx.cf.code.ByteOps.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.*;

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
        cv.visitField(ACC_PUBLIC|Opcodes.ACC_STATIC, fieldName, Type.getDescriptor(typeClass), null, null);

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

    /**
     *  添加一般的Field
     * @param cv
     * @param fieldName
     * @param typeClass
     */
    public static void addClassField(ClassVisitor cv, String fieldName, Class<?> typeClass){
        cv.visitField(ACC_PRIVATE, fieldName, Type.getDescriptor(typeClass), null, null);
    }

    /**
     *  添加一个onWindowFocusChanged 方法
     * @param cv
     * @param className
     */
    public static void addonWindowFocusChangedMethod(ClassVisitor cv, String className, String classTypeDesc, String fieldName){
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "onWindowFocusChanged", "(Z)V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(19, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, "android/support/v7/app/AppCompatActivity", "onWindowFocusChanged", "(Z)V", false);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLineNumber(20, l1);
        mv.visitVarInsn(ILOAD, 1);
        Label l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(21, l3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className, fieldName, "J");
        mv.visitMethodInsn(INVOKESTATIC, "android/os/SystemClock", "elapsedRealtime", "()J", false);
        mv.visitLdcInsn(Type.getType(classTypeDesc));
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESTATIC, className, "monitorPageLoaded", "(JJLjava/lang/String;)V", false);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLineNumber(22, l4);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(LCONST_0);
        mv.visitFieldInsn(PUTFIELD, className, fieldName, "J");
        mv.visitLabel(l2);
        mv.visitLineNumber(24, l2);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(RETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("this", classTypeDesc, null, l0, l5, 0);
        mv.visitLocalVariable("hasFocus", "Z", null, l0, l5, 1);
        mv.visitMaxs(5, 2);
        mv.visitEnd();
    }
}
