package android_app.instrumentid;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    File globalFile;
    public SQLite_Database db;
    String serverIP = "192.168.0.25";
    String serverPort = "5000";

    ImageView uploadFile;
    Intent fileIntent;
    Button viewLibrary;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new SQLite_Database(this);
        serverConnection();

        uploadFile = findViewById(R.id.uploadFile);
        uploadFile.setEnabled(false);
        uploadFile.setImageAlpha(75);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);

            return;
        }
        enableButton();
    }

    private void enableButton()
    {
        uploadFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("audio/*"); // File types allowed to be selected
                startActivityForResult(fileIntent, 10); // Specify which activity returning from

            }
        });

        viewLibrary = findViewById(R.id.viewLibrary);
        viewLibrary.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, Predictions_List.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED))
        {
            enableButton();
        }
        else
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    String getUploadURL()
    {
        String IPAddress = serverIP;
        String port = serverPort;
        String postURL = "http://"+IPAddress+":"+port+"/"+"uploadFile";

        return postURL;
    }

    String getConnectionURL()
    {
        String IPAddress = serverIP;
        String port = serverPort;
        String postURL = "http://"+IPAddress+":"+port+"/";

        return postURL;
    }

    void serverConnection()
    {
        String postURL = getConnectionURL();
        String postText="";

        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
        RequestBody post_Text = RequestBody.create(mediaType, postText);

        postConnectionRequest(postURL, post_Text);
    }

    void postConnectionRequest(String postURL, RequestBody requestBody)
    {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postURL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {

                // Cancel the post on failure.
                call.cancel();

                e.printStackTrace();

                runOnUiThread(new Runnable()
                {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run()
                    {
                        //uploadFile.setEnabled(false);
                        TextView responseText = findViewById(R.id.response);
                        ImageView serverStatus = (ImageView) findViewById(R.id.serverStatus);
                        serverStatus.setImageResource(R.drawable.server_status_offline);
                        responseText.setText("Failed To Connect To Server");
                        uploadFile.setEnabled(false);
                        uploadFile.setImageAlpha(75);
                        serverConnection();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TextView responseText = findViewById(R.id.response);
                        try
                        {
                            ImageView serverStatus = (ImageView) findViewById(R.id.serverStatus);
                            serverStatus.setImageResource(R.drawable.server_status_online);

                            uploadFile.setEnabled(true);
                            uploadFile.setImageAlpha(255);
                            responseText.setText(response.body().string());
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    void postPredictionRequest(String postURL, RequestBody requestBody)
    {
        TextView responseText = findViewById(R.id.fileStatus);
        responseText.setText("Uploading File To Server...");

        TextView predictionText = findViewById(R.id.prediction);
        predictionText.setText("");

        uploadFile.setEnabled(false);
        uploadFile.setImageAlpha(75);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postURL)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {

                // Cancel the post on failure.
                call.cancel();
                serverConnection();
                e.printStackTrace();

                runOnUiThread(new Runnable()
                {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run()
                    {
                        //uploadFile.setEnabled(false);
                        TextView responseText = findViewById(R.id.fileStatus);
                        responseText.setText("File Failed To Upload");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        uploadFile.setEnabled(true);
                        uploadFile.setImageAlpha(255);
                        try
                        {
                            String prediction = response.body().string();

                            TextView responseText = findViewById(R.id.fileStatus);
                            responseText.setText("");

                            TextView predictionText = findViewById(R.id.prediction);
                            predictionText.setText("Prediction:\n"+prediction);

                            db.open();
                            Long rowID = addRow(prediction);
                            db.close();

                            serverConnection();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 10 && resultCode == RESULT_OK)
        {
            String path = data.getData().getPath();

            File file = new File(path);
            String contentType = getMimeType(path);
            String fileName = file.getName();
            String filePath = "/sdcard/Music/"+fileName;
            File file2 = new File(filePath);

            Toast.makeText(this, "File "+fileName+ " selected", Toast.LENGTH_LONG).show();
            Button playFile = findViewById(R.id.playFile);
            playFile.setVisibility(View.VISIBLE);
            playFile.setText("Play "+fileName);

            globalFile = file2;

            final MediaPlayer player = MediaPlayer.create(this, Uri.parse(filePath));

            playFile.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    player.start();
                }
            });


            RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file2);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type", contentType)
                    .addFormDataPart("uploadFile", fileName, fileBody)
                    .build();

            String postURL = getUploadURL();

            postPredictionRequest(postURL, requestBody);
        }
    }

    private String getMimeType(String path)
    {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    //Insert statement to insert row into table. Row Id is returned
    public long addRow(String prediction)
    {
        long id;
        String fileName = globalFile.getName();
        String filePath = globalFile.getAbsolutePath();
        String formattedDate =
                new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(new Date());

        String formattedTime =
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        id = db.insertPrediction(
                fileName,
                filePath,
                prediction,
                formattedDate,
                formattedTime);
        return id;
    }
}