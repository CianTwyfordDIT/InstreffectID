package android_app.instrumentid;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
{

    TextView pathText;
    Button uploadFile;
    Intent fileIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathText = (TextView) findViewById(R.id.pathText);
        uploadFile = (Button) findViewById(R.id.uploadFile);

        uploadFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                startActivityForResult(fileIntent, 10);

            }
        });
        serverConnection();
    }

    void serverConnection()
    {
        String IPAddress = "192.168.0.25";
        String port = "5000";
        String postURL = "http://"+IPAddress+":"+port+"/";
        String postText="Connection Test From Android";

        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");
        RequestBody post_Text = RequestBody.create(mediaType, postText);

        postRequest(postURL, post_Text);
    }

    void postRequest(String postURL, RequestBody postText)
    {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postURL)
                .post(postText)
                .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {

                // Cancel the post on failure.
                call.cancel();

                e.printStackTrace();

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        TextView responseText = findViewById(R.id.response);
                        responseText.setText("Failed to Connect to Flask Server");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch (requestCode)
        {
            case 10:

                if (resultCode == RESULT_OK)
                {
                    String path = data.getData().getPath();
                    pathText.setText(path);
                }

                break;
        }
    }
}