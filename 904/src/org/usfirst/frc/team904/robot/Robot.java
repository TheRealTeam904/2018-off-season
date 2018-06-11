/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team904.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;

 /**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 */
public class Robot extends IterativeRobot {
	private static final String kDefaultAuto = "Default";
	private static final String kBaselineAuto = "Baseline";
	private static final String kCenterAuto = "CenterAuto";
	private static final String kLeftSideSwitch = "LeftSideSwitch";
	private static final String kLeftSideScale = "LeftSideScale";
	private static final String kRightSideSwitch = "RightSideSwitch";
	private static final String kRightSideScale = "RightSideScale";
	private static final String kSwitchSide = "SwitchSide";
	private String m_autoSelected;
	private SendableChooser<String> m_chooser = new SendableChooser<>();
	
	public static WPI_TalonSRX[] rightMotors = {new WPI_TalonSRX(2), new WPI_TalonSRX(3), new WPI_TalonSRX(4)};
	public static WPI_TalonSRX[] leftMotors = {new WPI_TalonSRX(5), new WPI_TalonSRX(6), new WPI_TalonSRX(7)};
	
	public static WPI_TalonSRX IntakeLeftMotor = new WPI_TalonSRX(10);
	public static WPI_TalonSRX IntakeRightMotor = new WPI_TalonSRX(11);

	public static WPI_TalonSRX arms = new WPI_TalonSRX(9);
	public static WPI_TalonSRX climber = new WPI_TalonSRX(8);

	public static DoubleSolenoid shift = new DoubleSolenoid(0, 1);
	public static DoubleSolenoid.Value shiftLow = DoubleSolenoid.Value.kReverse;
	public static DoubleSolenoid.Value shiftHigh = DoubleSolenoid.Value.kForward;
	
	public static DoubleSolenoid grabber = new DoubleSolenoid(2, 3);
	public static DoubleSolenoid.Value grabberClose = DoubleSolenoid.Value.kReverse;
	public static DoubleSolenoid.Value grabberOpen = DoubleSolenoid.Value.kForward;
	
	public static boolean highGear;
	

	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		
		//Auton Choices that appear on the SmartDashboard
		m_chooser.addDefault("Do Nothing", kDefaultAuto);
		m_chooser.addObject("Baseline", kBaselineAuto);
		m_chooser.addObject("Left", "L");
		m_chooser.addObject("Right", "R");
		m_chooser.addObject("CenterAuto", kCenterAuto);
		m_chooser.addObject("LeftSideSwitch", kLeftSideSwitch);
		m_chooser.addObject("LeftSideScale", kLeftSideScale);
		m_chooser.addObject("RightSideSwitch", kRightSideSwitch);
		m_chooser.addObject("RightSideScale", kRightSideScale);
		m_chooser.addObject("SwitchSide", kSwitchSide);
		SmartDashboard.putData("Auto choices", m_chooser);
		
		//Sets drive motors to brake mode, inverts left side so green 
		//means forward for both sides
		for(WPI_TalonSRX motor : rightMotors)
		{
			motor.setNeutralMode(NeutralMode.Brake);
			motor.setInverted(false);
			motor.set(0);
			//motor.configClosedloopRamp(2, 0);
			//motor.configOpenloopRamp(2, 0);
			
		}
		for(WPI_TalonSRX motor : leftMotors)
		{
			motor.setNeutralMode(NeutralMode.Brake);
			motor.setInverted(true);
			motor.set(0);
			//motor.configClosedloopRamp(2, 0);
			//motor.configOpenloopRamp(2, 0);
		}
		
		//Sets Chain climber and four-bar link to brake mode
		climber.setNeutralMode(NeutralMode.Brake);
		climber.set(0);
		arms.setNeutralMode(NeutralMode.Brake);
		arms.set(0);
		
		//Intake Motors, set to coast mode and sucking blocks in
		//is green for both of the Talons
		IntakeLeftMotor.setNeutralMode(NeutralMode.Coast);
		IntakeRightMotor.setNeutralMode(NeutralMode.Coast);
		IntakeLeftMotor.setInverted(false);
		IntakeRightMotor.setInverted(true);
		IntakeLeftMotor.set(0);
		IntakeRightMotor.set(0);
		
