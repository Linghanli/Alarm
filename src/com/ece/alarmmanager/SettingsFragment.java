package com.ece.alarmmanager;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
	
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
    	View v = inflater.inflate(R.layout.activity_settings, container, false);
    	tapSensitivity = (SeekBar) v.findViewById(R.id.seekBar1);
    	snoozeSpinner = (Spinner) v.findViewById(R.id.spinner1);
    	weatherSwitch = (Switch) v.findViewById(R.id.switch1);
    	country = (EditText) v.findViewById(R.id.weather);
		
		updateButton = (Button) v.findViewById(R.id.updateSettings);
		updateButton.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View v) {
    			updateSettings();
    	}
    	});
		
		SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		int s1 = settings.getInt("p1", 5);
		int s2 = settings.getInt("p2", 0);
		Boolean s3 = settings.getBoolean("p3", true);
		String s4 = settings.getString("p4", null);
		
		tapSensitivity.incrementProgressBy(s1);
		snoozeSpinner.setSelection(s2);
		weatherSwitch.setChecked(s3);
		country.setText(s4);
		
		return v;
    }
    
    public void updateSettings(){
		SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("p1", tapSensitivity.getProgress());
		editor.putInt("p2", snoozeSpinner.getSelectedItemPosition());
		editor.putBoolean("p3", weatherSwitch.isChecked());
		editor.putString("p4", country.getText().toString());
		
		if (editor.commit())
			Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
	}
    
    @Override
    public void onStop() {
    	getFragmentManager().popBackStack();
    	super.onStop();
    }
}