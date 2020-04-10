/* This class is the default activity created
once the application is opened. It displays a
splash screen for 4 seconds before opening
MainActivity.java

Associated Screen Layout: splash_screen.xml
*/

package android_app.instrumentid; //Project package

//Import Android functions
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity
{
    //Variables for associated screen layout
    private TextView tv;
    private ImageView iv;

    private int duration = 4000; //Variable for length of duration of activity thread (4 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen); //Set associated screen layout

        tv = (TextView) findViewById(R.id.tv); //Bind with textview from screen
        iv = (ImageView) findViewById(R.id.iv); //Bind with imageview from screen

        //Splash Screen transition
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.transition);
        //Animate both views simultaneously
        tv.startAnimation(animation);
        iv.startAnimation(animation);

        //Change to Main Screen
        final Intent i = new Intent(this, MainActivity.class);

        //Create new thread to run for specified period of time
        Thread timer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(duration); //Set duration of splash screen
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    //Start MainActivity.java
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //Transition to screen without animation
                    startActivity(i); //Go to Main Menu
                    finish();
                }
            }
        };
        timer.start(); //Start thread
    }
}
