/* This class acts as the home screen for the
client application. It connects all screens
and activities. The main function of this
class is to connect to the server, select and
send audio files and receive responses.

Associated Screen Layout: activity_main.xml
*/

package android_app.instrumentid; //Project package

//Import Android functions
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//Import Java functions
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//Import okhttp3 library with functions
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{
    //Initialise variables
    File globalFile; //Global variable for whole activity to access selected audio file
    public SQLite_Database db; //Object to access SQLite_Database methods
    String serverIP = "192.168.0.25"; //Server IP Address
    String serverPort = "5000"; //Server Port number

    //Variables for associated screen layout
    ImageView uploadFile;
    Intent fileIntent;
    ImageView viewLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Set associated screen layout
        db = new SQLite_Database(this); //Create new database object
        serverConnection(); //Attempt to connect to server on start up

        uploadFile = findViewById(R.id.uploadFile); //Bind with imageview from screen
        uploadFile.setEnabled(false); //Set image to be unclickable by default
        uploadFile.setImageAlpha(75); //Set image opacity to be 75 out of 255 to represent it being unclickable

        //Get necessary permissions to read device storage on first run of application
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

            return;
        }
        enableButton(); //Enable uploadFile and viewLibrary images to be clickable when user gives permission
    }

    @Override
    public void onBackPressed()
    {
        //When the back button is pressed at home screen, exit out of app gracefully
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void enableButton()
    {
        //Click listener on uploadFile image
        uploadFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fileIntent = new Intent(Intent.ACTION_GET_CONTENT); //Open file selector view
                fileIntent.setType("audio/*"); // File types allowed to be selected
                startActivityForResult(fileIntent, 10); // Specify which activity returning from

            }
        });

        //Click listener on viewLibrary image
        viewLibrary = findViewById(R.id.viewLibrary);
        viewLibrary.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Start Prediction_List class
                Intent intent = new Intent(MainActivity.this, Predictions_List.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //Change screen without transition animation
                MainActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        //Get result of user selection for permissions
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            enableButton(); //Make images clickable
        }
        else
        {
            //Request permission again if user denies - application can only function once this permission has been granted
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    //Function to create URL to connect to server
    String getConnectionURL()
    {
        String IPAddress = serverIP;
        String port = serverPort;
        String postURL = "http://"+IPAddress+":"+port+"/";

        return postURL; //Return constructed URL
    }

    //Function to create URL to upload file to server
    String getUploadURL()
    {
        String IPAddress = serverIP;
        String port = serverPort;
        String postURL = "http://"+IPAddress+":"+port+"/"+"uploadFile";

        return postURL; //Return constructed URL
    }

    //Method to create connection request
    void serverConnection()
    {
        String postURL = getConnectionURL();
        String postText="";

        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
        RequestBody post_Text = RequestBody.create(mediaType, postText);

        postConnectionRequest(postURL, post_Text);
    }

    //Method to post connection request
    void postConnectionRequest(String postURL, RequestBody requestBody)
    {

        OkHttpClient client = new OkHttpClient(); //Create new client object

        //Build the request with passed URL and body
        Request request = new Request.Builder()
                .url(postURL)
                .post(requestBody)
                .build();

        //Make a call to the server with the connection request
        client.newCall(request).enqueue(new Callback()
        {
            //Method for dealing with failed calls (no response from server)
            @Override
            public void onFailure(Call call, IOException e)
            {

                //Cancel the post on failure.
                call.cancel();

                e.printStackTrace();

                //Create new thread
                runOnUiThread(new Runnable()
                {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run()
                    {
                        TextView responseText = findViewById(R.id.response); //Bind with textview from screen, displays response
                        ImageView serverStatus = findViewById(R.id.serverStatus); //Bind with imageview from screen, shows server status bar at bottom of screen
                        TextView prediction = findViewById(R.id.prediction); //Bind with textview from screen, displays prediction

                        serverStatus.setImageResource(R.drawable.server_status_offline); //Set server status bar to offline (red)
                        responseText.setText("Failed To Connect To Server"); //Set textview and display text

                        uploadFile.setEnabled(false); //Disable uploadFile image to be clickable as server is not connected
                        uploadFile.setImageAlpha(75); //Set opacity to 75 out of 255

                        prediction.setText("Server Connection Required \nFor File Upload"); //Prediction could not be displayed, set text message instead
                        serverConnection(); //Automatically retry to connect to server
                    }
                });
            }

            //Method for dealing with responses from server
            //This means a connection was successful and communication is working
            @Override
            public void onResponse(Call call, final Response response)
            {
                //Create new thread
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TextView responseText = findViewById(R.id.response);  //Bind with textview from screen, displays response
                        try
                        {
                            ImageView serverStatus = (ImageView) findViewById(R.id.serverStatus); //Bind with imageview from screen, shows server status bar at bottom of screen
                            serverStatus.setImageResource(R.drawable.server_status_online); //Set server status bar to online (green)
                            TextView prediction = findViewById(R.id.prediction); //Bind with textview from screen, displays prediction

                            //Replace prediction text if set to this message when server failed to connect
                            if(prediction.getText().equals("Server Connection Required \nFor File Upload"))
                            {
                                prediction.setText("Upload File");
                            }

                            uploadFile.setEnabled(true); //Enable uploadFile image to be clickable as server is responding
                            uploadFile.setImageAlpha(255); //Set opacity to be full
                            responseText.setText(response.body().string()); //Set the response text to be the message sent by the server in its body
                        }
                        //Catch error
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    //Method to post prediction request
    void postPredictionRequest(String postURL, RequestBody requestBody)
    {
        TextView responseText = findViewById(R.id.prediction); //Bind with textview from screen
        responseText.setText("Uploading File To Server..."); //Set text

        Button playFile = findViewById(R.id.playFile); //Bind with button from screen
        playFile.setVisibility(View.INVISIBLE); //Set button to be invisible if it already exists from previous file selection

        uploadFile.setEnabled(false); //Disable image to be clickable during a file upload
        uploadFile.setImageAlpha(75); //Set opacity to 75 out of 255

        OkHttpClient client = new OkHttpClient(); //Create new client object

        //Build the request with passed URL and body
        Request request = new Request.Builder()
                .url(postURL)
                .post(requestBody)
                .build();

        //Make a call to the server with the prediction request
        client.newCall(request).enqueue(new Callback()
        {
            //Method for dealing with failed calls (no response from server)
            @Override
            public void onFailure(Call call, IOException e)
            {

                // Cancel the post on failure.
                call.cancel();

                serverConnection(); //Automatically retry to connect to server
                e.printStackTrace();

                //Create new thread
                runOnUiThread(new Runnable()
                {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run()
                    {
                        //uploadFile.setEnabled(false);
                        TextView responseText = findViewById(R.id.prediction); //Bind with textview from screen, displays prediction
                        responseText.setText("File Failed To Upload"); //Set text if request doesn't reach server
                    }
                });
            }

            //Method for dealing with prediction responses from server
            //This means a connection was successful and communication is working
            @Override
            public void onResponse(Call call, final Response response)
            {
                //Create new thread
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Button playFile = findViewById(R.id.playFile); //Bind with button from screen
                        playFile.setVisibility(View.VISIBLE); //Set button to be visible

                        uploadFile.setEnabled(true); //Enable image to be clickable to upload another file as there is a server connection
                        uploadFile.setImageAlpha(255); //Set opacity to full
                        try
                        {
                            String prediction = response.body().string(); //Set the response prediction text to be the message sent by the server in its body

                            TextView responseText = findViewById(R.id.prediction); //Bind with textview from screen
                            responseText.setText(""); //Clear response view

                            TextView predictionText = findViewById(R.id.prediction); //Bind with textview from screen
                            predictionText.setText("Prediction:\n"+prediction); //Set textview to prediction text from server

                            //Open SQLite_Database to automatically add prediction to database
                            db.open();
                            Long rowID = addRow(prediction); //Call method to add row and return row ID
                            //If rowID is -1, the row wasn't added. This is because the predicted file already exists in the database
                            if (rowID == -1)
                            {
                                //Display toast to user
                                Toast.makeText(MainActivity.this, "File not added to library. Already exists", Toast.LENGTH_LONG).show();
                            }
                            db.close(); //Close database

                            serverConnection(); //Automatically check to ensure there is still communication with server
                        }
                        //Catch error
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    //Method for dealing with result of file selector
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //Ensure correct activity context and the result was successful
        if (requestCode == 10 && resultCode == RESULT_OK)
        {
            String path = data.getData().getPath(); //Get the path for selected file

            File file = new File(path); //Create new file object with path of file selected
            String contentType = getMimeType(path); //Call method with path to file
            String fileName = file.getName(); //Get the name of the file from the file object

            //Create another file object with path to Music directory on device
            String filePath = "/sdcard/Music/"+fileName;
            File file2 = new File(filePath);

            Toast.makeText(this, "File "+fileName+ " selected", Toast.LENGTH_SHORT).show(); //Toast displaying file name selection to user
            Button playFile = findViewById(R.id.playFile); //Bind with button from screen
            playFile.setVisibility(View.VISIBLE); //Make button visible
            playFile.setText("Play "+fileName); //Set button text to filename

            globalFile = file2; //Put file into global variable to be used in addRow() function

            //Create a media player object to play file when playFile button is clicked
            final MediaPlayer player = MediaPlayer.create(this, Uri.parse(filePath));
            playFile.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    player.start();
                }
            });

            //Create a body containing the file to be sent to the server
            RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file2);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type", contentType)
                    .addFormDataPart("uploadFile", fileName, fileBody)
                    .build();

            String postURL = getUploadURL(); //Get URL for upload file method on server

            postPredictionRequest(postURL, requestBody); //Pass URL and body to method
        }
    }

    //Method for getting mime type of the file
    private String getMimeType(String path)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    //Insert statement to insert row into table. Row ID is returned
    public long addRow(String prediction)
    {
        //Initialise variables
        long id;
        //Get file name and path from global variable
        String fileName = globalFile.getName();
        String filePath = globalFile.getAbsolutePath();

        //Get system date and time
        String formattedDate =
                new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());

        //Time in hours and minutes used for displaying in list
        String formattedTime =
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        //Absolute time used for ordering list view by most recent
        String formattedAbsTime =
                new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        //Pass variables to method in SQLite_Database class
        id = db.insertPrediction(
                fileName,
                filePath,
                prediction,
                formattedDate,
                formattedTime,
                formattedAbsTime);
        return id; //Return rowID
    }
}