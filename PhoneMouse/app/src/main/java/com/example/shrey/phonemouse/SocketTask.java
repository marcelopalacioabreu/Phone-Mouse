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

public class SocketTask extends AsyncTask<Void, Void, Void> {
    private static int UDP_SERVER_PORT = 9876;
    private double[] mVelocity;
    private Queue<Actions> mActionQueue;

    /**
     * Create SocketTask pass in reference to mVelocity array and a queue for user actions
     * @param mVelocity double[]
     * @param mActionQueue Queue<Integer>
     */
    public SocketTask(double[] mVelocity, Queue<Actions> mActionQueue) {
        this.mVelocity = mVelocity;
        this.mActionQueue = mActionQueue;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DatagramSocket socket = null;
        try {
            //create socket
            socket = new DatagramSocket(UDP_SERVER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
        while(!isCancelled()) {
            try {
                String data = "";
                //if there non-movement actions send, otherwise send movement
                if(!mActionQueue.isEmpty()) {
                    data = mActionQueue.remove()+"";
                } else {
                    data = Actions.MOVE
                            + "," + mVelocity[0] + "," + mVelocity[1] + "," + mVelocity[2];
                }

                Log.d("socket",data);
                //send data
                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName("10.194.21.182"), UDP_SERVER_PORT);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }
}
