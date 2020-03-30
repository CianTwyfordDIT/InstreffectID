package android_app.instrumentid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private TextView tv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        tv = (TextView) findViewById(R.id.tv);
        iv = (ImageView) findViewById(R.id.iv);

        //Splash Screen transition
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.transition);
        tv.startAnimation(animation);
        iv.startAnimation(animation);

        //Change to Main Screen
        final Intent i = new Intent(this, MainActivity.class);

        Thread timer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(4000); //Set to have splash screen for 4 seconds
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    startActivity(i); //Go to Main Menu
                    finish();
                }
            }
        };
        timer.start();
    }
}
