package org.firstinspires.ftc.teamcode.Utility.Hardware;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.drivebase.MecanumDrive;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.Drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Subsystems.AngleSubsystem;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Subsystems.ArmSubsystem;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Subsystems.ClawSubsystem;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Subsystems.DriveSubsystem;
import org.firstinspires.ftc.teamcode.Utility.CommandBase.Subsystems.SlidesSubsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Config
public class RobotHardware {
    public DcMotorEx leftFront, rightFront, leftRear, rightRear, linear_1, linear_2, arm;
    public Servo angleOfClaw, leftClaw, rightClaw;
    public VoltageSensor batteryVoltageSensor;
    private final List<LynxModule> hubs;

    public AngleSubsystem angleOfArm;
    public ArmSubsystem armSystem;
    public ClawSubsystem claw;
    public DriveSubsystem driveSubsystem;
    public SlidesSubsystem slidesSubsystem;
    public MecanumDrive drive;

    public RobotHardware(HardwareMap hardwareMap) {

        leftFront = hardwareMap.get(DcMotorEx.class, "leftFront");
        leftRear = hardwareMap.get(DcMotorEx.class, "leftRear");
        rightRear = hardwareMap.get(DcMotorEx.class, "rightRear");
        rightFront = hardwareMap.get(DcMotorEx.class, "rightFront");

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightRear.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFront.setDirection(DcMotor.Direction.REVERSE);
        leftRear.setDirection(DcMotor.Direction.REVERSE);

        linear_1 = hardwareMap.get(DcMotorEx.class, "linear_1");
        linear_2 = hardwareMap.get(DcMotorEx.class, "linear_2");
        arm = hardwareMap.get(DcMotorEx.class, "arm");
        arm.setDirection(DcMotor.Direction.REVERSE);

        linear_1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linear_2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        arm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        arm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        arm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        arm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        linear_1.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linear_2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linear_1.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        linear_2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        linear_2.setDirection(DcMotor.Direction.REVERSE);


        Servo angleOfClaw = hardwareMap.get(Servo.class, "dump");
        Servo leftClaw = hardwareMap.get(Servo.class, "claw");
        Servo rightClaw = hardwareMap.get(Servo.class, "claw1");


        batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();
        for (LynxModule hub : hubs = hardwareMap.getAll(LynxModule.class)) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        armSystem = new ArmSubsystem(arm, batteryVoltageSensor);
        claw = new ClawSubsystem(hardwareMap, "claw", "claw1");
        angleOfArm = new AngleSubsystem(hardwareMap, "dump");
        driveSubsystem = new DriveSubsystem(new SampleMecanumDrive(hardwareMap), false);
        slidesSubsystem = new SlidesSubsystem(linear_1, linear_2, batteryVoltageSensor);
        CommandScheduler.getInstance().registerSubsystem(armSystem, claw, angleOfArm, driveSubsystem, slidesSubsystem);

    }

    public void currentUpdate(Telemetry telemetry) {
        List<Double> current_list = new ArrayList<>();
        current_list.add(leftFront.getCurrent(CurrentUnit.AMPS));
        current_list.add(rightFront.getCurrent(CurrentUnit.AMPS));
        current_list.add(leftRear.getCurrent(CurrentUnit.AMPS));
        current_list.add(rightRear.getCurrent(CurrentUnit.AMPS));
        current_list.add(arm.getCurrent(CurrentUnit.AMPS));
        current_list.add(linear_1.getCurrent(CurrentUnit.AMPS));
        current_list.add(linear_2.getCurrent(CurrentUnit.AMPS));

        double current = 0;

        for (LynxModule hub : hubs) {
            current += hub.getCurrent(CurrentUnit.AMPS);
        }

        telemetry.addData("Total Current Usage: ", current);
    }

    public void pidArmUpdateTelemetry(Telemetry telemetry) {
        telemetry.addData("Arm PIDF Limits: ", armSystem.p + ", " + armSystem.i + ", " + armSystem.d + ", " + armSystem.f);
    }

}