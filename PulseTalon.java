package com.milkenknights;

import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;

// this implementation is BAD!
// TODO: use threads
public class PulseTalon {
	private Talon t;
	private double pulseTime, delay;
	private double lastShootTime;
	private boolean isShooting;

	public PulseTalon(int channel, double pulseTime, double delay) {
		t = new Talon(channel);
		this.pulseTime = pulseTime;
		this.delay = delay;
	}

	public void set(double speed) {
		if (speed == 0) {
			isShooting = false;
			t.set(0);
		} else {
			if (isShooting) {
				if (Timer.getFPGATimestamp() - lastShootTime >= pulseTime + delay) {
					lastShootTime = Timer.getFPGATimestamp();
				} else if (Timer.getFPGATimestamp() - lastShootTime > pulseTime) {
					t.set(0);
				} else {
					t.set(speed);
				}
			} else {
				isShooting = true;
				lastShootTime = Timer.getFPGATimestamp();
			}
		}
	}
}
