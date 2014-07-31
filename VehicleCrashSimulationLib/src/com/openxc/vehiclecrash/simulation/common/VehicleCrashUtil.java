package com.openxc.vehiclecrash.simulation.common;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;

import com.openxc.sources.DataSourceException;
import com.openxc.sources.trace.TraceVehicleDataSource;
import com.openxc.vehiclecrash.simulation.R;

public class VehicleCrashUtil {

	private final String TAG = AppLog.getClassName();
	private static VehicleCrashUtil mVehicleCrashUtil = null;
	private OnVehicleCrashedListener mOnVehicleCrashedListener = null;
	public static double mPreviousVehicleSpeed = 0;
	public static double mPreviousEngineSpeed = 0;
	private String mSourceFile = null;
	private TraceVehicleDataSource mSource = null;
	private Context mContext = null;
	private boolean VehicleCrashProbable = false;
	private boolean VehicleCrashed = false;

	private VehicleCrashUtil() {
	}

	public static VehicleCrashUtil getInstance() {
		if (null == mVehicleCrashUtil) {
			mVehicleCrashUtil = new VehicleCrashUtil();
		}

		return mVehicleCrashUtil;
	}

	public void setOnVehicleCrashedListener(OnVehicleCrashedListener listener) {
		mOnVehicleCrashedListener = listener;
	}

	public double getPreviousVehicleSpeed() {
		return mPreviousVehicleSpeed;
	}

	public void setPreviousVehicleSpeed(double previousVehicleSpeed) {
		this.mPreviousVehicleSpeed = previousVehicleSpeed;
	}

	public double getPreviousEngineSpeed() {
		return mPreviousEngineSpeed;
	}

	public void setPreviousEngineSpeed(double previousEngineSpeed) {
		this.mPreviousEngineSpeed = previousEngineSpeed;
	}

	public String getSourceFile() {
		return mSourceFile;
	}

	public void setSourceFile(Context context, String sourceFile) {
		this.mContext = context;
		this.mSourceFile = sourceFile;
	}

	public Context getContext() {
		return mContext;
	}

	public void setContext(Context context) {
		this.mContext = context;
	}

	public TraceVehicleDataSource getSource() {

		try {

			if (null == mSource) {
				AppLog.info(TAG,
						"Trace Vehicle Data Source is null, Seting Using defined Drive Trace file.");
				if (null != mContext && null != mSourceFile) {
					AppLog.info(TAG, "Drive Trace file is defined by Client.");
					mSource = new TraceVehicleDataSource(mContext, new URI(
							getSourceFile()));
				} else {
					AppLog.info(TAG,
							"Client is not defining any Drive Trace file. Using defalut drive trace.");
					mSource = new TraceVehicleDataSource(mContext, new URI(
							"resource://" + R.raw.driving));
				}
			} else {
				AppLog.info(TAG,
						"Trace Vehicle Data Source is not null. Already set by Client App.");
			}

		} catch (DataSourceException e) {
			AppLog.error(TAG, "Couldn't add Source to Trace Vehicle : ", e);
		} catch (URISyntaxException e) {
			AppLog.error(TAG, "URI Syntax is no correct : ", e);
		}
		return mSource;
	}

	public void setSource(TraceVehicleDataSource source) {
		this.mSource = source;
	}

	public boolean checkVehicleCrash(double EngineSpeed, double VehicleSpeed) {
		AppLog.enter(TAG, AppLog.getMethodName());

		// check what speed we received
		if (EngineSpeed >= 0) {
			// engine speed is received
			if (mPreviousEngineSpeed > 1200 && EngineSpeed == 0.0) {
				if (VehicleCrashProbable) {

					VehicleCrashed = true;
				} else {
					VehicleCrashProbable = true;
				}
			}

			if (EngineSpeed > 0) {
				VehicleCrashed = false;
				VehicleCrashProbable = false;
			}
		}

		if (VehicleSpeed >= 0) {
			// vehicle speed is received
			if (mPreviousVehicleSpeed > 40 && VehicleSpeed == 0.0) {
				if (VehicleCrashProbable) {

					VehicleCrashed = true;
				} else {
					VehicleCrashProbable = true;
				}
			}

			if (VehicleSpeed > 0) {
				VehicleCrashed = false;
				VehicleCrashProbable = false;
			}
		}

		// notify if vehicle crashed
		if (VehicleCrashed) {

			AppLog.info(TAG, "Car Crashed !!!");
			AppLog.info("mPreviousEngineSpeed" + mPreviousEngineSpeed,
					"mPreviousVehicleSpeed " + mPreviousEngineSpeed);

			// notify to subscriber app
			if (null != mOnVehicleCrashedListener) {
				mOnVehicleCrashedListener.onVehicleCrashed();

			}
		}

		AppLog.exit(TAG, AppLog.getMethodName());

		return VehicleCrashed;
	}

}
