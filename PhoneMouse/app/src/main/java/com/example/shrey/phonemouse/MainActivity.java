package com.example.shrey.phonemouse;

import android.content.Context;
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
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private Button leftMouseButton;
    private Button rightMouseButton;

    private double[] mVelocity;
    private Queue<Actions> mActionQueue;

    private AsyncTask<Void, Void, Void> socketTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        mVelocity = new double[3];
        Arrays.fill(mVelocity, 0.0);

        mActionQueue = new LinkedList<>();
        //setup socket
        socketTask = new SocketTask(mVelocity, mActionQueue);
        socketTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        //close socket
        socketTask.cancel(true);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("ACCEL", "(" + sensorEvent.values[0] + ","
                + sensorEvent.values[1] + "," + sensorEvent.values[2] + ")");

        //update velocity if accleration is greater than .25
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mVelocity[0] += Math.abs(sensorEvent.values[0]) < .25 ? 0 : sensorEvent.values[0];
            mVelocity[1] += Math.abs(sensorEvent.values[1]) < .25 ? 0 : sensorEvent.values[1];
            mVelocity[2] += Math.abs(sensorEvent.values[2]) < .25 ? 0 : sensorEvent.values[2];
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
