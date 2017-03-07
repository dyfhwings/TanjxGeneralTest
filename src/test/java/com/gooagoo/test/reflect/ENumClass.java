package com.gooagoo.test.reflect;

public enum ENumClass
{
    INSTANCE;
    private String privateVariable = "private variable";

    private void privateMethod()
    {
        System.out.println("private method execute");
    }

    public void publicMethod()
    {
        System.out.println("public method execute");
    }
}
