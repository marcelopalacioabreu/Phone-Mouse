package com.example.shrey.phonemouse;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConnectionActivity extends AppCompatActivity {

    private EditText mIpAddressField;
    private Button mConnectButton;
    private ConnectTask mConnectTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        mIpAddressField = (EditText) findViewById(R.id.connection_address_field);
        mConnectButton = (Button) findViewById(R.id.connect_button);

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddressText = mIpAddressField.getText().toString();
                if(mConnectTask != null) {
                    Log.d("CANCEL","Click");
                    mConnectTask.closeSocket();
                }
                try {
                    mConnectTask = new ConnectTask(ConnectionActivity.this, ipAddressText);
                    mConnectTask.execute();
                } catch (Exception e) {
                    Toast invalidToast = Toast.makeText(ConnectionActivity.this
                            , "Invalid ip address", Toast.LENGTH_SHORT);
                    invalidToast.setGravity(Gravity.TOP,0,0);
                    invalidToast.show();
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void onPause() {
        Log.d("HI","PAUSE");
        mConnectTask.closeSocket();
        super.onPause();
    }
}
