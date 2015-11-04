package com.example.shriya.recordaudio;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
public class Recorder
{
  public static final int MSG_END_OF_RECORDING  = FLACRecorder.MSG_AMPLITUDES + 1;

  //For recording FLAC files.
  public FLACRecorder            mFLACRecorder;
  private WeakReference<Context>  mContext;


  // Handler for messages sent by Recorder
  private Handler                 mUpchainHandler;
  // Internal handler to hand to FLACRecorder.
  private Handler                 mInternalHandler;  

  private FLACRecorder.Amplitudes mAmplitudes;
  private FLACRecorder.Amplitudes mLastAmplitudes;

  public Recorder(Context context, Handler handler)
  {
    mContext = new WeakReference<Context>(context);
    
    mUpchainHandler = handler;
    mInternalHandler = new Handler(new Handler.Callback()
    {
      public boolean handleMessage(Message m)
      {
        switch (m.what) {
          case FLACRecorder.MSG_AMPLITUDES:
            FLACRecorder.Amplitudes amp = (FLACRecorder.Amplitudes) m.obj;
            mLastAmplitudes = new FLACRecorder.Amplitudes(amp);

            if (null != mAmplitudes) {
              amp.mPosition += mAmplitudes.mPosition;
            }
            mUpchainHandler.obtainMessage(FLACRecorder.MSG_AMPLITUDES,
                amp).sendToTarget();
            return true;


          case MSG_END_OF_RECORDING:
            if (null == mAmplitudes) {
              mAmplitudes = mLastAmplitudes;
            }
            else {
              mAmplitudes.accumulate(mLastAmplitudes);
            }


            mUpchainHandler.obtainMessage(MSG_END_OF_RECORDING).sendToTarget();
            return true;


          default:
            mUpchainHandler.obtainMessage(m.what, m.obj).sendToTarget();
            return true;
        }
      }
    });
  }



  public void start(String fileName)
  {
//Kill existing recorder instance
     Log.w("app", "in recorder");
      if (mFLACRecorder != null) {
      stop();
    }

    // Start recording!
    mFLACRecorder = new FLACRecorder( fileName, mInternalHandler);
    mFLACRecorder.start();
    mFLACRecorder.resumeRecording();
  }



  public void stop()
  {
    if (mFLACRecorder == null ) {
      // We're done.
      return;
    }
    // Pause recording & kill recorder
    mFLACRecorder.pauseRecording();
    //Free memory!
    mFLACRecorder.mShouldRun = false;
    mFLACRecorder.interrupt();
    try {
    	mFLACRecorder.join();
    } catch (InterruptedException ex) {
      // pass
    }
    mFLACRecorder = null;
  }



  public boolean isRecording()
  {
    if (null == mFLACRecorder) {
      return false;
    }
    return mFLACRecorder.isRecording();
  }



  public double getDuration()
  {
    if (null == mAmplitudes) {
      return 0f;
    }
    return mAmplitudes.mPosition / 1000.0;
  }



  public FLACRecorder.Amplitudes getAmplitudes()
  {
    return mAmplitudes;
  }
}
