package com.ece.alarmmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class AlarmActivity extends Activity implements OnInitListener, AsyncResponse{
	// Ish's addition:
	
	private static final int REQUEST_CODE = 1234;
	RetrieveXML asyncTask = new RetrieveXML();
	private TextToSpeech tts;
    private int state = 0;
    private AudioManager mAudioManager;
	private Properties configProp;
	private Properties text_en;
	private ArrayList<String> weatherInfo;
	private boolean wInfoRetrieved = false;
	//	Accelerometer Sensor
	  private SensorManager mSensorManager;  
	  private List<Sensor> sensors;
	  private Sensor mAccelerometer;	  
		
		private static final int DEFAULT_THRESHOLD = 400;
		private static final int DEFAULT_SHAKE_DURATION = 300;
		private static final int DEFAULT_PROCESS_DURATION = 2000;
	  private int shakeDuration;
	  private int processDuration;
	  //tap detection
	  private static int UPPER_SPEED_LIMIT = 3000;
	  private static int [] snoozeTimes = {1000, 2000, 4000, 8000};
      private static int SNOOZE_TIME;
	  private static int SHAKE_THRESHOLD = 1000;
	  private static long SHAKE_DURATION = 1500;
	  private static long PROCESS_DURATION = 3000;
	  private long lastUpdate;
	  private boolean firstShake;
	  private long firstShakeTime;
	  private long beginShakeTime;
	  private long delay;
	  private long curTime;
//	  private float last_x = 0;
//	  private float last_y = (float) 9.8;
	  private float z = 10;
	  private float last_z = 10;
	  private boolean shaken = false;
	  //shake detection
	  private int sum;
	  private float speed = 0;
	  private MediaPlayer mPlayer;
	  private int counter = 0;
	  //voice management
	  private boolean registerFlag;
	  private Window window;
	  private boolean musicPlayed = false;
	  private boolean fromWeatherService = false;
	  
	 @Override
     public void onCreate(Bundle savedInstanceState) 
    {
		 //createWakeLocks();
		 wakeDevice();
		 super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_alarm);

         Log.d("onCreated is executed", "oncreate");
         registerFlag=true;
        // tts = new TextToSpeech(this, this);
         mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
         loadProperties();
         asyncTask.delegate = this;
   	  // shake detection
       // lastUpdate = System.currentTimeMillis();
                 
        sum = 0;
        firstShake = false;
         //Accelerometer
          mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		  sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		  mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		  if(sensors.size() > 0) 
		  { 
		  	mAccelerometer = sensors.get(0); 
		  } 
		  
		  musicPlayed = false;
		  fromWeatherService = false;
		 // new PlayMusicTask().execute();
         
    }

		// Ish's addition
		protected void onResume() {
		    super.onResume();
		    Log.d("On Resume is executed", "again");
		    
		    if (!fromWeatherService){
			    
			    tts = new TextToSpeech(this, this);
			    
			    if (!musicPlayed)
			    	new PlayMusicTask().execute();
		    }
		    
			last_z = 10;
		    //Values from SharedPreference
	 		SharedPreferences settings = getSharedPreferences(SettingsFragment.PREF, 0);
	 		
			try{
				int p1 = settings.getInt("p1",0);
				SHAKE_THRESHOLD = (p1 != 0) ? p1:DEFAULT_THRESHOLD;
			}
			catch(Exception ex){SHAKE_THRESHOLD = DEFAULT_THRESHOLD;}
			
			try{
				int p2 = settings.getInt("p2",0);
				SNOOZE_TIME = (p2>=0 && p2<=3) ? snoozeTimes[p2]:4000;
			}
			catch(Exception ex){SNOOZE_TIME = 4000;}
			//Overriding the shake & process Duration if its available in preference
			try{
				long p6 = settings.getLong("p6",0);
				SHAKE_DURATION = (p6 != 0) ? p6:DEFAULT_SHAKE_DURATION;
			}
			catch(Exception ex){SHAKE_DURATION = DEFAULT_SHAKE_DURATION;}
			
			try{
				long p7 = settings.getLong("p7",0);
				PROCESS_DURATION = (p7 != 0) ? p7:DEFAULT_PROCESS_DURATION;
			}
			catch(Exception ex){PROCESS_DURATION = DEFAULT_PROCESS_DURATION;}
			
			if(registerFlag == true)
				mSensorManager.registerListener(mySensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
		    
		    lastUpdate = System.currentTimeMillis();
		}
	 
	 private void playMusic(){
		 if(mPlayer != null){
			 mPlayer.reset();
		 }
         mPlayer = MediaPlayer.create(AlarmActivity.this, R.raw.alarm);
         mPlayer.setOnCompletionListener(new OnCompletionListener(){

			@Override
			public void onCompletion(MediaPlayer mp) {
				if (counter < 3){
					counter++;
					mp.start();
				}
				else
					weatherService();
			}
        	 
         });
//         mPlayer.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
//        	    @Override
//        	    public void onPrepared(MediaPlayer mp) {
//        	    	mp.start();
//        	    }
//        });
         
		if(!mPlayer.isPlaying())
			mPlayer.start();
         Log.d("In PlayMusic()", "Music Started");
	 }
	 
	 	public void wakeDevice() {
		    window = this.getWindow();
		    window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		    window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		    window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		}

		//sensor management     
			private final SensorEventListener mySensorListener = new SensorEventListener()  { 
				public void onSensorChanged(SensorEvent event)  {    	
						    curTime = System.currentTimeMillis();
						// only allow one update every 100ms.
							delay = (curTime - lastUpdate);
							lastUpdate = curTime;
							
//							float x = event.values[0]; 
//							float y = event.values[1]; 
							z = event.values[2]; 
							
							//speed = Math.abs(x+y+z - last_x - last_y - last_z) / delay * 10000;
							speed = Math.abs(z - last_z) / delay * 10000;
							if (speed > SHAKE_THRESHOLD && speed < UPPER_SPEED_LIMIT){
								if(firstShake==false){
									firstShake = true;
									shaken = true;
									firstShakeTime = curTime;
									beginShakeTime = curTime;
									if (mPlayer != null && mPlayer.isPlaying()){
										mPlayer.stop();
									}
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
										Log.d("Sum", sum + "");
										snooze(SNOOZE_TIME);
									}
									else if(sum > 1){
										Log.d("Sum", sum + "");
										disable();
									}	
									firstShake=false;
									sum = 0;
							}   		        	  
					
//							last_x = x;
//							last_y = y;
							last_z = z;
				} 
				public void onAccuracyChanged(Sensor sensor, int accuracy) {}
			};
			
	public void snooze(int diff){
		
		long setAlarm = System.currentTimeMillis() + diff;
		
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, setAlarm, PendingIntent.getBroadcast(this,1,intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        finish();
	}
	public void disable(){
		musicPlayed = true;
		registerFlag = false;
	    mSensorManager.unregisterListener(mySensorListener);
	    if (mPlayer != null && mPlayer.isPlaying()){
	    	mPlayer.stop();
	    }
		weatherService();
        //finish();
	}
	public void weatherService(){
		String url = configProp.getProperty("weatherUrl");
		SharedPreferences settings = getSharedPreferences(SettingsFragment.PREF, 0);
		String woeId = settings.getString("p5", null);
		
		if (woeId == null)
			return;
		asyncTask.execute(url+woeId);
	}
	
	@Override
	public void processFinish(String output) {
		
		try{
			if (output == null){
				wInfoRetrieved = false;
			}
			else{
				wInfoRetrieved = true;
				
				weatherInfo = new ArrayList<String>();
				Document doc = getDomElement(output);
				NodeList n2 = doc.getElementsByTagName("yweather:condition");
				Element ele2 = (Element) n2.item(0);
				
				weatherInfo.add(ele2.getAttribute("temp"));
				weatherInfo.add(ele2.getAttribute("code"));
				
				NodeList n3 = doc.getElementsByTagName("yweather:forecast");
				Element ele3 = (Element) n3.item(0);
				
				weatherInfo.add(ele3.getAttribute("high"));
				weatherInfo.add(ele3.getAttribute("low"));
				weatherInfo.add(ele3.getAttribute("code"));
			}
			speakOut();
		}
		catch(Exception ex){return;}

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
                // return DOM
            return doc;
    }
	
	public void speakOut() {
		SharedPreferences settings = getSharedPreferences(SettingsFragment.PREF, 0);
		Boolean weatherVoiceEnabled = settings.getBoolean("p3", true);
		if(!weatherVoiceEnabled)
			return;
		
    	HashMap<String, String> hm = new HashMap<String,String>();
    	tts.setSpeechRate(0.95F);
    	//tts.setPitch(0.9F);
    	tts.setLanguage(Locale.US);
    	tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){

			@Override
			public void onDone(String uttId) {
				if (uttId.equalsIgnoreCase("endmsg") && wInfoRetrieved) {
		        	state = 2;
		        	startVoiceRecognitionActivity();
		        } 
				else if (uttId.equalsIgnoreCase("goodbye"))
					finish();
				
			}
			
			@Override
			public void onError(String arg0) {}
			@Override
			public void onStart(String arg0) {}
    		
    	});
    	
    	if (wInfoRetrieved){
	    	String text = getSalutation();
	    	tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	    	tts.playSilence(1000, TextToSpeech.QUEUE_ADD, null);
	    	
	    	String text2 = text_en.getProperty("weather1")+ " "+weatherInfo.get(0)+ " "+text_en.getProperty("weather2")
	    			+" "+text_en.getProperty("cond_"+weatherInfo.get(1));
	    	tts.speak(text2, TextToSpeech.QUEUE_ADD, null);
	    	tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
	    	
	    	String text3 = text_en.getProperty("forecast1")+ " "+weatherInfo.get(2)+ " "+text_en.getProperty("forecast2")
	    			+ " "+weatherInfo.get(3)+" "+text_en.getProperty("forecast_"+weatherInfo.get(4));
	    	tts.speak(text3, TextToSpeech.QUEUE_ADD, null);
	    	tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
	    	
	     	String text5 = text_en.getProperty("repeat");
	     	hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "endmsg");
	    	tts.speak(text5, TextToSpeech.QUEUE_ADD, hm);
	    	//tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
    	}
    	else{
	    	String text = getSalutation();
	    	tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	    	tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
	    	
    		String text2 = text_en.getProperty("network_error");
	    	tts.speak(text2, TextToSpeech.QUEUE_ADD, null);
	    	tts.playSilence(500, TextToSpeech.QUEUE_ADD, null);
	    	sayGoodday();
    	}

    	
    }
    
	private String getSalutation(){
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hour > 0 && hour < 12)
			return text_en.getProperty("good_morning");
		else if (hour >=12 && hour < 18)
			return text_en.getProperty("good_afternoon");
		else
			return text_en.getProperty("good_evening");
	}
	
	public void sayGoodday(){
    	String text = "Have a wonderful day";
    	HashMap<String, String> hm = new HashMap<String,String>();
    	hm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "goodbye");
    	tts.speak(text, TextToSpeech.QUEUE_ADD, hm);
    }
    
    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice Recognizer...");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100000L);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100000L);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        startActivityForResult(intent, REQUEST_CODE);
    }
	
	@Override
	public void onPause() {
		super.onPause();

	    Log.d("onPause", "in onPause");
		if (mPlayer != null && mPlayer.isPlaying()){
			Log.d("media player's state in onPause", "mPlayer");
			mPlayer.stop();
		}
		if (tts.isSpeaking())
			tts.stop();
	    mSensorManager.unregisterListener(mySensorListener);
	}

	@Override
	public void onStop() {
		super.onStop();
	    Log.d("onStop", "in onStop");
		if (mPlayer != null && mPlayer.isPlaying()){
			Log.d("media player's state in onStop", "mPlayer");
			mPlayer.stop();
		}
		if (tts.isSpeaking())
			tts.stop();
	    mSensorManager.unregisterListener(mySensorListener);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (mPlayer != null && mPlayer.isPlaying())
			mPlayer.stop();
		if (tts.isSpeaking())
			tts.stop();
	    mSensorManager.unregisterListener(mySensorListener);
	}

	@Override
	public void onInit(int status) {
		 if (status == TextToSpeech.SUCCESS) {
			 
	            int result = tts.setLanguage(Locale.US);
	 
	            if (result == TextToSpeech.LANG_MISSING_DATA
	                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	                Log.e("TTS", "This Language is not supported");
	            }
	 
	        } else {
	            Log.e("TTS", "Initilization Failed!");
	        }
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	Log.d("OnActivityResult", "done voice");
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            processSpeech(matches);
        }
        else if(state == 2 && resultCode == RESULT_CANCELED){
        	sayGoodday();
        }
        else if(state == 2 && resultCode == RESULT_FIRST_USER){
        	sayGoodday();
        }
        super.onActivityResult(requestCode, resultCode, data);
        fromWeatherService = true;
    }
	
    
    
	private void processSpeech(ArrayList<String> matches){
    	if (state != 2)
    		return;
    	
    	for (String str:matches){
    		String[] items = str.split(" ",-1);
    		for (int i=0; i<items.length;i++){
    			if (items[i].equalsIgnoreCase("yes") || items[i].equalsIgnoreCase("yea")){
    				speakOut();
    				return;
    			}
    			else if(items[i].equalsIgnoreCase("no")){
    				sayGoodday();
    				return;
    			}
    		}
    	}
    }
	

    private void loadProperties(){
        
        try {
			InputStream fileStream = getAssets().open("config.properties");
			configProp = new Properties();
			configProp.load(fileStream);
			
			fileStream = getAssets().open("text_en.properties");
			text_en = new Properties();
			text_en.load(fileStream);
			
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private class PlayMusicTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            while (!window.isActive()) {}
            Log.d("In PlayMusicTask.doInBackground", "after window.isActive()");
            playMusic();
            return null;
        }
        @Override
        protected void onPostExecute(Void arg0) {


		   // playMusic();
            //return null;
        }

    }
}
