package com.example.shrey.phonemouse;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Queue;

/**
 * Created by area5 on 11/12/2017.
 */

//shreyc2

public class SocketTask extends AsyncTask<Void, Void, Void> {
    private static int PORT = 9876;
    private double[] mVelocity;
    private Queue<Actions> mActionQueue;
    private InetAddress mIpAddress;

    /**
     * Create SocketTask pass in reference to mVelocity array and a queue for user actions
     *
     * @param mVelocity    double[]
     * @param mActionQueue Queue<Integer>
     */
    public SocketTask(double[] mVelocity, Queue<Actions> mActionQueue, InetAddress ipAddress) {
        this.mVelocity = mVelocity;
        this.mActionQueue = mActionQueue;
        this.mIpAddress = ipAddress;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DatagramSocket socket;
        try {
            //create socket
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
        while (!isCancelled()) {
            try {
                String data = "";
                //if there non-movement actions send, otherwise send movement
                if (!mActionQueue.isEmpty()) {
                    data = mActionQueue.remove() + "";
                } else {
                    data = Actions.MOVE
                            + "," + mVelocity[0] + "," + mVelocity[1] + "," + mVelocity[2];
                }

                Log.d("socket", data);
                //send data
                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), mIpAddress
                        , PORT);

                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }
}
