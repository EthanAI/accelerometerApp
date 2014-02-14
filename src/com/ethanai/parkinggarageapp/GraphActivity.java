package com.ethanai.parkinggarageapp;


import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
//import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

//TODO add reciever for orientation also add that graph, probably our important one
@SuppressLint("SimpleDateFormat")
public class GraphActivity extends Activity {
	
    private GraphicalView mChart;
    
    private TimeSeries rotationCountSeries;
    /*
    private TimeSeries mXSeries;
    private TimeSeries mYSeries;
    private TimeSeries mZSeries;
    private TimeSeries mAngleSeries;
    */

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    
    private XYSeriesRenderer rotationCountRenderer;
    /*
    private XYSeriesRenderer mXRenderer;
    private XYSeriesRenderer mYRenderer;
    private XYSeriesRenderer mZRenderer;
    private XYSeriesRenderer mAngleRenderer;
    */

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
       
    int plotDataCount = 100000; //plot limited by size of recent data object. Not limited here
    RecentSensorData recentData = new RecentSensorData();
    
    
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
    
	private final String ACCELEROMETER_TAG 	= "accelerometer";
	private final String MAGNETIC_TAG 		= "magnetic";
	private final String ORIENTATION_TAG 	= "orientation";
	private final String COMPASS_TAG 		= "compass";
	private final String PRESSURE_TAG 		= "pressure";
    
       	
	//http://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
	// Our handler for received Intents. This will be called whenever an Intent
	// with an action named "custom-event-name" is broadcasted.
    //TODO move this to another class or clean up
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Get extra data included in the Intent
		    String sensorType = intent.getStringExtra("sensorType");
		    Log.i("GraphActivityReceiver", "Got message: " + sensorType);
		    recentData = (RecentSensorData) intent.getSerializableExtra("recentData");
		    
