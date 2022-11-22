package com.platform.bot.utils;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class MyFileManager extends ForwardingJavaFileManager {

    private MyJavaClassFileObject javaClassObject;

    protected MyFileManager(StandardJavaFileManager standardJavaFileManager) {
        super(standardJavaFileManager);
    }

    public MyJavaClassFileObject getJavaClassObject() {
        return javaClassObject;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        this.javaClassObject = new MyJavaClassFileObject(className, kind);
        return javaClassObject;
    }
}