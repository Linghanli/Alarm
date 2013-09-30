package com.ece.alarmmanager;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import com.ece.alarmmanager.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends Activity
{
      
	 private Properties prop;
	 private TimePicker picker;
	 private TextView tView;
	
	  @Override
       public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            loadProperties();     
            
            tView = (TextView) findViewById(R.id.textView1);
            picker = (TimePicker) findViewById(R.id.picker1);
      }

    public void scheduleAlarm(View V)
    {
	    	
    		Calendar currenttime = Calendar.getInstance();
    		 System.currentTimeMillis();

    		Calendar alarmtime = Calendar.getInstance();
	        alarmtime.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
	        alarmtime.set(Calendar.MINUTE, picker.getCurrentMinute());
	        alarmtime.set(Calendar.SECOND, 0);
	        alarmtime.set(Calendar.MILLISECOND, 0);
	        
	        long diff = 0;
	        
	        if ((alarmtime.get(Calendar.HOUR_OF_DAY)==currenttime.get(Calendar.HOUR_OF_DAY) && 
	        		alarmtime.get(Calendar.MINUTE)==currenttime.get(Calendar.MINUTE))){
	        	// Do nothing
	        }
	        else if (alarmtime.before(currenttime))
	        	alarmtime.add(Calendar.DAY_OF_MONTH, 1); 
	        
	        
	        diff =  alarmtime.getTimeInMillis() - System.currentTimeMillis();
        	
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			
			long setAlarm = System.currentTimeMillis() + diff;
    		
			if (diffMinutes == 0 && diffHours == 0)
				tView.setText("Alarm triggers in less than 1 min");
			else
				tView.setText("Alarm triggers in "+diffHours+" hours "+diffMinutes+ "mins" );
			
			Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, setAlarm, PendingIntent.getBroadcast(this,1,intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            
            //Toast.makeText(this, "Alarm Scheduled", Toast.LENGTH_LONG).show();
    }
    
    private void loadProperties(){
        
        try {
			InputStream fileStream = getAssets().open("config.properties");
			prop = new Properties();
			prop.load(fileStream);
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
