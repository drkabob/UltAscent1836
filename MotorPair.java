package com.milkenknights;

import edu.wpi.first.wpilibj.SpeedController;

public class MotorPair implements SpeedController {
	private SpeedController ma;
	private SpeedController mb;

	private boolean reva;
	private boolean revb;

	public MotorPair(SpeedController a, SpeedController b) {
		// if we aren't told that the motors are reversed or not,
		// set them to false by default
		this(a,false,b,false);
	}

	public MotorPair(SpeedController a, boolean arev, SpeedController b, boolean brev) {
		ma = a;
		ma.set(0);
		reva = arev;

		mb = b;
		mb.set(0);
		revb = brev;
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
		if(reva) {
			ma.set(-speed);
		} else {
			ma.set(speed);
		}
		if(revb) {
			mb.set(-speed);
		} else {
			mb.set(speed);
		}
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
		set(speed);
	}
}
