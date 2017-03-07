package com.gooagoo.test.reflect;

public class TestImpl implements TestInterface
{

    @Override
    public void start()
    {
        System.out.println("start");
    }

    @Override
    public void stop()
    {
        System.out.println("stop");
    }

}
