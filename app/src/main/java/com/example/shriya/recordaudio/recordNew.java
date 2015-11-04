package com.example.shriya.recordaudio;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import static android.view.View.OnClickListener;

public class recordNew extends FragmentActivity {
    String[] menu;
    String selected;
    DrawerLayout dLayout;
    ListView dList;
    ArrayAdapter<String> adapter;
    LoginDataBaseAdapter loginDataBaseAdapter;
    private String name = "";
    String FILE;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
    String language = selected;
    private final static byte[] FINAL_CHUNK = new byte[]{'0', '\r', '\n', '\r', '\n'};
    // The key is obtained through the Google Developer Group
    String api_key = "AIzaSyBgnC5fljMTmCFeilkgLsOKBvvnx6CBS0M";

    // Name of the sound file (.flac) format
    String fileName = "";

    // URL for Google API
    String root = "https://www.google.com/speech-api/full-duplex/v1/";
    String dwn = "down?maxresults=1&pair=";
    String API_DOWN_URL = root + dwn;
    String up_p1 = "up?lang=" + language
            + "&lm=dictation&client=chromium&pair=";
    String up_p2 = "&key=";


    // return codes
    private static final long MIN = 10000000;
    private static final long MAX = 900000009999999L;
    long PAIR;


    private int mErrorCode = -1;
    private static final int DIALOG_RECORDING_ERROR = 0;
    // Rate of the recorded sound file
    int sampleRate;
    // Recorder instance
    private Recorder mRecorder;

    // Output for Google response in textbox
    TextView txtView;
    Button recordButton, stopButton, listenButton, saveButton;

    // Handler used for sending request to Google API
    Handler handler = new Handler();
    // Recording callbacks
    private Handler mRecordingHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message m) {
            switch (m.what) {
                case FLACRecorder.MSG_AMPLITUDES:
                    FLACRecorder.Amplitudes amp = (FLACRecorder.Amplitudes) m.obj;

                    break;

                case FLACRecorder.MSG_OK:
                    // Ignore
                    break;

                case Recorder.MSG_END_OF_RECORDING:

                    break;

                default:
                    mRecorder.stop();
                    mErrorCode = m.what;
                    //showDialog(DIALOG_RECORDING_ERROR);
                    break;
            }

            return true;
        }
    });

    // DOWN handler - handling response from the server
    Handler messageHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.w("app", "Init handler msg.what: " + msg.what);
            switch (msg.what) {
                case 1: // GET DOWNSTREAM json id="@+id/comment"
                    String mtxt = msg.getData().getString("text");


                    if (mtxt.length() > 1) {
                        final String f_msg = mtxt;
                        handler.post(new Runnable() { // This thread runs in the UI
                            // TREATMENT FOR GOOGLE RESPONSE
                            @Override
                            public void run() {
                                //initially, display only empty result
                                Log.w("app", "The message in txtview: " + f_msg);
                                txtView.setText(f_msg);
                            }
                        });
                    }
                    break;
                case 2:
                    break;
            }
        }
    };

    // upstream channel which sends the http request to the server
    Handler messageHandler2 = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: // GET DOWNSTREAM json
                    Log.d("ParseStarter", msg.getData().getString("post"));
                    break;
                case 2:
                    Log.d("ParseStarter", msg.getData().getString("post"));
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recordnew);
        //spinner

        Spinner customSpinner = (Spinner) findViewById(R.id.customspinner);
        customSpinner.setOnItemSelectedListener(new Languages(""));

        CustomAdapter adapter = new CustomAdapter(this,
                android.R.layout.simple_spinner_item,
                populateReindeer());
        customSpinner.setAdapter(adapter);
        progressDialog = new ProgressDialog(this, R.style.progDialog);


        //nav drawer


        txtView = (TextView) this.findViewById(R.id.txtView);
        recordButton = (Button) this.findViewById(R.id.record);
        stopButton = (Button) this.findViewById(R.id.stop);
        stopButton.setEnabled(false);
        listenButton = (Button) this.findViewById(R.id.listen);
        listenButton.setEnabled(false);
        saveButton = (Button) this.findViewById(R.id.saveme);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setBackgroundResource(R.drawable.borderbottom);
                listenButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
                recordButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
                stopButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));

                saveFile();
            }


        });
        listenButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                listenButton.setBackgroundResource(R.drawable.borderbottom);
                stopButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
                recordButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
                saveButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
