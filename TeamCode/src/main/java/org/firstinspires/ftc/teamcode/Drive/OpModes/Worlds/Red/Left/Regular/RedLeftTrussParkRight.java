package org.firstinspires.ftc.teamcode.Drive.OpModes.Worlds.Red.Left.Regular;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

import org.firstinspires.ftc.teamcode.Utility.RoadRunner.DriveConstants;
import org.firstinspires.ftc.teamcode.Utility.RoadRunner.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.TrajectorySequences.TrajectorySequence;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Commands.DriveCommand;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Commands.LowOuttakeCommand;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Commands.RestCommand;
import org.firstinspires.ftc.teamcode.Utility.Hardware.RobotHardware;
import org.firstinspires.ftc.teamcode.Utility.Vision.Prop.NewRedLeftProcessor;
import org.firstinspires.ftc.teamcode.Utility.Vision.Robot.Center.BlueCenterRobotScan;
import org.firstinspires.ftc.teamcode.Utility.Vision.Robot.Wall.RedWallRobotScan;
import org.firstinspires.ftc.vision.VisionPortal;

@Autonomous
@Disabled
public class RedLeftTrussParkRight extends OpMode {
    private VisionPortal visionPortal;
    private NewRedLeftProcessor colorMassDetectionProcessor;
    private RedWallRobotScan robotProcessor;

    private RobotHardware robot;
    private ElapsedTime time_since_start;
    private double loop;
    boolean robotSensed;

    @Override
    public void init() {
        CommandScheduler.getInstance().reset();
        robot = new RobotHardware(hardwareMap);

        CommandScheduler.getInstance().registerSubsystem(robot.armSystem);
        CommandScheduler.getInstance().registerSubsystem(robot.claw);
        CommandScheduler.getInstance().registerSubsystem(robot.angleOfArm);
        CommandScheduler.getInstance().registerSubsystem(robot.driveSubsystem);

        telemetry.addData("Not Ready: ", "Not able to proceed to camera detection... Restart robot now.");
        telemetry.update();

        robot.claw.grabBoth();

        colorMassDetectionProcessor = new NewRedLeftProcessor();
        robotProcessor = new RedWallRobotScan();

        colorMassDetectionProcessor.setDetectionColor(true); //false is blue, true is red
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam"))
                .addProcessors(colorMassDetectionProcessor, robotProcessor)
                .build();
    }

    @Override
    public void init_loop() {
        telemetry.addData("Currently Recorded Position: ", colorMassDetectionProcessor.getPropLocation());
        telemetry.addData("Camera State: ", visionPortal.getCameraState());
        CommandScheduler.getInstance().run();
        robot.armSystem.loop();
    }

