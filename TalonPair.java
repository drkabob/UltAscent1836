package com.milkenknights;

import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;

public class TalonPair implements SpeedController {
	private Talon ma;
	private Talon mb;

	// whether or not the Talon should be reversed
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

	@Override
	public double get() {
		return speed;
	}

	public double getA() {
		return ma.get();
	}

	public double getB() {
		return mb.get();
	}

	@Override
	public void set(double newspeed) {
		speed = newspeed;
		ma.set(reva ? -newspeed : newspeed);
		mb.set(revb ? -newspeed : newspeed);
	}

	@Override
	public void disable() {
		ma.disable();
		mb.disable();
	}

	// we have to implement these methods as part of SpeedController
	// even though they're not really used
	@Override
	public void pidWrite(double output) {
		ma.pidWrite(reva ? -output : output);
		mb.pidWrite(revb ? -output : output);
	}
	@Override
	public void set(double speed, byte syncGroup) {
		set(speed);
	}
}