/*

                Log.w("app","in listenrecord");
                FLACPlayer mFlacPlayer = new FLACPlayer(this, fileName);
                mFlacPlayer.start();
                */

            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        mRecorder = new Recorder(this, mRecordingHandler);

    }

    //records flac audio files
    public void recordButton(View v) {

        recordButton.setBackgroundResource(R.drawable.borderbottom);
        stopButton.setBackgroundColor(Color.parseColor("#ffffff"));
        listenButton.setBackgroundColor(Color.parseColor("#ffffff"));
        saveButton.setBackgroundColor(Color.parseColor("#ffffff"));

        Intent intent = getIntent();
        try {
            if (intent.getExtras().getString("fileName") != null) {
                fileName = intent.getExtras().getString("fileName");
                Log.w("app", "in recordButton: " + fileName);
                recordButton.setText("START");
                recordButton.setEnabled(false);

                Log.w("app", "file already selected: " + fileName + " Calling stop recording");
                recordButton.setEnabled(false);
                saveButton.setEnabled(true);
                //stopButton.setEnabled(true);
                sampleRate = 44100;
                getTranscription(sampleRate);

            }

        }
        catch(NullPointerException e){

            Log.w("app", "file not selected. Must record");

            fileName = Environment.getExternalStorageDirectory() + "/Music/MyRecords/" + UUID.randomUUID() + ".flac";
            mRecorder.start(fileName);

            txtView.setText("");
            recordButton.setEnabled(false);
            stopButton.setEnabled(true);
            Toast.makeText(getApplicationContext(), "Recording...",
                    Toast.LENGTH_SHORT).show();
            sampleRate = 44100;
            getTranscription(sampleRate);
            recordButton.setText("RECORD");



        }

            recordButton.setEnabled(true);
            stopButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
            listenButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
            saveButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
            recordButton.setText("RECORD");

        }


    public void stopRecording(View v) {
        stopButton.setBackgroundResource(R.drawable.borderbottom);
        saveButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
        recordButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));
        listenButton.setBackgroundColor(Color.parseColor("#ff1d2aff"));

        Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_LONG)
                .show();
        recordButton.setEnabled(true);
        listenButton.setEnabled(true);
        saveButton.setEnabled(true);

        sampleRate = mRecorder.mFLACRecorder.getSampleRate();
        getTranscription(sampleRate);
        mRecorder.stop();




    }
    public void listenRecord(View v) {
        Context context = this;
        Log.w("app","in listenrecord");
        FLACPlayer mFlacPlayer = new FLACPlayer(context, fileName);
        mFlacPlayer.start();

    }

    //actual transcription

    public int min(int a,int b)
    {
        return a<b?a:b;
    }
    public void getTranscription(int sampleRate) {

        File myfil = new File(fileName);
        Log.w("app","In transcription, reading file: "+fileName);
        if (!myfil.canRead())
            Log.d("ParseStarter", "FATAL no read access");


        PAIR = MIN + (long) (Math.random() * ((MAX - MIN) + 1L));

        // DOWN URL just like in curl full-duplex example plus the handler
        downChannel(API_DOWN_URL + PAIR, messageHandler);

        // UP chan, process the audio byteStream for interface to UrlConnection
        // using 'chunked-encoding'
        FileInputStream fis;
        try {

            fis = new FileInputStream(myfil);
            FileChannel fc = fis.getChannel(); // Get the file's size and then
            // map it into memory
            int sz = (int) fc.size();
            Log.w("app", "File size: " + sz);
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

            byte[] data2 = new byte[bb.remaining()];


            Log.w("app","bb,remaining: "+bb.remaining());
            Log.d("ParseStarter", "mapfil " + sz + " " + bb.remaining());
            bb.get(data2);

            upChannel(root + up_p1 + PAIR + up_p2 + api_key, messageHandler2,
                    data2);
            //}
        }catch(FileNotFoundException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private void downChannel(String urlStr, final Handler messageHandler) {

        final String url = urlStr;

        new Thread() {
            Bundle b;

            public void run() {
                String response = "NAO FOI";
                Message msg = Message.obtain();
                msg.what = 1;
                Scanner inStream = openHttpsConnection(url);
                if(inStream == null)
                {
                    Log.w("app","Connection Error");
                    return;
                }
                // process the stream and store it in StringBuilder
                while (inStream.hasNextLine()) {

                        recordNew.this.runOnUiThread(new Runnable() {
                            public void run() {

                                progressDialog.setTitle("Hold on! We're transcribing it for you!");
                                progressDialog.setMessage("hang on");
                                progressDialog.setCancelable(false);
                                progressDialog.show();

                            }
                        });

                      response = inStream.nextLine();
                    if(response.length()>17) {
                        Log.w("app","Parsing the response");
                        response = parseResponse(response);
                    }
                        b = new Bundle();
                    b.putString("text", response);
                    msg.setData(b);
                    Log.w("app","Dispatching the msg");
                    messageHandler.dispatchMessage(msg);
                }
                progressDialog.dismiss();

            }
        }.start();
    }
//parse response and extract words alone
    public String parseResponse(String response)
    {   Log.w("app","Response before: "+response);
        if(response.length()>17 && response.contains("confidence"))
            response=response.substring(42,response.indexOf("confidence")).replaceAll("\"","").replaceAll(",","");
        else if(response.length()>17 && !response.contains("confidence"))
            response=response.substring(42,response.indexOf("}")).replaceAll("\"","").replaceAll(",","");

        Log.w("app","Response after: "+response);
        return response;
    }

    private void upChannel(String urlStr, final Handler messageHandler,
                           byte[] arg3) {

        final String murl = urlStr;
        final byte[] mdata = arg3;
        Log.d("ParseStarter", "upChan " + mdata.length);


        new Thread() {
            public void run() {
                String response = "NAO FOI";
                Message msg = Message.obtain();
                msg.what = 2;
                Scanner inStream = openHttpsPostConnection(murl, mdata);
                inStream.hasNext();
                // process the stream and store it in StringBuilder
                while (inStream.hasNextLine()) {
                    response += (inStream.nextLine());
                    Log.d("ParseStarter", "POST resp " + response.length());
                }
                Bundle b = new Bundle();
                b.putString("post", response);
                msg.setData(b);
                // in.close(); // mind the resources
                messageHandler.sendMessage(msg);

            }
        }.start();
    }


    // GET for DOWNSTREAM
    private Scanner openHttpsConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;
        Log.d("ParseStarter", "dwnURL " + urlStr);

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpsURLConnection)) {
                throw new IOException("URL is not an Https URL");
            }

            HttpsURLConnection httpConn = (HttpsURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            // TIMEOUT is required
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");

            httpConn.connect();

            resCode = httpConn.getResponseCode();
            if (resCode == HttpsURLConnection.HTTP_OK) {
                return new Scanner(httpConn.getInputStream());
            }
//            else Toast.makeText(this,"Connection error!",Toast.LENGTH_SHORT);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // GET for UPSTREAM
    private Scanner openHttpsPostConnection(String urlStr, byte[] data) {
        InputStream in = null;
        byte[] mextrad = data;
        int resCode = -1;
        OutputStream out = null;
        // int http_status;


        //split byte array into chunks
        int totalsize = mextrad.length;
        int chunksize = totalsize / 10000;
        int totalchunks = (int) Math.ceil((double) totalsize / (double) chunksize);
        final byte[][] chunks = new byte[totalchunks][10000];
        int sizeleft = totalsize;
        Log.w("app", "Totalsize: " + totalsize + " totalChunks: " + totalchunks + " sizeLeft: " + sizeleft);

        int i = 0, index = 0;
        while (sizeleft > 0) {
            Log.w("app", "index: " + index + " mdata: " + mextrad[index]);
            for (int j = 0; j < min(10000, sizeleft); j++) {
                chunks[i][j] = mextrad[index++];

            }
            i++;
            sizeleft -= 10000;
            Log.w("app", "After iter " + i + " sizeLeft: " + sizeleft);

        }
        i--;
        Log.w("app","No of chunks: "+i);
        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpsURLConnection)) {
                throw new IOException("URL is not an Https URL");
            }

            HttpsURLConnection httpConn = (HttpsURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setChunkedStreamingMode(0);
            httpConn.setRequestProperty("Content-Type", "audio/x-flac; rate="
                    + sampleRate);
            httpConn.connect();

            try {
                // this opens a connection, then sends POST & headers.
                out = httpConn.getOutputStream();
                //segment audio files of duration>15s into byte streams and then send
                 Log.d("ParseStarter", "IO beg on data");

                Log.w("app","Going to write now!!");
                for(int k=0;k<=i;k++) {
                    Log.w("app","writing chunk "+k);
                    out.write(chunks[k]);
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                out.write(FINAL_CHUNK);

                Log.d("ParseStarter", "IO fin on data");
                resCode = httpConn.getResponseCode();

                Log.d("ParseStarter", "POST OK resp "
                        + httpConn.getResponseMessage().getBytes().toString());

                if (resCode / 100 != 2) {
                    Log.d("ParseStarter", "POST bad io ");
                }

            } catch (IOException e) {
                Log.d("ParseStarter", "FATAL " + e);

            }

            if (resCode == HttpsURLConnection.HTTP_OK) {
                Log.d("ParseStarter", "OK RESP to POST return scanner ");
                return new Scanner(httpConn.getInputStream());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
            txtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setText(txtView.getText());
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Type in Your Transcript's Title Right Here");
            builder.setView(input);
            String name = String.valueOf(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    body = txtView.getText().toString();
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
                    int savedFile = 1;
                    Toast toast = Toast.makeText(context, "Yay you've saved it! ", Toast.LENGTH_LONG);
                    toast.show();


                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Do something else
                    Context context = getApplicationContext();
                    CharSequence text = "You chickened out?";
                    int duration = Toast.LENGTH_LONG;

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

        String[] langs=new String[]{"en_UK","en_US","hi","fr_FR","ja"};

        for(int i=0;i<langs.length;i++)
            lang.add(i,new Languages(langs[i]));

        return lang;

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
}
