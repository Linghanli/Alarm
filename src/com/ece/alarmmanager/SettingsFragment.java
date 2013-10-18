package com.ece.alarmmanager;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
//	Accelerometer Sensor
	  private SensorManager mSensorManager;  
	  private List<Sensor> sensors;
	  private Sensor mAccelerometer;	  
	  private static int SHAKE_THRESHOLD = 1000;
	  public static int [] THRESHOLD = {2000, 1000, 400};
	  private static int NUM_THRESHOLD = 2;
	  private static final int UPPER_SPEED_LIMIT = 3000;
	  private static final int SHAKE_DURATION = 1500;
	  private static final int PROCESS_DURATION = 3000;
	  private long lastUpdate;
	  private boolean firstShake;
	  private long firstShakeTime;
	  private long beginShakeTime;
	  private long delay;
	  private long curTime;
	  private float last_x = 0;
	  private float last_y = (float) 9.8;
	  private float last_z = 0;
	  private boolean shaken = false;
	  //shake detection
	  private int sum;
	  private TextView tapDetection;
	  private float speed=0;
	  // Ish's section
	public static final String PREF = "EceAlarmApp";
	private SeekBar tapSensitivity;
	private Spinner snoozeSpinner;
	private Switch weatherSwitch;
	private EditText country;
	
	private Button updateButton;
	
    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	//Accelerometer
        sum = 0;
        firstShake = false;
         //Accelerometer
          mSensorManager = (SensorManager)getActivity().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
		  sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		  mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		  if(sensors.size() > 0) 
		  { 
		  	mAccelerometer = sensors.get(0); 
		  } 
		    mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		    lastUpdate = System.currentTimeMillis();
		  //Ish's section
    	
    	View v = inflater.inflate(R.layout.activity_settings, container, false);
    	tapSensitivity = (SeekBar) v.findViewById(R.id.seekBar1);
    	tapSensitivity.setMax(NUM_THRESHOLD);
    	snoozeSpinner = (Spinner) v.findViewById(R.id.spinner1);
    	weatherSwitch = (Switch) v.findViewById(R.id.switch1);
    	country = (EditText) v.findViewById(R.id.weather);
    	//Shake Detection
    	tapDetection = (TextView)v.findViewById(R.id.shakeDetectionTest);
		
		updateButton = (Button) v.findViewById(R.id.updateSettings);
		updateButton.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			updateSettings();
    	}
    	});
		
		
		
		return v;
    }

	//sensor management     
		private final SensorEventListener mySensorListener = new SensorEventListener()  { 
			public void onSensorChanged(SensorEvent event)  {    	
					    curTime = System.currentTimeMillis();
						//Log.d("Sum", sum + "");
					// only allow one update every 100ms.
						delay = (curTime - lastUpdate);
						lastUpdate = curTime;
						
						float x = event.values[0]; 
						float y = event.values[1]; 
						float z = event.values[2]; 
						
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
							if((curTime-beginShakeTime)>SHAKE_DURATION){
								shaken = false;
								sum = sum + 1;
								tapDetection.setText("Detected" + " " + Integer.toString(sum) + " " + "taps");
							}
						}           		        	    
				
						last_x = x;
						last_y = y;
						last_z = z;
			} 
			public void onAccuracyChanged(Sensor sensor, int accuracy) {}
		};
    public void updateSettings(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		int j; 
		editor.putInt("p1", j = tapSensitivity.getProgress());
		editor.putInt("p2", snoozeSpinner.getSelectedItemPosition());
		editor.putBoolean("p3", weatherSwitch.isChecked());
		editor.putString("p4", country.getText().toString());
		SHAKE_THRESHOLD=THRESHOLD[j];
		if (editor.commit())
			Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
	}

    @Override
    public void onStart() {
    	super.onStart();
		SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		int s1 = settings.getInt("p1", NUM_THRESHOLD/2);
		Log.d("sensivity", s1+"");
		int s2 = settings.getInt("p2", 0);
		Log.d("snooze time", s2+"");
		Boolean s3 = settings.getBoolean("p3", true);
		String s4 = settings.getString("p4", null);
		tapSensitivity.setProgress(s1);
		snoozeSpinner.setSelection(s2);
		weatherSwitch.setChecked(s3);
		country.setText(s4);
		
    	mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		Log.d("onStart", "started");
    }
    @Override
    public void onStop() {
	    mSensorManager.unregisterListener(mySensorListener);
    	getFragmentManager().popBackStack();
    	super.onStop();
    }
}