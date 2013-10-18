package com.ece.alarmmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment implements AsyncResponse {
//	Accelerometer Sensor
	  private SensorManager mSensorManager;  
	  private List<Sensor> sensors;
	  private Sensor mAccelerometer;	  
	  private static int SHAKE_THRESHOLD = 1000;
	  public static int [] THRESHOLD = {2000, 1000, 400};
	  private static int NUM_THRESHOLD = 2;
	  private static final int UPPER_SPEED_LIMIT = 3000;
	  private int SHAKE_DURATION = 200;
	  private int PROCESS_DURATION = 2000;
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
	private EditText city;
	private Button citySearch;
	
	private Properties configProp;
	RetrieveXML asyncTask;
	private String queryCity;
	private String woeId;
	
	private static final int DEFAULT_SHAKE_DURATION = 300;
	private static final int DEFAULT_PROCESS_DURATION = 2000;
	
	private EditText shakeDuration;
	private EditText processDuration;
	
    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	//load config
    	loadProperties();
    	
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
       	city = (EditText) v.findViewById(R.id.weather);
    	city.setEnabled(weatherSwitch.isChecked());
    	citySearch = (Button)v.findViewById(R.id.citySearch);
    	citySearch.setEnabled(weatherSwitch.isChecked());
    	
    	citySearch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {			
				if (city.getText().toString() != null && city.getText().toString().length() > 0)
					searchCity(city.getText().toString());		
			}
    		
    	});
    	
    	weatherSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
					city.setEnabled(isChecked);	
					citySearch.setEnabled(isChecked);
				}
			});
    	
    	//Shake Detection
    	tapDetection = (TextView)v.findViewById(R.id.shakeDetectionTest);
    	
    	shakeDuration = (EditText)v.findViewById(R.id.shake_duration);
    	processDuration = (EditText)v.findViewById(R.id.process_duration);
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
		editor.putString("p4", city.getText().toString());
		editor.putString("p5", woeId);
		editor.putString("p6", shakeDuration.getText().toString());
		editor.putString("p7", processDuration.getText().toString());
		SHAKE_THRESHOLD=THRESHOLD[j];
		if (editor.commit())
			Toast.makeText(getActivity(), "Settings Saved", Toast.LENGTH_SHORT).show();
	}
    
	public void searchCity(String query){
		hideSoftKeyboard();
    	asyncTask = new RetrieveXML();
    	asyncTask.delegate = this;
		queryCity = query;
		String baseUrl = configProp.getProperty("geoPlanetUrl");
		String appId = configProp.getProperty("appid");
		String url = baseUrl+"('"+query+"')?appid="+appId;
		asyncTask.execute(url);
	}
	
	@Override
	public void processFinish(String output) {
		if (output == null){
			Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			Document doc = getDomElement(output);
			NodeList n2 = doc.getElementsByTagName("locality1");
			Element ele2 = (Element) n2.item(0);
				
			if (queryCity.equalsIgnoreCase(ele2.getTextContent())){
				woeId = ele2.getAttribute("woeid");
				Toast.makeText(getActivity(), "City Found", Toast.LENGTH_SHORT).show();
			}
			else{
				woeId = null;
				queryCity = null;
				city.setText("");
				Toast.makeText(getActivity(), "City Not Found", Toast.LENGTH_SHORT).show();
			}
		}
		catch (Exception ex){return;}
		
	}
	
	public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
 
            } catch (ParserConfigurationException e) {
                return null;
            } catch (SAXException e) {
                return null;
            } catch (IOException e) {
                 return null;
            }
            return doc;
    }

    @Override
    public void onStart() {
    	super.onStart();
		SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		int s1 = settings.getInt("p1", NUM_THRESHOLD/2);
		Log.d("sensivity", s1+"");
		int s2 = settings.getInt("p2", 0);
		Log.d("snooze time", s2+"");
		Boolean s3 = settings.getBoolean("p3", false);
		String s4 = settings.getString("p4", null);
		tapSensitivity.setProgress(s1);
		snoozeSpinner.setSelection(s2);
		weatherSwitch.setChecked(s3);
		city.setText(s4);
		
		String s6 = settings.getString("p6", Integer.toString(DEFAULT_SHAKE_DURATION));
		shakeDuration.setText(s6);
		
		String s7 = settings.getString("p7", Integer.toString(DEFAULT_PROCESS_DURATION));
		processDuration.setText(s7);
		
    	mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		Log.d("onStart", "started");
    }
    
    @Override
    public void onStop() {
	    mSensorManager.unregisterListener(mySensorListener);
	    updateSettings();
    	getFragmentManager().popBackStack();
    	super.onStop();
    }
    
    @Override
    public void onPause() {
    	mSensorManager.unregisterListener(mySensorListener);
	    updateSettings();
	    super.onPause();
    }
    
    private void loadProperties(){
        
        try {
			InputStream fileStream = getActivity().getAssets().open("config.properties");
			configProp = new Properties();
			configProp.load(fileStream);
	
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void hideSoftKeyboard(){
        if(getActivity().getCurrentFocus()!=null && getActivity().getCurrentFocus() instanceof EditText){
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(city.getWindowToken(), 0);
        }
    }
}