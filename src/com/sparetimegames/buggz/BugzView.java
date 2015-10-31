package com.sparetimegames.buggz;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.sparetimegames.imaging.Dimension;
import com.sparetimegames.imaging.ImageUtil;
import com.sparetimegames.imaging.Rectangle;
import com.sparetimegames.imaging.TextPainter;
import com.sparetimegames.util.JSONable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 7/30/12
 * Time: 8:10 PM
 * Copyright(c) Diane Loux 2012
 */
public class BugzView extends SurfaceView
        implements SurfaceHolder.Callback, BugListener, JSONable
{
    private static final int LONG_DELAY = 3500; // 3.5 seconds
    private static final int SHORT_DELAY = 2000; // 2 seconds
    private static final int INITIAL_ANIMATION_DURATION = 15000;
    private static final int MIN_ANIMATION_DURATION = 3000;
    private static final int INITIAL_SPOTS = 5; // initial # of spots
    private static final int SPOT_DELAY = 500; // delay in milliseconds
    private static final int LIVES = 3; // start with 3 lives
    private static final int MAX_LIVES = 7; // maximum # of total lives
    private static final int NEW_LEVEL = 10; // spots to reach new level
    private BugzThread bugzThread; // controls the game loop
    private BugManager bugManager; //keeps track of,updates and draws all the bugs


    private int numLives = 0;
    // constant for accessing the high score in SharedPreference
    private static final String HIGH_SCORE = "HIGH_SCORE";
    private static final String SAVED_GAME = "SAVED_GAME";


    private SharedPreferences preferences; // stores the high score
    private Handler spotHandler; // adds new spots to the game
    // variables for managing the game
    private long animationTime = INITIAL_ANIMATION_DURATION; // how long each spot remains on the screen
    private static final int INITIAL_SPEED = 4;
    private static final int SPEED_INCREMENT = 1;

    private int spotsTouched; // number of spots touched
    private int score; // current score
    private int level; // current level
    private int speed = INITIAL_SPEED; //current speed of any new bugs.
    private Activity activity; // to display Game Over dialog in GUI thread
    private boolean dialogIsDisplayed = false;
    private boolean gameOver = false; // is the game over?
    private int highScore = 0; // the game's all time high score
    private Drawable backdrop;
    private Rectangle parentBounds = null;


    private Bitmap lifeImage;
    private TextPainter highScoreTextView;
    private TextPainter currentScoreTextView;
    private RectF labelRect;
    private Paint labelPaint;
    private TextPainter levelTextView;

    private Paint backgroundPaint; //if the backdrop is null


    private Resources resources; // used to load resources

    private static final int HIT_SOUND_ID = 1;
    private static final int MISS_SOUND_ID = 2;
    private static final int DISAPPEAR_SOUND_ID = 3;
    private static final int MURDER_SOUND_ID = 4;
    private static final int SOUND_PRIORITY = 1;
    private static final int SOUND_QUALITY = 100;
    private static final int MAX_STREAMS = 4;
    private int volume; // sound effect volume
    private SoundPool soundPool; // plays sound effects


    public BugzView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        activity = (Activity) context;
        WallpaperManager man = WallpaperManager.getInstance(activity);
        this.backdrop = man.getFastDrawable();
        backgroundPaint = new Paint();

        // register SurfaceHolder.Callback listener
        getHolder().addCallback(this);

        // load the high score
        preferences = activity.getPreferences(Context.MODE_PRIVATE);
        highScore = preferences.getInt(HIGH_SCORE, 0);

        // save Resources for loading external values
        resources = context.getResources();
        int lifeInt = R.drawable.life;
        lifeImage = BitmapFactory.decodeResource(resources, lifeInt);


        spotHandler = new Handler(); // used to add spots when game starts

    }

    //Methods called in response to stages in the application life cycle
    // called by the Buggz Activity when it receives a call to onPause
    public void stopRunning()
    {
        boolean wasRunning = false;
        if (bugzThread != null)
        {
            wasRunning = bugzThread.threadIsRunning;
        }
        stopThread();
        removeCallbacks();

        if (soundPool != null)
        {
            soundPool.release(); // release audio resources
            soundPool = null;
        }

        //Save the game state...
        if (wasRunning)
        {
            this.saveGameState();
        }


    } // end method stopRunning

    private void removeCallbacks()
    {
        if (spotHandler != null)
        {
            spotHandler.removeCallbacks(addSpotRunnable);
        }
    }

    public void stopThread()
    {
        if (bugzThread != null)
        {
            bugzThread.setRunning(false);
        }
    }

    public void startThread()
    {
        gameOver = false; // the game is not over
        bugzThread = new BugzThread(getHolder());
        bugzThread.start();
    }


    // called by the Buggz Activity when it receives a call to onResume
    public void resume(Context context)
    {
        initializeSoundEffects(context); // initialize app's SoundPool

        if (!dialogIsDisplayed)
        {
            setupGame();
        }
    } // end method resume

    //Either sets up a new game or restores a saved game.
    public void setupGame()
    {

        boolean hasSavedGame = preferences.contains(SAVED_GAME);
        if (!hasSavedGame)
        {
            newGame();
        }
        else if (bugManager != null)
        {
            boolean gameRestored = this.restoreSavedGame();
            if (!gameRestored)
            {
                newGame();
            }

        }


    }

    private void initializeNewGame()
    {

        animationTime = INITIAL_ANIMATION_DURATION; // init animation length
        speed = INITIAL_SPEED;
        spotsTouched = 0; // reset the number of spots touched
        score = 0; // reset the score
        level = 1; // reset the level
        numLives = LIVES; //reset the lives
    }

    private void initializeBugManager(Rectangle parentBounds)
    {
        if (bugManager == null)
        {
            int green = R.drawable.green_bug;
            int red = R.drawable.red_bug;
            int yellow = R.drawable.yellow_bug;
            int dead = R.drawable.blood;
            Bitmap redBug = BitmapFactory.decodeResource(resources, red);
            Bitmap greenBug = BitmapFactory.decodeResource(resources, green);
            Bitmap yellowBug = BitmapFactory.decodeResource(resources, yellow);
            Bitmap deadBug = BitmapFactory.decodeResource(resources, dead);
            bugManager = new BugManager(redBug, greenBug, yellowBug, deadBug, parentBounds, animationTime);
            bugManager.setBugListener(this);
            bugManager.setBugSpeed(speed);

        }
        else
        {
            bugManager.setParentBounds(parentBounds);
            bugManager.setBugSpeed(speed);
            bugManager.clearBugs();
        }
    }

    public void newGame()
    {
        //The BugManager is dependent on the size of the
        //screen and so does not get created until onSizeChanged
        //so it may not be ready yet.
        if (bugManager == null)
        {

            return;
        }

        initializeNewGame();
        initializeBugManager(parentBounds);

        // add INITIAL_SPOTS new spots at SPOT_DELAY time intervals in ms
        for (int i = 1; i <= INITIAL_SPOTS; ++i)
        {

            spotHandler.postDelayed(addSpotRunnable, i * SPOT_DELAY);
        }
        //start the BugzThread
        //if this is not the first game..if it is the first
        //game the thread will be started after the surface is created.
        if (gameOver)
        {
            this.stopThread();
            this.startThread();
        } // end if
    }

    public void saveGameState()
    {
        JSONObject jsonObject = this.toJSONObject();
        if (jsonObject != null)
        {
            String json = jsonObject.toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SAVED_GAME, json);
            editor.commit();

        }
    }

    public boolean restoreSavedGame()
    {

        String json = preferences.getString(SAVED_GAME, null);


        //clear out the saved game.
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(SAVED_GAME);
        editor.commit();

        //reconstitute the saved game attributes.
        try
        {
            JSONObject jsonObject = new JSONObject(json);
            return fromJSONObject(jsonObject);
        }
        catch (JSONException e)
        {
            return false;
        }


    }

    /**
     * Must populate this object from the given JSON source.
     *
     * @param jo The source JSON data.
     */
    public boolean fromJSONObject(JSONObject jo)
    {

        try
        {
            //get the number of lives
            numLives = jo.getInt("lives");

            //get the number of spots touched
            spotsTouched = jo.getInt("spotsTouched");

            //get the current score
            score = jo.getInt("score");

            //get the current animation time
            animationTime = jo.getLong("animationTime");

            //get the current bug speed
            speed = jo.getInt("speed");

            //get the current level
            level = jo.getInt("level");

            //Now get all the bugs..
            JSONArray bugArray = jo.getJSONArray("bugs");
            bugManager.setBugSpeed(speed);
            bugManager.restoreBugs(bugArray);


        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return false;
        }


        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Must return a JSON-compliant representation of this object,
     * in the form of a JSONObject.
     *
     * @return The JSONObject representation.
     */
    public JSONObject toJSONObject()
    {
        JSONObject jsonObject = new JSONObject();


        try
        {
            //save the number of lives we have now
            jsonObject.put("lives", numLives);

            //save the number of spots touched
            jsonObject.put("spotsTouched", spotsTouched);

            //save the current score
            jsonObject.put("score", score);

            //save the current animationTime
            jsonObject.put("animationTime", animationTime);

            //save the current bug speed
            jsonObject.put("speed", speed);

            //save the current level
            jsonObject.put("level", level);

            //Now save all the bugs..
            JSONArray bugArray = bugManager.saveBugs();

            jsonObject.put("bugs", bugArray);
        }
        catch (JSONException e)
        {
            return null;
        }
        return jsonObject;
    }

    // create the app's SoundPool for playing game audio
    private void initializeSoundEffects(Context context)
    {

        // initialize SoundPool to play the app's three sound effects
        soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,
                SOUND_QUALITY);

        // set sound effect volume
        AudioManager manager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        soundPool.load(context, R.raw.hit, SOUND_PRIORITY);
        soundPool.load(context, R.raw.miss, SOUND_PRIORITY);
        soundPool.load(context, R.raw.disappear, SOUND_PRIORITY);

        soundPool.load(context, R.raw.murderer, SOUND_PRIORITY);


    } // end method initializeSoundEffect

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh)
    {
        super.onSizeChanged(width, height, oldw, oldh);
        highScoreTextView = new TextPainter();
        currentScoreTextView = new TextPainter();
        levelTextView = new TextPainter();

        float insetX = (float)width * .02f;
        float insetY = (float)height * .05f;

        int divisor = 50;
        float x = insetX; //30;
        float y = insetY; //50;

        int maxDim = Math.max(width, height);
        float textSize = maxDim / divisor;
        float labelTop = 0;
        float labelBottom = y;
        float labelLeft = 0;
        float labelRight = width;

        LinearGradient grad = ImageUtil.createRainbow(new Point(0,0), new Point(width,height));

        backgroundPaint.setShader(grad);

        levelTextView.setTextSize(textSize);
        highScoreTextView.setTextSize(textSize);
        currentScoreTextView.setTextSize(textSize);

        levelTextView.setAlignment(TextPainter.ALIGN_LEFT);
        currentScoreTextView.setAlignment(TextPainter.ALIGN_CENTER);
        highScoreTextView.setAlignment(TextPainter.ALIGN_RIGHT);

        currentScoreTextView.setText(resources.getString(R.string.score) + " " + score);
        levelTextView.setText(resources.getString(R.string.level) + " " + level);
        highScoreTextView.setText(resources.getString(R.string.high_score) + " " + highScore);

        y = insetY;
        levelTextView.setLocation(x, y);

        x = width - insetX;
        highScoreTextView.setLocation(x, y);

        x = width / 2;
        currentScoreTextView.setLocation(x, y);

        Dimension d = levelTextView.getSize();
        labelBottom += d.height;
        //float bottomPadding = .15f * (float)d.height;
        //labelBottom += currentScoreTextView.getVerticalSpacing();
        labelRect = new RectF(labelLeft, labelTop, labelRight, labelBottom);
        labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        parentBounds = new Rectangle();
        parentBounds.width = width;
        parentBounds.height = height;
        initializeBugManager(parentBounds);


        setupGame();
    } // end method onSizeChanged

    private void showShortToast(String msg)
    {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.short_toast,
                                       (ViewGroup) findViewById(R.id.short_toast_root));
