package com.ece.alarmmanager;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainFragment extends Fragment {

	 private static int SNOOZE_TIME;
	 private static int [] snoozeTimes = {1000, 2000, 4000, 8000};
	 private Properties configProp;
	 private Properties text_en;
	 
	 //private TimePicker picker;
	 private TextView tView;
	 
	 private Button alarmButton;
	 private Button settingsButton;
	 private boolean disableButton;
	 public static final String PREF = "EceAlarmApp";
	
    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, container, false);
        loadProperties();     
        
        tView = (TextView) v.findViewById(R.id.textView1);
        //picker = (TimePicker) v.findViewById(R.id.picker1);
        
        SharedPreferences settings = getActivity().getSharedPreferences(SettingsFragment.PREF, 0);
        disableButton = settings.getBoolean("disableAlarmButton", false);
        alarmButton = (Button) v.findViewById(R.id.button1);
        alarmButton.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			if (disableButton)
    				disableAlarm();
    			else
    				flipCard();
    	}
    	});
        
    	final Button autoConfigButton = (Button) v.findViewById(R.id.autoConfig);
    	autoConfigButton.setOnClickListener(
    			new View.OnClickListener(){
    				public void onClick(View v){
    					getFragmentManager().beginTransaction().addToBackStack(null).commit();
	    				Intent intent = new Intent(getActivity(), AutoConfig.class);	
	    				getActivity().startActivity(intent);
    				}
    			}
    	);
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
    
    public void scheduleAlarm()
    {
		    //Switch button to Disable Alarm button
		    
 		
//			Intent intentAlarm = new Intent(getActivity(), AlarmReceiver.class);
//		    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//            alarmManager.set(AlarmManager.RTC_WAKEUP, setAlarm, PendingIntent.getBroadcast(getActivity(),1,intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            
    }
    
    public void disableAlarm(){
    	disableButton = false;
    	alarmButton.setText("Schedule Alarm");
    	tView.setText("Alarm is Unscheduled");
    	Toast.makeText(getActivity().getApplicationContext(), "Alarm Disabled", Toast.LENGTH_SHORT).show();
    	
    	SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("disableAlarmButton", disableButton);
		editor.commit();
    	
    	Intent intentAlarm = new Intent(getActivity(), AlarmReceiver.class);
    	PendingIntent pi = PendingIntent.getBroadcast(getActivity(),1,intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
	    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);
        pi.cancel();      
    }
    

    @Override
    public void onStart() {
    	super.onStart();
		SharedPreferences settings = getActivity().getSharedPreferences(SettingsFragment.PREF, 0);
		SNOOZE_TIME = snoozeTimes[settings.getInt("p2", 0)];
		
		disableButton = settings.getBoolean("disableAlarmButton", false);
	;
		if (disableButton)
		{	
			alarmButton.setText("Cancel Alarm");
			if (settings.getLong("diff", 1) == 10000)
			{
				tView.setText("Alarm triggers in less than 1 min");
			}
			else
				tView.setText("Alarm triggers in "+settings.getLong("diffHours", 1)+" hours "+settings.getLong("diffMinutes", 1)+ "mins" );

		}
		else
		{
			tView.setText("Alarm is Unscheduled");
			alarmButton.setText("Schedule Alarm");
		}
    }
}
