package com.gooagoo.test.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectTest
{
    public static void main(String[] args) throws Exception
    {
        ENumClass cls = ENumClass.INSTANCE;
        System.out.println(cls);
        reflect(cls);
    }

    private static void reflect(ENumClass cls) throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
    {
        Class<?> clazz = cls.getClass();
        System.out.println(clazz);
        Field field = clazz.getDeclaredField("privateVariable");
        field.setAccessible(true);
        // 取出对象内对应字段的值
        String value = (String) field.get(cls);
        System.out.println("对象对应字段的值是：" + value);
        // 设置某个对象内对应字段的值
        field.set(cls, "reflect write");
        value = (String) field.get(cls);
        System.out.println("对象对应字段的值是：" + value);
        field.setAccessible(false);

        Method method = clazz.getDeclaredMethod("privateMethod");
        method.setAccessible(true);
        method.invoke(cls);
        method.setAccessible(false);
        method.invoke(cls);
    }
}
