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
	private static final int LEFT_MOTOR = 4;
	private static final int RIGHT_MOTOR = 9;
	private static final int SHOOTER_TALON = 6;
	private static final int ACTUATOR_TALON = 1;
	private static final int KICKER_TALON = 5;
	private static final int COMPRESSOR_PRESSURE_SWITCH = 5;
	private static final int COMPRESSOR_RELAY_CHANNEL = 1;
	private static final int DRIVE_SOLENOID_A = 1;
	private static final int DRIVE_SOLENOID_B = 2;
	private static final int HOOK_SOLENOID_A = 3;
	private static final int HOOK_SOLENOID_B = 4;
	private static final int SHOOTER_ENC = 1;
	
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
	private static final int SHOOTER_COMBINED = 3;
	private int shooterMode;
	
	private Counter shooterEnc;
	private Encoder lWheels;
	private Encoder rWheels;

	// stuff for the InsightLT display
	private InsightLT display;
	private DecimalData disp_batteryVoltage;
	private StringData disp_message;
	
	private void defaultVoltageShooter(boolean on) {
		voltageShooter(on, 0.65);
	}
	
	private void voltageShooter(boolean on, double frac) {
		double output = on ? Utils.voltageSpeed(frac) : 0;
		shooter.set(output);
		kicker.set(output);
	}

	private void bangBangShooter(boolean on, double targetRPM) {
		double shooterOutput;
		if (on) {
			shooterOutput = Utils.getBangBang(targetRPM, 0.5, shooterEnc);
		} else {
			shooterOutput = 0;
		}
		shooter.set(shooterOutput);
		kicker.set(shooterOutput);
	}
	
	private void combinedShooter(boolean on) {
		if (on) {
			// shooter gets bang bang at 3500
			// kicker gets 80% voltage
			shooter.set(Utils.getBangBang(3500,0.5,shooterEnc));
			kicker.set(Utils.voltageSpeed(0.8));
		} else {
			shooter.set(0);
			kicker.set(0);
		}
	}

	private void shooterOff() {
		shooter.set(0);
		kicker.set(0);
	}
	
	private void defaultActuator(boolean on) {
		actuator.set(on ? 0.5 : 0);
	}

	public Knight() {
        prefs = new PrefsHelper();
        
		// get robot preferences, stored on the cRIO
		drive = new Drive(LEFT_MOTOR, RIGHT_MOTOR);

		shooter = new Talon(SHOOTER_TALON);
		actuator = new Talon(ACTUATOR_TALON);
		kicker = new Talon(KICKER_TALON);

		//shooterMode = SHOOTER_MODE_VOLTAGE;
		shooterMode = SHOOTER_BANG_BANG;

		xbox = new JStick(1);
		atk = new JStick(2);

		lcd = DriverStationLCD.getInstance();

		usingCheesy = false;
		integral_err = 0;
		prev_err = 0;

		// pressure sensor is  3
        compressor = new Compressor(COMPRESSOR_PRESSURE_SWITCH,COMPRESSOR_RELAY_CHANNEL);
		driveGear = new SolenoidXORPair(DRIVE_SOLENOID_A,DRIVE_SOLENOID_B);
		normalGear = driveGear.get();
		hookClimb = new SolenoidXANDPair(HOOK_SOLENOID_A,HOOK_SOLENOID_B);
		
		shooterEnc = new Counter(SHOOTER_ENC);

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
		//shooter.set(0.9);
		//kicker.set(0.9);
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
		
		//voltageShooter(true,0.6);
		bangBangShooter(true,3800);
		if (Timer.getFPGATimestamp() - autonStart > WAIT_AFTER_ACTUATOR) {
			defaultActuator(true);
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
		defaultActuator(atk.isPressed(1));
		
		// change shooter modes
		if (atk.isPressed(11)) {
			shooterMode = SHOOTER_MODE_VOLTAGE;
		} else if (atk.isPressed(10)) {
			shooterMode = SHOOTER_BANG_BANG;
		} else if (atk.isPressed(9)) {
			shooterMode = SHOOTER_COMBINED;
		}
		
		if (shooterMode == SHOOTER_MODE_VOLTAGE) {
			if (atk.isPressed(2)) {
				defaultVoltageShooter(true);
			} else if (atk.isPressed(4) || atk.isPressed(5)) {
				voltageShooter(true, 0.6);
			} else {
				shooterOff();
			}
			//defaultVoltageShooter(atk.isPressed(2));
		} else if (shooterMode == SHOOTER_BANG_BANG) {
			if (atk.isPressed(2)) {
				bangBangShooter(true, 3800);
			} else if (atk.isPressed(4) || atk.isPressed(5)) {
				bangBangShooter(true, 3500);
			} else {
				shooterOff();
			}
		} else if (shooterMode == SHOOTER_PID) {
			// TO: shooter PID
		} else if (shooterMode == SHOOTER_COMBINED) {
			combinedShooter(atk.isPressed(2));
		} else {
			shooterOff();
		}

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
		
		if (shooterMode == SHOOTER_MODE_VOLTAGE) {
			lcd.println(DriverStationLCD.Line.kUser1,1,"Shooter mode: voltage ");
		} else if (shooterMode == SHOOTER_BANG_BANG) {
			lcd.println(DriverStationLCD.Line.kUser1,1,"Shooter mode: bangbang");
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
