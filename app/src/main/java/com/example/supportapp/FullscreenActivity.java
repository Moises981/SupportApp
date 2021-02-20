package com.example.supportapp;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.supportapp.databinding.ActivityFullscreenBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private static final String F_MAIN_MESSAGE = "main_message";
    private static final String F_SHOW_NAME = "show_name";
    private static final String F_COLOR_PRIMARY = "color_primary";
    private static final String F_COLOR_TEXT_MSG = "color_text_msg";
    private static final String F_COLOR_BTN = "color_btn";


    private ActivityFullscreenBinding bnd;
    private TextView mContentView;
    private FirebaseRemoteConfig mfirebaseRemoteConfig;


    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bnd = bnd.inflate(getLayoutInflater());
        setContentView(bnd.getRoot());
        //etContentView(R.layout.activity_fullscreen);

        mContentView = findViewById(R.id.fullscreen_content);

        configFirebaseRemoteConfig();

        bnd.btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v!= null){
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null){
                        imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    }
                }

                bnd.fullscreenContentControls.requestFocus();
                bnd.etName.setText("");
                bnd.etPhone.setText("");
                mContentView.setText(R.string.Register_turn_message_success);
            }
        });
    }

    private void configFirebaseRemoteConfig() {
        long cacheExpiration = 3600;
        mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(cacheExpiration)
                .build();

        mfirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mfirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default);

        ConfigFetch();



    }

    private void ConfigFetch() {
        mfirebaseRemoteConfig.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    mfirebaseRemoteConfig.fetchAndActivate();
                    Snackbar.make(bnd.fullscreenContentControls,R.string.TurnRegister_message_config_remote,Snackbar.LENGTH_LONG).show();
                }
                else{
                    Snackbar.make(bnd.fullscreenContentControls,R.string.TurnRegister_message_config_local,Snackbar.LENGTH_LONG).show();
                }
            }
        });

        displayMainMessage();
    }

    private void displayMainMessage() {
        bnd.etName.setVisibility(mfirebaseRemoteConfig.getBoolean(F_SHOW_NAME)?View.VISIBLE:View.GONE);
        String message_remote = mfirebaseRemoteConfig.getString(F_MAIN_MESSAGE);
        message_remote = message_remote.replace("\\n","\n");
        mContentView.setText(message_remote);
        displayColor();
    }

    private void displayColor() {
        bnd.contentMain.setBackgroundColor(Color.parseColor(mfirebaseRemoteConfig.getString(F_COLOR_PRIMARY)));
        mContentView.setTextColor(Color.parseColor(mfirebaseRemoteConfig.getString(F_COLOR_TEXT_MSG)));
        Log.d("datad",mfirebaseRemoteConfig.getString(F_COLOR_BTN));
        bnd.btnRequest.setBackgroundColor(Color.parseColor(mfirebaseRemoteConfig.getString(F_COLOR_BTN)));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.postDelayed(mHidePart2Runnable, 100);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}