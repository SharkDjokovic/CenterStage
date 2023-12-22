package org.firstinspires.ftc.teamcode.drive.opmode.OldAutonomous;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.common.hardware.Robot;
import org.firstinspires.ftc.teamcode.drive.DriveConstants;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;
import org.firstinspires.ftc.teamcode.util.ColourMassDetectionProcessor2;
import org.firstinspires.ftc.vision.VisionPortal;
import org.opencv.core.Scalar;


@Autonomous
@Config
public class RedLeftBackup extends OpMode {
    private VisionPortal visionPortal;
    private ColourMassDetectionProcessor2 colourMassDetectionProcessor2;

    private Robot robot;
    private ElapsedTime time_since_start;
    private double loop;

    /**
     * User-defined init method
     * <p>
     * This method will be called once, when the INIT button is pressed.
     */
    @Override
    public void init() {
        CommandScheduler.getInstance().reset();
        robot = new Robot(hardwareMap);

        CommandScheduler.getInstance().registerSubsystem(robot.a);
        CommandScheduler.getInstance().registerSubsystem(robot.claw);
        CommandScheduler.getInstance().registerSubsystem(robot.angle);
        CommandScheduler.getInstance().registerSubsystem(robot.driveSubsystem);

        telemetry.addData("Successful: ", "Ready for RedRight (Backdrop Side)");
        telemetry.addData("Running: ", "1 pixel autonomous. All subsystems will run.");
        telemetry.update();

        robot.claw.grabBoth();
        // the current range set by lower and upper is the full range
        // HSV takes the form: (HUE, SATURATION, VALUE)
        // which means to select our colour, only need to change HUE
        // the domains are: ([0, 180], [0, 255], [0, 255])
        // this is tuned to detect red, so you will need to experiment to fine tune it for your robot
        // and experiment to fine tune it for blue
        Scalar lower = new Scalar(0, 80, 80); // the lower hsv threshold
        Scalar upper = new Scalar(180, 250, 250); // the upper hsv threshold
        double minArea = 100; // the minimum area for the detection to consider for your prop

        colourMassDetectionProcessor2 = new ColourMassDetectionProcessor2(
                lower,
                upper,
                () -> minArea, // these are lambda methods, in case we want to change them while the match is running, for us to tune them or something
                () -> 225, // the left dividing line, in this case the left third of the frame
                () -> 426 // the left dividing line, in this case the right third of the frame
        );
        visionPortal = new VisionPortal.Builder()
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam")) // the camera on your robot is named "Webcam 1" by default
                .addProcessor(colourMassDetectionProcessor2)
                .build();

        // you may also want to take a look at some of the examples for instructions on
        // how to have a switchable camera (switch back and forth between two cameras)
        // or how to manually edit the exposure and gain, to account for different lighting conditions
        // these may be extra features for you to work on to ensure that your robot performs
        // consistently, even in different environments
    }

    /**
     * User-defined init_loop method
     * <p>
     * This method will be called repeatedly during the period between when
     * the init button is pressed and when the play button is pressed (or the
     * OpMode is stopped).
     * <p>
     * This method is optional. By default, this method takes no action.
     */
    @Override
    public void init_loop() {
        telemetry.addData("Currently Recorded Position", colourMassDetectionProcessor2.getRecordedPropPosition());
        telemetry.addData("Camera State", visionPortal.getCameraState());
        telemetry.addData("Currently Detected Mass Center", "x: " + colourMassDetectionProcessor2.getLargestContourX() + ", y: " + colourMassDetectionProcessor2.getLargestContourY());
        telemetry.addData("Currently Detected Mass Area", colourMassDetectionProcessor2.getLargestContourArea());
        CommandScheduler.getInstance().run();
        robot.a.loop();
    }

