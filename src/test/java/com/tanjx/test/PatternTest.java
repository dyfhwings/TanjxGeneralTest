package com.tanjx.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternTest
{
    public static void main(String[] args)
    {
        Pattern pattern = Pattern.compile("^<text.*>(.*)</text>$", Pattern.DOTALL);
        Matcher match = pattern.matcher("<text>this is a test!</text>");
        if (match.find())
        {
            System.out.println(match.group(1));
        }
    }
}
