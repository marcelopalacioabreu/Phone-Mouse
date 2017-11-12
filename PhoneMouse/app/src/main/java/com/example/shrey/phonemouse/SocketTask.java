package com.example.shrey.phonemouse;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by area5 on 11/12/2017.
 */

public class SocketTask extends AsyncTask<Void, Void, Void> {
    private static int UDP_SERVER_PORT = 9876;

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("START","!");
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(UDP_SERVER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
        while(true) {
            try {
                String data = "Hello, World";
                DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), InetAddress.getByName("10.0.2.2"), UDP_SERVER_PORT);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        return null;
    }
}
