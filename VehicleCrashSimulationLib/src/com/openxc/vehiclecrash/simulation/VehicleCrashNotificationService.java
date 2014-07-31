package com.openxc.vehiclecrash.simulation;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.openxc.VehicleManager;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;
import com.openxc.sources.trace.TraceVehicleDataSource;
import com.openxc.vehiclecrash.simulation.common.AppLog;
import com.openxc.vehiclecrash.simulation.common.VehicleCrashUtil;

public class VehicleCrashNotificationService extends Service {
	private final String TAG = AppLog.getClassName();

	private boolean mCrashDetected = false;
	private final Handler mHandler = new Handler();
	private VehicleManager mVehicleManager = null;
	private VehicleSpeed.Listener mSpeedListener = null;
	private EngineSpeed.Listener mEngineSpeed = null;
	private ServiceConnection mConnection = null;
	private boolean mIsBound = false;

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new VehicleCrashBinder();

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class VehicleCrashBinder extends Binder {
		VehicleCrashNotificationService getService() {
			return VehicleCrashNotificationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		AppLog.enter(TAG, AppLog.getMethodName());
		AppLog.enter(TAG, AppLog.getMethodName());

		return mBinder;
	}

	@Override
	public void onCreate() {
		AppLog.enter(TAG, AppLog.getMethodName());

		super.onCreate();
		VehicleCrashUtil.getInstance().setContext(this);

		initialize();

		AppLog.exit(TAG, AppLog.getMethodName());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AppLog.enter(TAG, AppLog.getMethodName());

		super.onStartCommand(intent, flags, startId);

		if (START_FLAG_RETRY == flags) {
			doUnbindService();
			doBindService();
		} else {
			doBindService();
		}

		AppLog.exit(TAG, AppLog.getMethodName());
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		AppLog.enter(TAG, AppLog.getMethodName());

		super.onDestroy();
		doUnbindService();

		mVehicleManager = null;
		mSpeedListener = null;
		mEngineSpeed = null;
		mConnection = null;
		mCrashDetected = false;
		mIsBound = false;

		AppLog.exit(TAG, AppLog.getMethodName());
	}

	private void initialize() {
		AppLog.enter(TAG, AppLog.getMethodName());

		mSpeedListener = new VehicleSpeed.Listener() {
			
			private String TAG = AppLog.getClassName();

			public void receive(Measurement measurement) {
				AppLog.enter(TAG, AppLog.getMethodName());
				if (mCrashDetected == true) {
					// Stopping Vehicle Crash Notification Service
					// if Vehicle Crashed
					stopSelf();
					return;
				}
				final VehicleSpeed speed = (VehicleSpeed) measurement;
				mHandler.post(new Runnable() {
					public void run() {
						double vehicleSpeed = speed.getValue().doubleValue();
						double engineSpeed = -1.0;
						AppLog.info(TAG, "Vehicle Speed is : " + vehicleSpeed);
						if (VehicleCrashUtil.getInstance().checkVehicleCrash(
								engineSpeed, vehicleSpeed)) {
							mCrashDetected = true;
						} else {
							VehicleCrashUtil.getInstance()
									.setPreviousVehicleSpeed(vehicleSpeed);
						}
					}
				});
				AppLog.exit(TAG, AppLog.getMethodName());
			}
		};

		mEngineSpeed = new EngineSpeed.Listener() {
			private String TAG = AppLog.getClassName();

			public void receive(Measurement measurement) {
				AppLog.enter(TAG, AppLog.getMethodName());
				if (mCrashDetected == true) {
					// Stopping Vehicle Crash Notification Service
					// if Vehicle Crashed
					stopSelf();
					return;
				}

				final EngineSpeed speed = (EngineSpeed) measurement;
				mHandler.post(new Runnable() {
					public void run() {
						double engineSpeed = speed.getValue().doubleValue();
						double vehicleSpeed = -1.0;
						
						AppLog.info(TAG, "Engine Speed is : " + engineSpeed);
						if (VehicleCrashUtil.getInstance().checkVehicleCrash(
								engineSpeed, vehicleSpeed)) {
							mCrashDetected = true;
						} else {
							VehicleCrashUtil.getInstance()
									.setPreviousEngineSpeed(engineSpeed);
						}
					}
				});
				AppLog.exit(TAG, AppLog.getMethodName());
			}
		};

		mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				AppLog.enter(TAG, AppLog.getMethodName());

				mVehicleManager = ((VehicleManager.VehicleBinder) service)
						.getService();

				try {

					TraceVehicleDataSource source = VehicleCrashUtil
							.getInstance().getSource();

					if (null != source) {
						AppLog.info(TAG,
								"Trace Vehicle Data Source is not null, Using defined Drive Trace.");
						mVehicleManager.addSource(source);
					} else {
						AppLog.info(TAG,
								"Trace Vehicle Data Source is null, fetching real data.");
					}

					AppLog.info(TAG, "Adding VehicleSpeed listener.");
					mVehicleManager.addListener(VehicleSpeed.class,
							mSpeedListener);
					AppLog.info(TAG, "Adding EngineSpeed listener.");
					mVehicleManager
							.addListener(EngineSpeed.class, mEngineSpeed);

				} catch (VehicleServiceException e) {
					
					AppLog.error(TAG,
							"Couldn't add listeners for measurements", e);
				} catch (UnrecognizedMeasurementTypeException e) {
					AppLog.error(TAG,
							"Couldn't add listeners for measurements", e);
				}
				mIsBound = true;
				AppLog.exit(TAG, AppLog.getMethodName());
			}

			public void onServiceDisconnected(ComponentName className) {
				AppLog.enter(TAG, AppLog.getMethodName());

				AppLog.info(TAG, "VehicleService disconnected unexpectedly");
				mVehicleManager = null;
				mIsBound = false;

				AppLog.exit(TAG, AppLog.getMethodName());
			}
		};

		AppLog.exit(TAG, AppLog.getMethodName());
	}

	private void doUnbindService() {
		AppLog.enter(TAG, AppLog.getMethodName());

		mCrashDetected = false;
		if (mIsBound) {
			AppLog.info(TAG, "Unbinding from vehicle service");
			unbindService(mConnection);
			mIsBound = false;
		}
		AppLog.exit(TAG, AppLog.getMethodName());
	}

	private void doBindService() {
		
		AppLog.enter(TAG, AppLog.getMethodName());

		bindService(new Intent(this, VehicleManager.class), mConnection,
				Context.BIND_AUTO_CREATE);

		AppLog.exit(TAG, AppLog.getMethodName());
	}
}
