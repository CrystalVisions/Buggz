package com.sparetimegames.buggz;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import com.sparetimegames.imaging.Dimension;
import com.sparetimegames.imaging.Rectangle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/30/12
 * Time: 10:35 PM
 * Copyright(c) Diane Loux 2012
 */
public class BugManager
{
    private final Queue<Bug> bugs = new ConcurrentLinkedQueue<Bug>();
    private Bitmap redBug, greenBug, yellowBug, deadBug;
    private Dimension bugSize;
    //private Dimension deadSize;
    private Rectangle parentBounds;
    private Random random = new Random(System.currentTimeMillis());
    private BugListener bugListener;
    private Queue<Bug> deadBugs = new ConcurrentLinkedQueue<Bug>();
    private Queue<Bug> bloodBugs = new ConcurrentLinkedQueue<Bug>();
    private long bugLife;
    private int bugSpeed;

    public BugManager(Bitmap red, Bitmap green, Bitmap yellow, Bitmap dead, Rectangle parentBounds, long bugLife)
    {
        this.bugLife = bugLife;
        this.redBug = red;
        this.greenBug = green;
        this.yellowBug = yellow;
        this.deadBug = dead;
        bugSize = new Dimension(red.getWidth(), red.getHeight());
        //deadSize = new Dimension(dead.getWidth(), dead.getHeight());
        this.parentBounds = parentBounds;
    }

    public void setParentBounds(Rectangle bounds)
    {
        this.parentBounds = bounds;
    }

    public void setBugListener(BugListener listener)
    {
        this.bugListener = listener;
    }

    public void setBugSpeed(int speed)
    {
        this.bugSpeed = speed;
        for (Bug bug : bugs)
        {
            bug.setSpeed(speed);
        }
    }

    public Point getEmptyPosition()
    {
        Dimension spriteSize = bugSize;
        Rectangle trialSpaceOccupied =
                new Rectangle(0, 0,
                        spriteSize.width,
                        spriteSize.height);
        Random rand =
                new Random(
                        System.currentTimeMillis());
        boolean empty = false;
        int numTries = 0;

        // Search for an empty position
        while (!empty && numTries++ < 100)
        {
            // Get a trial position
            trialSpaceOccupied.x =
                    Math.abs(rand.nextInt() %
                            parentBounds.width);
            trialSpaceOccupied.y =
                    Math.abs(rand.nextInt() %
                            parentBounds.height);

            // Iterate through existing
            // sprites, checking if position
            // is empty
            boolean collision = false;
            for (Bug bug : bugs)
            {
                Rectangle testSpaceOccupied = bug.getSpaceOccupied();
                if (trialSpaceOccupied.intersects(testSpaceOccupied))
                {
                    collision = true;
                }
            }

            empty = !collision;
        }//end while loop
        return new Point(
                trialSpaceOccupied.x,
                trialSpaceOccupied.y);
    }


    public void updatePositions(double elapsedTime)
    {
        for (Bug bug : bugs)
        {
            if (bug.isMoveable())
            {


                bug.updatePosition(elapsedTime);

                //Test for collision.
                Bug hitBug = this.testForCollision(bug);
                if (hitBug != null)
                {
                    this.bounceOffBug(bug, hitBug);
                }
            }


        }

        for (Bug bug : bloodBugs)
        {
            if (bug.isShrinkingBlood())
            {
                bug.shrinkBlood();
            }
        }
    }

    public Bug checkForMouseHit(int x, int y)
    {
        for (Bug bug : bugs)
        {
            if (bug.testCollision(x, y))
            {
                return bug;
            }
        }

        return null;
    }

//    public int getLiveCount()
//    {
//        int counter = 0;
//        for (Bug bug : bugs)
//        {
//            if (!bug.isIgnoreCollisions())
//            {
//                counter++;
//            }
//        }
//        return counter;
//    }

    private Bug testForCollision(Bug testBug)
    {
        //check for blood collisions first
        for (Bug bug : bloodBugs)
        {
            if (!bug.isMoveable() && !bug.isShrinkingBlood())
            {
                if (testBug.testCollision(bug))
                {
                    bug.setShrinkingBlood(true);
                }
            }
        }
        //Check for collision with other
        //bugs
        for (Bug bug : bugs)
        {
            if (bug == testBug)
            {
                continue;
            }

            if (bug.isIgnoreCollisions())
            {

                continue;
            }

            if (testBug.testCollision(bug))
            {
                return bug;
            }
        }

        return null;
    }

