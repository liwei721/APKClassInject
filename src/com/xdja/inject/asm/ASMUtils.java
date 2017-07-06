package com.xdja.inject.asm;

import com.xdja.inject.Constants;
import com.xdja.inject.util.Util;
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
     *  访问方法体内部的field
     * @param mv
     * @param className
     * @param fieldName
     * @param typeSign
     */
    public static void setClassStaticFieldValue(MethodVisitor mv, String className, String fieldName, String typeSign) {
        mv.visitFieldInsn(PUTSTATIC, className, fieldName, typeSign);
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
     *  在一个方法中添加静态方法的调用
     *
     * @param mv
     * @param methodName
     * @param methodDesc
     * @param owner
     */
    // TODO: 2017/6/29  考虑其他种类的调用。
    public static void addStaticMethodToMethod(MethodVisitor mv, String methodName, String methodDesc, String owner){
        mv.visitMethodInsn(INVOKESTATIC, owner, methodName, methodDesc, false);
    }


    /**
     *  将对应的String转成class
     * @param str
     * @return
     */
    public static Class formatStrToClass(String str){
        if (Util.isStrEmpty(str)){
            return null;
        }

        if (str.equalsIgnoreCase("String")){
            return String.class;
        }else if (str.equalsIgnoreCase("Boolean")){
            return Boolean.class;
        }else if (str.equalsIgnoreCase("Int")){
            return Integer.class;
        }else if (str.equalsIgnoreCase("char")){
            return char.class;
        }else if (str.equalsIgnoreCase("byte")){
            return Byte.class;
        }else if (str.equalsIgnoreCase("Short")){
            return short.class;
        }else if (str.equalsIgnoreCase("Float")){
            return Float.class;
        }else if (str.equalsIgnoreCase("Double")){
            return Double.class;
        }else if (str.equalsIgnoreCase("long")){
            return Long.class;
        }else if (str.contains(".")){
            // 某个引用类型
            try {
                Class c = Class.forName(str);
                return c.getClass();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
