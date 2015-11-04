package com.example.shriya.recordaudio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


/**
 * Created by shriya on 10-02-2015.
 */


public class SplashActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int myTimer = 1000;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, loginDB.class);
                startActivity(i);
                finish(); // close this activity
            }
        }, myTimer);

    }

}