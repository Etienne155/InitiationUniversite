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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

    AnimatorSet movingButton;
    ValueAnimator buttonXAnimation;
    ValueAnimator buttonYAnimation;

    //final int[] levels = {-1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    final int[] levels = {-1, 0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33};

    static boolean executed;
    static int count;
    static int random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set title
        setTitle("Activités d'acceuil DIRO");

        // Init count
        final SharedPreferences shared = getSharedPreferences(pref, MODE_PRIVATE);
        count = shared.getInt("Count", 0);
        executed = shared.getBoolean("Executed", false);

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
        random = 0;
        duration = -1;

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
                if (getRandomInt(0, 8) == 0) {
                    Toast.makeText(getApplicationContext(),
                            messages[getRandomInt(0, messages.length - 1)],
                            Toast.LENGTH_LONG).show();
                }

                // Sauvegarder la valeur count
                SharedPreferences.Editor editor = shared.edit();
                editor.putInt("Count", count);
                editor.apply();

                if (count == levels[11]) {
                    Log.d("level", "EXECUTED TRUE, count:" + count);

                    button.setText("***Réponse finale***");
                    stopAnimation();

                    executed = true;
                    editor.putBoolean("Executed", executed);
                    editor.apply();
                }

                if (!executed) {

                    if (count > levels[3]) {
                        changeBackground();
                    }

                    if (count > levels[5]) {
                        changePosition();
                    }

                    if (count == levels[6] || count == levels[7]) {
                        reCAPTCHA();
                    }

                    if (count > levels[7] + 1) {
                        bounceAnimation();
                    }

                    if (count == levels[3] + 1) {
                        button.setText("***Haskell 4 life***");
                    }

                    if (count == levels[5] + 1) {
                        Toast.makeText(getApplicationContext(),
                                "Oups, passé tout droit, continuer", Toast.LENGTH_LONG)
                                .show();
                    }

                    if (count == levels[8] + 1) {
                        Toast.makeText(getApplicationContext(),
                                "Va falloir travailler un peu plus...", Toast.LENGTH_LONG)
                                .show();
                    }

                }
            }
        });
    }

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.count_reset) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Êtes-vous sûre de vouloir réinitialiser le compteur?")
                   .setTitle("Réinitialisation du compteur");

            builder.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Log.d("level", "REINITIALISER");

                    // Init parameters
                    count = 0;
                    textView.setText("0");
                    xBias = 0.5f;
                    yBias = 0.5f;
                    duration = -1;
                    executed = false;

                    stopAnimation();

                    // Init count var
                    SharedPreferences shared = getSharedPreferences(pref, MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putInt("Count", 0);
                    editor.apply();
                }
            });

            builder.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("level", "PAS REINITIALISER");

                    // do nothing
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * Change button position
     */
    public void changePosition() {
        Log.d("level", "changePosition");

        xBias = getRandomFloat();
        yBias = getRandomFloat();
        cSet.setVerticalBias(R.id.buttonCount, xBias);
        cSet.setHorizontalBias(R.id.buttonCount, yBias);
        cSet.applyTo(cLayout);
    }

    /*
     * Put button back to center
     */
    public void resetPosition() {
        Log.d("level","resetPosition");

        stopAnimation();
        xBias = 0.5f;
        yBias = 0.5f;
        cSet.setVerticalBias(R.id.buttonCount, xBias);
        cSet.setHorizontalBias(R.id.buttonCount, yBias);
        cSet.applyTo(cLayout);
    }

    /*
     * Change background color
     */
    public void changeBackground() {
        Log.d("level", "changeBackground");

        int red = getRandomInt(0, 255);
        int green = getRandomInt(0, 255);
        int blue = getRandomInt(0, 255);
        lLayout.setBackgroundColor(Color.argb(255, red, green, blue));
    }

    /*
     * Reset background color
     */
    public void resetBackground() {
        Log.d("level", "resetBackground");

        lLayout.setBackgroundResource(android.R.color.background_light);
    }

    /*
     * Change orientation
     */
    public void changeOrientation() {
        Log.d("random","changeOrientation");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /*
     * Reset orientation
     */
    public void resetOrientation() {
        Log.d("level","resetOrientation");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    public void reCAPTCHA() {
        Log.d("level","reCAPTCHA");

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


    /*
     * Get random int
     */
    public int getRandomInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    /*
     * Get random float
     */
    public float getRandomFloat() {
        Random rand = new Random();
        return rand.nextFloat();
    }

    /*
     * Start animations
     */
    public void startAnimations(float targetXBias, float targetYBias) {
        Log.d("level", "startAnimations");

        // stopping previous animation
        if (movingButton != null) {
            movingButton.removeAllListeners();
        }

        // creating animations
        buttonXAnimation = ValueAnimator.ofFloat(xBias, targetXBias);
        buttonYAnimation = ValueAnimator.ofFloat(yBias, targetYBias);

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

    /*
     * Bounce animation
     */
    public void bounceAnimation() {
        Log.d("level", "bounceAnimation");

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

    /*
     Stop animation
     */
    public void stopAnimation() {
        Log.d("level", "stopAnimation");

        if (movingButton != null) {
            movingButton.removeAllListeners();
            duration = -1;
        }
    }

}
