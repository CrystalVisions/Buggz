package com.sparetimegames.buggz;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/31/12
 * Time: 10:45 AM
 * Copyright(c) Diane Loux 2012
 */
public class Tracer
{
    public static String trace()
    {
        Exception e = new Exception("Tracer trace");
        StackTraceElement[] elements = e.getStackTrace();
        StringBuilder buf = new StringBuilder();
        for (StackTraceElement element : elements)
        {
            buf.append(element.toString());
            buf.append("\n");


        }

        e.printStackTrace();
        return buf.toString();
    }
}
