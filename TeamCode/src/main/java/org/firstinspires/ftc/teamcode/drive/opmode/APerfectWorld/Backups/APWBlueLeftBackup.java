package org.firstinspires.ftc.teamcode.drive.opmode.APerfectWorld.Backups;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.common.commandbase.command.IntakeCommand;
import org.firstinspires.ftc.teamcode.common.hardware.Robot;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.util.ColorPropDetectionProcessor;
import org.firstinspires.ftc.vision.VisionPortal;
import org.opencv.core.Scalar;

@Autonomous
@Config
@Disabled
public class APWBlueLeftBackup extends OpMode {
    private VisionPortal visionPortal;
    private ColorPropDetectionProcessor colorMassDetectionProcessor;

    private Robot robot;
    private double loop;
    private ElapsedTime time_since_start;

    @Override
    public void init() {
        CommandScheduler.getInstance().reset();
        robot = new Robot(hardwareMap);

        CommandScheduler.getInstance().registerSubsystem(robot.a);
        CommandScheduler.getInstance().registerSubsystem(robot.claw);
        CommandScheduler.getInstance().registerSubsystem(robot.angle);
        CommandScheduler.getInstance().registerSubsystem(robot.driveSubsystem);

        telemetry.addData("Not Ready: ", "Not able to proceed to camera detection... Restart robot now.");
        telemetry.update();

        robot.claw.grabBoth();
        Scalar lower = new Scalar(80, 50, 50);
        Scalar upper = new Scalar(180, 255, 255);

        double minArea = 100; //min area for prop detection

        colorMassDetectionProcessor = new ColorPropDetectionProcessor(
                lower,
                upper,
                () -> minArea,
                () -> 213, //left third
                () -> 426 //right third
        );
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam"))
                .addProcessor(colorMassDetectionProcessor)
                .build();

    }

    @Override
    public void init_loop() {
        telemetry.addData("Successful: ", "Ready for BlueLeft (Backdrop Side)");
        telemetry.addData("Ready to Run: ", "1 pixel autonomous (26 points). All subsystems initialized.");
        telemetry.addData("Currently Recorded Position", colorMassDetectionProcessor.getRecordedPropPosition());
        telemetry.addData("Camera State", visionPortal.getCameraState());
        telemetry.addData("Currently Detected Mass Center", "x: " + colorMassDetectionProcessor.getLargestContourX() + ", y: " + colorMassDetectionProcessor.getLargestContourY());
        telemetry.addData("Currently Detected Mass Area", colorMassDetectionProcessor.getLargestContourArea());
        CommandScheduler.getInstance().run();
        robot.a.loop();
    }

    @Override
    public void start() {
        if (visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING) {
            visionPortal.stopLiveView();
            visionPortal.stopStreaming();
        }

        ColorPropDetectionProcessor.PropPositions recordedPropPosition = colorMassDetectionProcessor.getRecordedPropPosition();
        robot.driveSubsystem.setPoseEstimate(new Pose2d(12.45, 66.78, Math.toRadians(270.00)));
        switch (recordedPropPosition) {
            case LEFT:
            case UNFOUND:
                TrajectorySequence tapeLeft = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(12.45, 66.78, Math.toRadians(270.00)))
                        .splineToConstantHeading(
                                new Vector2d(26.00, 42.57), Math.toRadians(270.00),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                TrajectorySequence parkLeft = robot.driveSubsystem.trajectorySequenceBuilder(tapeLeft.end())
                        .lineToConstantHeading(
                                new Vector2d(22.90, 53.37),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToSplineHeading(
                                new Pose2d(65.73, 68.34, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(tapeLeft)),
                                new WaitCommand(2000),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(parkLeft)),
                                new WaitCommand(1000),
                                new IntakeCommand(robot)
                        )
                );
                break;
            case MIDDLE:
                TrajectorySequence tapeMiddle = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(12.45, 66.78, Math.toRadians(270.00)))
                        .lineToConstantHeading(
                                new Vector2d(14.89, 33.17),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                TrajectorySequence parkMiddle = robot.driveSubsystem.trajectorySequenceBuilder(tapeMiddle.end())
                        .lineToConstantHeading(
                                new Vector2d(15.41, 54.24),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToSplineHeading(
                                new Pose2d(65.73, 68.34, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(tapeMiddle)),
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(parkMiddle)),
                                new WaitCommand(350),
                                new IntakeCommand(robot)
                        )
                );

                break;
            case RIGHT:
                TrajectorySequence tapeRight = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(12.45, 66.78, Math.toRadians(270.00)))
                        .splineTo(
                                new Vector2d(7.05, 37.35), Math.toRadians(230.19),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                TrajectorySequence parkRight = robot.driveSubsystem.trajectorySequenceBuilder(tapeRight.end())
                        .lineToSplineHeading(
                                new Pose2d(65.73, 68.34, Math.toRadians(0.00)),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .build();

                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(tapeRight)),
                                new WaitCommand(2000),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(parkRight)),
                                new WaitCommand(1000),
                                new IntakeCommand(robot)
                        )
                );
                break;
        }
    }

    @Override
    public void loop() {
        CommandScheduler.getInstance().run();
        robot.a.loop();
        robot.driveSubsystem.update();
        double time = System.currentTimeMillis();
        telemetry.addData("Time Elapsed: ", time_since_start);
        telemetry.addData("Loop: ", time - loop);
        telemetry.addData("Arm Position: ", robot.a.getCachePos());
        loop = time;

        telemetry.update();
    }

    @Override
    public void stop() {
        colorMassDetectionProcessor.close();
        visionPortal.close();
        telemetry.addLine("Closed Camera.");
        telemetry.update();
        CommandScheduler.getInstance().reset();
    }
}