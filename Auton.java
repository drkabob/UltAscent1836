/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.milkenknights;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;

/**
 *
 * @author Nat
 * 
 * This class is a base for autonomous functions.
 * It extends the Command class so that we can implement it
 * with SmartDashboard.
 */
public class Auton extends Command {
    
    // Variable declarations
    private Knight knight;
    private double autonStart;
    private int frisbeesThrown;
    private int autonType;
    private double toShoot;
    private boolean finished = false;
    
    // Constants
    private final double WAIT_AFTER_ACTUATOR = 1;
    private final double WAIT_AFTER_SHOOTING = WAIT_AFTER_ACTUATOR + 3.5;
    private final double DRIVE_DISTANCE = 102;
    private final double SLOWDOWN_TIME = 0.25;
    private final double AUTON_DONE = 200;
    
    // Pseudo-enumerator for Auton options...
    public static int AUTON_CENTER = 0;
    public static int AUTON_SIDE = 1;
    
    public Auton(Knight knight, int autonType) {
        super();
        this.knight = knight;
        this.autonType = autonType;
        
        if (autonType == AUTON_CENTER) {
            toShoot = Knight.SHOOTER_RPM_HIGH;
        } else if (autonType == AUTON_SIDE) {
            toShoot = Knight.SHOOTER_RPM_LOW;
        }
    }

    protected void initialize() {
        
        // Initialize variables.
	autonStart = Timer.getFPGATimestamp();
	frisbeesThrown = 0;
        
        // Set drive gear to low
	knight.driveGear.set(false);
		
	// reset encoders
	knight.rightEnc.reset();
	knight.leftEnc.reset();
    }

    protected void execute() {
        double currentTime = Timer.getFPGATimestamp() - autonStart;
        if (currentTime > WAIT_AFTER_SHOOTING) {
            knight.bangBangShooter(false, 0);

            knight.defaultActuator(false);

            double left = 0;
            double right = 0;
            boolean leftDone = false;

            if (Math.abs(knight.leftEnc.getDistance()) < DRIVE_DISTANCE
                    || Math.abs(knight.rightEnc.getDistance()) < DRIVE_DISTANCE) {
                left = 1;
                right = 1;
            } else {
                if (knight.finishedMovingForward == -1) {
                    knight.finishedMovingForward = currentTime;
                }

                if (currentTime - knight.finishedMovingForward < SLOWDOWN_TIME) {
                    knight.driveGear.set(false);
                    left = -0.5;
                    right = -0.5;
                } else {
                    knight.driveGear.set(true);
                }
            }

            knight.drive.tankDrive(left, right);
        } else if (currentTime > WAIT_AFTER_ACTUATOR) {
            knight.defaultActuator(true);           
            knight.bangBangShooter(true, toShoot);
        } else if (currentTime > AUTON_DONE) {
            finished = true;
        } else {
            knight.bangBangShooter(true, toShoot);
        }
    }

    protected boolean isFinished() {
        return finished;
        
    }

    protected void end() {
        
    }

    protected void interrupted() {
        
    }
    
}
