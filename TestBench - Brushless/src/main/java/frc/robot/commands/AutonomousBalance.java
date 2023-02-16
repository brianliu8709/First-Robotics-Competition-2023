// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import frc.robot.subsystems.DriveTrain;
import frc.robot.subsystems.ExampleSubsystem;
import frc.robot.utilities.GyroPIDController;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;

/** An example command that uses an example subsystem. */
public class AutonomousBalance extends CommandBase {
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final DriveTrain m_driveTrain;
  private double yAngle;
  private double zAngle;
  private Timer m_timer;

  /**
   * Creates a new ExampleCommand.
   *
   * @param subsystem The subsystem used by this command.
   */
  public AutonomousBalance(DriveTrain m_driveTrain) {
    this.m_driveTrain = m_driveTrain;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(m_driveTrain);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_driveTrain.calibrateIMU();
    m_timer = new Timer();
    m_timer.start();
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
      if (m_timer.hasElapsed(25)) {
        // find up-down (y) angle, calculate PID output
        yAngle = m_driveTrain.getYAngle();
        double pidOutputY = GyroPIDController.calculateY(yAngle);
        System.out.println("y pid output: " + pidOutputY);
        double clampedPIDOutputY = MathUtil.clamp(pidOutputY, -.5, .5);
        System.out.println("clamped y pid output: " + clampedPIDOutputY);
        // m_driveTrain.doDrive(clampedPIDOutputY, clampedPIDOutputY);

        // find left-right (z) angle, calculate PID output
        zAngle = m_driveTrain.getZAngle();
        double pidOutputZ = -GyroPIDController.calculateZ(zAngle);
        System.out.println("z pid output: " + pidOutputZ);
        double clampedPIDOutputZ = MathUtil.clamp(pidOutputZ, -.5, .5);
        System.out.println("clamped z pid output: " + clampedPIDOutputZ);

        double leftThrottle = clampedPIDOutputY;
        double rightThrottle = clampedPIDOutputY;
        // double leftThrottle = 0.2;
        // double rightThrottle = 0.2;
        // calculate overall motor settings based on z and y pid outputs
        // m_driveTrain.doDrive(clampedPIDOutputZ, -clampedPIDOutputZ);
        if (clampedPIDOutputZ < 0) {
          // rightThrottle *= 0.5;
          rightThrottle *= (1 - Math.abs(clampedPIDOutputZ));
        }
        else if (clampedPIDOutputZ > 0) {
          // leftThrottle *= .5;
          leftThrottle *= (1 - Math.abs(clampedPIDOutputZ));
        }
        //clockwise/right is positive for arcadeDrive, make sure pid/gyro is consistent
        // m_driveTrain.arcadeDrive(clampedPIDOutputY, clampedPIDOutputZ);
        SmartDashboard.putNumber("Z PID Output", clampedPIDOutputZ);
        SmartDashboard.putNumber("Y PID Output", clampedPIDOutputY);
        SmartDashboard.putNumber("Left throttle", leftThrottle);
        SmartDashboard.putNumber("Right throttle", rightThrottle);
        SmartDashboard.putNumber("Y angle", yAngle);
        SmartDashboard.putNumber("Z angle", zAngle);

        if (Math.abs(yAngle) > 3) {
          m_driveTrain.doDrive(leftThrottle, rightThrottle);

        }
    }
    else {
      System.out.println("waiting to drive. time elapsed: " + m_timer.get());
      m_driveTrain.doDrive(0, 0);
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
