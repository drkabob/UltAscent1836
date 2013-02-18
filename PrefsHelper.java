package com.milkenknights;

import edu.wpi.first.wpilibj.Preferences;

public class PrefsHelper {
	private Preferences prefs;
	public PrefsHelper() {
		prefs = Preferences.getInstance();
	}
	/**
	 * @author Bernard
	 * renamed method from "reload" to "getInstance" for consistency
	 */
	public void resetInstance() {
		prefs = Preferences.getInstance();
	}
	
	public String getString(String key, String backup) {
		try {
			return prefs.getString(key, backup);
		} catch (Preferences.IncompatibleTypeException e) {
			return backup;
		}
	}
	
	public int getInt(String key, int backup) {
		try {
			return prefs.getInt(key, backup);
		} catch (Preferences.IncompatibleTypeException e) {
			return backup;
		}
	}
	
	public double getDouble(String key, double backup) {
		try {
			return prefs.getDouble(key, backup);
		} catch (Preferences.IncompatibleTypeException e) {
			return backup;
		}
	}

	public boolean getBoolean(String key, boolean backup) {
		try {
			return prefs.getBoolean(key, backup);
		} catch (Preferences.IncompatibleTypeException e) {
			return backup;
		}
	}

	public float getFloat(String key, float backup) {
		try {
			return prefs.getFloat(key, backup);
		} catch (Preferences.IncompatibleTypeException e) {
			return backup;
		}
	}

	public float getLong(String key, long backup) {
		try {
			return prefs.getLong(key, backup);
		} catch (Preferences.IncompatibleTypeException e) {
			return backup;
		}
	}
}
