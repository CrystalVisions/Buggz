package com.sparetimegames.imaging;

import android.graphics.PointF;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/30/12
 * Time: 1:51 AM
 * * Original Author: Håvard Rast Blok
 * E-mail: hrblok@rememberjava.com
 * Web   : www.rememberjava.com
 * Modified for android July 7/30/2012
 * Copyright(c) Diane Loux 2012
 */

/*
* Representation of a vector in 2D user space
*/
public class Vector2D extends PointF
{
    public double x;
    public double y;

    public Vector2D()
    {
        super();
    }


    /**
     * Sets the location of this <code>PointF</code> to the
     * specified <code>double</code> coordinates.
     *
     * @param x,&nbsp;y the coordinates of this <code>PointF</code>
     *
     */
    public void setLocation(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v)
    {
        this(v.x, v.y);
    }

    public double getLength()
    {
        return Math.sqrt(x * x + y * y);
    }

    /*
    * Returns the normalized vector of this Vector2D
    */
    public Vector2D normalize()
    {
        return new Vector2D(x / getLength(), y / getLength());
    }

    /*
    * Normalize this Vector2D
    */
    public void normalizeThis()
    {
        Vector2D v = normalize();

        x = v.x;
        y = v.y;
    }

    /*
    * Returns the scaled vector of this Vector2D
    */
    public Vector2D scale(double s)
    {
        return new Vector2D(s * x, s * y);
    }

    /*
    * Scale this Vector2D
    */
    public Vector2D scaleThis(double s)
    {
        Vector2D v = scale(s);

        x = v.x;
        y = v.y;
        return this;
    }

    /*
    * Adding other.x and other.y to x and y of this Vector2D
    */
    public void translate(Vector2D other)
    {
        x += other.x;
        y += other.y;
    }

    /*
    * Translates this point, at location (x, y),
    * by dx along the x axis and dy along the y axis
    * so that it now represents the point  (x + dx,  y + dy)
    */
    public void translate(double dx, double dy)
    {
        x += dx;
        y += dy;
    }

    public double dot(Vector2D other)
    {

        return (this.x * other.x) + (this.y * other.y);

    }

    public double angleBetween(Vector2D second)
    {
        return (Math.atan2(second.y,second.x) - Math.atan2(this.y,this.x));

    }
}

