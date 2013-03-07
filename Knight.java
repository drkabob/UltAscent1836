/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package com.milkenknights;


import com.milkenknights.InsightLT.DecimalData;
import com.milkenknights.InsightLT.InsightLT;
import com.milkenknights.InsightLT.StringData;

import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Knight extends IterativeRobot {	
	private static final double jitterRange = 0.008;
	
	JStick xbox; // XBox controller
	JStick atk; // Logitech ATK3 controller

	private boolean usingCheesy;
	private DriverStationLCD lcd;
    
	private Compressor compressor;
	// solenoids in "reverse" means high gear,
	// solenoids in "forward" means low gear
	private DoubleSolenoid solenoids;
	
	//public final static PrefsHelper prefs = new PrefsHelper();
    public static PrefsHelper prefs;
	
	private Drive drive;
	private SpeedController shooter;
	private SpeedController intake;
	private SpeedController actuator;
	private SpeedController elevator;
	private SpeedController kicker;

	private Counter shooterEnc;
	private Encoder lWheels;
	private Encoder rWheels;

	// stuff for the InsightLT display
	private InsightLT display;
	private DecimalData disp_batteryVoltage;
	private StringData disp_message;
	
	public Knight() {
        prefs = new PrefsHelper();
        
		// get robot preferences, stored on the cRIO
		drive = new Drive(new Talon(prefs.getInt("leftmotor",4)),
				new Talon(prefs.getInt("rightmotor",9)));

		shooter = new Talon(prefs.getInt("shooter", 6));
		intake = new Talon(prefs.getInt("intake", 2));
		actuator = new Talon(prefs.getInt("actuator", 1));
		elevator = new Talon(prefs.getInt("elevator", 3));
		kicker = new Talon(prefs.getInt("kicker",5));

		xbox = new JStick(1);
		atk = new JStick(2);

		lcd = DriverStationLCD.getInstance();

		usingCheesy = false;
		integral_err = 0;
		prev_err = 0;

		// pressure sensor is  3
        compressor = new Compressor(5,1);
		solenoids = new DoubleSolenoid(1,2);
		
		shooterEnc = new Counter(1);
		lWheels = new Encoder(3,4);
		rWheels = new Encoder(6,7);

		// configure the display to have two lines of text
		display = new InsightLT(InsightLT.TWO_ONE_LINE_ZONES);
		display.startDisplay();

		// add battery display
		disp_batteryVoltage = new DecimalData("Bat:");
		display.registerData(disp_batteryVoltage,1);

		// this shows what mode the robot is in
		// i.e. teleop, autonomous, disabled
		disp_message = new StringData();
		display.registerData(disp_message,2);
	}
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
		compressor.start();
		solenoids.set(DoubleSolenoid.Value.kReverse);
		drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
		drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft,true);

		shooterEnc.start();
    }

    /**
     * This function is called periodically during autonomous
     */
    double integral_err;
    double prev_err;
    public void autonomousPeriodic() {
                actuator.set(1);
		//drive.tankDrive(0.4, 0.4);
		disp_batteryVoltage.setData(DriverStation.getInstance().getBatteryVoltage());
		disp_message.setData("autonomous");
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
		xbox.update();
		atk.update();
		
		// Press A to toggle cheesy drive
		if (xbox.isReleased(JStick.XBOX_A)) {
			usingCheesy = !usingCheesy;
		}

		// use LB to toggle high and low gear
		if (xbox.isReleased(JStick.XBOX_LB)) {
			if(solenoids.get().value == DoubleSolenoid.Value.kForward_val) {
				solenoids.set(DoubleSolenoid.Value.kReverse);
			} else {
				solenoids.set(DoubleSolenoid.Value.kForward);
			}
		}

		// show the solenoids status
		switch (solenoids.get().value) {
		case DoubleSolenoid.Value.kForward_val:
			lcd.println(DriverStationLCD.Line.kUser3,1,"Low Gear ");
			break;
			
		case DoubleSolenoid.Value.kReverse_val:
			lcd.println(DriverStationLCD.Line.kUser3,1,"High Gear");
			break;
		}

		// show if the compressor is running
		if (compressor.getPressureSwitchValue()) {
			lcd.println(DriverStationLCD.Line.kUser6,1,"Compressor is off    ");
		} else {
			lcd.println(DriverStationLCD.Line.kUser6,1,"Compressor is running");
		}

		// joystick button 2 controls intake and elevator
		if (atk.isPressed(2)) {
			intake.set(1);
			elevator.set(1);
		} else {
			intake.set(0);
			elevator.set(0);
		}

		// joystick button 3 should spin the shooter
                if(atk.isPressed(3)) {
                    shooter.set(-1);
                    kicker.set(-1);
                } else {
                    shooter.set(0);
                    kicker.set(0);
                }

		// joystick trigger should spin actuator,
		// but only if the shooter is moving
		actuator.set((atk.isPressed(3) && atk.isPressed(1)) ? 1 : 0);
		
		double leftStickX = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSX), jitterRange);
		double leftStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSY), jitterRange);
		double rightStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_RSY), jitterRange);

		if (usingCheesy) {
			drive.cheesyDrive(rightStickY, leftStickX, xbox.isPressed(JStick.XBOX_LJ));
			lcd.println(DriverStationLCD.Line.kUser4,1,"cheesy drive");
		} else {
			if (!drive.straightDrive(xbox.getAxis(JStick.XBOX_TRIG))) {
				drive.tankDrive(leftStickY, rightStickY);
				lcd.println(DriverStationLCD.Line.kUser4,1,"tank drive   ");
			} else {
				lcd.println(DriverStationLCD.Line.kUser4,1,"straightDrive");
			}
		}
		
		// print encoder values to see if they're working
		lcd.println(DriverStationLCD.Line.kUser2,1,""+shooterEnc.get());
		lcd.println(DriverStationLCD.Line.kUser5, 1,""+lWheels.get()+" "+rWheels.get());
		
		lcd.updateLCD();

		// update the display
		disp_batteryVoltage.setData(DriverStation.getInstance().getBatteryVoltage());
		disp_message.setData("teleop");
    }

	public void disabledPeriodic() {
		disp_batteryVoltage.setData(DriverStation.getInstance().getBatteryVoltage());
		disp_message.setData("disabled");
	}
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
}
