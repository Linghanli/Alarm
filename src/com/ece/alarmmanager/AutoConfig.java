package com.ece.alarmmanager;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AutoConfig extends Activity {
	  private SensorManager mSensorManager;  
	  private List<Sensor> sensors;
	  private Sensor mAccelerometer;
	  private final String PREF = "EceAlarmApp";
	  //shake detection
	  private long lastUpdate;
	  private long delay;
	  private long curTime;

	  private int sum;
	  private float speed = 0;
	  
	  private static int UPPER_SPEED_LIMIT = 3000;
	  private static int SHAKE_THRESHOLD = 400;
	  private static long SHAKE_DURATION = 1500;
	  private static long PROCESS_DURATION = 3000;
	  
	  private float z = 10;
	  private float last_z = 10;
	  private boolean shaken = false;
	  //time record
	  private boolean firstShake;
	  private long firstShakeTime;
	  private long lastShakeTime;
	  
	  private long beginShakeTime;
	  
	  private int tapCount;
	  
	  private boolean tapDetected;
	  //interface
	  private TextView text;
	  private Thread thread;
	  private Handler handler = new Handler();
	  //interface timer
	  private long startTime;
	  private long currentTime;
	  //settings update
	  private SharedPreferences settings;
	  private SharedPreferences.Editor editor;
	  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_config);

    	Log.d("autoConfig", "reached 2");
		settings = getSharedPreferences(PREF, 0);
		editor = settings.edit();
		text = (TextView)findViewById(R.id.textview);
		//initialization
		firstShake = false;
		//Accelerometer
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		  sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		  mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		  if(sensors.size() > 0) 
		  { 
		  	mAccelerometer = sensors.get(0); 
		  } 
		  startTime = System.currentTimeMillis();
		 //tap time record 
		  tapCount = 0;
		  
			thread = new Thread() {
				public void run() {
					// do something here
					if(tapDetected==false){
						Toast.makeText(getApplicationContext(), "Please tap harder or Tap closer to the Phone.", Toast.LENGTH_SHORT).show();
						Log.d("Tread Run", "now add to message queue");
					}
					handler.postDelayed(this, 6000);
				}
			};
			
			
