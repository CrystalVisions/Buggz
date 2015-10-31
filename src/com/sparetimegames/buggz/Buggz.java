// SpotOn.java
// Activity for the Buggz app
package com.sparetimegames.buggz;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;

public class Buggz extends Activity
{
    //private SpotOnView view; // displays and manages the game
    private BugzView view;
    // called when this Activity is first created
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        view = (BugzView)findViewById(R.id.bugzView);


        // allow volume keys to set game volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    } // end method onCreate

    // called when this Activity moves to the background
    @Override
    public void onPause()
    {
        super.onPause();
        view.stopRunning(); // release resources held by the View
    } // end method onPause

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        view.stopRunning();
    }

    // called when this Activity is brought to the foreground
    @Override
    public void onResume()
    {
        super.onResume();
        view.resume(this); // re-initialize resources released in onPause
    } // end method onResume
} // end class Buggz


/**************************************************************************
 * (C) Copyright 1992-2012 by Deitel & Associates, Inc. and               *
 * Pearson Education, Inc. All Rights Reserved.                           *
 *                                                                        *
 * DISCLAIMER: The authors and publisher of this book have used their     *
 * best efforts in preparing the book. These efforts include the          *
 * development, research, and testing of the theories and programs        *
 * to determine their effectiveness. The authors and publisher make       *
 * no warranty of any kind, expressed or implied, with regard to these    *
 * programs or to the documentation contained in these books. The authors *
 * and publisher shall not be liable in any event for incidental or       *
 * consequential damages in connection with, or arising out of, the       *
 * furnishing, performance, or use of these programs.                     *
 **************************************************************************/
