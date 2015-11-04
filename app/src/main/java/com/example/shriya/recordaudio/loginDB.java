package com.example.shriya.recordaudio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by shriya on 1/28/2015.
 */
public class loginDB extends Activity {

    LoginDataBaseAdapter loginDataBaseAdapter;
    Button loginbut;
    Button signupbtn;
    Switch switchpass;
   private EditText u;
    private EditText p;
    String username,pass;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logindb);

        loginDataBaseAdapter = new LoginDataBaseAdapter(this);
        loginDataBaseAdapter = loginDataBaseAdapter.open();

        u = (EditText) findViewById(R.id.user);
        p = (EditText) findViewById(R.id.password);
        switchpass = (Switch) findViewById(R.id.showpass);
        switchpass.setChecked(false);
        switchpass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (!isChecked) {
                    p.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    p.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });


        loginbut = (Button) findViewById(R.id.loginbtn);
        loginbut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                username = u.getText().toString();
                pass = p.getText().toString();
                Log.w("app", "user: " + username + " pass: " + pass);

                validate();
            }
        });

        signupbtn = (Button) findViewById(R.id.signup);
        signupbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                username = u.getText().toString();
                pass = p.getText().toString();
                Log.w("app", "user: " + username + " pass: " + pass);

                register();
            }
        });
        if (loginDataBaseAdapter.getLogged() == 0) {
            Log.w("app", "must register. no one logged in!");
            register();
        }
        checkLoggedIn();

    }

    private void checkLoggedIn() {
        String hello = "yes";
        String result=loginDataBaseAdapter.getLoggedIn();
        if(result.equals("register"))
            register();
        else if(result.equals("logged"))

        {
            Intent ii1 = new Intent(loginDB.this, MainActivity.class);
            startActivity(ii1);
            finish();
        }
        else if(result.equals("not logged"))
        {
            Toast.makeText(loginDB.this, "Login Now! You're not Logged in yet.", Toast.LENGTH_LONG).show();


        }

    }


    public void validate()
    {
        Log.w("app","validating...");

        String result=loginDataBaseAdapter.getSingleEntry(username,pass);

        if(result.equals("SUCCESS"))
        {
            Toast.makeText(loginDB.this, "Yay! Your Login was Successful", Toast.LENGTH_LONG).show();
            Intent ii=new Intent(loginDB.this,MainActivity.class);
            startActivity(ii);
            finish();

        }
        else
        if(result.equals("INVALID")){
            Toast.makeText(loginDB.this, "Please Reenter your details", Toast.LENGTH_LONG).show();
            u.setText("");
            p.setText("");
            u.requestFocus();
        }
    }



    public void register()
    {
        username=u.getText().toString();
        pass=p.getText().toString();


        if(pass.equals("") || username.equals(""))
        {
            Toast.makeText(getApplicationContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }
        loginDataBaseAdapter.insertEntry(username,pass);

        // reg_btn.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(), "Account Successfully Created ", Toast.LENGTH_LONG).show();
        Log.w("app", "registered");
        Intent intent1 = new Intent(this, MainActivity.class);
        startActivity(intent1);
        finish();


    }



}
