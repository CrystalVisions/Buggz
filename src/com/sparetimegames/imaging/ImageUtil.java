package com.sparetimegames.imaging;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 8/2/12
 * Time: 7:47 PM
 * Copyright(c) Diane Loux 2012
 */
public class ImageUtil
{
    public static Random rnd = new Random(System.currentTimeMillis());

    public static LinearGradient createRainbow(Point startPoint, Point endPoint)
    {
        int[] colors = new int[7];
        colors[0] = Color.rgb(255, 7, 57);
        colors[1] = Color.rgb(255, 110, 43);
        colors[2] = Color.rgb(255, 241, 50);
        colors[3] = Color.rgb(86, 255, 97);
        colors[4] = Color.rgb(22, 169, 255);
        colors[5] = Color.rgb(17, 17, 255);
        colors[6] = Color.rgb(114, 20, 255);
        float x1 = (float) startPoint.x;
        float y1 = (float) startPoint.y;
        float x2 = (float) endPoint.x;
        float y2 = (float) endPoint.y;
        return new LinearGradient(x1, y1, x2, y2, colors, null, Shader.TileMode.CLAMP);

    }

    //returns a random color.
    public static int getRandomColor()
    {


        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}
