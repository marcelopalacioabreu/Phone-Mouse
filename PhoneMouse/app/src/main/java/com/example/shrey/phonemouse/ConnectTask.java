package com.example.shrey.phonemouse;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by dino on 11/18/17.
 */

public class ConnectTask extends AsyncTask<Void, Void, Boolean> {
    private static int PORT = 9876;

    InetAddress mIpAddress;
    Context mContext;

    public ConnectTask(Context context, String ipAddress) throws Exception {
        this.mContext = context;
        this.mIpAddress = InetAddress.getByName(ipAddress);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        DatagramSocket socket;
        try {
            //create socket
            socket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        while (!isCancelled()) {
            try {
                String data = "Connect";

                DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(), mIpAddress
                        , PORT);

                socket.send(sendPacket);
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                String receiveStatus = new String(receivePacket.getData()).trim();
                Log.d("RECE","sdfa");
                return receiveStatus.equals("Connected");
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean connectionResult) {
        if (connectionResult) {
            Toast.makeText(mContext, "Connected", Toast.LENGTH_SHORT).show();
            Intent mouseIntent = new Intent(mContext, MouseActivity.class);
            mouseIntent.putExtra("IP_ADDRESS", mIpAddress.toString());
            mContext.startActivity(mouseIntent);
        } else {

        }
    }
}