    @Override
    public void start() {
        time_since_start = new ElapsedTime();

        FtcDashboard.getInstance().startCameraStream(robotProcessor, 30);

        NewRedLeftProcessor.PropPositions recordedPropPosition = colorMassDetectionProcessor.getPropLocation();
        robot.driveSubsystem.setPoseEstimate(new Pose2d(-40.11, -63.48, Math.toRadians(450.00)));
        switch (recordedPropPosition) {
            case LEFT:
                TrajectorySequence movement1Left = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(-40.11, -63.48, Math.toRadians(450.00)))
                        .splineToConstantHeading(
                                new Vector2d(-51.6, -43.70), Math.toRadians(90.00),
                                SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToSplineHeading(
                                new Pose2d(-46, -57, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(8, -57),
                                SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .turn(Math.toRadians(30))
                        .build();

                TrajectorySequence movement2Left = robot.driveSubsystem.trajectorySequenceBuilder(movement1Left.end())
                        .lineToSplineHeading(
                                new Pose2d(37.72, -57, Math.toRadians(0)),
                                SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(37.72, -25.8),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(50, -25.8),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                TrajectorySequence movement3Left = robot.driveSubsystem.trajectorySequenceBuilder(movement2Left.end())
                        .lineToConstantHeading(
                                new Vector2d(35, -25.8),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(35, -57),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(50, -57),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new DriveCommand(robot.driveSubsystem, movement1Left),
                                new WaitUntilCommand(() -> robotSensed || time_since_start.seconds() > 22),
                                new ParallelCommandGroup(
                                        new LowOuttakeCommand(robot),
                                        new DriveCommand(robot.driveSubsystem, movement2Left)
                                ),
                                new WaitCommand(350),
                                new InstantCommand(() -> robot.claw.releaseRight()),
                                new WaitCommand(350),
                                new ParallelCommandGroup(
                                        new DriveCommand(robot.driveSubsystem, movement3Left),
                                        new RestCommand(robot)
                                )
                        )
                );
                break;
            case RIGHT:
            case UNFOUND:
                TrajectorySequence movement1Right = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(-40.11, -63.48, Math.toRadians(450.00)))
                        .splineTo(
                                new Vector2d(-34.99, -37.93), Math.toRadians(45.00),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToSplineHeading(
                                new Pose2d(-46, -57, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(8, -57),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .turn(Math.toRadians(30))
                        .build();

                TrajectorySequence movement2Right = robot.driveSubsystem.trajectorySequenceBuilder(movement1Right.end())
                        .lineToSplineHeading(
                                new Pose2d(37.72, -57, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(37.72, -35.90),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(50, -35.90),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                TrajectorySequence movement3Right = robot.driveSubsystem.trajectorySequenceBuilder(movement2Right.end())
                        .lineToConstantHeading(
                                new Vector2d(35, -35.90),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(35, -57),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(50, -57),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new DriveCommand(robot.driveSubsystem, movement1Right),
                                new WaitUntilCommand(() -> robotSensed || time_since_start.seconds() > 22),
                                new ParallelCommandGroup(
                                        new LowOuttakeCommand(robot),
                                        new DriveCommand(robot.driveSubsystem, movement2Right)
                                ),
                                new WaitCommand(350),
                                new InstantCommand(() -> robot.claw.releaseRight()),
                                new WaitCommand(350),
                                new ParallelCommandGroup(
                                        new DriveCommand(robot.driveSubsystem, movement3Right),
                                        new RestCommand(robot)
                                )

                        )
                );
                break;
            case MIDDLE:
                TrajectorySequence movement1Middle = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(-40.11, -63.48, Math.toRadians(450.00)))
                        .splineToConstantHeading(
                                new Vector2d(-44.50, -33.10), Math.toRadians(90.00),
                                SampleMecanumDrive.getVelocityConstraint(15, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToSplineHeading(
                                new Pose2d(-46, -56, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(8, -56),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .turn(Math.toRadians(30))
                        .build();

                TrajectorySequence movement2Middle = robot.driveSubsystem.trajectorySequenceBuilder(movement1Middle.end())
                        .lineToSplineHeading(
                                new Pose2d(37.72, -56, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(45, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(37.72, -30.5),
                                SampleMecanumDrive.getVelocityConstraint(50, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(51, -30.5),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                TrajectorySequence movement3Middle = robot.driveSubsystem.trajectorySequenceBuilder(movement2Middle.end())
                        .lineToConstantHeading(
                                new Vector2d(35, -30.5),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(35, -57),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(
                                new Vector2d(50, -57),
                                SampleMecanumDrive.getVelocityConstraint(10, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(5000),
                                new DriveCommand(robot.driveSubsystem, movement1Middle),
                                new WaitUntilCommand(() -> robotSensed || time_since_start.seconds() > 22),
                                new ParallelCommandGroup(
                                        new LowOuttakeCommand(robot),
                                        new DriveCommand(robot.driveSubsystem, movement2Middle)
                                ),
                                new WaitCommand(350),
                                new InstantCommand(() -> robot.claw.releaseRight()),
                                new WaitCommand(350),
                                new ParallelCommandGroup(
                                        new DriveCommand(robot.driveSubsystem, movement3Middle),
                                        new RestCommand(robot)
                                )

                        )
                );
                break;
        }
    }

    @Override
    public void loop() {
        CommandScheduler.getInstance().run();
        robot.armSystem.loop();
        robot.slidesSubsystem.loop();
        robot.driveSubsystem.update();

        double time = System.currentTimeMillis();
        telemetry.addData("loop ", time - loop);
        telemetry.addData("Time Elapsed: ", time_since_start);
        telemetry.addData("Current Loop Time: ", time - loop);

        robotSensed = (robotProcessor.getSensedBoolean() != RedWallRobotScan.Sensed.TRUE);

        telemetry.addData("Robot Sensed: ", robotSensed);

        loop = time;

        telemetry.update();
    }

    @Override
    public void stop() {
        visionPortal.close();
        telemetry.addLine("Closed Camera.");
        telemetry.update();
        CommandScheduler.getInstance().reset();
    }
}