		//Sets us in low gear and closes the block clamp
		//at the start of initialization
		shift.set(shiftLow);
		highGear = false;
		grabber.set(grabberClose);
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * <p>You can add additional auto modes by adding additional comparisons to
	 * the switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		m_autoSelected = m_chooser.getSelected();
		
		leftMotors[0].configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		leftMotors[0].setSelectedSensorPosition(0, 0, 100);
		rightMotors[0].configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 10);
		rightMotors[0].setSelectedSensorPosition(0, 0, 100);
	
		String gameData;
		switch (m_autoSelected) {
		case kBaselineAuto:
			baseline();
			break;
			
		case kCenterAuto:
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			if (gameData.length() > 0)
				if (gameData.charAt(0) == 'R')
			CenterAutonRight();
				else if (gameData.charAt(0) == 'L') 
					CenterAutonLeft();
			break;
			
		case kLeftSideSwitch:
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			if (gameData.length() > 0)
				if (gameData.charAt(0) == 'L')
					LeftSideSwitch();
				else if (gameData.charAt(1) == 'L')
					LeftSideScale();
				else 
					SwitchSide();
			break;
			
		case kLeftSideScale:
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			if (gameData.length() > 0)
				if(gameData.charAt(1) == 'L')
					LeftSideScale();
				else if (gameData.charAt(0) == 'L')
					LeftSideSwitch();
				else
					SwitchSide();
			break;
			
		case kRightSideSwitch:
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			if (gameData.length() > 0)
				if (gameData.charAt(0) == 'R')
					RightSideSwitch();
				else if (gameData.charAt(1) == 'R')
					RightSideScale();
				else SwitchSide();
			break;
		case kRightSideScale:
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			if (gameData.length() > 0)
				if(gameData.charAt(1) == 'R')
					RightSideScale();
				else if (gameData.charAt(0) == 'R')
					RightSideSwitch();
				else
					SwitchSide();
			break;
			
		case kSwitchSide:
			gameData = DriverStation.getInstance().getGameSpecificMessage();
			SwitchSide();
			break;
			
		case kDefaultAuto:
			default:
				break;
		}
	}

	/**
	 * This function is called periodically during autonomous.
	 */
	@Override
	public void autonomousPeriodic() {
		
	}

	public void baseline() {
		SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
		SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		while ((rightMotors[0].getSelectedSensorPosition(0) < 72427) && (leftMotors[0].getSelectedSensorPosition(0) < 72437)){
			drive(0, -0.25);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
			}
		drive(0, 0);
		}
	 
	
	public void CenterAutonRight() {
		SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
		SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));	
		while ((rightMotors[0].getSelectedSensorPosition(0) < 13072) && (leftMotors[0].getSelectedSensorPosition(0) <13072)) {
			drive(0,-.3);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((leftMotors[0].getSelectedSensorPosition(0) < 28322) && (rightMotors[0].getSelectedSensorPosition(0) < 35254)) {
			drive(0.3,0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		} 
		while ((rightMotors[0].getSelectedSensorPosition(0) < 65000) && (leftMotors[0].getSelectedSensorPosition(0) < 105668)) {
			drive(0,-0.3);
			arms.set(-.4);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while (83500 >= rightMotors[0].getSelectedSensorPosition(0)) {
			drive(-0.3,0);
			arms.set(0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while((rightMotors[0].getSelectedSensorPosition(0) < 83600) && (leftMotors[0].getSelectedSensorPosition(0) < 83600)) {
			drive(0,-0.3);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		drive (0, 0);
		arms.set(0);
		grabber.set(DoubleSolenoid.Value.kForward);
			
	}
	
	public void CenterAutonLeft() {
		SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
		SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));	
		while ((rightMotors[0].getSelectedSensorPosition(0) < 13072) && (leftMotors[0].getSelectedSensorPosition(0) <13072)) {
			drive(0,-.3);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((rightMotors[0].getSelectedSensorPosition(0) < 28322) && (leftMotors[0].getSelectedSensorPosition(0) < 35254)) {
			drive(-0.3,0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		} 
		while (( leftMotors[0].getSelectedSensorPosition(0) < 65000) && (rightMotors[0].getSelectedSensorPosition(0) < 105668)) {
			drive(0,-0.3);
			arms.set(-.4);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while (83500 >= leftMotors[0].getSelectedSensorPosition(0)) {
			drive(0.3,0);
			arms.set(0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while((leftMotors[0].getSelectedSensorPosition(0) < 83600) && (rightMotors[0].getSelectedSensorPosition(0) < 83600)) {
			drive(0,-0.3);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		drive (0, 0);
		arms.set(0);
		grabber.set(DoubleSolenoid.Value.kForward);
	}
	
	public void LeftSideSwitch() {
		SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
		SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		while ((leftMotors[0].getSelectedSensorPosition(0) < 89118) && (rightMotors[0].getSelectedSensorPosition(0) < 89118)) {
			drive(0,-0.25);
			arms.set(-0.3);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((leftMotors[0].getSelectedSensorPosition(0) < 109368) && (rightMotors[0].getSelectedSensorPosition(0) > 68868)) {
			drive(0.25,0);
			arms.set(0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		grabber.set(DoubleSolenoid.Value.kForward);
			drive(0, 0);
	}
	
	public void RightSideSwitch() {
		while ((leftMotors[0].getSelectedSensorPosition(0) < 89118) && (rightMotors[0].getSelectedSensorPosition(0) < 89118)) {
			drive(0,-0.25);
			arms.set(-0.3);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((rightMotors[0].getSelectedSensorPosition(0) < 104368) && (leftMotors[0].getSelectedSensorPosition(0) > 73868)) {
			drive(-0.25,0);
			arms.set(0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((rightMotors[0].getSelectedSensorPosition(0) < 116068) && (leftMotors[0].getSelectedSensorPosition(0) < 85568)) {
			drive(0,-0.25);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		grabber.set(DoubleSolenoid.Value.kForward);
			drive(0, 0);
	}
	
	public void RightSideScale() {
		while ((leftMotors[0].getSelectedSensorPosition(0) < 213848) && (rightMotors[0].getSelectedSensorPosition(0) < 213848)) {
			drive(0,-0.4);
			arms.set(-0.35);
			climber.set(-1);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((rightMotors[0].getSelectedSensorPosition(0) >= 198597) && (leftMotors[0].getSelectedSensorPosition(0) < 229097)) {
			drive(0.25,0);
			arms.set(0);
			climber.set(0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((rightMotors[0].getSelectedSensorPosition(0) < 204835) && (leftMotors[0].getSelectedSensorPosition(0) < 235335)) {
			drive(0,-0.25);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		grabber.set(DoubleSolenoid.Value.kForward);
		drive (0, 0);
	}
	
	public void LeftSideScale() {
		while ((rightMotors[0].getSelectedSensorPosition(0) < 213848) && (leftMotors[0].getSelectedSensorPosition(0) < 213848)) {
			drive(0,-0.4);
			arms.set(-0.35);
			climber.set(-1);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((leftMotors[0].getSelectedSensorPosition(0) >= 198597) && (rightMotors[0].getSelectedSensorPosition(0) < 229097)) {
			drive(-0.25,0);
			arms.set(0);
			climber.set(0);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		while ((leftMotors[0].getSelectedSensorPosition(0) < 204835) && (rightMotors[0].getSelectedSensorPosition(0) < 235335)) {
			drive(0,-0.25);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		grabber.set(DoubleSolenoid.Value.kForward);
			drive(0,0);
	}
	
	public void SwitchSide() {
		while ((rightMotors[0].getSelectedSensorPosition(0) < 89118) && (leftMotors[0].getSelectedSensorPosition(0) < 89118)) {
			drive(0,-0.25);
			SmartDashboard.putNumber("encoderLeft", leftMotors[0].getSelectedSensorPosition(0));
			SmartDashboard.putNumber("encoderRight", rightMotors[0].getSelectedSensorPosition(0));
		}
		drive(0,0);
	}
	
	
	// Driver xBox Controller input, axes for driving
	public static Joystick stick = new Joystick(0);
	public static double driveStickForwardAxis = stick.getRawAxis(1);
	public static double driveStickTurnAxis = stick.getRawAxis(4);
			
	
	// Operator xBox Controller functions
	public static XboxController controller = new XboxController(1);
	public static int accessoryStickArmsAxis = 5;
	public static int accessoryStickClimbAxis = 1;
	public static int accessoryStickGrabberGrabTrigger = 3;
	public static int accessoryStickGrabberReleaseTrigger = 2;
	public static int accessoryStickCubeIntakeButton = 6;
	public static int accessoryStickCubeOutputButton = 5;
	
	public void drive(double turn, double forward) {
		double motorLeft = (forward - turn);
		double motorRight = (forward + turn);
		double scaleFactor;
		if ((Math.max(Math.abs(motorLeft), Math.abs(motorRight)) > 1)) {
			scaleFactor = Math.max(Math.abs(motorLeft), Math.abs(motorRight));
		} 
		else {
			scaleFactor = 1;
		}
		motorLeft = motorLeft / scaleFactor;
		motorRight = motorRight / scaleFactor;
		for(WPI_TalonSRX motor : leftMotors){
			motor.set(motorLeft);
		}
		for(WPI_TalonSRX motor : rightMotors){
			motor.set(motorRight);
		}
	}
	
	//sets a dead zone of .2
	public double deadzone(double x) {
		if(x > 0.20)
			x = (x - 0.20) * 1.25;
		else if(x < -0.20)
			x = (x + 0.20) * 1.25;
		else
			x = 0;
		return x;
	}
	
	public double[] deadzone(double x, double y) {	
		return new double[] {(deadzone(x) * 0.75), (deadzone(y) * 0.75)};
	}
	
	
	/**
	 * This function is called periodically during operator control.
	 */
	@Override
	public void teleopPeriodic() {
		//Drive train dead zone
		double[] xy = deadzone(stick.getRawAxis(4),stick.getRawAxis(1));
		drive(xy[0], xy[1]);
		
		//Full Speed Climber
		if (deadzone(controller.getRawAxis(accessoryStickClimbAxis))>.3)
			climber.set(1);
		else if (deadzone(controller.getRawAxis(accessoryStickClimbAxis))<-.3)
			climber.set(-1);
		else climber.set(0);
		
		arms.set(deadzone(controller.getRawAxis(accessoryStickArmsAxis)));
		
		//Grabber Arm Clamp
		if (controller.getRawAxis(accessoryStickGrabberGrabTrigger) > .5) {
			grabber.set(DoubleSolenoid.Value.kReverse);
		}
		else if (controller.getRawAxis(accessoryStickGrabberReleaseTrigger) > .5) {
			grabber.set(DoubleSolenoid.Value.kForward);
		}
		else grabber.set(DoubleSolenoid.Value.kOff);
		
		//Gear Shifting
		boolean triggerLowGear = stick.getRawButton(5) ; 
		boolean triggerHighGear = stick.getRawButton(6) ; {
				
			if (triggerLowGear) {
				shift.set(shiftLow);
				highGear = false;
				SmartDashboard.putBoolean("High Gear:", highGear);
			} else if (triggerHighGear) {
				shift.set(shiftHigh);
				highGear = true;
				SmartDashboard.putBoolean("High Gear:", highGear);
			} else {
				shift.set(DoubleSolenoid.Value.kOff);
			}
		}
		
		//Intake Wheels
		if (controller.getRawButton(accessoryStickCubeIntakeButton)) {
			IntakeLeftMotor.set(-.8);
			IntakeRightMotor.set(-.8);
		}
		else if (controller.getRawButton(accessoryStickCubeOutputButton)) {
			IntakeLeftMotor.set(.8);
			IntakeRightMotor.set(.8);
		}
		else {
			IntakeLeftMotor.set(0);
			IntakeRightMotor.set(0);
		}
	}

	/**
	 * This function is called periodically during test mode.
	 */
	@Override
	public void testPeriodic() {
	}
}
