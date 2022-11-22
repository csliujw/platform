package com.platform.bot.utils;

public class MyClassLoader extends ClassLoader {

    public Class loadClass(String fullName, MyJavaClassFileObject javaClassObject) {
        byte[] classData = javaClassObject.getBytes();
        return this.defineClass(fullName, classData, 0, classData.length);
    }
}