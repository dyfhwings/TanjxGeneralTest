package com.tanjx.test;

public class StringTest
{

    public static void main(String[] args)
    {
        System.out.println(1L << 65L);
        //查看jdk版本
        System.out.println(System.getProperty("java.version") + ""
                + " x " + System.getProperty("sun.arch.data.model"));

        /*
         * 字符串字面量到底是什么时候放入到string pool中的？
         * "Goodmorning"会在编译期被放入静态常量池，在class加载后进入运行时常量池，但何时进入字符串池？
         */
        /*        String strObj = new StringBuilder("Good").append("morning").toString();
                strObj.intern();
                String s4 = "Goodmorning";
                System.out.println(strObj == s4);*/

        StringTest st = new StringTest();
        st.test();
    }

    public void test()
    {
        String strObj = new StringBuilder("Good").append("morning").toString();
        String literal = "Goodmorning";
        //System.out.println("literal.intern() == literal :" + (literal.intern() == literal));
        //strObj.intern();
        System.out.println("strObj.intern() == strObj :" + (strObj.intern() == strObj));
        System.out.println("strObj.intern() == literal :" + (strObj.intern() == literal));
        System.out.println("strObj == literal :" + (strObj == literal));
    }
}
