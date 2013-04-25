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

import edu.wpi.first.wpilibj.AnalogTrigger;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStationLCD;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SafePWM;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Knight extends IterativeRobot {
	private static final double JITTER_RANGE = 0.008;
	
	JStick xbox; // XBox controller
	JStick atk; // Logitech ATK3 controller

	private boolean usingCheesy;
	private DriverStationLCD lcd;
    
	private Compressor compressor;

	// Pair state "true" means high gear,
	// Pair state "false" means low gear
	private SolenoidPair driveGear;

	// used to remember the gear that is being used
	// while in and out of slow mode
	private boolean normalGear;

	private SolenoidPair hookClimb;
	
	//public final static PrefsHelper prefs = new PrefsHelper();
    public static PrefsHelper prefs;
	
	private Drive drive;
	private SpeedController shooter;
	private SpeedController actuator;
	private SpeedController kicker;

	private static final int SHOOTER_MODE_VOLTAGE = 0;
	private static final int SHOOTER_BANG_BANG = 1;
	private static final int SHOOTER_PID = 2;
	private int shooterMode;
	
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
		//actuator = new Pulse(prefs.getInt("actuator", 1),0.22,0.3);
		actuator = new Talon(1);
		kicker = new Talon(prefs.getInt("kicker",5));

		shooterMode = SHOOTER_MODE_VOLTAGE;

		xbox = new JStick(1);
		atk = new JStick(2);

		lcd = DriverStationLCD.getInstance();

		usingCheesy = false;
		integral_err = 0;
		prev_err = 0;

		// pressure sensor is  3
        compressor = new Compressor(5,1);
		driveGear = new SolenoidXORPair(1,2);
		normalGear = driveGear.get();
		hookClimb = new SolenoidXANDPair(3,4);
		
		shooterEnc = new Counter(1);
		//shooterEnc = new Counter(new AnalogTrigger(1));
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
		driveGear.set(true);

		drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
		drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft,true);

		shooterEnc.start();
    }
	//This function is called at the start of autonomous
	Timer timer;

	double autonStart;
	int frisbeesThrown;
	public void autonomousInit() {
		shooter.set(0.9);
		kicker.set(0.9);
		autonStart = Timer.getFPGATimestamp();
		frisbeesThrown = 0;
		driveGear.set(true);
	}
	/**
	 * This function is called periodically during autonomous
	 */
	double integral_err;
	double prev_err;
	double last_timer;
	boolean frisbeeDone;
	final double WAIT_AFTER_ACTUATOR = 5;
	final double DELAY_BETWEEN_FRISBEES = 2.25;
	final double FRISBEE_SHOOT_TIME = 0.25;

	final double DRIVE_FORWARD_TIME = 2;

	public void autonomousPeriodic() {
		/*
		//drive.tankDrive(0.4, 0.4);
		disp_batteryVoltage.setData(DriverStation.getInstance().getBatteryVoltage());
		disp_message.setData("autonomous");

		if (timer.get() > 1000) {
			shooter.set(-1);
		}
		if (timer.get() > 6000) {
			actuator.set(0.4);
		}
		lcd.println(DriverStationLCD.Line.kUser1, 1, "" + timer.get());
		lcd.updateLCD();
		*/
		
		/*
		double currentTime = Timer.getFPGATimestamp() - autonStart;
		double cycleTime = currentTime - WAIT_AFTER_ACTUATOR - (frisbeesThrown*DELAY_BETWEEN_FRISBEES);
		SmartDashboard.putNumber("current time", currentTime);
		SmartDashboard.putNumber("cycle time", cycleTime);
		if (cycleTime > 0) {
			if (cycleTime < FRISBEE_SHOOT_TIME) {
				frisbeeDone = false;
				actuator.set(1);
			} else {
				if (!frisbeeDone) {
					frisbeeDone = true;
					frisbeesThrown++;
					actuator.set(0);
				}
			}
		} else {
			actuator.set(0);
		}
		SmartDashboard.putBoolean("Frisbee done",frisbeeDone);
		SmartDashboard.putNumber("Frisbees thrown",frisbeesThrown);
		*/
		/*
		if (currentTime < DRIVE_FORWARD_TIME) {
			drive.tankDrive(0.4,0.4);
		} else {
			drive.tankDrive(0,0);
			actuator.set(1);
		}
		*/
		
		if (Timer.getFPGATimestamp() - autonStart > WAIT_AFTER_ACTUATOR) {
			actuator.set(1);
		}
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
			driveGear.toggle();
			normalGear = !normalGear;
		}

		// show the solenoids status
		lcd.println(DriverStationLCD.Line.kUser3,1,driveGear.get()?"High Gear":"Low Gear ");

		// show if the compressor is running
		if (compressor.getPressureSwitchValue()) {
			lcd.println(DriverStationLCD.Line.kUser6,1,"Compressor is off    ");
		} else {
			lcd.println(DriverStationLCD.Line.kUser6,1,"Compressor is running");
		}

		// joystick button 1 spins the actuator
		// joystick button 2 spins the shooter and kicker
		// joystick button 3 revereses the shooter and kicker
		// this control system does not use the optical encoders
		actuator.set(atk.isPressed(1) ? 0.5 : 0);

		double shooterOutput = 0;
		double desiredRPM = 3000;
		if (shooterMode == SHOOTER_MODE_VOLTAGE) {
			shooterOutput = atk.isPressed(2) ? (12.5*0.9) / DriverStation.getInstance().getBatteryVoltage() : 0;
		} else if (shooterMode == SHOOTER_BANG_BANG) {
			if (atk.isPressed(2)) {
				// bang bang?
				// TODO: put magic numbers somewhere else
				if (KnightUtils.periodToRPM(shooterEnc.getPeriod()) < 4000) {
					shooterOutput = 1;
				} else {
					shooterOutput = 0.5;
				}		
			} else if (atk.isPressed(4) || atk.isPressed(5)) {
				if (KnightUtils.periodToRPM(shooterEnc.getPeriod()) < 4500) {
					shooterOutput = 1;
				} else {
					shooterOutput = 0.5;
				}
			}
			//shooterOutput = atk.isPressed(2) ? Utils.getBangBang(desiredRPM, 0.3, shooterEnc) : 0;
		} else if (shooterMode == SHOOTER_PID) {
			// TO: shooter PID
		}

		shooter.set(shooterOutput);
		kicker.set(shooterOutput);

		// toggle the hook climb
		if (xbox.isReleased(JStick.XBOX_RB)) {
			hookClimb.toggle();
		}

		//double leftStickX = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSX), JITTER_RANGE);
		double leftStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_LSY), JITTER_RANGE);
		double rightStickX = JStick.removeJitter(xbox.getAxis(JStick.XBOX_RSX), JITTER_RANGE);
		double rightStickY = JStick.removeJitter(xbox.getAxis(JStick.XBOX_RSY), JITTER_RANGE);

		boolean slowMode = xbox.getAxis(JStick.XBOX_TRIG) < -0.5;
		if (slowMode) {
			driveGear.set(false);
		} else {
			driveGear.set(normalGear);
		}

		if (usingCheesy) {
			drive.cheesyDrive(leftStickY*(slowMode?0.6:1), rightStickX, xbox.isPressed(JStick.XBOX_LJ));
			lcd.println(DriverStationLCD.Line.kUser4,1,"cheesy drive");
		} else {
			if (!drive.straightDrive(xbox.getAxis(JStick.XBOX_TRIG))) {
				drive.tankDrive(leftStickY*(slowMode?0.6:1), rightStickY*(slowMode?0.6:1));
				lcd.println(DriverStationLCD.Line.kUser4,1,"tank drive   ");
			} else {
				lcd.println(DriverStationLCD.Line.kUser4,1,"straightDrive");
			}
		}
		
		// print encoder values to see if they're working
		lcd.println(DriverStationLCD.Line.kUser2,1,""+shooterEnc.getPeriod());
		lcd.println(DriverStationLCD.Line.kUser5, 1,""+lWheels.get()+" "+rWheels.get());
		
		SmartDashboard.putNumber("Shooter speed", shooterEnc.getPeriod());
		SmartDashboard.putNumber("Shooter RPM", 60/shooterEnc.getPeriod());
		SmartDashboard.putNumber("Shooter count", shooterEnc.get());
		
		lcd.updateLCD();

		// update the display
		disp_batteryVoltage.setData(DriverStation.getInstance().getBatteryVoltage());
		disp_message.setData("teleop");
    }

	public void disabledPeriodic() {
		disp_batteryVoltage.setData(DriverStation.getInstance().getBatteryVoltage());
		disp_message.setData("disabled");
	}

	private boolean shootTester;

	private boolean pwmtest;
	private SafePWM[] pwms;

	public void testInit() {
		timer.start();
		pwmtest = false;
		pwms = new SafePWM[10];
		for (int i = 0; i < 10; ++i) {
			pwms[i] = new SafePWM(i+1);
		}
	}	

	/**
	 * This function is called periodically during test mode
	 */
	public void testPeriodic() {
		xbox.update();
		atk.update();

		// toggle between PWM test and austin's thing
		if (xbox.isPressed(JStick.XBOX_LB)) {
			pwmtest = false;
		}
		if (xbox.isPressed(JStick.XBOX_RB)) {
			pwmtest = true;
		}

		if (pwmtest) {
			for (int i = 0; i < 10; ++i) {
				if (atk.isPressed(i+1)) {
					pwms[i].setRaw(143);
				}
			}
		} else {
			if (xbox.isReleased(JStick.XBOX_A)) {
					shootTester = !shootTester;
					lcd.println(DriverStationLCD.Line.kUser1, 1, "Shooter Tester     ");
				} else {
					lcd.println(DriverStationLCD.Line.kUser1, 1, "Normal Tester      ");
			}

			//Only spins shooter	
			shooter.set((atk.isPressed(7)) ? -1 : 0);
			//Only spins the kicker
			kicker.set((atk.isPressed(6)) ? 1 : 0);
			//Slow start for shooting 1
			if(shootTester && atk.isPressed(1)) {
				if(timer.get() > 2) {
					shooter.set(1);
					lcd.println(DriverStationLCD.Line.kUser2, 1, "Shooter: On    ");
				}
				if(timer.get() > 4) {
					kicker.set(1);	
					lcd.println(DriverStationLCD.Line.kUser3, 1, "Kicker: On     ");
				}
				if(timer.get() > 7) {
					//actuator.set(1);	
					lcd.println(DriverStationLCD.Line.kUser4, 1, "CAM: On        ");
				} else {
					timer.reset();
					shooter.set(0);
					kicker.set(0);
					actuator.set(0);
					lcd.println(DriverStationLCD.Line.kUser2, 1, "Shooter: Off   ");
					lcd.println(DriverStationLCD.Line.kUser3, 1, "Kicker: Off    ");
					lcd.println(DriverStationLCD.Line.kUser4, 1, "CAM: Off       ");
				}
			}
			lcd.println(DriverStationLCD.Line.kUser1, 1, "" + timer.get());
			lcd.updateLCD();
		}
	}
}
