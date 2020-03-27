package android_app.instrumentid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Prediction extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prediction_screen);

        getPrediction();
    }

    void getPrediction()
    {
        Intent intent = getIntent();
        String response = intent.getStringExtra("key");

        TextView predictionText = findViewById(R.id.prediction);
        predictionText.setText(response);
    }
}
