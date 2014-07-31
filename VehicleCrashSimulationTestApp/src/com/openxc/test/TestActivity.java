package com.openxc.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.openxc.vehiclecrash.simulation.VehicleCrashNotificationService;
import com.openxc.vehiclecrash.simulation.common.AppLog;
import com.openxc.vehiclecrash.simulation.common.OnVehicleCrashedListener;
import com.openxc.vehiclecrash.simulation.common.VehicleCrashUtil;

public class TestActivity extends Activity implements OnVehicleCrashedListener,
		android.view.View.OnClickListener {

	private final String TAG = AppLog.getClassName();
	private TextView mVehicleCrashStatusView;
	private Button btnRestart;
	private final String VEHICLE_NOT_CRASHED_MSG = "Vehicle is running safely !!!";
	private final String VEHICLE_CRASHED_MSG = "Vehicle Crashed !!!";
	boolean b1 = false;
	String sourceFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		AppLog.enter(TAG, AppLog.getMethodName());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		mVehicleCrashStatusView = (TextView) findViewById(R.id.vehicle_crash);
		mVehicleCrashStatusView.setText(VEHICLE_NOT_CRASHED_MSG);
		btnRestart = (Button) findViewById(R.id.btnRestart);
		btnRestart.setOnClickListener(this);
		btnRestart.setVisibility(View.INVISIBLE);
		AppLog.exit(TAG, AppLog.getMethodName());
	}

	@Override
	public void onResume() {
		startLibService();
		super.onResume();
	}

	public void startLibService() {
		AppLog.enter(TAG, AppLog.getMethodName());

		btnRestart.setVisibility(View.INVISIBLE);
		mVehicleCrashStatusView.setTextColor(Color.BLACK);
		mVehicleCrashStatusView.setText(VEHICLE_NOT_CRASHED_MSG);

		// Registering OnVehicleCrashedListener
		// To get Notification when Vehicle Crashed
		VehicleCrashUtil.getInstance().setOnVehicleCrashedListener(this);

		// Providing reference of Drive Trace File
		String sourceFile = "resource://" + R.raw.carcrash;
		VehicleCrashUtil.getInstance().setSourceFile(this, sourceFile);

		AppLog.info(TAG, "Starting vehicle crash service");
		startService(new Intent(this, VehicleCrashNotificationService.class));

		AppLog.exit(TAG, AppLog.getMethodName());
	}

	@Override
	public void onPause() {

		AppLog.enter(TAG, AppLog.getMethodName());
		super.onPause();
		AppLog.exit(TAG, AppLog.getMethodName());
	}

	@Override
	protected void onDestroy() {
		AppLog.enter(TAG, AppLog.getMethodName());
		stopService(new Intent(this, VehicleCrashNotificationService.class));
		super.onDestroy();
		AppLog.exit(TAG, AppLog.getMethodName());
	}

	@Override
	public void onVehicleCrashed() {
		AppLog.enter(TAG, AppLog.getMethodName());

		AppLog.info(TAG, VEHICLE_CRASHED_MSG);
		mVehicleCrashStatusView.setTextColor(Color.RED);
		mVehicleCrashStatusView.setText(VEHICLE_CRASHED_MSG +"  Notification Received In App");
		btnRestart.setVisibility(View.VISIBLE);
		AppLog.exit(TAG, AppLog.getMethodName());
	}

	
	// Event on reStart button
	@Override
	public void onClick(View view) {

		if (R.id.btnRestart == view.getId()) {
			//stop all background services
			stopService(new Intent(this, VehicleCrashNotificationService.class));
			// to reregister service on on click restart button
			startLibService();
		}

	}

}
