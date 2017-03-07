package com.tanjx.test;

import java.math.BigInteger;

public class mytest
{
    public static void main(String[] args) throws Exception
    {
        BigInteger b1 = new BigInteger(new String("1234567890").getBytes());
        BigInteger b2 = new BigInteger(new String("1234567890").getBytes());
        Long l1 = new Long(new String("1234567890"));
        Long l2 = new Long(new String("1234567891"));
        System.out.println(l1 ^ l2);
        System.out.println(Integer.toBinaryString(11));
    }
}
