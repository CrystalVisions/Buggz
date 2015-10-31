package com.sparetimegames.imaging;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/30/12
 * Time: 10:59 AM
 * Copyright(c) Diane Loux 2012
 * class to make it easier to port existing code to android
 */
public class Dimension
{
    public int width;
    public int height;

    public Dimension()
    {
        this.width = 0;
        this.height = 0;
    }

    public Dimension(int w, int h)
    {

        this.width = w;
        this.height = h;
    }


}
