package com.ece.alarmmanager;

import java.util.Properties;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.TimePicker;

public class MainActivity extends Activity implements FragmentManager.OnBackStackChangedListener
{
	//useless stuff
     public static final String TAG = "what";
	 private Properties prop;
	//useful stuff 

	 private TimePicker picker;
	 private TextView tView;
	 private boolean mShowingSettings = false;
	 @Override
	 protected void onSaveInstanceState(Bundle outState) {
	     Log.d("called", "now");
	 }
	  @Override
       public void onCreate(Bundle savedInstanceState){
		  
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_flip);   

            if (savedInstanceState == null) {
                // If there is no saved instance state, add a fragment representing the
                // front of the card to this activity. If there is saved instance state,
                // this fragment will have already been added to the activity.
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();
            } else {
            	mShowingSettings = (getFragmentManager().getBackStackEntryCount() > 0);
            }
            
      }

		@Override
		public void onBackStackChanged() {
			// TODO Auto-generated method stub
			
		}
		 private void flipCard() {
		        if (mShowingSettings) {
		            getFragmentManager().popBackStack();
		            return;
		        }

		        // Flip to the back.

		        mShowingSettings = true;
		        getFragmentManager()
		                .beginTransaction()
		                .setCustomAnimations(
		                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
		                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
		                .replace(R.id.container, new SettingsFragment())
		                .addToBackStack(null)
		                .commit();

		        // Defer an invalidation of the options menu (on modern devices, the action bar). This
		        // can't be done immediately because the transaction may not yet be committed. Commits
		        // are asynchronous in that they are posted to the main thread's message loop.
		        /*mHandler.post(new Runnable() {
		            @Override
		            public void run() {
		                invalidateOptionsMenu();
		            }
		        });*/
		    }
		 
    
}
