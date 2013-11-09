package com.ece.alarmmanager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
	public static final String PREF = "EceAlarmApp";     
	@Override
    public void onReceive(Context context, Intent intent){
	 	
	   SharedPreferences settings = context.getSharedPreferences(PREF, 0);
	   SharedPreferences.Editor editor = settings.edit();
	   editor.putBoolean("disableAlarmButton", false);
	   editor.commit();	
	        	
       Toast.makeText(context, "Alarm Triggered", Toast.LENGTH_LONG).show();
       Intent i = new Intent();
       i.setClassName("com.ece.alarmmanager", "com.ece.alarmmanager.AlarmActivity");
       i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       context.startActivity(i);  
    }
      
}
