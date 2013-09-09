package com.tw.go.plugin.material.artifactrepository.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static void setField(Object o, String name, Object value) {
        try {
            field(o, name).set(o, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void setStaticField(Class kls, String name, Object value) {
        try {
            Field field = kls.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Object getStaticField(Class kls, String name) {
        try {
            Field field = kls.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Object o, String name) {
        try {
            return field(o, name).get(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Method method(String name, Class klass) throws NoSuchFieldException {
        if (klass == null) {
            return null;
        }
        Method[] fields = klass.getDeclaredMethods();
        for (Method field : fields) {
            if (field.getName().equals(name)) {
                field.setAccessible(true);
                return field;
            }
        }
        return method(name, klass.getSuperclass());
    }

    public static Object invoke(Object o, String method, Object... args) throws Exception {
        Class[] argTypes = new Class[args.length];


        for(int i = 0; i < args.length; i++) {
            argTypes[i] = args.getClass();
        }
        Method mthd = method(method, o.getClass());
        mthd.setAccessible(true);
        return mthd.invoke(o, args);
    }

    private static Field field(Object o, String name) throws NoSuchFieldException {
        return field(name, o.getClass());
    }

    private static Field field(String name, Class klass) throws NoSuchFieldException {
        if (klass == null) {
            return null;
        }
        Field[] fields = klass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(name)) {
                field.setAccessible(true);
                return field;
            }
        }
        return field(name, klass.getSuperclass());
    }
}

