package com.ece.alarmmanager;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainFragment extends Fragment {
	 
	 private Properties configProp;
	 private Properties text_en;
	 
	 private TimePicker picker;
	 private TextView tView;
	 
	 private Button alarmButton;
	 private Button settingsButton;
	
    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, container, false);
        loadProperties();     
        
        tView = (TextView) v.findViewById(R.id.textView1);
        picker = (TimePicker) v.findViewById(R.id.picker1);
        
        alarmButton = (Button) v.findViewById(R.id.button1);
        alarmButton.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			scheduleAlarm();
    	}
    	});
        
        settingsButton = (Button) v.findViewById(R.id.button2);
        settingsButton.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			flipCard();
    	}
    	});
        
        return v;
    }
    
    private void loadProperties(){
        
        try {
			InputStream fileStream = getActivity().getAssets().open("config.properties");
			configProp = new Properties();
			configProp.load(fileStream);
			
			fileStream = getActivity().getAssets().open("text_en.properties");
			text_en = new Properties();
			text_en.load(fileStream);
			
			fileStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public void scheduleAlarm()
    {
	    	
    		Calendar currenttime = Calendar.getInstance();
    		//System.currentTimeMillis();

    		Calendar alarmtime = Calendar.getInstance();
	        alarmtime.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
	        alarmtime.set(Calendar.MINUTE, picker.getCurrentMinute());
	        alarmtime.set(Calendar.SECOND, 0);
	        alarmtime.set(Calendar.MILLISECOND, 0);
	        
	        long diff = 0;
	        //compare alarm time to current time
	        if ((alarmtime.get(Calendar.HOUR_OF_DAY)==currenttime.get(Calendar.HOUR_OF_DAY) && 
	        		alarmtime.get(Calendar.MINUTE)==currenttime.get(Calendar.MINUTE))){
	        	// Do nothing
	        }
	        else if (alarmtime.before(currenttime))	// add one day to alarm time
	        	alarmtime.add(Calendar.DAY_OF_MONTH, 1); 
	        
	        
	        diff =  alarmtime.getTimeInMillis() - System.currentTimeMillis();
        	
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			if (diffMinutes == 0 && diffHours == 0)
			{
				tView.setText("Alarm triggers in less than 1 min");
				diff = 3000;
			}
			else
				tView.setText("Alarm triggers in "+diffHours+" hours "+diffMinutes+ "mins" );
			
			
			long setAlarm = System.currentTimeMillis() + diff;
    		
			
			Intent intentAlarm = new Intent(getActivity(), AlarmReceiver.class);
		    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, setAlarm, PendingIntent.getBroadcast(getActivity(),1,intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            
            //Toast.makeText(this, "Alarm Scheduled", Toast.LENGTH_LONG).show();
    }
    
    private void flipCard() {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, new SettingsFragment())
                .addToBackStack(null)
                .commit();
    }

}
