package com.gooagoo.test.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyTest
{
    public static void main(String[] args)
    {
        TestInterface proxy = (TestInterface) Proxy.newProxyInstance(TestInterface.class.getClassLoader(),
                new Class[] { TestInterface.class },
                new TestProxy(new TestImpl()));
        proxy.start();
        proxy.stop();

        TestInterface proxyNoInstance = (TestInterface) Proxy.newProxyInstance(TestInterface.class.getClassLoader(),
                new Class[] { TestInterface.class },
                new TestProxyNoInstance());
        proxyNoInstance.start();
        proxyNoInstance.stop();
    }

    private static class TestProxy implements InvocationHandler
    {
        private TestInterface testInterface;

        public TestProxy(TestInterface test)
        {
            this.testInterface = test;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().equals("start"))
            {
                System.out.println("test start execute");
            }
            Object obj = method.invoke(this.testInterface, args);
            if (method.getName().equals("stop"))
            {
                System.out.println("test execute stop");
            }

            return obj;
        }

    }

    private static class TestProxyNoInstance implements InvocationHandler
    {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().equals("start"))
            {
                System.out.println("test start execute");
            }
            //没有实例，此行将会不断报错（可能底层会不断重复尝试找到实例）
            //method.invoke(proxy, args);
            if (method.getName().equals("stop"))
            {
                System.out.println("test execute stop");
            }

            return null;
        }

    }
}