//		  while(true){
//
//			  currentTime = System.currentTimeMillis();
//			  if( (currentTime - startTime) > 2000 && tapCount == 0){
//				  Toast.makeText(getApplicationContext(), "Please tap harder or Tap closer to the Phone.", Toast.LENGTH_LONG).show();
//				  startTime = currentTime;
//			  }			  
//			  
//		  }
	}

	  @Override
	  public void onResume(){
		  super.onResume();
		  tapDetected = false;
		  lastUpdate = System.currentTimeMillis();
		  last_z=10;
		  mSensorManager.registerListener(mySensorListener1Tap, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		  text.setText("Waiting for 1 Tap...");
		  handler.removeCallbacks(thread);
		  handler.postDelayed(thread, 4000);
	  }
		@Override
		public void onPause() {
			super.onPause();
			
			handler.removeCallbacks(thread);
		    mSensorManager.unregisterListener(mySensorListener1Tap);
		    mSensorManager.unregisterListener(mySensorListener2Taps);
		    
		}
		@Override
		public void onStop() {
			super.onStop();

		    mSensorManager.unregisterListener(mySensorListener1Tap);
		    mSensorManager.unregisterListener(mySensorListener2Taps);
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();

		    mSensorManager.unregisterListener(mySensorListener1Tap);
		    mSensorManager.unregisterListener(mySensorListener2Taps);
		}
		//sensor management first shake     
			private final SensorEventListener mySensorListener1Tap = new SensorEventListener()  { 
				public void onSensorChanged(SensorEvent event)  {    	
						    curTime = System.currentTimeMillis();
						// only allow one update every 100ms.
							delay = (curTime - lastUpdate);
							//Log.d("delay time", delay + "");
							lastUpdate = curTime;
							
							//float x = event.values[0]; 
							//float y = event.values[1]; 
							z = event.values[2]; 
							//Log.d("z output", z + "");
							//speed = Math.abs(x+y+z - last_x - last_y - last_z) / delay * 10000;
							speed = Math.abs(z - last_z) / delay * 10000;
							//Log.d("speed", speed + "");
							if (speed > SHAKE_THRESHOLD && speed < UPPER_SPEED_LIMIT){
								if(firstShake==false){
									firstShake = true;
									firstShakeTime = curTime;
									lastShakeTime = curTime;
								}
								else{
									lastShakeTime = curTime;
								}
								Log.d("speed", speed + "");
								//Log.d("Threshold passed", speed + "");
							}   
							else if( (curTime-lastShakeTime)>1000 && firstShake == true){
								// prepare to switch to second tap detection stage
								firstShake = false;
								shaken = false;
								last_z = 10;
								lastUpdate = System.currentTimeMillis();
								SHAKE_DURATION = (lastShakeTime - firstShakeTime);
								SHAKE_DURATION = ( (SHAKE_DURATION) > 200 ) ? SHAKE_DURATION: 200;
								Log.d("ShakeDuration is", SHAKE_DURATION + "");
								PROCESS_DURATION = 3 * SHAKE_DURATION;
								PROCESS_DURATION = (PROCESS_DURATION>2000) ? PROCESS_DURATION: 2000;
								Log.d("PROCESS_DURATION is", PROCESS_DURATION + "");
								sum = 0;
								//SHAKE_DURATION = elapsedTime;
								//tapDetected = true;
								//text.setText("Please wait" + " " + elapsedTime + " " + "ms before 2nd tap.");
								text.setText("Please tap twice..."); 
								mSensorManager.unregisterListener(mySensorListener1Tap);
								mSensorManager.registerListener(mySensorListener2Taps, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
							}
					
							last_z = z;
				} 
				public void onAccuracyChanged(Sensor sensor, int accuracy) {}
			};		
			


			private final SensorEventListener mySensorListener2Taps = new SensorEventListener()  { 
				public void onSensorChanged(SensorEvent event)  {    	
						    curTime = System.currentTimeMillis();
						// only allow one update every 100ms.
							delay = (curTime - lastUpdate);
							lastUpdate = curTime;
							
							z = event.values[2]; 
							
							//speed = Math.abs(x+y+z - last_x - last_y - last_z) / delay * 10000;
							speed = Math.abs(z - last_z) / delay * 10000;
							if (speed > SHAKE_THRESHOLD && speed < UPPER_SPEED_LIMIT){
								if(firstShake==false){
									firstShake = true;
									shaken = true;
									firstShakeTime = curTime;
									beginShakeTime = curTime;
								}
								if(shaken==false){
									shaken = true;
									beginShakeTime = curTime;
								}
							}
							else if(shaken == true){
								if((curTime-beginShakeTime) > SHAKE_DURATION){
									shaken = false;
									sum = sum + 1;
								}
							}    
							if(curTime - firstShakeTime > PROCESS_DURATION){
									if(shaken==true){
										shaken = false;
										sum = sum+1;
									}
									if(sum == 1){
										oneTap();
										Log.d("Sum", sum + "");
									}
									else if(sum >= 2){
										Log.d("Sum", sum + "");

										editor.putLong("p6", SHAKE_DURATION);
										editor.putLong("p7", PROCESS_DURATION);
										finish();
									}	
									firstShake=false;
									sum = 0;
							}   		        	  
					
							last_z = z;
				} 
				public void onAccuracyChanged(Sensor sensor, int accuracy) {}
			};

	private void oneTap(){

	    mSensorManager.unregisterListener(mySensorListener2Taps);
		LayoutInflater inflater = LayoutInflater.from(this);
		final View oneTapDialogView = inflater.inflate(R.layout.one_tap_dialog, null);

		AlertDialog.Builder dialog = new AlertDialog.Builder( this );
        dialog.setView( oneTapDialogView );
        final AlertDialog dialogPopup = dialog.create();
        dialogPopup.setCancelable(false);
		final Button tapHarderButton = (Button) oneTapDialogView.findViewById(R.id.tapHarder);
		tapHarderButton.setOnClickListener(
				new Button.OnClickListener() {
					
	                public void onClick(View v) {
	                	dialogPopup.dismiss();
	                	tapDetected = false;
	                	firstShake = false;
	          		    last_z=10;
	                	lastUpdate=System.currentTimeMillis();
	                	mSensorManager.registerListener(mySensorListener1Tap, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	                	text.setText("Redo 1 tap. But this time, tap harder. Waiting...");
	                }
				}
		);
		final Button tapSlowerButton = (Button) oneTapDialogView.findViewById(R.id.tapSlower);
		tapSlowerButton.setOnClickListener(
				new Button.OnClickListener() {
					
	                public void onClick(View v) {
	                	dialogPopup.dismiss();
	                	tapDetected = false;
	                	mSensorManager.registerListener(mySensorListener2Taps, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
	                	text.setText("Please tap twice again. But this time, space 2 taps out longer. Waiting..."); 
	                }
				}
		);

	    mSensorManager.unregisterListener(mySensorListener2Taps);
		tapDetected = true;
		dialogPopup.show();
        
		
	}
//			AlertDialog dialog = new AlertDialog.Builder(YourActivity.this)
//		    .setTitle("Enter the Zip Code")
//		    .setView(yourCustomView)
//		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//		        public void onClick(DialogInterface dialog, int whichButton) {
//		            mSomeVariableYouHaveOnYourActivity = etName.getText().toString();
//		        }
//		    })
//		    .setNegativeButton("Cancel", null).create();
//		dialog.show();
}