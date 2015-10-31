package com.sparetimegames.imaging;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/30/12
 * Time: 2:46 AM
 * Copyright(c) Diane Loux 2012
 * class to make it easier to port code to android
 */
public class Rectangle
{
    private Rect rect;

    public int x, y, width, height;

    public Rectangle()
    {
        x = 0;
        y = 0;
        width = 0;
        height = 0;
        rect = new Rect();
    }

    public Rectangle(Rect wrappedRect)
    {
        this.x = wrappedRect.left;
        this.y = wrappedRect.top;
        this.width = wrappedRect.width();
        this.height = wrappedRect.height();
    }

    public Rectangle(int x, int y, int w, int h)
    {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        rect = new Rect(x, y, x + w, y + h);
    }

    public Rect getRect()
    {
        rect.left = this.x;
        rect.top = this.y;
        rect.right = this.x + this.width;
        rect.bottom = this.y + this.height;
        return rect;
    }

    public RectF getRectF()
    {
        Rect r = getRect();
        return new RectF(r.left, r.top, r.right, r.bottom);
    }

    public boolean contains(int x, int y)
    {
        Rect wrappedRect = getRect();
        return wrappedRect.contains(x, y);
    }

    public boolean intersects(Rectangle spaceOccupied)
    {
        Rect wrappedRect = getRect();
        Rect soRect = spaceOccupied.getRect();
        return wrappedRect.intersect(soRect);
    }




    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("x = ");
        buf.append(x);
        buf.append("\n");
        buf.append("y = ");
        buf.append(y);
        buf.append("\n");
        buf.append("width = ");
        buf.append(width);
        buf.append("\n");
        buf.append("height = ");
        buf.append(height);
        return buf.toString();
    }
}
