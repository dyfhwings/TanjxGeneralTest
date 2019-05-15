package com.tanjx.calc.object.size;

import java.util.ArrayList;
import java.util.List;

public class Test
{
    public static void main(String[] args) throws IllegalAccessException
    {
        final ClassIntrospector ci = new ClassIntrospector();

        ObjectInfo res;
        ObjectA a = new ObjectA();
        a.setAge(10);
        a.setName("tanjx");
        a.setCode("TANJX");
        a.setContents("AAAAABBBBBCCCCC");
        res = ci.introspect(a);
        System.out.println("size=" + res.getDeepSize());

        ObjectB b = new ObjectB();
        b.setAge(10);
        b.setName("tanjx");
        b.setCode("TANJX");
        List<String> list = new ArrayList<String>();
        list.add("AAAAA");
        list.add("BBBBB");
        list.add("CCCCC");
        b.setContents(list);
        res = ci.introspect(b);
        System.out.println("size=" + res.getDeepSize());

        DelayedItem<String> item = new DelayedItem<String>("EOT_sect_gtsa08_MongoDB_57108f974f36451880badd95c416c605",
                System.currentTimeMillis());
        res = ci.introspect(item);
        System.out.println("size=" + res.getDeepSize());
    }
}
