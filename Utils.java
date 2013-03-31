package com.milkenknights;

import edu.wpi.first.wpilibj.CounterBase;

public class Utils {
	public static double getBangBang(double targetRPM, double slowOutput, CounterBase source) {
		return 60/source.getPeriod() < targetRPM ? 1 : slowOutput;
	}
}
