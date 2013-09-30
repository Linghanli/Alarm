package com.ece.alarmmanager;

import com.ece.alarmmanager.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;

public class AlarmActivity extends Activity{
	
	private MediaPlayer mPlayer;
	private int counter = 0;
	 @Override
     public void onCreate(Bundle savedInstanceState) 
    {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_alarm);
         mPlayer = MediaPlayer.create(AlarmActivity.this, R.raw.alarm);
         mPlayer.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer arg0) {
				if (counter < 3){
					counter++;
					arg0.start();
				}
			}
        	 
         });
         mPlayer.start();
    }
	 
	public void snooze(View v){
		mPlayer.stop();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mPlayer.stop();
	}

	@Override
	public void onStop() {
		super.onStop();
		mPlayer.stop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mPlayer.stop();
	}
}
