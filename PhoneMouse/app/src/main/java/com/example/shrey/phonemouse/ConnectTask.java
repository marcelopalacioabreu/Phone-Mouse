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
    DatagramSocket mSocket;

    public ConnectTask(Context context, String ipAddress) throws Exception {
        this.mContext = context;
        this.mIpAddress = InetAddress.getByName(ipAddress);
        mSocket = new DatagramSocket(PORT);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        while (!isCancelled()) {
            try {
                String data = "Connect";

                DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(), mIpAddress
                        , PORT);

                mSocket.send(sendPacket);
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                mSocket.receive(receivePacket);
                String receiveStatus = new String(receivePacket.getData()).trim();
                mSocket.close();
                Log.d("FINISHED",data);
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
            mouseIntent.putExtra("IP_ADDRESS", mIpAddress);
            mContext.startActivity(mouseIntent);
        } else {

        }
    }

    public void closeSocket() {
        Log.d("Close","CANCEL");
        mSocket.close();
    }
}