    /**
     * User-defined start method
     * <p>
     * This method will be called once, when the play button is pressed.
     * <p>
     * This method is optional. By default, this method takes no action.
     * <p>
     * Example usage: Starting another thread.
     */
    @Override
    public void start() {
        // shuts down the camera once the match starts, we dont need to look any more
        if (visionPortal.getCameraState() == VisionPortal.CameraState.STREAMING) {
            visionPortal.stopLiveView();
            visionPortal.stopStreaming();
        }

        // gets the recorded prop position
        ColourMassDetectionProcessor2.PropPositions recordedPropPosition = colourMassDetectionProcessor2.getRecordedPropPosition();

        // now we can use recordedPropPosition to determine where the prop is! if we never saw a prop, your recorded position will be UNFOUND.
        // if it is UNFOUND, you can manually set it to any of the other positions to guess
        if (recordedPropPosition == ColourMassDetectionProcessor2.PropPositions.UNFOUND) {
            recordedPropPosition = ColourMassDetectionProcessor2.PropPositions.MIDDLE;
        }

        // now we can use recordedPropPosition in our auto code to modify where we place the purple and yellow pixels
        switch (recordedPropPosition) {
            case LEFT:
            case UNFOUND:
                TrajectorySequence dropPixelLeft = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(-39.26, -65.04, Math.toRadians(90.00)))
                        .splineToConstantHeading(
                                new Vector2d(-60.86, -35.96), Math.toRadians(90.00),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(new Vector2d(-59.99, -45.53))
                        .build();


                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(dropPixelLeft)),
                                new WaitCommand(1000),
                                new ParallelCommandGroup(
                                        new InstantCommand(() -> robot.a.armIntake()),
                                        new InstantCommand(() -> robot.angle.intake())
                                )
                        )

                );
                break;
            // code to do if we saw the prop on the left
            case MIDDLE:
                // code to do if we saw the prop on the middle
                TrajectorySequence dropPixelMiddle = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(-39.26, -65.04, Math.toRadians(90.00)))
                        .lineToSplineHeading(
                                new Pose2d(-42.57, -34.39, Math.toRadians(90.00)),
                                SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(new Vector2d(-37.87, -43.62))
                        .build();


                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(dropPixelMiddle)),
                                new WaitCommand(1000),
                                new ParallelCommandGroup(
                                        new InstantCommand(() -> robot.a.armIntake()),
                                        new InstantCommand(() -> robot.angle.intake())
                                )
                        )
                );
                break;
            case RIGHT:
                // code to do if we saw the prop on the right
                TrajectorySequence dropPixelRight = robot.driveSubsystem.trajectorySequenceBuilder(new Pose2d(-39.26, -65.04, Math.toRadians(90.00)))
                        .splineTo(new Vector2d(
                                        -33.0, -32.47), Math.toRadians(0.00),
                                        SampleMecanumDrive.getVelocityConstraint(30, DriveConstants.MAX_ANG_VEL, DriveConstants.TRACK_WIDTH),
                                        SampleMecanumDrive.getAccelerationConstraint(DriveConstants.MAX_ACCEL)
                        )
                        .lineToConstantHeading(new Vector2d(-48.49, -30.04))
                        .build();



                CommandScheduler.getInstance().schedule(
                        new SequentialCommandGroup(
                                new WaitCommand(500),
                                new InstantCommand(() -> robot.driveSubsystem.followTrajectorySequencenotAsync(dropPixelRight)),
                                new WaitCommand(1000),
                                new ParallelCommandGroup(
                                        new InstantCommand(() -> robot.a.armIntake()),
                                        new InstantCommand(() -> robot.angle.intake())
                                )
                        )
                );
                break;
        }
    }

    /**
     * User-defined loop method
     * <p>
     * This method will be called repeatedly during the period between when
     * the play button is pressed and when the OpMode is stopped.
     */
    @Override
    public void loop() {
        CommandScheduler.getInstance().run();
        robot.a.loop();
        robot.driveSubsystem.update();

        double time = System.currentTimeMillis();
        telemetry.addData("Loop: ", time - loop);
        telemetry.addData("Arm Position: ", robot.a.getCachePos());
        loop = time;

        telemetry.update();
    }


    /**
     * User-defined stop method
     * <p>
     * This method will be called once, when this OpMode is stopped.
     * <p>
     * Your ability to control hardware from this method will be limited.
     * <p>
     * This method is optional. By default, this method takes no action.
     */
    @Override
    public void stop() {
        // this closes down the portal when we stop the code, its good practice!
        colourMassDetectionProcessor2.close();
        visionPortal.close();
        CommandScheduler.getInstance().reset();
    }
}