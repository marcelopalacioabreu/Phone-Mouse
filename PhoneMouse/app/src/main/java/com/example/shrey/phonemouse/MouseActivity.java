package com.example.shrey.phonemouse;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class MouseActivity extends AppCompatActivity implements SensorEventListener {

    public static final double ACCELERATION_THRESHOLD = .25;
    public static final int RESET_COUNT = 100;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private Button leftMouseButton;
    private Button rightMouseButton;

    private double[] mVelocity;
    private Queue<Actions> mActionQueue;

    private SocketTask mSocketTask;

    private InetAddress mIpAddress;

    private int mStationaryCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mouse);

        Intent intent = getIntent();

        mIpAddress = (InetAddress)intent.getSerializableExtra("IP_ADDRESS");

        mVelocity = new double[3];

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        leftMouseButton = (Button) findViewById(R.id.left_mouse_button);
        rightMouseButton = (Button) findViewById(R.id.right_mouse_button);

        leftMouseButton.setOnTouchListener(leftMouseButtonTouch);
        rightMouseButton.setOnTouchListener(rightMouseButtonTouch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        Arrays.fill(mVelocity, 0.0);

        mActionQueue = new LinkedList<>();
        //setup socket
        mSocketTask = new SocketTask(mVelocity, mActionQueue, mIpAddress);
        mSocketTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mSocketTask.closeSocket();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("ACCEL", "(" + sensorEvent.values[0] + ","
                + sensorEvent.values[1] + "," + sensorEvent.values[2] + ")");

        if (mVelocity[0] < ACCELERATION_THRESHOLD && mVelocity[1] < ACCELERATION_THRESHOLD) {
            mStationaryCount++;
            if(mStationaryCount >= RESET_COUNT) {
                Log.d("RESET","RESET");
                mStationaryCount = 0;
                Arrays.fill(mVelocity, 0.0);
            }
        }

        //update velocity if accleration is greater than .25
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mVelocity[0] += Math.abs(sensorEvent.values[0]) < ACCELERATION_THRESHOLD ? 0 : sensorEvent.values[0];
            mVelocity[1] += Math.abs(sensorEvent.values[1]) < ACCELERATION_THRESHOLD ? 0 : sensorEvent.values[1];
            mVelocity[2] += Math.abs(sensorEvent.values[2]) < ACCELERATION_THRESHOLD ? 0 : sensorEvent.values[2];
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private View.OnTouchListener leftMouseButtonTouch = new View.OnTouchListener() {

        //listen for button up and down
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mActionQueue.add(Actions.LEFT_PRESS);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mActionQueue.add(Actions.LEFT_RELEASE);
            }
            return true;
        }
    };

    private View.OnTouchListener rightMouseButtonTouch = new View.OnTouchListener() {

        //listen for button up and down
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mActionQueue.add(Actions.RIGHT_PRESS);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                mActionQueue.add(Actions.RIGHT_RELEASE);
            }
            return true;
        }
    };

}
