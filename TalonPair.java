package com.milkenknights;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;

public class TalonPair implements SpeedController {
	private Talon ma;
	private Talon mb;

	private boolean reva;
	private boolean revb;

	private double speed;

	public TalonPair(int a, int b) {
		// if we aren't told that the motors are reversed or not,
		// set them to false by default
		this(a,false,b,false);
	}

	public TalonPair(int a, boolean arev, int b, boolean brev) {
		ma = new Talon(a);
		ma.set(0);
		reva = arev;

		mb = new Talon(b);
		mb.set(0);
		revb = brev;
	}

	public double get() {
		return speed;
	}

	public double getA() {
		return ma.get();
	}

	public double getB() {
		return mb.get();
	}

	public void set(double newspeed) {
		speed = newspeed;
		ma.set(reva ? -newspeed : newspeed);
		mb.set(revb ? -newspeed : newspeed);
	}

	public void disable() {
		ma.disable();
		mb.disable();
	}

	// we have to implement these methods as part of SpeedController
	// even though they're not really used
	public void pidWrite(double output) {
		ma.pidWrite(reva ? -output : output);
		mb.pidWrite(revb ? -output : output);
	}
	public void set(double speed, byte syncGroup) {
		set(speed);
	}
}
