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
import android.view.View;
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
import org.restrpc.client.NativeRpcClient;
import org.restrpc.client.RpcClient;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class MainActivity extends Activity implements CvCameraViewListener2, SensorEventListener {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 1;

    private CameraBridgeViewBase mOpenCvCameraView;
    private SensorManager sensorManager;
    float[] gravity, geomagnetic, rotationVector;
    boolean enableView = true;

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

    @SuppressLint("SetTextI18n")
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
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager!=null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            Sensor linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Sensor Geomagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
            Sensor orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if(accelerometer != null){
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, linearAcc, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, Geomagnetic, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
            }

        }else{
            Toast.makeText(this, "Sensor service is not detected", Toast.LENGTH_SHORT).show();
        }

        //set text
        List<View> views = new ArrayList<>();
        views.add(findViewById(R.id.sensorGraView));
        views.add(findViewById(R.id.sensorAccView));
        views.add(findViewById(R.id.sensorGyroView));
        views.add(findViewById(R.id.sensorOrientView));
        views.add(findViewById(R.id.sensorLinearAccView));
        views.add(findViewById(R.id.sensorMagView));
        views.add(findViewById(R.id.orientation));
        for(View each : views)
        {
            ((TextView)each).setTextColor(Color.parseColor("#FFFFFF"));
            ((TextView)each).setText("");
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


//        if(rpcClientInit())
//            text.setText("true");
//        else
//            text.setText("false");

        mOpenCvCameraView = findViewById(R.id.main_surface);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

//        RpcClient rpcClient = new NativeRpcClient();
//        // Connect to the C++ RPC server which is listening on `127.0.0.1:9000`.
//        rpcClient.connect("127.0.0.1:9000");

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

        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        Log.d(TAG, "  width:" + String.valueOf(width) + "  height" + String.valueOf(width));
    }

    @Override
    public void onCameraViewStopped() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public Mat onCameraFrame(CvCameraViewFrame frame) {
        // get current camera frame as OpenCV Mat object
        Mat mat = frame.rgba();
        // native call to process current camera frame
        //imageProc(mat.getNativeObjAddr());
        //imageTrans(mat.getNativeObjAddr());




        // return processed frame for live preview
        return mat;
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(!enableView)
            return;
        float[] r = new float[9];
        float[] I = new float[9];
        float[] values = new float[3];
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ((TextView)findViewById(R.id.sensorAccView)).setText("Acceleration \nX:" + String.format("%.2f", sensorEvent.values[0]) +"\n"+ "Y:" + String.format("%.2f", sensorEvent.values[1]) +"\n"+ " Z: " + String.format("%.2f", sensorEvent.values[2]));
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            ((TextView)findViewById(R.id.sensorLinearAccView)).setText("线性加速度 \nX:" + String.format("%.2f", sensorEvent.values[0]) +"\n"+ "Y:" + String.format("%.2f", sensorEvent.values[1]) +"\n"+ " Z: " + String.format("%.2f", sensorEvent.values[2]));
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
            ((TextView)findViewById(R.id.sensorGraView)).setText("Gravity \nX:" + String.format("%.2f", sensorEvent.values[0]) +"\n"+ "Y:" + String.format("%.2f", sensorEvent.values[1]) +"\n"+ " Z: " + String.format("%.2f", sensorEvent.values[2]));
            gravity = sensorEvent.values;
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            rotationVector = sensorEvent.values;
            ((TextView)findViewById(R.id.sensorGyroView)).setText("Gyroscope \nX:" + String.format("%.2f", sensorEvent.values[0]) +"\n"+ "Y:" + String.format("%.2f", sensorEvent.values[1]) +"\n"+ " Z: " + String.format("%.2f", sensorEvent.values[2]));
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            geomagnetic = sensorEvent.values;
        }
        else if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
//            geomagnetic = sensorEvent.values;
        }


        if(gravity!=null && geomagnetic!=null && rotationVector!=null)
        {
            SensorManager.getRotationMatrixFromVector(r, rotationVector);
            for(int i=0; i<r.length ;i++)
            {
                r[i] = Float.parseFloat(String.format("%.3f", r[i]));
            }
            ((TextView)findViewById(R.id.sensorGyroView)).setText("RotationMatrix\n by gyroscope \n" + r[0] + " " + r[1] + " " + r[2] +
                    "\n" + r[3] + " " + r[4] + " " + r[5] + "\n" +
                    r[6] + " " + r[7] + " " + r[8] +"\n" );

            SensorManager.getRotationMatrix(r, I, gravity, geomagnetic);

            SensorManager.getOrientation(r, values);
            for(int i=0; i<values.length ;i++)
            {
                values[i] = Float.parseFloat(String.format("%.3f", values[i]));
            }
            ((TextView)findViewById(R.id.orientation)).setText("Rotation angles \n" +  values[0] + " " + values[1] + " " +  values[2]);

            for(int i=0; i<r.length ;i++)
            {
                r[i] = Float.parseFloat(String.format("%.3f", r[i]));
            }
            for(int i=0; i<I.length ;i++)
            {
                I[i] = Float.parseFloat(String.format("%.3f", I[i]));
            }

            ((TextView)findViewById(R.id.sensorOrientView)).setText("RotationMatrix\n by gravity \n" + r[0] + " " + r[1] + " " + r[2] +
                                                                                    "\n" + r[3] + " " + r[4] + " " + r[5] + "\n" +
                                                                                            r[6] + " " + r[7] + " " + r[8] +"\n" );
            ((TextView)findViewById(R.id.sensorMagView)).setText("RotationMatrix \n by geomagnetic \n" + I[0] + " " + I[1] + " " + I[2] +
                    "\n" + I[3] + " " + I[4] + " " + I[5] + "\n" +
                    I[6] + " " + I[7] + " " + I[8] +"\n" );
        }
        //SensorManager.getOrientation(r, values);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private native boolean rpcClientInit();
    private native void imageProc(long mat);
    private native void imageTrans(long mat);
    private native int getTime();

}
