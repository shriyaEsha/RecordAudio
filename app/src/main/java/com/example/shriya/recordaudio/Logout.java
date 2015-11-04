package com.example.shriya.recordaudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by shriya on 2/4/2015.
 */
public class Logout extends Activity {
    LoginDataBaseAdapter loginDataBaseAdapter;
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
        loginDataBaseAdapter = new LoginDataBaseAdapter(this);
        loginDataBaseAdapter=loginDataBaseAdapter.open();

        loginDataBaseAdapter.logout();
        Toast.makeText(getApplicationContext(),"Logged Out Successfully!",Toast.LENGTH_LONG).show();

        Intent i = new Intent(getApplicationContext(),loginDB.class);
        startActivity(i);
        finish();
    }

    }
