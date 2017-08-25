package com.etien.initiationuniversite;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Random;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity implements Executor {

    String pref = "PREFS";
    TextView textView;
    Button button;
    LinearLayout lLayout;
    ConstraintLayout cLayout;
    ConstraintSet cSet;

    double screenRatio;
    float xBias;
    float yBias;
    long duration;
    int orientation;

    AnimatorSet movingButton;
    ValueAnimator buttonXAnimation;
    ValueAnimator buttonYAnimation;

    final int[] levels = {-1, 0, 100, 200, 250, 300, 350, 400, 450, 500, 550, 585, 650};
    final int[] challenges = {650};

    static int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init count
        final SharedPreferences shared = getSharedPreferences(pref, MODE_PRIVATE);
        count = shared.getInt("Count", 0);
//        SharedPreferences.Editor editor = shared.edit();
//        editor.putInt("Count", 0);
//        editor.apply();

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

        duration = -1;
        orientation = 0;

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

                boolean excecuted = false;
                switch (count) {
                    case 650:
                        resetBackground();
                        resetOrientation();
                        duration = 1;
                        bounceAnimation();
                        excecuted = true;
                        break;
                }

                for (int i = 0; i < levels.length && !excecuted; i++) {
                    if (count == levels[i]) {
                        reCAPTCHA();
                        excecuted = true;
                    }
                }

                if (!excecuted) {
                    if (count > levels[11]) {
                        resetBackground();
                        resetOrientation();
                        changePosition();
                        bounceAnimation();
                    } else if (count > levels[10]) {
                        stopAnimation();
                        resetBackground();
                        resetPosition();
                        resetOrientation();
                    } else if (count > levels[9]) {
                        changeBackground();
                        if (getRandomFloat() < 0.5f) {
                            bounceAnimation();
                        } else {
                            stopAnimation();
                        }
                        if (getRandomFloat() < 0.5f) {
                            changePosition();
                        }
                        if (getRandomFloat() < 0.5f) {
                            changeOrientation();
                        }
                        if (getRandomFloat() < 0.15f) {
                            reCAPTCHA();
                        }
                    } else if (count > levels[8]) {
                        changeBackground();
                        changePosition();
                        bounceAnimation();
                        changeOrientation();
                    } else if (count > levels[7]) {
                        changeBackground();
                        resetOrientation();
                        changePosition();
                        bounceAnimation();
                    } else if (count > levels[6]) {
                        changeBackground();
                        changeOrientation();
                        bounceAnimation();
                    } else if (count > levels[5]) {
                        changeBackground();
                        resetOrientation();
                        bounceAnimation();
                    } else if (count > levels[4]) {
                        changeBackground();
                        changePosition();
                        changeOrientation();
                    } else if (count > levels[3]) {
                        changeBackground();
                        resetOrientation();
                        changePosition();
                    } else if (count > levels[2]) {
                        changeBackground();
                        resetOrientation();
                        resetPosition();
                    } else if (count > levels[1]) {
                        resetBackground();
                        resetOrientation();
                        resetPosition();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.count_reset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Êtes-vous sûre de vouloir réinitialiser le compteur?")
                   .setTitle("Réinitialisation du compteur");

            builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    count = 0;
                    textView.setText("0");

                    SharedPreferences shared = getSharedPreferences(pref, MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putInt("Count", 0);
                    editor.apply();
                }
            });

            builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changePosition() {
        // Change button position
        // Random width change
        stopAnimation();
        xBias = getRandomFloat();
        yBias = getRandomFloat();
        cSet.setVerticalBias(R.id.buttonCount, xBias);
        cSet.setHorizontalBias(R.id.buttonCount, yBias);
        cSet.applyTo(cLayout);
    }

    public void resetPosition() {
        // Put button back to center
        stopAnimation();
        xBias = 0.5f;
        yBias = 0.5f;
        cSet.setVerticalBias(R.id.buttonCount, xBias);
        cSet.setHorizontalBias(R.id.buttonCount, yBias);
        cSet.applyTo(cLayout);
    }

    public void changeBackground() {
        // change background color
        int red = getRandomInt(0, 255);
        int green = getRandomInt(0, 255);
        int blue = getRandomInt(0, 255);
        lLayout.setBackgroundColor(Color.argb(255, red, green, blue));
    }

    public void resetBackground() {
        lLayout.setBackgroundResource(android.R.color.background_light);
    }

    public void changeOrientation() {
        switch (++orientation % 4) {
            case 0:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case 2:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case 3:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
        }
    }

    public void resetOrientation() {
        orientation = 0;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void reCAPTCHA() {
        SafetyNet.getClient(this).verifyWithRecaptcha("6LfSTCoUAAAAADXo17hcVLx60yK7PHinMLhuUxpZ")
                .addOnSuccessListener((Executor) this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Validate the user response token using the
                                    // reCAPTCHA siteverify API.
                                    Log.d("reCAPTCHA", "reCAPTCHA solved successfully");
                                }
                            }
                        })
                .addOnFailureListener((Executor) this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d("reCAPTCHA", "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d("reCAPTCHA", "Error: " + e.getMessage());
                        }
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

    public void startAnimations(float targetXBias, float targetYBias) {
        // stopping previous animation
        if (movingButton != null) {
            movingButton.removeAllListeners();
        }

        // creating animations
        buttonXAnimation = ValueAnimator.ofFloat(xBias, targetXBias);
        buttonYAnimation = ValueAnimator.ofFloat(yBias, targetYBias);

//        long duration = (long) Math.sqrt(Math.pow((targetXBias - xBias) * screenRatio, 2) + Math.pow(targetYBias - yBias, 2) * 100000.0);

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
        if (duration > 0) {
            if(getRandomFloat() < 0.30f) {
                duration++;
            }
            movingButton.setDuration(duration);
        } else {
            movingButton.setDuration(1000);
        }

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

    public void stopAnimation() {
        if (movingButton != null) {
            movingButton.removeAllListeners();
            duration = -1;
        }
    }
    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }
}
