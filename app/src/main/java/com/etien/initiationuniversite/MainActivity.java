package com.etien.initiationuniversite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
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

    double screenRatio;
    Float xBias;
    Float yBias;

    AnimatorSet movingButton;
    ValueAnimator buttonXAnimation;
    ValueAnimator buttonYAnimation;

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

        xBias = 0.5f;
        yBias = 0.5f;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenRatio = (double) metrics.widthPixels/metrics.heightPixels;

        // Init text
        textView.setText("" + count);

        // Button actions
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update count on click
                textView.setText("" + ++count);
                bounceAnimation();

                if (count >= 40 && getRandomInt(0,1) == 0) {
                    // change background color
                    int red = getRandomInt(0, 255);
                    int green = getRandomInt(0, 255);
                    int blue = getRandomInt(0, 255);
                    lLayout.setBackgroundColor(Color.argb(255, red, green, blue));
                }

                if (count % 50 == 0) {
                    // reCAPTCHA
                } else {
                    if (count >= 20) {
                        // Change button position
                        // Random width change
                        cSet.setVerticalBias(R.id.buttonCount, getRandomFloat());
                        cSet.setHorizontalBias(R.id.buttonCount, getRandomFloat());
                        cSet.applyTo(cLayout);
                    }
                }

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

        bounceAnimation();
    }


    public int getRandomInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public float getRandomFloat() {
        Random rand = new Random();
        return rand.nextFloat();
    }

    public void startAnimations(float targetXBias, float targetYBias) {
        // stopping previous animation
        if (movingButton != null) {
            movingButton.removeAllListeners();
        }

        // creating animations
        buttonXAnimation = ValueAnimator.ofFloat(xBias, targetXBias);
        buttonYAnimation = ValueAnimator.ofFloat(yBias, targetYBias);

//        long duration = (long) Math.sqrt(Math.pow((targetXBias - xBias) * screenRatio, 2) + Math.pow(targetYBias - yBias, 2) * 100000.0);
        long duration = 1000;

        // listener to update button position
        buttonXAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                xBias = (float) animation.getAnimatedValue();
                cSet.setHorizontalBias(R.id.buttonCount, xBias);
                cSet.applyTo(cLayout);
            }
        });

        buttonYAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                yBias = (float) animation.getAnimatedValue();
                cSet.setVerticalBias(R.id.buttonCount, yBias);
                cSet.applyTo(cLayout);
            }
        });

        // Animation coordination
        movingButton = new AnimatorSet();
        movingButton.play(buttonXAnimation).with(buttonYAnimation);
        movingButton.setDuration(duration);
        movingButton.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                bounceAnimation();
            }
        });

        // Starting animation
        movingButton.start();
    }

    public void bounceAnimation() {
        int reboundType;

        if (xBias == 0 || xBias == 1) {
            reboundType = getRandomInt(0, 2);
        } else if (yBias == 0 || yBias == 1) {
            reboundType = getRandomInt(0, 2) + 3;
        } else {
            reboundType = getRandomInt(0, 3) + 1;
        }

        switch (reboundType) {
            case 0:
                startAnimations((xBias + 1) % 2, getRandomFloat());
                break;
            case 1:
                startAnimations(getRandomFloat(), 0.0f);
                break;
            case 2:
                startAnimations(getRandomFloat(), 1.0f);
                break;
            case 3:
                startAnimations(0.0f, getRandomFloat());
                break;
            case 4:
                startAnimations(1.0f, getRandomFloat());
                break;
            case 5:
                startAnimations(getRandomFloat(), (yBias + 1) % 2);
                break;
        }
    }
}
