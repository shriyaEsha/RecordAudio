/**
 * This file is part of Audioboo, an android program for audio blogging.
 * Copyright (C) 2011 Audioboo Ltd.
 * Copyright (C) 2010,2011 Audioboo Ltd.
 * All rights reserved.
 *
 * Author: Jens Finkhaeuser <jens@finkhaeuser.de>
 *
 * $Id$
 **/

package com.example.shriya.recordaudio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.example.jni.FLACStreamDecoder;

import java.nio.ByteBuffer;

public class FLACPlayer extends Thread
{
  private static final int PAUSED_SLEEP_TIME  = 10 * 60 * 1000;

  public volatile boolean mShouldRun;


  public static abstract class PlayerListener
  {
    public abstract void onError();
    public abstract void onFinished();
  }
 private Context           mContext;

  private FLACStreamDecoder mDecoder;

  private AudioTrack        mAudioTrack;

  private String            mPath;

  private volatile boolean  mPaused;

  private volatile long     mSeekPos = -1;
  private volatile long     mPlayPos = 0;

  private PlayerListener    mListener;

//Plays the audio file
  public FLACPlayer(Context context, String path)
  {
    mContext = context;
    mPath = path;

    mShouldRun = true;
    mPaused = false;
  }



  public void pausePlayback()
  {
    mPaused = true;
    interrupt();
  }



  public void resumePlayback()
  {
    mPaused = false;
    interrupt();
  }



  public void seekTo(long position)
  {
    mSeekPos = position;
    interrupt();
  }



  public long currentPosition()
  {
    return mPlayPos;
  }



  public void setListener(PlayerListener listener)
  {
    mListener = listener;
  }



  public void run()
  {
    //initialize the decoder.
    try {
      mDecoder = new FLACStreamDecoder(mPath);
    } catch (IllegalArgumentException ex) {
      
      if (null != mListener) {
        mListener.onError();
      }
      return;
    }

    //channel sample rate and configuration
      int sampleRate = mDecoder.sampleRate();
    int channelConfig = mapChannelConfig(mDecoder.channels());
    int format = mapFormat(mDecoder.bitsPerSample());


    int decoder_bufsize = mDecoder.minBufferSize();
    int bufsize = decoder_bufsize;
    //create audio track
      try {
      mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
          channelConfig, format, bufsize, AudioTrack.MODE_STREAM);
      mAudioTrack.play();

      ByteBuffer buffer = ByteBuffer.allocateDirect(bufsize);
      byte[] tmpbuf = new byte[bufsize];
      while (mShouldRun) {
        try {
          // if audio is paused, sleep thread
          if (mPaused) {
            sleep(PAUSED_SLEEP_TIME);
            continue;
          }

          // Seek to a position
          long seekPos = mSeekPos;
          if (seekPos > 0) {
            int sample = (int) (seekPos / 1000f * sampleRate);
            mDecoder.seekTo(sample);
            mSeekPos = -1;
          }

          // play a chunk of audio track
          int read = mDecoder.read(buffer, bufsize);
          if (read <= 0) {
            // We're done with playing back!
            break;
          }

          buffer.rewind();
          buffer.get(tmpbuf, 0, read);
          mAudioTrack.write(tmpbuf, 0, read);

          mPlayPos = (long) (mDecoder.position() * 1000f / sampleRate);
        } catch (InterruptedException ex) {
        }
      }

      mAudioTrack.stop();
      mAudioTrack.release();
      mAudioTrack = null;
      mDecoder.release();
      mDecoder = null;


      if (null != mListener) {
        mListener.onFinished();
      }

    } catch (IllegalArgumentException ex) {
     
      if (null != mListener) {
        mListener.onError();
      }
    }
  }



  private int mapChannelConfig(int channels)
  {
    switch (channels) {
      case 1:
        return AudioFormat.CHANNEL_CONFIGURATION_MONO;

      case 2:
        return AudioFormat.CHANNEL_CONFIGURATION_STEREO;

      default:
        throw new IllegalArgumentException("Only supporting one or two channels!");
    }
  }



  private int mapFormat(int bits_per_sample)
  {
    switch (bits_per_sample) {
      case 8:
        return AudioFormat.ENCODING_PCM_8BIT;

      case 16:
        return AudioFormat.ENCODING_PCM_16BIT;

      default:
        throw new IllegalArgumentException("Only supporting 8 or 16 bit samples!");
    }
  }
}