//        CharSequence seq = msg.subSequence(0,msg.length());
        int duration = Toast.LENGTH_SHORT;
        Context context = activity.getApplicationContext();
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(msg);
        Toast toast = new Toast(context);//Toast.makeText(context,seq,duration);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        bugManager.pauseAging(SHORT_DELAY);
        toast.show();
    }
    // Runnable used to add new spots to the game at the start
    private Runnable addSpotRunnable = new Runnable()
    {
        public void run()
        {
            addNewSpot(); // add a new spot to the game
        } // end method run
    }; // end Runnable

    public void addNewSpot()
    {
        bugManager.addBug();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int eventType = event.getAction();
        if (eventType != MotionEvent.ACTION_DOWN)
        {
            return false;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        boolean bugWasSquished = bugManager.handleTouch(x, y);
        if (bugWasSquished)
        {
            touchedSpot();
        }
        else
        {
            handleMissedBug();
        }
        return true;

    }

    private void handleMissedBug()
    {
        // play the missed sound
        if (soundPool != null)
        {
            soundPool.play(MISS_SOUND_ID, volume, volume,
                    SOUND_PRIORITY, 0, 1f);
        }

        score -= 15 * level; // remove some points
        score = Math.max(score, 0); // do not let the score go below zero
        //displayScores(); // update scores/level on screen
    }

    private void touchedSpot()
    {
        ++spotsTouched; // increment the number of spots touched
        score += 10 * level; // increment the score

        // play the hit sounds
        if (soundPool != null)
        {
            soundPool.play(HIT_SOUND_ID, volume, volume,
                    SOUND_PRIORITY, 0, 1f);
        }

        // increment level if player touched 10 spots in the current level
        if (spotsTouched % NEW_LEVEL == 0)
        {
            String msg = resources.getString(R.string.level_up);
            showShortToast(msg);
            if (level % 5 == 0 && soundPool != null)
            {
                soundPool.play(MURDER_SOUND_ID, volume, volume, SOUND_PRIORITY, 0, 1f);
            }

            ++level; // increment the level
            animationTime *= 0.95; // make game 5% faster than prior level
            if (animationTime <= MIN_ANIMATION_DURATION)
            {
                animationTime = MIN_ANIMATION_DURATION;
            }

            speed += SPEED_INCREMENT;
            int MAX_SPEED = 27;
            if (speed > MAX_SPEED)
            {
                speed = MAX_SPEED;
            }

            bugManager.setBugSpeed(speed);
            // if the maximum number of lives has not been reached
            if (numLives < MAX_LIVES)
            {
                numLives++;
            }

        }
        if (!gameOver)
        {
            addNewSpot(); // add another untouched spot
        }


    }

    public void missedSpot()
    {

        if (gameOver) // if the game is already over, exit
        {
            return;
        }

        // play the disappear sound effect
        if (soundPool != null && numLives != 0)
        {
            soundPool.play(DISAPPEAR_SOUND_ID, volume, volume,
                    SOUND_PRIORITY, 0, 1f);
        }

        // if the game has been lost
        if (numLives == 0)
        {

            gameOver = true; // the game is over

            // if the last game's score is greater than the high score
            if (score > highScore)
            {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(HIGH_SCORE, score);
                editor.commit(); // store the new high score
                highScore = score;
            } // end if

            this.removeCallbacks();

            // display a high score dialog
            this.showGameOverDialog();
        }
        else // remove one life
        {
            numLives--;
            if (numLives < 0)
            {
                numLives = 0;
            }

            addNewSpot(); // add another spot to game
        } // end else
    }


    private void showGameOverDialog()
    {
        // create a dialog displaying the given String
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.game_over);
        dialogBuilder.setMessage(resources.getString(R.string.score) +
                " " + score);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton(R.string.reset_game,
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //displayScores(); // ensure that score is up to date
                        dialogIsDisplayed = false;
                        newGame(); // start a new game
                    } // end method onClick
                } // end DialogInterface
        ); // end call to dialogBuilder.setPositiveButton
        activity.runOnUiThread(
                new Runnable()
                {
                    public void run()
                    {
                        dialogIsDisplayed = true;
                        dialogBuilder.show(); // display the dialog
                    } // end method run
                } // end Runnable
        ); // end call to runOnUiThread

    }

    private void updatePositions(double elapsedTimeMS)
    {


        bugManager.updatePositions(elapsedTimeMS);
    }

    public void drawBackground(Canvas canvas)
    {
        if (backdrop != null)
        {
            backdrop.draw(canvas);
        }
        else
        {
            Paint paint = backgroundPaint;

            canvas.drawRect(parentBounds.getRect(), paint);
        }


    }

    public void drawLives(Canvas canvas)
    {
        float x = 5;
        float y = parentBounds.height - lifeImage.getHeight();
        y -= 5;
        for (int i = 0; i < numLives; i++)
        {
            canvas.drawBitmap(lifeImage, x, y, null);
            x += lifeImage.getWidth();

        }
    }

    public void drawText(Canvas canvas)
    {

        String highScoreString = resources.getString(R.string.high_score) + " " + highScore;
        String scoreString = resources.getString(R.string.score) + " " + score;
        String levelString = resources.getString(R.string.level) + " " + level;
        highScoreTextView.setText(highScoreString);
        currentScoreTextView.setText(scoreString);
        levelTextView.setText(levelString);
        canvas.drawRoundRect(labelRect, 10, 10, labelPaint);

        highScoreTextView.paintText(canvas);
        currentScoreTextView.paintText(canvas);
        levelTextView.paintText(canvas);


    }

    // draws the game to the given Canvas
    public void drawGameElements(Canvas canvas)
    {
        drawBackground(canvas);
        drawText(canvas);

        drawLives(canvas);

        //bugs on top!
        bugManager.drawBugs(canvas);


    }

    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        if (!dialogIsDisplayed)
        {
            // start the game loop thread
            this.startThread();
        } // end if
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
    {
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        boolean retry = true;
        bugzThread.setRunning(false);
        while (retry)
        {
            try
            {
                bugzThread.join();
                retry = false;
            } // end try
            catch (InterruptedException ignored)
            {
            } // end catch
        } // end while

    }

    public void bugExpired()
    {
        this.missedSpot();
    }

    private class BugzThread extends Thread
    {
        private final SurfaceHolder surfaceHolder; // for manipulating canvas
        private boolean threadIsRunning = true; // running by default

        public BugzThread(SurfaceHolder holder)
        {
            surfaceHolder = holder;
            setName("BugzThread");
        }

        // changes running state
        public void setRunning(boolean running)
        {
            threadIsRunning = running;
        } // end method setRunning

        @Override
        public void run()
        {
            Canvas canvas = null; // used for drawing
            long previousFrameTime = System.currentTimeMillis();

            while (threadIsRunning)
            {
                try
                {
                    canvas = surfaceHolder.lockCanvas(null);

                    // lock the surfaceHolder for drawing
                    synchronized (surfaceHolder)
                    {
                        bugManager.trimDead();
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousFrameTime;

                        updatePositions(elapsedTimeMS); // update game state
                        drawGameElements(canvas); // draw

                        previousFrameTime = currentTime; // update previous time

                    } // end synchronized block
                } // end try
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                finally
                {
                    if (canvas != null)
                    {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                } // end finally
            } // end while
        } // end method run
    }
}

