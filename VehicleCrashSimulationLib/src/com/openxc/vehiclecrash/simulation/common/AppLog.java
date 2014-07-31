package com.openxc.vehiclecrash.simulation.common;

import android.util.Log;

public class AppLog {
	public static final boolean ENABLE_LOG_DEBUG = true;

	private static final String ENTER_TAG;
	private static final String EXIT_TAG;
	static {
		ENTER_TAG = " <<<<<<<<";
		EXIT_TAG = " >>>>>>>>";
	}

	enum LogLevel {
		LOG_LEVEL_ERROR, LOG_LEVEL_WARNING, LOG_LEVEL_INFO, LOG_LEVEL_DEBUG, LOG_LEVEL_VERBOSE
	}

	private static void outLog(LogLevel loglevel, String tag, String log) {
		switch (loglevel) {
		case LOG_LEVEL_ERROR:
			Log.e(tag, log);
			break;
		case LOG_LEVEL_WARNING:
			Log.w(tag, log);
			break;
		case LOG_LEVEL_INFO:
			Log.i(tag, log);
			break;
		case LOG_LEVEL_DEBUG:
			Log.d(tag, log);
			break;
		case LOG_LEVEL_VERBOSE:
			Log.v(tag, log);
			break;
		default:
			break;
		}
	}

	public static final void trace(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_DEBUG, tag, log);
	}

	/**
	 * for display debug. working same as Log.d(tag,log) with enter tag
	 * 
	 * use at starting of method to mark initial point.
	 */
	public static final void enter(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_DEBUG, tag, log + ENTER_TAG);
	}

	/**
	 * for display debug. working same as Log.d(tag,log) with exit tag
	 * 
	 * use at ending of method to mark exit point.
	 * 
	 */
	public static final void exit(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_DEBUG, tag, log + EXIT_TAG);
	}

	public static final void checkIf(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_DEBUG, tag, log);
	}

	/**
	 * for display information . working same as Log.i(tag,log)
	 * 
	 */
	public static final void info(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_INFO, tag, log);
	}

	/**
	 * for display warning. working same as Log.d(tag,log)
	 * 
	 */
	public static final void warning(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_WARNING, tag, log);
	}

	/**
	 * for display error. working same as Log.e(tag,log)
	 * 
	 */
	public static final void error(String tag, String log) {
		if (ENABLE_LOG_DEBUG)
			outLog(LogLevel.LOG_LEVEL_ERROR, tag, log);
	}

	public static final void error(String tag, String log, Throwable tr) {
		error(tag, log);
		tr.printStackTrace();
	}

	public static String getMethodName() {
		return new Exception().getStackTrace()[1].getMethodName();
	}

	public static String getClassName() {
		return new Exception().getStackTrace()[1].getClassName();
	}
}
