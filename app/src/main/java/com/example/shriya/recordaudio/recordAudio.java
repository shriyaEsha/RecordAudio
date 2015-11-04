package com.example.shriya.recordaudio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static com.example.shriya.recordaudio.R.layout.recordaudio;

//import android.support.v4.app.Fragment;

public class recordAudio extends FragmentActivity {
    public int index = 0;
    String selectedFromList;
    ArrayList<String> FilesInFolder;
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    public String outputFile = null;
    private Button stopBtn;
    String[] menu;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    LoginDataBaseAdapter loginDataBaseAdapter;
    String address,subject;
    public void start(View view) {

        //recording
        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        outputFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/Music/MyRecords/" + UUID.randomUUID().toString() + ".flac";
        myRecorder.setOutputFile(outputFile);
        Log.w("app", "Oye " + outputFile);

        stopBtn = (Button) findViewById(R.id.stop);
        stopBtn.setVisibility(View.VISIBLE);

        refresh();

        try {
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

    }

    public void stop(View view) {
        //incr index of stored record

        stopBtn.setVisibility(View.INVISIBLE);
        refresh();

        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder = null;
        } catch (IllegalStateException e) {
            //  it is called before start()
            e.printStackTrace();
        } catch (RuntimeException e) {
            // no valid audio/video data has been received
            e.printStackTrace();
        }
    }

    public void play(View view) {
        try {

            myPlayer.setDataSource(outputFile);
            myPlayer.prepare();
            myPlayer.start();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopPlay(View view) {
        try {
            if (myPlayer != null) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        f.mkdirs();
        File[] files = f.listFiles();
        if (files.length == 0)
            return null;
        else {
            for (int i = 0; i < files.length; i++)
                MyFiles.add(files[i].getName());
        }

        return MyFiles;
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        String[] menuItems;
        String[] menuItems1;
        if (v.getId() == R.id.listview) {

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                menu.setHeaderTitle("YOUR OPTIONS");

                menuItems = getResources().getStringArray(R.array.menu);
                for (int i = 0; i < menuItems.length; i++)
                    menu.add(Menu.NONE, i, i, menuItems[i]);


            }

        }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Log.w("app","Item index: "+menuItemIndex);
        Resources res= getResources();
        String[] menuItems = res.getStringArray(R.array.menu);
        String menuItemName = menuItems[menuItemIndex];

        String listItemName = FilesInFolder.get(info.position);

        Log.w("app", "Item id selected: " + menuItemName);
        Log.w("app", "File selected: " + selectedFromList);
        System.out.println(selectedFromList);

            switch (menuItemName) {
                case "EDIT MY NAME":
                    try {
                        final android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

                        final File oldfile = new File(selectedFromList);
                        final Context context = getApplicationContext();
                        AlertDFragment alertdFragment = new AlertDFragment();
                        alertdFragment.show(fm, "Rename your audio to:");
                        //editText

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;

                case "DELETE ME":
                    //Toast t=Toast.makeText(MainActivity.this,"Play me!",Toast.LENGTH_LONG);
                    try {
                        File f = new File(selectedFromList);
                        Log.w("app", "Deleting " + selectedFromList);
                        f.delete();

                        refresh();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return true;

                case "PLAY":
                    Context context = this;
                    Log.w("app","in listenrecord");
                    FLACPlayer mFlacPlayer = new FLACPlayer(context, selectedFromList);
                    mFlacPlayer.start();
                    return true;
                case "TRANSCRIBE":
                    Log.w("app","In transcribe");
                    Intent myIntent = new Intent(getApplicationContext(), recordNew.class);
                    myIntent.putExtra("fileName", selectedFromList);

                    startActivity(myIntent);
                    break;
                case "OPEN PDF":
                    try {
                        File file = new File(selectedFromList);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        Log.w("app", "opening pdf");
                        startActivity(intent);
                    } catch (Exception e) {
                    }
                    return true;



            }

        return true;
    }
    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(recordaudio);

        //nav drawer


//creating new directory called MyRecords

        File myrecords = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/MyRecords");
        myrecords.mkdirs();

        //set output file pop up



        final ListView listview = (ListView) findViewById(R.id.listview);
        listview.setBackgroundColor(Color.argb((int) 0.7, 200, 50, 50));

        final ArrayList<String> list = new ArrayList<String>();
        FilesInFolder = GetFiles("/sdcard/Music/MyRecords/");
        if (FilesInFolder != null) {
            for (int i = 0; i < FilesInFolder.size(); ++i) {
                list.add(FilesInFolder.get(i));
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter(recordAudio.this,
                    android.R.layout.simple_list_item_1, list);
            listview.setAdapter(adapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    refresh();
                    final String item = (String) parent.getItemAtPosition(position);
                    selectedFromList = Environment.getExternalStorageDirectory().
                            getAbsolutePath() + "/Music/MyRecords/" + (String) (listview.getItemAtPosition(position));

                    Log.w("app", "select list view item: " + selectedFromList);
                    registerForContextMenu(listview);


                }

            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.w("app","Kill audio");
            Context context = this;
            Log.w("app","in listenrecord");
            finish();
           return true;


        }
        return super.onKeyDown(keyCode, event);
    }
    public void refresh()
    {
        //refresh List
        FilesInFolder = GetFiles("/sdcard/Music/MyRecords/");
        Log.w("app","In refresh");
        final ListView listview = (ListView) findViewById(R.id.listview);


        final ArrayList<String> list = new ArrayList<String>();

        if(FilesInFolder!=null) {
            Log.w("app", "Files in folder not null");
            for (int i = 0; i < FilesInFolder.size(); ++i) {
                list.add(FilesInFolder.get(i));
            }
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           int position, long id) {
                refresh();
                final String item = (String) parent.getItemAtPosition(position);
                selectedFromList = Environment.getExternalStorageDirectory().
                        getAbsolutePath() + "/Music/MyRecords/" + (String) (listview.getItemAtPosition(position));

                Log.w("app", "select list view item: " + selectedFromList);
                Toast t = Toast.makeText(recordAudio.this, "Play me!", Toast.LENGTH_LONG);

                registerForContextMenu(listview);
                return false;
            }

        });


    }


    class AlertDFragment extends android.support.v4.app.DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Context context = getApplicationContext();
            final EditText input= new EditText(context);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            input.setLayoutParams(lp);

            final File oldfile = new File(selectedFromList);



            AlertDialog dia= new AlertDialog.Builder(getActivity())
                    // Set Dialog Icon
                    .setTitle("Type in Your Audio's Title Right Here")
                            // Set Dialog Message
                    .setView(input)

                            // Positive button
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something else
                            String filename = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyRecords/"+input.getText().toString()+".flac";
                            final File newfile = new File(filename);
                            oldfile.renameTo(newfile);
                            refresh();

                            Toast toast = Toast.makeText(context,"Yay you've done it! "+ newfile, Toast.LENGTH_LONG);
                            toast.show();


                        }
                    })

                            // Negative Button
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Do something else
                            Context context = getApplicationContext();
                            CharSequence text = "You chickened out?";
                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();

                        }

                    }).create();
            return dia;

        }
    }




}


