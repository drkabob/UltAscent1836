package com.milkenknights;

import edu.wpi.first.wpilibj.SpeedController;

public class MotorTriple implements SpeedController {
	private SpeedController ma;
	private SpeedController mb;
	private SpeedController mc;
	
	public MotorTriple(SpeedController a, SpeedController b, SpeedController c) {
		
	}
	@Override
	public void disable() {
		
	}

	@Override
	public double get() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(double arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(double arg0, byte arg1) {
		// TODO Auto-generated method stub

	}
	@Override
	public void pidWrite(double arg0) {

	}

	
}
