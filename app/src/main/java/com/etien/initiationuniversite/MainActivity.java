package com.etien.initiationuniversite;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    String pref = "PREFS";
    TextView textView;
    Button button;
    LinearLayout lLayout;
    ConstraintLayout cLayout;
    ConstraintSet cSet;
    static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init count
        final SharedPreferences shared = getSharedPreferences(pref, MODE_PRIVATE);
        count = shared.getInt("Count", 0);

        // Get views
        textView = (TextView) findViewById(R.id.count);
        button = (Button) findViewById(R.id.buttonCount);
        lLayout = (LinearLayout) findViewById(R.id.linear_layout);
        cLayout = (ConstraintLayout) findViewById(R.id.constraint_layout);

        // Init constraint set
        cSet = new ConstraintSet();
        cSet.clone(cLayout);

        // Init text
        textView.setText("" + count);

        // Button actions
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update count on click
                textView.setText("" + ++count);

                // Change button position
                // Random width change
                cSet.setVerticalBias(R.id.buttonCount, getRandomFloat());
                cSet.setHorizontalBias(R.id.buttonCount, getRandomFloat());
                cSet.applyTo(cLayout);

                // change background color
                int red = getRandomInt(0,255);
                int green = getRandomInt(0,255);
                int blue = getRandomInt(0,255);
                lLayout.setBackgroundColor(Color.argb(255, red, green, blue));

                // Message
                Resources res = getResources();
                String[] messages = res.getStringArray(R.array.messages);
                if (getRandomInt(0, 20) == 0) {
                    Toast.makeText(getApplicationContext(),
                            messages[getRandomInt(0, messages.length - 1)],
                            Toast.LENGTH_LONG).show();
                }

                // Sauvegarder la valeur count
                SharedPreferences.Editor editor = shared.edit();
                editor.putInt("Count", count);
                editor.apply();
            }
        });
    }


    public int getRandomInt(int min, int max) {

        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public float getRandomFloat() {
        Random rand = new Random();
        return rand.nextFloat();
    }
}
