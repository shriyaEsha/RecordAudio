package com.example.shriya.recordaudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.ArrayList;

public class TranscribeLive extends FragmentActivity implements RecognitionListener {
    private int savedFile=0;
    AudioManager amanager;
    Button saveme,shareme,clearme;
    private TextView returnedText;
    private Switch switch1;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private int switched=0;
    private String name="";
    String FILE;
    String[] menu;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    LoginDataBaseAdapter loginDataBaseAdapter;
    String selected;
    Context mcontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transcribelive);


        //spinner
        Spinner customSpinner = (Spinner) findViewById(R.id.customspinner);
        customSpinner.setOnItemSelectedListener(new Languages(""));

        CustomAdapter adapter = new CustomAdapter(this,
                android.R.layout.simple_spinner_item,
                populateReindeer());
        customSpinner.setAdapter(adapter);



        returnedText = (TextView) findViewById(R.id.editme);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        switch1 = (Switch) findViewById(R.id.startRec);
       amanager = (AudioManager) getSystemService(AUDIO_SERVICE);
      saveme=(Button) findViewById(R.id.saveText);
        clearme=(Button) findViewById(R.id.clearText);
        clearme=(Button) findViewById(R.id.clearText);
        shareme = (Button) findViewById(R.id.shareText);
        saveme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile();
                 }




        });
        mcontext=this.getApplicationContext();

        returnedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) mcontext.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(returnedText.getText());
                Toast.makeText(mcontext, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        shareme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("application/pdf");
                    if (savedFile == 0) {
                        Toast.makeText(getApplicationContext(), "Save your transcript first!", Toast.LENGTH_SHORT);
                        saveFile();
                    } else {
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Transcript");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, returnedText.getText().toString());
                        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(FILE));

                        startActivity(Intent.createChooser(sharingIntent, "Share Pdf"));
                    }
                }

                catch(Exception e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }


        });

        clearme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                returnedText.setText("");

                Toast toast = Toast.makeText(getApplicationContext(),"Cleared Transcript", Toast.LENGTH_LONG);
                toast.show();


            }
        });



        startDictation();

    }
    public void saveFile(){
        try{
            final android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

            //  final File oldfile= new File(selectedFromList);
            final Context context = getApplicationContext();
            AlertDFragment alertdFragment = new AlertDFragment();
            alertdFragment.show(fm, "Rename your transcript to:");
            //editText

        }

        catch(Exception e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
public void startDictation()
{   progressBar.setVisibility(View.INVISIBLE);
    speech = SpeechRecognizer.createSpeechRecognizer(this);
    speech.setRecognitionListener(this);

    //add dropdown list and recognize other languages
    Log.w("app","selected lang: "+selected);
    Toast.makeText(TranscribeLive.this,"selected language: "+selected,Toast.LENGTH_LONG).show();
    recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,selected);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,  RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selected);

    recognizerIntent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, selected);
    recognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"en_UK","fr_FR"});

  //  recognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"en-UK","en-US","en-IN","hi_IN","fr_FR"});
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {

            if(!isChecked)
            {
                switched=0;
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.INVISIBLE);
                speech.stopListening();
                amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
                amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

            }
            else
            {   switched=1;
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
                amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

                speech.startListening(recognizerIntent);
            }
        }
    });

}
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("app","paused speech");
        if (speech == null) {
            speech.destroy();
            switched=0;
            Log.i(LOG_TAG, "destroy");
            speech.stopListening();
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        }
        else {
            if (switched == 1) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                speech.startListening(recognizerIntent);
            }
        }


    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        //toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        Toast.makeText(TranscribeLive.this,errorMessage,Toast.LENGTH_SHORT).show();

        //toggleButton.setChecked(false);
        if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH)
                || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT))
        {
            Log.d("app", "didn't recognize anything");
            // keep going
            if(switched==1) {
                startDictation();
                Log.w("app", "keep going!");

                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);
                speech.startListening(recognizerIntent);
            }
            else if(switched==0) {
                progressBar.setVisibility(View.INVISIBLE);
                progressBar.setIndeterminate(false);
                speech.stopListening();

            }

        }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = matches.get(0)+" ";
        returnedText.append(text);
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error. Do you have internet?";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error! Check your internet settings and try again.";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "Speech input has stopped. have you finished?";
                break;
            default:
                message = "Didn't understand, please try again!";
                break;
        }
        return message;
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();

            //moveTaskToBack(false);

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Sure you want to leave?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                    speech.stopListening();
                        speech.destroy();
                        finish();



                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        return;
                    }
                })
                .show();

    }
    class AlertDFragment extends android.support.v4.app.DialogFragment {
        String body="";
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //nav drawer


            final Context context = getApplicationContext();
            final EditText input= new EditText(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            input.setLayoutParams(lp);


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Type in Your Transcript's Title Right Here");
            builder.setView(input);
            name= String.valueOf(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Do something else
                  //  String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/MyRecords/Transcribed/";
                    body = returnedText.getText().toString();
                    Document document=new Document();
                    try {

                        FILE = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music/MyRecords/"+input.getText().toString()+".pdf";

                        document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(FILE));
                        document.open();
                        document.add(new Paragraph(body));
                        document.close();



                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    savedFile=1;
                    Toast toast = Toast.makeText(context, "Yay you've saved it! ", Toast.LENGTH_SHORT);
                    toast.show();


                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Do something else
                    Context context = getApplicationContext();
                    CharSequence text = "You chickened out?";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    finish();

                }

            });
            AlertDialog dia = builder.create();
            return dia;

        }

    }

    private class Languages implements AdapterView.OnItemSelectedListener{

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         selected = parent.getItemAtPosition(pos).toString();
            Toast t=Toast.makeText(getApplicationContext(),"Clicked "+selected,Toast.LENGTH_SHORT);
            t.show();
            startDictation();
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
        private String name;

        Languages(String name){

            this.name = name;

        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {

            return getName();

        }

    }


    class CustomAdapter extends ArrayAdapter<Languages> {

        private Activity context;
        ArrayList<Languages> language;

        public CustomAdapter(Activity context, int resource, ArrayList<Languages> lang) {

            super(context, resource, lang);
            this.context = context;
            this.language = lang;

        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

            View row = convertView;



            if (row == null) {

                LayoutInflater inflater = context.getLayoutInflater();
                row = inflater.inflate(R.layout.spinner_row, parent, false);

            }

            Languages current = language.get(position);

            TextView name = (TextView) row.findViewById(R.id.spinnerText);
            name.setText(current.getName());

            return row;

        }


    }
    private ArrayList<Languages> populateReindeer(){

        final ArrayList<Languages> lang = new ArrayList<Languages>();

        String[] langs=new String[]{"en_UK","en_US","hi","fr_FR","ja","es_US"};

        for(int i=0;i<langs.length;i++)
            lang.add(i,new Languages(langs[i]));

        return lang;

    }



}