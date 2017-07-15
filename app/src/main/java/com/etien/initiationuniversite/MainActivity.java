package com.etien.initiationuniversite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static android.R.attr.max;

public class MainActivity extends AppCompatActivity {

    String pref = "PREFS";
    TextView textView;
    Button button;
    LinearLayout layout;
    static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init count
        final SharedPreferences shared = getSharedPreferences(pref, MODE_PRIVATE);
        count = shared.getInt("Count",0);

        // Get views
        textView = (TextView) findViewById(R.id.count);
        button = (Button) findViewById(R.id.buttonCount);
        layout = (LinearLayout) findViewById(R.id.layout);

        // Init text
        textView.setText("Nombre : " + count);

        // Get dimensions
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int height = size.y;

        Log.d("dimensions", "width:" + width + " height:" + height);

        // Move button
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        final int maxwidth = width*2/3;
        final int maxheight = height*2/3;

        // Random width change
        int randwidth = getRandomInt(maxwidth,0);
        int randheight = getRandomInt(maxheight,0);

        // left, top, right,bottom
        params.setMargins(randwidth, randheight, 0, 0);
        button.setLayoutParams(params);

        // Button actions
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update count on click
                textView.setText("Nombre : " + count);
                count++;

                // Change button position
                // Random width change
                int randwidth = getRandomInt(maxwidth,0);
                int randheight = getRandomInt(maxheight,0);

                // left, top, right,bottom
                params.setMargins(randwidth, randheight, 0, 0);
                button.setLayoutParams(params);

                // change background color
                int red = getRandomInt(255,0);
                int green = getRandomInt(255,0);
                int blue = getRandomInt(255,0);
                layout.setBackgroundColor(Color.argb(255, red, green, blue));

                // Change button size
                int leftpadding = getRandomInt(300,0);
                int toppadding = getRandomInt(300,0);
                int rightpadding = getRandomInt(300,0);
                int bottompadding = getRandomInt(300,0);
                button.setPadding(leftpadding,toppadding,rightpadding,bottompadding);

                // Message
                Resources res = getResources();
                String[] messages = res.getStringArray(R.array.messages);
                if(getRandomInt(20,0) == 0){
                    Toast.makeText(getApplicationContext(),
                            messages[getRandomInt(messages.length-1,0)],
                            Toast.LENGTH_LONG).show();
                }

                // Sauvegarder la valeur count
                SharedPreferences.Editor editor = shared.edit();
                editor.putInt("Count", count);
                editor.commit();
            }
        });
    }

    public int getRandomInt(int max, int min){

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