    private void bounceOffBug(Bug oneSprite, Bug otherSprite)
    {
        //Swap motion vectors for
        // bounce algorithm
        Point swap =
                oneSprite.getMotionVector();
        oneSprite.setMotionVector(
                otherSprite.getMotionVector());
        otherSprite.setMotionVector(swap);

        oneSprite.updateRotation();
        otherSprite.updateRotation();
    }

    public void addBug(Bug bug)
    {
        bugs.add(bug);
    }

    public void addBug()
    {
        Point position = this.getEmptyPosition();
        Bug bug = makeBug(position);
        addBug(bug);
    }




    public JSONArray saveBugs()
    {
        JSONArray jsonArray = new JSONArray();
        for(Bug bug: bugs)
        {
            JSONObject json = bug.toJSONObject();
            if(json != null)
            {
                jsonArray.put(json);
            }
        }

        return jsonArray;
    }

    public void restoreBugs(JSONArray jsonArray)
    {
        bugs.clear();
        bloodBugs.clear();
        deadBugs.clear();
        int len = jsonArray.length();
        for(int i = 0; i < len; i++)
        {
            try
            {
                JSONObject json = jsonArray.getJSONObject(i);
                Bug bug = makeBug(json);
                if(bug != null)
                {
                    addBug(bug);
                }

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
    public Bug makeBug(JSONObject json)
    {
        Bug bug = new Bug();
        boolean ok = bug.fromJSONObject(json);
        if (!ok)
        {
            return null;
        }

        bug.setBounds(parentBounds);
        int colorRand = bug.getBitmapType();
        Bitmap bugImage;
        switch (colorRand)
        {
            case 0:
                bugImage = greenBug;
                break;
            case 1:
                bugImage = redBug;
                break;
            default:
                bugImage = yellowBug;
        }



        bug.setImage(bugImage);
        return bug;
    }

    public Bug makeBug(Point position)
    {
        //Get a random image..
        Bitmap bugImage;
        int colorRand = random.nextInt(3);

        switch (colorRand)
        {
            case 0:
                bugImage = greenBug;
                break;
            case 1:
                bugImage = redBug;
                break;
            default:
                bugImage = yellowBug;
        }

        //Get a random motion vector
        int speedRand = bugSpeed;
        Point motionVector = new Point(random.nextInt() % 3, random.nextInt() % 3);

        if(motionVector.x < 0)
        {
            motionVector.x -= speedRand;
        }
        else
        {
            motionVector.x += speedRand;
        }
        if(motionVector.y < 0)
        {
            motionVector.y -= speedRand;
        }
        else
        {
            motionVector.y += speedRand;
        }

        Bug bug = new Bug(bugImage, position, motionVector, true, true, parentBounds, bugLife, bugSpeed);

        bug.setBitmapType(colorRand);
        return bug;
    }

    public void clearBugs()
    {
        bugs.clear();
        bloodBugs.clear();
    }

    public void drawBugs(Canvas g)
    {
        for (Bug blood : bloodBugs)
        {
            blood.drawBug(g);
        }

        for (Bug bug : bugs)
        {
            bug.drawBug(g);

        }


    }

    public void trimDead()
    {

        for (Bug bug : bugs)
        {

            if (bug.isExpired())
            {
                deadBugs.add(bug);
            }

        }

        for (Bug bug : bloodBugs)
        {
            if (bug.isShrinkingBlood())
            {
                if (bug.isTooSmallToShrink())
                {
                    deadBugs.add(bug);
                }

            }
        }

        for (Bug bug : deadBugs)
        {
            bugs.remove(bug);
            bloodBugs.remove(bug);
            if (bugListener != null && bug.isMoveable())
            {
                bugListener.bugExpired();
            }
        }

        deadBugs.clear();
    }

    private boolean freezeBugAt(int x, int y)
    {
        Bug hitBug = checkForMouseHit(x, y);
        if (hitBug == null || hitBug.isIgnoreCollisions())
        {
            return false;
        }

        hitBug.freezeSprite(deadBug, x, y);
        bugs.remove(hitBug);
        bloodBugs.add(hitBug);
        return true;
    }

    public boolean handleTouch(int x, int y)
    {
        Bug hitBug = checkForMouseHit(x, y);
        return !(hitBug == null || hitBug.isIgnoreCollisions()) && freezeBugAt(x, y);

    }

    //pause the aging on all bugs for a number of milliseconds
    public void pauseAging(int ms)
    {
        for(Bug bug: bugs)
        {
            bug.doPauseAging(true);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

          public void run() {
            //Do something after seconds
            for(Bug bug: bugs)
            {
                bug.doPauseAging(false);
            }
          }
        }, ms);
    }
}
