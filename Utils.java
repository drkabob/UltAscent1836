package com.milkenknights;

import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.DriverStation;

public class Utils {
	public static double getBangBang(double targetRPM, double slowOutput, CounterBase source) {
		return periodToRPM(source.getPeriod()) < targetRPM ? 1 : slowOutput;
	}
	
	public static double periodToRPM(double periodInSeconds) {
		return 60/periodInSeconds;
	}
	
	public static double voltageSpeed(double frac) {
		double voltage = DriverStation.getInstance().getBatteryVoltage();

		if (voltage != 0) {
			return (12.5*frac) / voltage;
		} else {
			return 0;
		}
	}
}