		    if(sensorType.equals(ACCELEROMETER_TAG)) {
 
		    } else if (sensorType.equals(MAGNETIC_TAG)) {
		    		    	
		    } else if (sensorType.equals(ORIENTATION_TAG)) {
		    	TextView tvTest = (TextView) findViewById(R.id.testField);
		    	tvTest.setText("Floor: " + recentData.parkedFloor);
		    	updateChart();	
		    } else if (sensorType.equals(COMPASS_TAG)) {
		    	
		    } else if (sensorType.equals(PRESSURE_TAG)) {	
		    }
		    
		}
	};
	    
	// Called when the activity is first created. 
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
		
		//make this poll sensor service status and verify if it is running. May need some kind of trigger to repaint
	    TextView tvTest = (TextView) findViewById(R.id.testField);
	    tvTest.setText("0.0"); //recentEntries.get(recentEntries.size() - 1).toString());
			
	    //listeners are so we can hear when the sensor service updates so we can update our graph view. A better way to communicate?
	    //need to list each term we are listening for here
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ACCELEROMETER_TAG));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(MAGNETIC_TAG));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(ORIENTATION_TAG));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(COMPASS_TAG));
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(PRESSURE_TAG));	
		
		
		//location code trial
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		float minAccuracy = 500.0f;
		long minTime = 1000 * 60 * 5;
		Location mBestReading;
		
		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;
		List<String> matchingProviders = mLocationManager.getAllProviders();

		for (String provider : matchingProviders) {

			Location location = mLocationManager.getLastKnownLocation(provider);

			if (location != null) {

				float accuracy = location.getAccuracy();
				long time = location.getTime();

				if (accuracy < bestAccuracy) {

					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;

				}
			}
		}
		if (bestAccuracy > minAccuracy || bestTime < minTime) {
			bestResult = null;
		}
		//location.getAccuracy()
		//location.getLongitude()
		//location.getLatitude()
		//end of location code
		
	}
	

	@Override
	protected void onDestroy() {
	  // Unregister since the activity is about to be closed.
	  //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  super.onDestroy();
	}
	
    private void initChart() {
    	DisplayMetrics metrics = this.getResources().getDisplayMetrics();
    	float MEDIUM_TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, metrics);

    	rotationCountSeries = new TimeSeries("Turns"); //add better time element?
    	mDataset.addSeries(rotationCountSeries);
    	rotationCountRenderer = new XYSeriesRenderer();
    	rotationCountRenderer.setColor(Color.RED);
    	mRenderer.addSeriesRenderer(rotationCountRenderer);
    	
		mRenderer.setYTitle("Quarter Turns");
		mRenderer.setAxisTitleTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setLegendTextSize(MEDIUM_TEXT_SIZE);
		
		mRenderer.setShowGridX(true);
		//TODO add button to restore view to following
		
		mRenderer.setLabelsTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.BLACK);
		
    	/*
        //mCurrentSeries = new TimeSeries("Accelerometer");
        mXSeries = new TimeSeries("X");
        mYSeries = new TimeSeries("Y");
        mZSeries = new TimeSeries("Z");
        mAngleSeries = new TimeSeries("Angle");

        mDataset.addSeries(mXSeries);
        mDataset.addSeries(mYSeries);
        mDataset.addSeries(mZSeries);
        mDataset.addSeries(mAngleSeries);

        mXRenderer = new XYSeriesRenderer();
        mXRenderer.setColor(Color.RED);
        mYRenderer = new XYSeriesRenderer();
        mYRenderer.setColor(Color.YELLOW);
        mZRenderer = new XYSeriesRenderer();
        mZRenderer.setColor(Color.GREEN);
        mAngleRenderer = new XYSeriesRenderer();
        mAngleRenderer.setColor(Color.WHITE);

        mRenderer.addSeriesRenderer(mXRenderer);
		mRenderer.addSeriesRenderer(mYRenderer);
		mRenderer.addSeriesRenderer(mZRenderer);
		mRenderer.addSeriesRenderer(mAngleRenderer);
        
		//mRenderer.setYTitle("gs of force");
		mRenderer.setYTitle("Angle");
		mRenderer.setAxisTitleTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setLegendTextSize(MEDIUM_TEXT_SIZE);
		
		mRenderer.setShowGridX(true);
		//TODO add button to restore view to following
		
		mRenderer.setLabelsTextSize(MEDIUM_TEXT_SIZE);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.BLACK);
		//mRenderer.setMarginsColor(Color.RED);
		
    	//mCurrentRenderer.setChartValuesTextSize(val);
		*/

    }

    private void loadData() {
    	rotationCountSeries.clear();
    	/*
    	mXSeries.clear();
    	mYSeries.clear();
    	mZSeries.clear();
    	mAngleSeries.clear();
    	*/
    	//for(int i = 0; i < plotDataCount && i < recentData.accRecent.size(); i++) {
    	//	mCurrentSeries.add(i, recentData.accRecent.get(i).x);
    	//}
    	
    	for(int i = 0; i < plotDataCount && i < recentData.orientRecent.size(); i++) {
    		rotationCountSeries.add(i, recentData.orientRecent.get(i).totalTurnDegrees / 90);
    		/*
    		mXSeries.add(i, recentData.orientRecent.get(i).azimuthInDegrees);
    		mYSeries.add(i, recentData.orientRecent.get(i).pitchInDegrees);
    		mZSeries.add(i, recentData.orientRecent.get(i).rollInDegrees);
    		//mAngleSeries.add(i, recentData.orientRecent.get(i).inclinationInDegrees);
    		mAngleSeries.add(i, recentData.orientRecent.get(i).totalTurnDegrees);
    		*/
    	}
    }

    protected void onResume() {
        super.onResume();
        updateChart();
    }
    
    /*
     * Deletes and reloads all the data. Runs really fast anyway, but could be optimized if needed. 
     * (maybe if we plot 7 or 8 sensors at once it might be important?)
     * Good demo here https://www.youtube.com/watch?v=E9fozQ5NlSo
     */
    private void updateChart() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        
        if(mChart == null) {
        	initChart();
        	loadData();
        	mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        	layout.addView(mChart);
        
        //} else if(recentData.accRecent != null && recentData.accRecent.size() > 0) {

        } else if(recentData.magnRecent != null && recentData.magnRecent.size() > 0) {
        	//initChart();
        	loadData();
        	//mChart = ChartFactory.getLineChartView(this, mDataset, mRenderer);
        	//layout.addView(mChart);
        	mChart.repaint();
        }
    }  

	
  /*
   * Function for the buttons
   */
	public void changeToTextActivity(View view) {
	    Intent intent = new Intent(GraphActivity.this, TextActivity.class);
	    startActivity(intent);
		//this.finish();
	}
	
	public void startSensorService(View view) { 
		Intent intent = new Intent(getBaseContext(), SensorService.class);
		intent.putExtra("maxReadingHistoryCount", plotDataCount);
		startService(intent); //start Accelerometer Service. Pass it info
	}
	
	public void stopSensorService(View view) {
		stopService(new Intent(getBaseContext(), SensorService.class)); //start Accelerometer Service. Pass it info
	}
  
}