package com.example.shriya.recordaudio;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import static com.example.shriya.recordaudio.R.layout.activity_main;

//import android.support.v4.app.Fragment;

public class MainActivity extends FragmentActivity {
    String[] menu;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    LoginDataBaseAdapter loginDataBaseAdapter;
    ImageButton app_draw1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(activity_main);
        setContentView(activity_main);

        app_draw1 = (ImageButton) findViewById(R.id.drawer1);

        app_draw1.setOnClickListener(
                new View.OnClickListener() {

                    public void onClick(View v) {

                        dLayout.openDrawer(Gravity.LEFT);
                    }
                }
        );
        //catch OOM error
    //    Thread.currentThread().setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());



        loginDataBaseAdapter = new LoginDataBaseAdapter(this);
        loginDataBaseAdapter = loginDataBaseAdapter.open();
            //sliding drawer

        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        dList = (ListView) findViewById(R.id.left_drawer);
        final String[] names =
                getResources().getStringArray(R.array.drawermenu);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        dList.setAdapter(adapter);
        dList.setSelector(android.R.color.holo_blue_dark);
        dList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                dLayout.closeDrawers();
                Bundle args = new Bundle();
                args.putString("Menu", names[position]);
                //args.remove("LOGIN");
                Fragment detail = new MenuDetailFragment();
                detail.setArguments(args);
                FragmentManager fragmentManager = getFragmentManager();
                switch (names[position]) {
                    // fragmentManager.beginTransaction().replace(R.id.content_frame, detail).commit();
                    case "VIEW FILES":
                        Intent myIntent = new Intent(getApplicationContext(), recordAudio.class);

                        startActivity(myIntent);
                        break;
                    case "TRANSCRIBE LIVE":
                        myIntent = new Intent(getApplicationContext(), TranscribeLive.class);

                        startActivity(myIntent);
                        break;
                    case "RECORD":
                        myIntent = new Intent(getApplicationContext(), recordNew.class);
                        startActivity(myIntent);
                        break;
                    case "LOGOUT":
                        myIntent = new Intent(getApplicationContext(), Logout.class);
                        startActivity(myIntent);
                        finish();
                        break;


                }
            }
        });


    }
/*
    public static class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            if (ex.getClass().equals(OutOfMemoryError.class)) {

                try {
                    Log.w("app", "Memory OVERFLOW!!!!");

                    android.os.Debug.dumpHprofData("/sdcard/dump.hprof");
                } catch (IOException e) {
                  Log.w("app", "Memory OVERFLOW!!!!");


                }
                ex.printStackTrace();
            }


        }
    }
*/

}


