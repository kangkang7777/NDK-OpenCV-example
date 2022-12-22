package com.example.nativeopencvandroidtemplate;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity extends Activity implements CvCameraViewListener2, SensorEventListener {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1;

    private CameraBridgeViewBase mOpenCvCameraView;

    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.i(TAG, "OpenCV loaded successfully");

                // Load native library after(!) OpenCV initialization
                System.loadLibrary("native-lib");

                mOpenCvCameraView.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST
        );

        setContentView(R.layout.activity_main);

        //sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager!=null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if(accelerometer!=null){
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }else{
            Toast.makeText(this, "Sensor service is not detected", Toast.LENGTH_SHORT).show();
        }

        //show frame
        final Handler handler = new Handler();
        handler.post( new Runnable(){
            @SuppressLint("SetTextI18n")
            public void run() {
                final TextView text= (TextView)findViewById(R.id.frameView);
                text.setTextColor(Color.parseColor("#FFFFFF"));
                text.setTextSize(30);
                text.setText(getTime() + " ms");
                handler.postDelayed(this, 100);
            }
        });

        mOpenCvCameraView = findViewById(R.id.main_surface);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mOpenCvCameraView.setCameraPermissionGranted();
            } else {
                String message = "Camera permission was not granted";
                Log.e(TAG, message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Unexpected permission request");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        Log.d(TAG, "  width:" + String.valueOf(width) + "  height" + String.valueOf(width));
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame frame) {
        // get current camera frame as OpenCV Mat object
        Mat mat = frame.rgba();
        // native call to process current camera frame
        imageProc(mat.getNativeObjAddr());

        // return processed frame for live preview
        return mat;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ((TextView)findViewById(R.id.sensorView)).setText("X:" + sensorEvent.values[0] +"\n"+ "Y:" + sensorEvent.values[1] +"\n"+ " Z: " + sensorEvent.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    private native void imageProc(long mat);
    private native int getTime();

}
