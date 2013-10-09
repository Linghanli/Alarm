package com.ece.alarmmanager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
         @Override
            public void onReceive(Context context, Intent intent)
            {
        	 
        	 /*PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        	 PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyAlarmWakeLock");
        	 wl.acquire();*/
        	 //WakeLocker.acquire(context);
        	
               Toast.makeText(context, "Alarm Triggered", Toast.LENGTH_LONG).show();
               Intent i = new Intent();
               i.setClassName("com.ece.alarmmanager", "com.ece.alarmmanager.AlarmActivity");
               i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(i);  
            }
      
}
