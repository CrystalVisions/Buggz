package com.sparetimegames.buggz;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import com.sparetimegames.imaging.Rectangle;
import com.sparetimegames.imaging.Vector2D;
import com.sparetimegames.util.JSONable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/30/12
 * Time: 10:36 PM
 * Copyright(c) Diane Loux 2012
 */
public class Bug implements JSONable
{
    private static final double animationDelay = 83;
//    private TextPainter painter;
    private Bitmap image;
    private Rectangle spaceOccupied;
    private Point motionVector;
    private Rectangle bounds;
    private Random rand;
    private boolean needsRotation;
    private boolean moveable = true;
    private boolean ignoreCollisions;


    private Matrix matrix;
    private long lifetime;
    private long birthday;
    private boolean shrinkingBlood = false;
    private boolean tooSmallToShrink = false;
    private int bloodSize;
    private long bloodAnimationStartTime;

    private long currentAge = 0;
    private int bitmapType = 0;
    private int speed = 0;
    private boolean pauseAging = false;

    public Bug(Bitmap img, Point position, Point motionVector, boolean needsRotation, boolean moveable, Rectangle bounds, long projectedAge, int bugSpeed)
    {
        this.lifetime = projectedAge;

        matrix = new Matrix();

        birthday = System.currentTimeMillis();
        this.needsRotation = needsRotation;
        this.moveable = moveable;
        //Seed a random number generator
        // for this sprite with the sprite
        // position.
        rand = new Random(position.x);
        this.image = img;

        setSpaceOccupied(new Rectangle(
                position.x,
                position.y,
                image.getWidth(),
                image.getHeight()));
        this.motionVector = motionVector;
        this.bounds = bounds;
        this.setSpeed(bugSpeed);
        initRotation();
    }

    //empty constructor for use in restoring a bug from json representation
    //note: birthday must be reset to the current time when the game
    //is restored, and the bitmap must be set based on the bitmapType
    //from outside the class and the bounds rectangle must be set because
    // it is based on the current device.
    Bug()
    {
        matrix = new Matrix();
        this.needsRotation = true; //we wont save bugs that do not need it
        this.moveable = true; //we wont save bugs that are blood

        birthday = System.currentTimeMillis();
        rand = new Random(birthday);

    }

    public void doPauseAging(boolean pause)
    {
        this.pauseAging = pause;
        if(!pause)
        {
            //we just unpaused, need to set the new birthday
            birthday = System.currentTimeMillis();
        }

    }

    /**
     * Must return a JSON-compliant representation of this object,
     * in the form of a JSONObject.
     *
     * @return The JSONObject representation.
     *
     */
    public JSONObject toJSONObject()
    {
        if (!this.moveable)
        {
            return null;
        }
        JSONObject jo = new JSONObject();

        try
        {
            jo.put("bitmapType", getBitmapType());
            jo.put("age", getAge());
            jo.put("locationX", getLocation().x);
            jo.put("locationY", getLocation().y);
            jo.put("motionX", motionVector.x);
            jo.put("motionY", motionVector.y);
            jo.put("speed", (double) speed);
            jo.put("lifetime", lifetime);
        }
        catch (JSONException e)
        {
            return null; //cannot serialize this bug
        }
        return jo;
    }

    /**
     * Must populate this object from the given JSON source.
     *
     * @param src The source JSON data.
     *
     */
    public boolean fromJSONObject(JSONObject src)
    {
        if (src == null)
        {
            return false;
        }
        try
        {
            bitmapType = src.getInt("bitmapType");
            currentAge = src.getLong("age");
            int x = src.getInt("locationX");
            int y = src.getInt("locationY");
            spaceOccupied = new Rectangle(x, y, 0, 0);
            x = src.getInt("motionX");
            y = src.getInt("motionY");
            motionVector = new Point(x, y);
            speed = src.getInt("speed");
            lifetime = src.getLong("lifetime");
        }
        catch (JSONException e)
        {
            return false;
        }

       return true;
    }


    int getBitmapType()
    {
        return bitmapType;
    }

    void setBitmapType(int type)
    {
        this.bitmapType = type;
    }


