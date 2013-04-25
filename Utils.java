package com.milkenknights;

import edu.wpi.first.wpilibj.CounterBase;

public class Utils {
	public static double getBangBang(double targetRPM, double slowOutput, CounterBase source) {
		return periodToRPM(source.getPeriod()) < targetRPM ? 1 : slowOutput;
	}
	
	public static double periodToRPM(double periodInSeconds) {
		return 60/periodInSeconds;
	}
}
