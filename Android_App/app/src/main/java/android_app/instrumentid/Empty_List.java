/* This class is used for dealing with an empty
library list view. It displays a message and a
button for the user to return to the home screen.

Associated Screen Layout: empty_list.xml
*/

package android_app.instrumentid; //Project package

//Import Android functions
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Empty_List extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_list); //Set associated screen layout

        //Button to return to main menu if list is empty
        Button returnToMain = findViewById(R.id.returnButton);
        returnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //Change screen without transition animation
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        //When the back button is pressed at empty list screen, return to home screen gracefully
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //Change screen without transition animation
        startActivity(intent);
    }
}