    public void setSpeed(int s)
    {
        this.speed = s;
//        String text = "" + s;
//        painter = new TextPainter();
//        painter.setText(text);
//        painter.setTextSize(this.spaceOccupied.width/2);
    }

    void setImage(Bitmap bitmap)
    {
        this.image = bitmap;
        spaceOccupied.width = image.getWidth();
        spaceOccupied.height = image.getHeight();
    }

    void setBounds(Rectangle bounds)
    {
        this.bounds = bounds;
    }

    void setMotionVector(
            Point motionVector)
    {

        this.motionVector = motionVector;


    }


    public Rectangle getSpaceOccupied()
    {
        return spaceOccupied;
    }

    public boolean isShrinkingBlood()
    {
        return shrinkingBlood;
    }

    public void setShrinkingBlood(boolean b)
    {

        this.shrinkingBlood = b;
        if (shrinkingBlood)
        {
            bloodAnimationStartTime = System.currentTimeMillis();
        }
    }

    private void setSpaceOccupied(Rectangle rectangle)
    {
        this.spaceOccupied = rectangle;

    }

    public void setSpaceOccupied(
            Point position)
    {
        spaceOccupied.x = position.x;
        spaceOccupied.y = position.y;

    }

    public Point getMotionVector()
    {
        return motionVector;
    }


    private void initRotation()
    {


        updateRotation();

    }

    void updateRotation()
    {

        if (!this.needsRotation)
        {
            return;
        }
        Vector2D firstDir = new Vector2D(0, 1);
        Vector2D secondDir = new Vector2D(motionVector.x, motionVector.y);

        double theta = firstDir.angleBetween(secondDir);
        theta += Math.toRadians((double) 180);
        matrix.reset();
        double angle = Math.toDegrees(theta);
        //matrix.postRotate((float)angle);

        matrix.postRotate((float) angle, spaceOccupied.width / 2, spaceOccupied.height / 2);


        matrix.postTranslate(spaceOccupied.x, spaceOccupied.y);

    }

    public void updatePosition(double elapsedTime)
    {


        if (!this.moveable)
        {

            return;
        }
        Point position = new Point(
                spaceOccupied.x, spaceOccupied.y);

        //Insert random behavior.  During
        // each update, a sprite has about
        // one chance in 50 of making a
        // random change to its
        // motionVector.  When a change
        // occurs, the motionVector
        // coordinate values are forced to
        // fall between -7 and 7.  This
        // puts a cap on the maximum speed
        // for a sprite.
        if (rand.nextInt() % 50 == 0)
        {
            int accel = speed + 4;
            Point randomOffset =
                    new Point(rand.nextInt() % 3,
                            rand.nextInt() % 3);

            motionVector.x += randomOffset.x;
            if (motionVector.x >= accel)
            {
                motionVector.x -= accel;
            }
            if (motionVector.x <= -accel)
            {
                motionVector.x += accel;
            }
            motionVector.y += randomOffset.y;
            if (motionVector.y >= accel)
            {
                motionVector.y -= accel;
            }
            if (motionVector.y <= -accel)
            {
                motionVector.y += accel;
            }


        }//end if

        double interval = elapsedTime/animationDelay;
        //Move the sprite on the screen
        double dx = interval * (double)motionVector.x;
        double dy = interval * (double)motionVector.y;

        position.offset((int)dx, (int)dy);

        //Bounce off the walls
        boolean bounceRequired = false;
        Point tempMotionVector = new Point(
                motionVector.x,
                motionVector.y);

        //Handle walls in x-dimension
        if (position.x < bounds.x)
        {
            bounceRequired = true;
            position.x = bounds.x;
            //reverse direction in x
            tempMotionVector.x =
                    -tempMotionVector.x;
        }
        else if ((
                position.x + spaceOccupied.width)
                > (bounds.x + bounds.width))
        {
            bounceRequired = true;
            position.x = bounds.x +
                    bounds.width -
                    spaceOccupied.width;
            //reverse direction in x
            tempMotionVector.x =
                    -tempMotionVector.x;
        }//end else if

        //Handle walls in y-dimension
        if (position.y < bounds.y)
        {
            bounceRequired = true;
            position.y = bounds.y;
            tempMotionVector.y =
                    -tempMotionVector.y;
        }
        else if ((position.y +
                spaceOccupied.height)
                > (bounds.y + bounds.height))
        {
            bounceRequired = true;
            position.y = bounds.y +
                    bounds.height -
                    spaceOccupied.height;
            tempMotionVector.y =
                    -tempMotionVector.y;
        }//end else if

        if (bounceRequired)
        //save new motionVector
        {
            setMotionVector(
                    tempMotionVector);
        }
        //update spaceOccupied
        setSpaceOccupied(position);

        updateRotation();
    }//end updatePosition()

