package com.milkenknights;

import edu.wpi.first.wpilibj.SpeedController;

public class MotorTriple implements SpeedController {
	private SpeedController ma;
	private SpeedController mb;
	private SpeedController mc;

	private boolean reva;
	private boolean revb;
	private boolean revc;

	private double speed;
	
	public MotorTriple(SpeedController a, SpeedController b, SpeedController c) {
		ma = a;
				
	}

	public MotorTriple(SpeedController a, boolean arev, SpeedController b, boolean brev, SpeedController c, boolean crev) {
		ma = a;
		ma.set(0);
		reva = arev;

		mb = b;
		mb.set(0);
		revb = brev;
		
		mc = c;
		mc.set(0);
		revc = crev;
	}

	@Override
	public void disable() {
		ma.disable();
		mb.disable();
		mc.disable();
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
	public double getC() {
		return mc.get();
	}

	@Override
	public void set(double newspeed) {
		ma.set(reva ? -newspeed : newspeed);
		mb.set(revb ? -newspeed : newspeed);
		mc.set(revc ? -newspeed : newspeed);
	}

	@Override
	public void set(double newspeed, byte arg1) {
		set(newspeed);
	}
	@Override
	public void pidWrite(double output) {
		ma.pidWrite(reva ? -output : output);
		mb.pidWrite(revb ? -output : output);
		mc.pidWrite(revc ? -output : output);
	}

	
}
