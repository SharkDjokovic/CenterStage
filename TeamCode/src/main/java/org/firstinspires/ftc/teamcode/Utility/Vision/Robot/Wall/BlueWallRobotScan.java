package org.firstinspires.ftc.teamcode.Utility.Vision.Robot.Wall;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.util.concurrent.atomic.AtomicReference;

public class BlueWallRobotScan implements VisionProcessor, CameraStreamSource {

    private final AtomicReference<Bitmap> lastFrame =
            new AtomicReference<>(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));

    private Mat testMat = new Mat();
    private Mat highMat = new Mat();
    private Mat lowMat = new Mat();
    private Mat finalMat = new Mat();

    private double senseThreshold = 0.04;
    Telemetry telemetry;

    Sensed sensedBoolean;
    double sensedPerc;

    Rect SENSED_RECTANGLE;

    @Override
    public void init(int width, int height, CameraCalibration calibration) {

        this.SENSED_RECTANGLE = new Rect(
                new Point(0.1 * width, 0.54 * height),
                new Point(0.6 * width, 0.68 * height)
        );

        lastFrame.set(Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565));
    }

    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {

        Imgproc.cvtColor(frame, testMat, Imgproc.COLOR_RGB2HSV);

        Scalar blueHSVLower = new Scalar(85, 100, 20);
        Scalar blueHSVUpper = new Scalar(140, 255, 255);

        Core.inRange(testMat, blueHSVLower, blueHSVUpper, finalMat);

        double sensedBox = Core.sumElems(finalMat.submat(SENSED_RECTANGLE)).val[0];

        this.sensedPerc = sensedBox / SENSED_RECTANGLE.area() / 255;

        if(sensedPerc > senseThreshold) {
            sensedBoolean = Sensed.FALSE;
        } else {
            sensedBoolean = Sensed.TRUE;
        }

        Scalar redBorder = new Scalar(255, 0, 0);
        Scalar greenBorder = new Scalar(0, 255, 0);

        switch (sensedBoolean) {
            case TRUE:
                Imgproc.rectangle(frame, SENSED_RECTANGLE, redBorder);
                break;
            case FALSE:
                Imgproc.rectangle(frame, SENSED_RECTANGLE, greenBorder);
                break;
        }

        Bitmap b = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(frame, b);
        lastFrame.set(b);

        return null;

    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
    }

    public Sensed getSensedBoolean() {
        return this.sensedBoolean;
    }
    public Double getSensedPercent() {
        return this.sensedPerc;
    }

    public void setTelemetry(Telemetry telemetry) {
        this.telemetry = telemetry;
    }

    @Override
    public void getFrameBitmap(Continuation<? extends Consumer<Bitmap>> continuation) {
        continuation.dispatch(bitmapConsumer -> bitmapConsumer.accept(lastFrame.get()));
    }

    public void updateTelemetry() {
        telemetry.addLine("Robot Processor")
                .addData("Percent", sensedPerc);
    }

    public enum Sensed {
        TRUE,
        FALSE;
    }
}