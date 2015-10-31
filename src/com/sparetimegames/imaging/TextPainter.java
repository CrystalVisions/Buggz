package com.sparetimegames.imaging;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/31/12
 * Time: 1:07 PM
 * Copyright(c) Diane Loux 2012
 */
public class TextPainter
{
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_CENTER = 2;


    private Paint paint;
    private String text;
    private Dimension size;
    private int x,y;


    public TextPainter()
    {
        paint = new Paint();
        size = new Dimension(0,0);
        int color = Color.WHITE;
        paint.setAntiAlias(true);
        paint.setColor(color);
        text = "";
        x = 0;
        y = 0;
        //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        //paint.setFakeBoldText(true);

    }


    public Dimension getSize()
    {
        return size;
    }

    public void setText(String str)
    {
        boolean hasChanged = true;
        if (str.length() == text.length())
        {
            hasChanged = false;
        }
        this.text = str;
        if (hasChanged)
        {

            this.calculateSize();
        }


    }

    public void setTextSize(float ts)
    {
        paint.setTextSize(ts);
        this.calculateSize();

    }

    public void setAlignment(int flag)
    {

        switch (flag)
        {
            case ALIGN_LEFT:
                paint.setTextAlign(Paint.Align.LEFT);

                break;
            case ALIGN_RIGHT:
                paint.setTextAlign(Paint.Align.RIGHT);
                break;
            case ALIGN_CENTER:
                paint.setTextAlign(Paint.Align.CENTER);
                break;
            default:
                paint.setTextAlign(Paint.Align.LEFT);
                break;

        }


    }


    public void setLocation(float x, float y)
    {
        this.x = (int) x;
        this.y = (int) y;

    }



    public void setLocation(int x, int y)
    {
        this.x = x;
        this.y = y;

    }



    private void calculateSize()
    {


        Rect rect = new Rect();
        paint.getTextBounds(this.text, 0, text.length(), rect);

        this.size.width = rect.width();
        this.size.height = rect.height();



    }


    public void paintText(Canvas canvas)
    {
        canvas.drawText(text, x, y, paint);
    }

    public float getVerticalSpacing()
    {
        return paint.getFontSpacing();
    }
}
