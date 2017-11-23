package com.example.shrey.phonemouse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by area5 on 11/12/2017.
 */

//shreyc2

public class SocketTask extends AsyncTask<Void, Void, Void> {
    private double[] mVelocity;
    private Queue<Actions> mActionQueue;
    private BluetoothSocket mSocket;

    /**
     * Create SocketTask pass in reference to mVelocity array and a queue for user actions
     *
     * @param mVelocity    double[]
     * @param mActionQueue Queue<Integer>
     */
    public SocketTask(double[] mVelocity, Queue<Actions> mActionQueue, BluetoothDevice device) {
        this.mVelocity = mVelocity;
        this.mActionQueue = mActionQueue;
        try {
            mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
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
            while(!isCancelled()) {
                String data;
                if(!mActionQueue.isEmpty()) {
                    data = mActionQueue.remove()+"";
                } else {
                    data = Actions.MOVE + ","
                            + mVelocity[0] + "," + mVelocity[1];
                }
                mSocket.getOutputStream().write((data+"\n").getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
