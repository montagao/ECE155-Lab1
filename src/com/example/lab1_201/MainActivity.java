package com.example.lab1_201;

import android.app.Activity;

import java.util.Arrays;

import android.app.ActionBar;
import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;
import ca.uwaterloo.sensortoy.*;

public class MainActivity extends Activity {
	
	static LineGraphView graph;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
		


		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.layout1);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			try {

				graph = new LineGraphView(getActivity(), 100, Arrays.asList("x", "y", "z"));
				layout.addView(graph);
				graph.setVisibility(View.VISIBLE);
			} catch ( NullPointerException e){
				Log.d("exception", "null pointer!");
			}
			
			
			// using label id to add view
			//TextView lightValuesOut = (TextView) rootView.findViewById(R.id.label1);
			
			// programmatically adding labels
			TextView lightValuesOut = addNewLabel("", rootView, layout);
			TextView accelValuesOut = addNewLabel("", rootView, layout);
			TextView magFieldValuesOut = addNewLabel("", rootView, layout);
			TextView rotVecValuesOut = addNewLabel("", rootView, layout);
			
	
			
			class someSensorEventListener implements SensorEventListener {
				TextView output;
				private int sensorType;
				private float[] recordVals = new float[3];
				private String sensorString;
				private String sensorValString;
				private String sensorRecordValString = "x: 0 y: 0 z: 0";
				
				public someSensorEventListener(TextView outputView, int wantedSensorType)
				{
					output = outputView;
					sensorType = wantedSensorType;
					
					// Change initial label to correspond to Sensor being recorded.
					switch (wantedSensorType)
					{
					case Sensor.TYPE_ACCELEROMETER:
						sensorString = "\nAcclerometer Reading:";
						break;
					case Sensor.TYPE_LIGHT:
						sensorString = "\nLight Reading:";
						sensorRecordValString = "";
						break;
					case Sensor.TYPE_MAGNETIC_FIELD:
						sensorString = "\nMagnetic Field Reading:";
						break;
					case Sensor.TYPE_ROTATION_VECTOR:
						sensorString = "\nRotation Vector Reading:";
						break;
					default:
						Log.d("Error","Invalid SensorType given! App will not work.");
					}
				}
				
				// Resets all record values to 0;
				public void clearRecords()
				{
					for (int i = 0; i < recordVals.length; i++)
					{
						recordVals[i] = 0;
					}
				}

				public void onAccuracyChanged(Sensor s, int i) {}
				
				// for all sensors beside Light sensor, displays the x: y: z: values associated with it
				// and the absolute value records of each 
				public void onSensorChanged(SensorEvent se) {
					
					if (se.sensor.getType() == sensorType){

						if (sensorType != Sensor.TYPE_LIGHT){
							if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
								graph.addPoint(se.values);
							
							sensorValString = String.format("\n x: %.2f y: %.2f z: %.2f", se.values[0], se.values[1], se.values[2]);
							
							for (int i = 0; i < 3; i ++){
								if (abs(se.values[i]) > abs(recordVals[i]))
								{
									recordVals[i] = se.values[i];
								}
							}
							sensorRecordValString = String.format("\nRecord: x: %.2f y: %.2f z: %.2f", recordVals[0],  recordVals[1], recordVals[2]);
						} else {
							sensorValString = String.format(" %.2f", se.values[0]);
						}
						output.setText(String.valueOf(sensorString+sensorValString + sensorRecordValString)); 
					}
				}

				private float abs(float f) {
					// returns absolute value of a float
					if (f < 0)
						return (float) (-1.0*f);
					else
						return f;
				}
			}
			
			// Create sensor manager and Sensor references for each applicable sensor
			SensorManager sensorManager = (SensorManager) rootView.getContext().getSystemService(SENSOR_SERVICE);
			Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			Sensor magFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			Sensor rotVecSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			
			// Create references to all needed eventListeners.
			SensorEventListener lightListener = new someSensorEventListener(lightValuesOut, Sensor.TYPE_LIGHT);
			sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
			
			final SensorEventListener accelListener = new someSensorEventListener(accelValuesOut, Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
			
			final SensorEventListener magFieldListener = new someSensorEventListener(magFieldValuesOut, Sensor.TYPE_MAGNETIC_FIELD);
			sensorManager.registerListener(magFieldListener, magFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
			
			final SensorEventListener rotVecListener = new someSensorEventListener(rotVecValuesOut, Sensor.TYPE_ROTATION_VECTOR);
			sensorManager.registerListener(rotVecListener, rotVecSensor, SensorManager.SENSOR_DELAY_NORMAL);
			
			// add clear button for class
			final Button clearButton = new Button(rootView.getContext());
			clearButton.setText("Clear");
			layout.addView(clearButton);
			clearButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					((someSensorEventListener) accelListener).clearRecords();
					((someSensorEventListener) magFieldListener).clearRecords();
					((someSensorEventListener) rotVecListener).clearRecords();
					
				}
			});
			return rootView;
		
		}
		
		public TextView addNewLabel(String label,View rootView, LinearLayout layout)
		{
			TextView l = new TextView(rootView.getContext());
			l.setText(label);
			layout.addView(l);
			return l;
		}
		
	}
	

}
