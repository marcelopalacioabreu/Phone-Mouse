package com.example.shrey.phonemouse;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by area5 on 11/12/2017.
 */

//shreyc2

public class SocketTask extends AsyncTask<Void, Void, Void> {
    public static final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public static final int MOVE = 0;
    private double[] mVelocity;
    private Queue<Integer> mActionQueue;
    private BluetoothSocket mSocket;

    /**
     * Create SocketTask pass in reference to mVelocity array and a queue for user actions
     *
     * @param mVelocity    double[]
     * @param mActionQueue Queue<Integer>
     */
    public SocketTask(double[] mVelocity, Queue<Integer> mActionQueue, BluetoothDevice device) {
        this.mVelocity = mVelocity;
        this.mActionQueue = mActionQueue;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_UUID));
        } catch (IOException e) {
            Log.d("Failed", "OHNO");
            e.printStackTrace();
        }

    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception
            mSocket.connect();
        } catch (IOException connectException) {

            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {

            }
            return null;
        }
        try {
            while (!isCancelled()) {
                String data;
                //send velocity if no other user interaction
                if (!mActionQueue.isEmpty()) {
                    data = mActionQueue.remove() + "";
                } else {
                    data = String.format("%d,%.3f,%.3f", MOVE, mVelocity[0], mVelocity[1]);
                }
                mSocket.getOutputStream().write((data + "\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
