// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  VictorSP motor0 = new VictorSP(0);
  VictorSP motor1 = new VictorSP(1);
  VictorSP motor2 = new VictorSP(2);
  VictorSP motor3 = new VictorSP(3);
  TalonSRX shooter = new TalonSRX(3);
  TalonSRX geararm = new TalonSRX(2);
  // Spark gearwheels; // pneumatics?

  Servo shooterStopper = new Servo(5);
  Timer timer = new Timer();

  Joystick JS = new Joystick(0);

  final double DEADBAND = 0.1; // accounts for joystick drift
  final double TWIST_DEADBAND = 0.35; // the joystick has a lot of twist drift

  final double STOPPER_OPEN_POS = 1.0; // servo opens, allowing fuel to travel to shooter
  final double STOPPER_CLOSED_POS = 0.5; // servo closes, keeping fuel behind the shooter
  final double SHOOTER_DEMO_SPEED = 0.5414; // shoots like 3ft high
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {

    motor0.setInverted(false);
    motor1.setInverted(false);
    motor2.setInverted(true);
    motor3.setInverted(true);

    // double shooterSpeed = JS.getRawAxis(3); // slider at the bottom (black flip switch)
    double throttle = -JS.getRawAxis(1); // y axis/forward
    double twist = JS.getRawAxis(2); // rotation, if you twist the joystick
    double strafe = JS.getRawAxis(0); // x axis/side to side

    boolean traction = JS.getRawButton(1);
		boolean raise = JS.getRawButton(7);
		boolean lower = JS.getRawButton(8);
		// boolean intake = JS.getRawButton(11);
		// boolean outtake = JS.getRawButton(12);
		boolean shoot = JS.getRawButton(3);

    if (Math.abs(throttle) < DEADBAND) {
      throttle = 0;
    }
    if (Math.abs(twist) < TWIST_DEADBAND) {
      twist = 0;
    }
    if (Math.abs(strafe) < DEADBAND) {
      strafe = 0;
    }
		
    // motor0.set(twist + throttle - strafe);
    // motor1.set(twist + throttle + strafe);
    // motor2.set(-twist + throttle + strafe);
    // motor3.set(-twist + throttle - strafe);
    motor0.set(twist - throttle);
    motor1.set(twist - throttle);
    motor2.set(-twist - throttle);
    motor3.set(-twist - throttle);

    // restarts timer when you start pressing the shoot button
    if (JS.getRawButtonPressed(3)) {
      timer.restart();
    }

    boolean useConstantDemoSpeed = true; // make false to vary shooter speed based on the joystick slider
    if (shoot) {
      double shooterSpeed;
      if (useConstantDemoSpeed) {        
        shooterSpeed = SHOOTER_DEMO_SPEED; // shoots like 3 feet high
      } else {
        // this will change the shooter speed based on the slider at the bottom (black flip switch)
        // (the slider value goes from -1 to 1, so 0.5x + 0.5 makes it go from 0 to 1 instead)
        shooterSpeed = (JS.getRawAxis(3) * 0.5 + 0.5);
      }
      shooter.set(ControlMode.PercentOutput, shooterSpeed);

      // unblocks the fuel after the shooter has spun for 1s and built up enough speed
      shooterStopper.set(timer.get() > 1.0 ? STOPPER_OPEN_POS : STOPPER_CLOSED_POS);
    } else {
      shooter.set(ControlMode.PercentOutput, 0.0); // stops shooter
      shooterStopper.set(0.5); // blocks fuel from reaching shooter

      timer.reset();
      timer.stop();
    }
      
    // pneumatics?
    // if (intake)
    //   gearwheels.set(-1);
    // if (outtake)
    //   gearwheels.set(0.5);
    // else
    //   gearwheels.set(0);  

    if (raise) 
      geararm.set(ControlMode.PercentOutput, -.8);
    else if (lower)
      geararm.set(ControlMode.PercentOutput, .7);
    else
      geararm.set(ControlMode.PercentOutput, 0);

  }


  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
