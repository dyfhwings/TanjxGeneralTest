package com.tanjx.calc.object.size;

import java.util.HashMap;
import java.util.Map;

public class Test
{
    public static void main(String[] args) throws IllegalAccessException
    {
        final ClassIntrospector ci = new ClassIntrospector();

        ObjectInfo res;
        final Map<String, String> map = new HashMap<String, String>();
        map.put("aa", "bcccb");
        res = ci.introspect(map);
        System.out.println("size=" + res.getDeepSize());
    }
}
