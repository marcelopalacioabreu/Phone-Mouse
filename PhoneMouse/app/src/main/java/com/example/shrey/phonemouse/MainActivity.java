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
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;

    private TextView mAccelXText;
    private TextView mAccelYText;
    private TextView mAccelZText;

    private double mVelocityX;
    private double mVelocityY;
    private double mVelocityZ;

    private AsyncTask<Void,Void,Void> socketTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mVelocityX = 0;
        mVelocityY = 0;
        mVelocityZ = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        socketTask = new SocketTask();
        socketTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        socketTask.cancel(true);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Log.d("VEL","("+mVelocityX+","+mVelocityY+","+mVelocityZ+")");
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelXText.setText(sensorEvent.values[0]+"");
            mAccelYText.setText(sensorEvent.values[1]+"");
            mAccelZText.setText(sensorEvent.values[2]+"");
        } else if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
