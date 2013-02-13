/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.milkenknights;


import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Talon;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Knight extends IterativeRobot {
	private static final int leftMotor = 8;
	private static final int rightMotor = 5;
	
	private static final double jitterRange = 0.008;
	
	JStick xbox;
	private boolean usingCheesy;
	private DriverStationLCD lcd;
	
	private Drive drive;
	public Knight() {
		drive = new Drive(new Talon(leftMotor),new Talon(rightMotor));
		xbox = new JStick(1);
		lcd = DriverStationLCD.getInstance();

		usingCheesy = false;
		integral_err = 0;
		prev_err = 0;
	}
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    }

    /**
     * This function is called periodically during autonomous
     */
    double integral_err;
    double prev_err;
    public void autonomousPeriodic() {
    	//drive.tankDrive(0.4, 0.4);
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
		xbox.update();
		
		// Press A to toggle cheesy drive
		if (xbox.isReleased(JStick.XBOX_A)) {
			usingCheesy = !usingCheesy;
		}
		
		double leftStickX = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSX), jitterRange);
		double leftStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSY), jitterRange);
		double rightStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_RSY), jitterRange);

		if (usingCheesy) {
			drive.cheesyDrive(rightStickY, leftStickX, xbox.isPressed(JStick.XBOX_LJ));
			lcd.println(DriverStationLCD.Line.kUser4,1,"cheesy");
		} else {
			if (!drive.straightDrive(xbox.getAxis(JStick.XBOX_TRIG))) {
				drive.tankDrive(leftStickY, rightStickY);
			} else {
				lcd.println(DriverStationLCD.Line.kUser4,1,"straightDrive");
			}
		}
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