    public void drawBug(Canvas g)
    {
        if (!moveable)
        {
            g.drawBitmap(image, spaceOccupied.x, spaceOccupied.y, null);
        }
        else
        {
            g.drawBitmap(image, matrix, null);
//            painter.setLocation(spaceOccupied.x,spaceOccupied.y);
//            painter.paintText(g);
        }
        //g.drawBitmap(image, matrix, null);

    }

    public boolean testCollision(
            Bug testSprite)
    {
        //Check for collision with
        // another sprite
        return testSprite != this && spaceOccupied.intersects(testSprite.getSpaceOccupied());
    }//end testCollision

    public boolean testCollision(int x, int y)
    {
        return spaceOccupied.contains(x, y);
    }

    public boolean isMoveable()
    {
        return moveable;
    }


    public void freezeSprite(Bitmap deadImage, int x, int y)
    {

        this.image = deadImage;
        this.moveable = false;
        this.bloodSize = this.image.getWidth();

        this.needsRotation = false;
        this.ignoreCollisions(true);
        setLocation(x, y);
        adjustFromCenter();


    }


    public void setLocation(int x, int y)
    {
        this.setSpaceOccupied(new Point(x, y));
    }


    public Point getLocation()
    {
        return new Point(spaceOccupied.x, spaceOccupied.y);
    }


    private void adjustFromCenter()
    {
        //at this point we are at the center..need
        //to move left and up by half the height and width
        Point p = getLocation();
        int dx = -image.getWidth() / 2;
        int dy = -image.getHeight() / 2;
        p.offset(dx, dy);
        this.setSpaceOccupied(p);

    }

    private Point getCenterPoint()
    {
        float x = spaceOccupied.x;
        float y = spaceOccupied.y;
        float hW = image.getWidth() / 2;
        float hH = image.getHeight() / 2;

        int px = (int) (x + hW);
        int py = (int) (y + hH);
        return new Point(px, py);

    }

    public boolean isTooSmallToShrink()
    {
        return tooSmallToShrink;
    }

    public void shrinkBlood()
    {
        if (tooSmallToShrink)
        {
            return;
        }
        Point p = getCenterPoint();
        long elapsedTime = System.currentTimeMillis() - bloodAnimationStartTime;
        long bloodAnimationDuration = 2000;
        float t = (float) elapsedTime / (float) bloodAnimationDuration;
        if (t >= 1)
        {
            tooSmallToShrink = true;
            return;
        }

        //linear interpolation to determine the new size of the
        //bitmap
        int numPixelsToShrink = bloodSize - 1;
        float minSize = (float)bloodSize/20;
        float scale = t * (float)numPixelsToShrink;
        int pix = bloodSize - (int) scale;
        if (pix < minSize)
        {
            tooSmallToShrink = true;
            return;
        }

        Point currentPoint = new Point();
        currentPoint.x = spaceOccupied.x;
        currentPoint.y = spaceOccupied.y;

        image = Bitmap.createScaledBitmap(image, pix, pix, false);
        spaceOccupied.width = image.getWidth();
        spaceOccupied.height = image.getHeight();
        setSpaceOccupied(p);
        adjustFromCenter();


    }

    public void ignoreCollisions(boolean ignoreCollisions)
    {
        this.ignoreCollisions = ignoreCollisions;
    }

    public boolean isIgnoreCollisions()
    {
        return ignoreCollisions;
    }

    public long getAge()
    {
        if(pauseAging)
        {
            return 0;
        }
        long timeNow = System.currentTimeMillis();
        long elapsedTime = timeNow - birthday;
        currentAge += elapsedTime;
        birthday = timeNow;

        return currentAge;

    }

    public boolean isExpired()
    {

        return !ignoreCollisions && getAge() > this.lifetime;

    }


}
