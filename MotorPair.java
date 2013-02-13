package com.milkenknights;

import edu.wpi.first.wpilibj.SpeedController;

public class MotorPair implements SpeedController {
	private SpeedController ma;
	private SpeedController mb;

	private double adjustA;
	private double adjustB;

	public MotorPair(SpeedController a, SpeedController b) {
		this(a,1,b,1);
	}

	public MotorPair(SpeedController a, double adjA, SpeedController b, double adjB) {
		ma = a;
		ma.set(0);
		adjustA = adjA;

		mb = b;
		mb.set(0);
		adjustB = adjB;
	}

	public double get() {
		return (getA()+getB())/2;
	}

	public double getA() {
		return ma.get();
	}

	public double getB() {
		return mb.get();
	}

	public void set(double speed) {
		ma.set(speed*adjustA);
		mb.set(speed*adjustB);
	}

	public void disable() {
		ma.disable();
		mb.disable();
	}

	// checks the boolean on. If it is true, set
	// the victor to 1 if dir is true, or set it
	// to -1 if dir is false (reverse).
	public void setAsBool(boolean on, boolean dir) {
		set(on ? (dir ? 1 : -1) : 0);
	}
	
	// checks the boolean on. If it is true, set the
	// victor to speed. Otherwise, set it to zero.
	public void checkAndSet(boolean on, double speed) {
		set(on ? speed : 0);
	}

	// we have to implement these methods as part of SpeedController
	// even though they're not really used
	public void pidWrite(double output) {
		ma.pidWrite(output);
		mb.pidWrite(output);
	}
	public void set(double speed, byte syncGroup) {
		ma.set(speed*adjustA, syncGroup);
		mb.set(speed*adjustB, syncGroup);
	}
}
