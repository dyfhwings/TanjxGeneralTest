package com.tanjx.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Test
{

    private static List<Test> objs = new ArrayList<Test>();

    static
    {
        objs.add(new Test(Test.S_NAME, Test.NAME, Test.COUNT));
        objs.add(new Test(Test.S_NAME, Test.NAME, Test.COUNT));
    }

    private static final String S_NAME = "aaa";
    private static final String NAME = new String("aaa");
    private static final Long COUNT = 1l;

    private String name;
    private String title;
    private Long count;

    public Test(String name, String title, Long count)
    {
        this.name = name;
        this.title = title;
        this.count = count;
    }

    public static void main(String[] args) throws Exception
    {
        Date start = new Date();
        Thread.sleep(1000);
        Date end = new Date();
        long diff = end.getTime() - start.getTime();
        System.out.println(diff);
    }

    @Override
    public String toString()
    {
        return "Test [name=" + this.name + ",title=" + this.title + ",count=" + this.count + "]";
    }

}