package com.ece.alarmmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
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

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class SettingsFragment extends Fragment implements AsyncResponse {
//	Accelerometer Sensor
	  private SensorManager mSensorManager;  
	  private List<Sensor> sensors;
	  private Sensor mAccelerometer;	  
	  //shake detection parameters
	  private static final int DEFAULT_THRESHOLD = 400;
	  private static final int DEFAULT_SHAKE_DURATION = 300;
	  private static final int DEFAULT_PROCESS_DURATION = 2000;
	  private static int SHAKE_THRESHOLD = 1000;
	  private long SHAKE_DURATION = 200;
	  private long PROCESS_DURATION = 2000;
	  private static int UPPER_SPEED_LIMIT = 3000;	  
	  //shake detection logic
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
	  private int sum;
	  private TextView tapDetection;
	  private float speed=0;
    // shared preference
	public static final String PREF = "EceAlarmApp";

	//UI widgets
	private TimePicker picker;
	private Spinner snoozeSpinner;
	private Switch weatherSwitch;
	private EditText city;
	private Button citySearch;
	//Voice Settings
	private Properties configProp;
	RetrieveXML asyncTask;
	private String queryCity;
	private String woeId;
	
	  
	
    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) 
    {
//        //Accelerometer
//        mSensorManager = (SensorManager)getActivity().getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
//		sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
//	    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//		if(sensors.size() > 0) 
//		{ 
//		  	mAccelerometer = sensors.get(0); 
//		} 
		//mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    	//shake detection logic initialization
//        sum = 0;
//        firstShake = false;
//        lastUpdate = System.currentTimeMillis();

    	//load pre-set configuration 
    	loadProperties();
    	
    	//UI intialization
    	View v = inflater.inflate(R.layout.activity_settings, container, false);
    	//tapSensitivity = (SeekBar) v.findViewById(R.id.seekBar1);
    	//tapSensitivity.setMax(NUM_THRESHOLD);

    	
        picker = (TimePicker) v.findViewById(R.id.timepicker);
    	snoozeSpinner = (Spinner) v.findViewById(R.id.spinner1);
    	weatherSwitch = (Switch) v.findViewById(R.id.switch1);
       	city = (EditText) v.findViewById(R.id.weather);
    	city.setEnabled(weatherSwitch.isChecked());
    	citySearch = (Button)v.findViewById(R.id.citySearch);
    	citySearch.setEnabled(weatherSwitch.isChecked());

    	Log.d("OnCreate View", "reached 2");
    	citySearch.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {			
				if (city.getText().toString() != null && city.getText().toString().length() > 0)
					try {
						searchCity(city.getText().toString());
					} catch (UnsupportedEncodingException e) {
						woeId = null;
						queryCity = null;
						city.setText("");
						Toast.makeText(getActivity(), "City Not Found", Toast.LENGTH_SHORT).show();
					}		
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

    	final Button updateButton = (Button) v.findViewById(R.id.update);
    	updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateSettings();
            }
        });
    	
    	//tapDetection = (TextView)v.findViewById(R.id.shakeDetectionTest);

    	Log.d("OnCreate View", "reached 3");
 		return v;
    }

    private void updateAlarmTime(SharedPreferences.Editor editor){
		Calendar alarmTime = Calendar.getInstance();	
		Calendar currentTime = Calendar.getInstance();        
		alarmTime.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
		alarmTime.set(Calendar.MINUTE, picker.getCurrentMinute());
		alarmTime.set(Calendar.SECOND, 0);
		alarmTime.set(Calendar.MILLISECOND, 0);
        if ((alarmTime.get(Calendar.HOUR_OF_DAY)==currentTime.get(Calendar.HOUR_OF_DAY) && 
        		alarmTime.get(Calendar.MINUTE)==currentTime.get(Calendar.MINUTE))){
        	// Do nothing
        }
        else if (alarmTime.before(currentTime))	// add one day to alarm time
        	alarmTime.add(Calendar.DAY_OF_MONTH, 1); 

		long diff =  alarmTime.getTimeInMillis() - System.currentTimeMillis();
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffHours = diff / (60 * 60 * 1000) % 24;
		if (diffMinutes == 0 && diffHours == 0)
		{
			//tView.setText("Alarm triggers in less than 1 min");
			diff = 10000;
		}
			//tView.setText("Alarm triggers in "+diffHours+" hours "+diffMinutes+ "mins" );
		long setAlarm = System.currentTimeMillis() + diff;
		editor.putLong("p8", setAlarm);
		editor.putInt("p9", alarmTime.get(Calendar.HOUR_OF_DAY));
		editor.putInt("p10", alarmTime.get(Calendar.MINUTE));
		editor.putLong("diff", diff);
		editor.putLong("diffHours", diffHours);
		editor.putLong("diffMinutes", diffMinutes);
		
		Intent intentAlarm = new Intent(getActivity(), AlarmReceiver.class);
	    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, setAlarm, PendingIntent.getBroadcast(getActivity(),1,intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

	    editor.putBoolean("disableAlarmButton", true);
    }
    
    public void updateSettings(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		int j; 
		updateAlarmTime(editor);
		//editor.putInt("p1", j = tapSensitivity.getProgress());
		editor.putInt("p2", snoozeSpinner.getSelectedItemPosition());
		editor.putBoolean("p3", weatherSwitch.isChecked());
		
		if (woeId != null){
			editor.putString("p4", city.getText().toString().trim());
			editor.putString("p5", woeId);
		}
		
//		editor.putInt("p1", SHAKE_THRESHOLD = Integer.parseInt(threshOld.getText().toString()));
//		editor.putInt("p6", SHAKE_DURATION = Integer.parseInt(shakeDuration.getText().toString()));
//		editor.putInt("p7", PROCESS_DURATION = Integer.parseInt(processDuration.getText().toString()));
		//SHAKE_THRESHOLD=THRESHOLD[j];
		if (editor.commit())
			Toast.makeText(getActivity(), "Settings Saved", Toast.LENGTH_SHORT).show();
		
	    getFragmentManager().popBackStack();
	}
   
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//    	  super.onActivityResult()
//    	  if (requestCode == 1) {
//
//    	     if(resultCode == RESULT_OK){      
//    	         String result=data.getStringExtra("result");          
//    	     }
//    	     if (resultCode == RESULT_CANCELED) {    
//    	         //Write your code if there's no result
//    	     }
//    	  }
//    }
    
    
	public void searchCity(String query) throws UnsupportedEncodingException{
		hideSoftKeyboard();
    	asyncTask = new RetrieveXML();
    	asyncTask.delegate = this;
    	query = query.trim();
		queryCity = query.trim();
		query = URLEncoder.encode(query, "utf-8");
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
		catch (Exception ex){
			woeId = null;
			queryCity = null;
			city.setText("");
			Toast.makeText(getActivity(), "City Not Found", Toast.LENGTH_SHORT).show();
		}
		
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
		//int s1 = settings.getInt("p1", NUM_THRESHOLD/2);
		//Log.d("sensivity", s1+"");
		int s2 = settings.getInt("p2", 0);
		//Log.d("snooze time", s2+"");
		Boolean s3 = settings.getBoolean("p3", false);
		String s4 = settings.getString("p4", null);

    	Log.d("OnStart", "reached 4");
		//tapSensitivity.setProgress(s1);
		snoozeSpinner.setSelection(s2);
		weatherSwitch.setChecked(s3);
		city.setText(s4);
		woeId = settings.getString("p5", null);
		Calendar currentTime = Calendar.getInstance();
		int s9 = settings.getInt("p9", currentTime.get(Calendar.HOUR_OF_DAY));
		int s10 = settings.getInt("p10", currentTime.get(Calendar.MINUTE));
		picker.setCurrentHour(currentTime.get(Calendar.HOUR_OF_DAY));
		picker.setCurrentMinute(currentTime.get(Calendar.MINUTE));
//		picker.setCurrentHour(s9);
//		picker.setCurrentMinute(s10);
	//	int s1 = settings.getInt("p1", DEFAULT_THRESHOLD);
		//threshOld.setText(Integer.toString(s1));
	//	Log.d("s1 reached", s1+"");
	//	long s6 = settings.getLong("p6", DEFAULT_SHAKE_DURATION);
		//shakeDuration.setText(Integer.toString(s6));
		
	//	long s7 = settings.getLong("p7", DEFAULT_PROCESS_DURATION);
		//processDuration.setText(Integer.toString(s7));

//    	mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    public void onStop() {
//	    mSensorManager.unregisterListener(mySensorListener);
	  //  updateSettings();
    	FragmentManager fm = getFragmentManager();
    	if(fm.getBackStackEntryCount()>1)
    		fm.popBackStack();
	    Log.d("onStop is Called", "settingsFragment");
    	super.onStop();
    }
    
    @Override
    public void onPause() {
 //   	mSensorManager.unregisterListener(mySensorListener);
	    //updateSettings();
	    Log.d("onpause is Called", "settingsFragment");
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
    

//	//sensor management     
//	private final SensorEventListener mySensorListener = new SensorEventListener()  { 
//			public void onSensorChanged(SensorEvent event)  {    	
//					    curTime = System.currentTimeMillis();
//						//Log.d("Sum", sum + "");
//					// only allow one update every 100ms.
//						delay = (curTime - lastUpdate);
//						lastUpdate = curTime;
//						
//						float z = event.values[2]; 
//						
//						//speed = Math.abs(x+y+z - last_x - last_y - last_z) / delay * 10000;
//						speed = Math.abs(z - last_z) / delay * 10000;
//						if (speed > SHAKE_THRESHOLD && speed < UPPER_SPEED_LIMIT){
//							if(firstShake==false){
//								firstShake = true;
//								shaken = true;
//								firstShakeTime = curTime;
//								beginShakeTime = curTime;
//							}
//							if(shaken==false){
//								shaken = true;
//								beginShakeTime = curTime;
//							}
//						}
//						else if(shaken == true){
//							if((curTime-beginShakeTime)>SHAKE_DURATION){
//								shaken = false;
//								sum = sum + 1;
//								//tapDetection.setText("Detected" + " " + Integer.toString(sum) + " " + "taps");
//							}
//							
//						}           
//						
//						if(curTime - firstShakeTime > PROCESS_DURATION){
//							if(shaken==true){
//								shaken = false;
//								sum = sum+1;
//								//tapDetection.setText("Detected" + " " + Integer.toString(sum) + " " + "taps");
//							}
//							//tapDetection.setText("Detected" + " " + Integer.toString(0) + " " + "taps");
//							firstShake=false;
//							sum = 0;
//					    }   			        	    
//				
//						last_z = z;
//			} 
//			public void onAccuracyChanged(Sensor sensor, int accuracy) {}
//	};
}